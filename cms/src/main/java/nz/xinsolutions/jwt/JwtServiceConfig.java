package nz.xinsolutions.jwt;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *      To contain all configurations required for the JWT services to function.
 */
public class JwtServiceConfig {

    /**
     * A list of public key locations
     */
    private List<String> publicKeyLocations;

    /**
     * A list of private key locations
     */
    private List<String> privateKeyLocations;

    /**
     * Hour in milliseconds, default expiration time.
     */
    public static final int DAY_IN_MS = 1000 * 60 * 60 * 24;


    /**
     * Create a new JwtServiceConfig instance based on settings or normal fallback defaults
     * specified in the web.xml
     *
     * @return the jwt service configuration instance to build the rest off of.
     */
    public static JwtServiceConfig fromWebContext(ServletContext context) {

        JwtServiceConfig cfg = new JwtServiceConfig();

        cfg.setPublicKeyLocations(Collections.singletonList("/keys/jwt_public_key.der"));
        cfg.setPrivateKeyLocations(Collections.singletonList("/keys/jwt_private_key.der"));

        // TODO: allow override from servlet context.

        return cfg;
    }


    // ----------------------------------------------------------------------------------
    //      Getters and Setters
    // ----------------------------------------------------------------------------------

    public List<String> getPublicKeyLocations() {
        return publicKeyLocations;
    }

    public void setPublicKeyLocations(List<String> publicKeyLocations) {
        this.publicKeyLocations = publicKeyLocations;
    }

    public List<String> getPrivateKeyLocations() {
        return privateKeyLocations;
    }

    public void setPrivateKeyLocations(List<String> privateKeyLocations) {
        this.privateKeyLocations = privateKeyLocations;
    }

    /**
     * @return the expiration time
     */
    public long getJWTExpirationTime() {
        return DAY_IN_MS;
    }
}
