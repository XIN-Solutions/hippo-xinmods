package nz.xinsolutions.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.gc.iotools.stream.os.OutputStreamToInputStream;
import nz.xinsolutions.core.HashHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Optional;
import java.util.function.Consumer;

public class S3AssetStorage {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(S3AssetStorage.class);
    
    public static final String ASSETS_S3_DEFAULT_STATE = "false";
    public static final String ASSETS_S3_PROPERTY = "s3.assets";
    public static final String ENV_S3_ASSETS_ENABLED = "S3_ASSETS_ENABLED";
    public static final String ENV_S3_ASSETS_BUCKET = "S3_ASSETS_BUCKET";
    public static final String ENV_S3_ASSETS_PREFIX = "S3_ASSETS_PREFIX";
    
    
    /**
     * Feature is enabled when -Ds3.assets=true or S3_ASSETS_ENABLED=true
     * and S3_ASSETS_BUCKET has been set.
     *
     * @return true if the assets are enabled.
     */
    public boolean isEnabled() {
        
        boolean featureEnabled = (
            "true".equals(System.getProperty(ASSETS_S3_PROPERTY, ASSETS_S3_DEFAULT_STATE)) ||
            "true".equals(System.getenv(ENV_S3_ASSETS_ENABLED))
        );
        
        String bucket = getBucketEnvironmentVariable();
        boolean validConfiguration = StringUtils.isNotEmpty(bucket);
        
        return featureEnabled && validConfiguration;
    }
    
    
    /**
     * For a `full uri` and system properties
     * @param fullUri
     * @return
     */
    public boolean existsInS3(String fullUri) {
        String bucket = getBucketEnvironmentVariable();
        String imageLocation = getEncodedS3Key(fullUri);
        
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        return s3Client.doesObjectExist(bucket, imageLocation);
    }
    
    
    /**
     * @return the location at which the image and its rendering instructions should be stored.
     */
    protected String getEncodedS3Key(String fullUri) {
        String keyPrefix = getKeyPrefix();
        
        String encodeLocation = getEncodedLocation(fullUri);
        return String.format("%s/%s", keyPrefix, encodeLocation);
    }
    
    /**
     * @return the key prefix, or an empty string when not provided.
     */
    @NotNull
    protected String getKeyPrefix() {
        return Optional.of(System.getenv(ENV_S3_ASSETS_PREFIX)).orElse("");
    }
    
    /**
     * @return the bucket environment variable
     */
    protected String getBucketEnvironmentVariable() {
        return System.getenv(ENV_S3_ASSETS_BUCKET);
    }
    
    /**
     * @return the encoded location
     */
    public String getEncodedLocation(String fullUri) {
        ImageUrlAnalyser analyser = newImageUrlAnalyser();
        
        String extension = analyser.getExtension(fullUri);
        String image = analyser.getBinaryLocation(fullUri);
        String instr = analyser.getInstructionString(fullUri);
        
        return String.format(
            "%s/%s.%s",
            HashHelper.hashToMD5(image),
            HashHelper.hashToMD5(instr),
            extension
        );
    }
    
    
    /**
     * When invoking the binary content of an S3 object will be streamed to the user. At this
     * point you should have already checked whether the object exists on S3.
     *
     * @param fullUri   the URI to stream for from S3
     * @param resp      the response object to pipe the binary content into.
     */
    public void streamFromS3(String fullUri, HttpServletResponse resp) {
        String bucket = getBucketEnvironmentVariable();
        String encodedLocation = this.getEncodedS3Key(fullUri);
        
        // some custom headers to indicate where the image came from
        resp.setHeader("X-S3-Streamed", "true");
        resp.setHeader("X-S3-Object", encodedLocation);
        
        // set the content type based on the extension.
        resp.setHeader("Content-Type", newImageUrlAnalyser().getMimeType(fullUri));

        
        // retrieve the object and pipe the contents through to the response.
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
            S3Object s3Obj = s3Client.getObject(bucket, encodedLocation);
            
            S3ObjectInputStream s3ObjInpStr = s3Obj.getObjectContent();
            ServletOutputStream outStr = resp.getOutputStream();
            
            IOUtils.copy(s3ObjInpStr, outStr);
            
        }
        catch (Exception ex) {
            LOG.error("Couldn't stream from S3 from '{}', caused by:", encodedLocation, ex);
        }
    }
    
    
    /**
     * Write an image buffer to S3 using easystreams conversion from outputstream
     * to inputstream.
     */
    public void writeImageOutputStreamToS3(String fullUrl, Consumer<OutputStream> imgTrigger) {
        
        String bucket = getBucketEnvironmentVariable();
        String encodedLocation = getEncodedS3Key(fullUrl);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(newImageUrlAnalyser().getMimeType(fullUrl));
        
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

        //
        // use easy-streams to convert outputstream into inputstream efficiently
        // docs: https://io-tools.sourceforge.net/easystream/tutorial/tutorial.html
        //
        OutputStreamToInputStream<Void> out = new OutputStreamToInputStream<Void>() {
            
            @Override
            protected Void doRead(InputStream inputStream) {
                LOG.info("Attempting to write to s3://{}/{}", bucket, encodedLocation);
                // write to s3.
                s3Client.putObject(bucket, encodedLocation, inputStream, metadata);
                return null;
            }
            
        };
        
        try {
            // call to consumer, so it can populate the outputstream with an encoded image.
            imgTrigger.accept(out);
        }
        finally {
            IOUtils.closeQuietly(
                out,
                (ioEx) -> LOG.error("Couldn't close properly, caused by: ", ioEx)
            );
        }
    }
    
    
    /**
     * @return a new analyser instance
     */
    protected ImageUrlAnalyser newImageUrlAnalyser() {
        return new ImageUrlAnalyser();
    }
    
    
}
