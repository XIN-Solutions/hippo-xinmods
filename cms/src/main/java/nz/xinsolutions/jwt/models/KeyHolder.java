package nz.xinsolutions.jwt.models;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * This class is able to hold a public/private combination of an RSA asymmetric key.
 */
public class KeyHolder {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(KeyHolder.class);

    private RSAKey rsaKey;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;


    /**
     * @return the key identifier
     */
    public String getKeyIdentifier() {
        return this.rsaKey.getKeyID();
    }

    /**
     * @return the key element instance of our currently active key
     */
    protected RSAKey determineRsaKey(RSAPublicKey pubKey) {
        try {
            return
                new RSAKey.Builder(pubKey)
                    .keyIDFromThumbprint()
                    .build();

        }
        catch (JOSEException jEx) {
            LOG.error("Could not generate rsa key, caused by: ", jEx);
            throw new IllegalStateException("Cannot generate JWKS", jEx);
        }
    }



    // -------------------------------------------------------
    //      Getters and Setters
    // -------------------------------------------------------

    public RSAKey getRsaKey() {
        return rsaKey;
    }

    public void setRsaKey(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
        this.rsaKey = determineRsaKey(publicKey);
    }
}
