package nz.xinsolutions.packages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ZipUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 23/09/17
 */
@Component
public class PackageService {
    
    public static final String FILE_CND_EXPORT_JSON = "cndExport.json";
    public static final String FILE_METADATA = "metadata.json";
    @Autowired
    private MultiPathExporter multiPathExporter;
    
    
    @Autowired
    private PartialCndExporter partialCndExporter;
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PackageService.class);
    
    private List<Package> packages = new ArrayList<Package>() {{
        
        add(new Package() {{
            setId("blog-package");
            setFilters(Arrays.asList(
                "/content/documents/blog",
                "/hippo:namespaces/xinmods",
                "/hippo:configuration/hippo:queries/hippo:templates/new-xinmods-genericimageset-folder",
                "/hippo:configuration/hippo:queries/hippo:templates/new-xinmods-genericimageset-image"));
            setCnds(Arrays.asList(
                "xinmods:basedocument",
                "xinmods:content",
                "xinmods:postdetails",
                "xinmods:eventdetails",
                "xinmods:menuitem",
                "xinmods:menu"
            ));
        }});
    
        add(new Package() {{
            setId("shop-package");
            setRequiredCnds(Arrays.asList("xinmods:content"));
            setFilters(Arrays.asList("/content/documents/shop"));
            setCnds(Arrays.asList("xinmods:content", "xinmods:postdetails", "xinmods:eventdetails"));
        }});

    }};
    
    /**
     * Build a package
     *
     * @param packageId
     * @param outStream
     * @throws PackageException
     */
    public void build(Session jcrSession, String packageId, OutputStream outStream) throws PackageException {
     
        Package pkg = getPackage(packageId);
        if (pkg == null) {
            throw new PackageException("No such package with identifier: " + packageId);
        }
    
        try {
            List<Node> filterNode = getFilterNodes(jcrSession, pkg);
            File tmpBaseFolder = multiPathExporter.exportZippedContent(filterNode);
            
            String cnd =
                partialCndExporter.exportCnds(
                    jcrSession.getWorkspace(),
                    pkg.getCnds().toArray(new String[0])
                );
            
            LOG.info("CND:\n" + cnd);
            
            String packageMetadata = fromPojoToString(pkg);
            
            // write package description to file
            writeStringAsFile(tmpBaseFolder, FILE_METADATA, packageMetadata);
            
            // write CND string to file
            writeStringAsFile(tmpBaseFolder, FILE_CND_EXPORT_JSON, cnd);
    
            // pack and stream back to the user
            ZipUtil.pack(tmpBaseFolder, outStream, ZipUtil.DEFAULT_COMPRESSION_LEVEL);

            LOG.info("Attempting to delete the temporary folder in which package content was generated");
            FileUtils.deleteDirectory(tmpBaseFolder);
            LOG.info("Deleted temporary folder at " + tmpBaseFolder.getAbsolutePath());
        }
        catch (IOException ioEx) {
            LOG.error("IO Exception, caused by: ", ioEx);
        }
        catch (RepositoryException rEx) {
            LOG.error("Cannot find node, caused by: ", rEx);
        }
        
        
    }
    
    /**
     * @return the json string of <code>pojo</code>.
     */
    protected String fromPojoToString(Object pojo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter strWriter = new StringWriter();
            objectMapper.writeValue(strWriter, pojo);
            return strWriter.toString();
        }
        catch (IOException ioEx) {
            LOG.error("Could not properly marshal the pojo, caused by: ", ioEx);
        }
        return null;
    }
    
    
    /**
     * Write a string to file.
     *
     * @param folder    is the folder to write to the file to
     * @param filename  is the name of the file to create
     * @param content   is the contents of the file.
     *
     * @throws FileNotFoundException
     */
    protected void writeStringAsFile(File folder, String filename, String content) throws FileNotFoundException {
        File metainfo = new File(folder, filename);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(metainfo)));
        writer.print(content);
        writer.close();
    }
    
    /**
     * Get the jcr nodes for a package instance
     *
     * @param jcrSession
     * @param pkg
     * @return
     *
     * @throws RepositoryException
     */
    protected List<Node> getFilterNodes(Session jcrSession, Package pkg) throws RepositoryException {
        
        List<Node> filterNode = new ArrayList<>();
        
        // all filters
        for (String filter : pkg.getFilters()) {
            
            if (!jcrSession.nodeExists(filter)) {
                LOG.info("The node at path `{}` does not exist.", filter);
                continue;
            }
            
            Node node = jcrSession.getNode(filter);
            filterNode.add(node);
        }
        
        return filterNode;
    }
    
    
    /**
     * @return a list of all pages
     * @throws PackageException
     */
    public List<Package> getPackages() throws PackageException {
        return packages;
    }
    
    
    /**
     * Get package information for package with id <code>id</code>
     *
     * @param id is the package identifier to retrieve
     * @return the package instance or null if not found
     * @throws PackageException
     */
    public Package getPackage(String id) throws PackageException {
        return getPackages()
                    .stream()
                        .filter( pkg -> pkg.getId().equals(id) )
                    .findFirst()
                    .orElse(null)
            ;
    }
    
    /**
     * Make sure that a package with `packageId` exists.
     *
     * @param packageId is the identifier of the package
     * @throws PackageException thrown when something goes wrong
     * @return true if the package exists
     */
    public boolean packageExists(String packageId) throws PackageException {
        return getPackage(packageId) != null;
    }
}
