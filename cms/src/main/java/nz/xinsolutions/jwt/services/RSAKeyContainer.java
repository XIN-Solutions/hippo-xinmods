package nz.xinsolutions.jwt.services;

import com.amazonaws.util.IOUtils;
import nz.xinsolutions.jwt.JwtServiceConfig;
import nz.xinsolutions.jwt.models.KeyHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Load RSA keys from disk.
 */
public class RSAKeyContainer {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(RSAKeyContainer.class);

    /**
     * Application configuration
     */
    private final JwtServiceConfig config;

    /**
     * Initialise data-members
     *
     * @param config the JWT Service Configuration
     */
    public RSAKeyContainer(JwtServiceConfig config) {
        this.config = config;
    }

    /**
     * A list of key holders
     */
    private List<KeyHolder> keys;

    /**
     * Initialise the keyholder by taking all the private and public key locations
     * and adding a KeyHolder instance for each of them to the keys collection.
     */
    public void initialiseKeys() {

        this.keys = new ArrayList<>();
        for (int idx = 0; idx < getPrivateKeyLocations().size(); ++idx) {

            String privLoc = getPrivateKeyLocations().get(idx),
                   pubLoc = getPublicKeyLocations().get(idx)
            ;


            KeyHolder holder = new KeyHolder();
            holder.setPrivateKey(getPrivateRSAKey(privLoc));
            holder.setPublicKey(getPublicRSAKey(pubLoc));
            keys.add(holder);
        }
    }

    /**
     * @return the list of keys.
     */
    public List<KeyHolder> getKeys() {
        return keys;
    }

    /**
     * @return the private rsa key
     */
    protected RSAPrivateKey getPrivateRSAKey(String keyLocation) {
        try {
            ByteArrayOutputStream baOutStr = new ByteArrayOutputStream();
            IOUtils.copy(this.getClass().getResourceAsStream(keyLocation), baOutStr);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(baOutStr.toByteArray());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        }
        catch (Exception ex) {
            LOG.error("Exception when loading private key, caused by: ", ex);
            throw new IllegalStateException("Need to be able to load the private key to do anything useful.");
        }
    }

    /**
     * @param keyLocation where to find the public key.
     * @return the public rsa key
     */
    protected RSAPublicKey getPublicRSAKey(String keyLocation) {
        try {
            ByteArrayOutputStream baOutStr = new ByteArrayOutputStream();
            IOUtils.copy(this.getClass().getResourceAsStream(keyLocation), baOutStr);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(baOutStr.toByteArray());

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        }
        catch (Exception ex) {
            LOG.error("Exception when loading public key, caused by: ", ex);
            throw new IllegalStateException("Need to be able to load the public key to do anything useful.");
        }
    }

    /**
     * @return the location of the public key file
     */
    protected List<String> getPublicKeyLocations() {
        return this.config.getPublicKeyLocations();
    }


    /**
     * @return the location of the public key file
     */
    protected List<String> getPrivateKeyLocations() {
        return this.config.getPrivateKeyLocations();
    }

}
