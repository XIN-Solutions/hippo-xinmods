package nz.xinsolutions.jwt.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import nz.xinsolutions.jwt.JwtServiceConfig;
import nz.xinsolutions.jwt.models.JwtUserInfo;
import nz.xinsolutions.jwt.models.KeyHolder;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * This class is able to generate a JWT for public consumption based on the
 * private keys. It will be decorated with claims regarding the user currently
 * logged in to the CMS.
 */
public class JwtGenerator {


    public static final String JWT_ISSUER = "brxm-xinmods";
    public static final String JWT_AUDIENCE = "brxm-xinmods";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_USERGROUPS = "usergroups";
    public static final String CLAIM_IS_ACTIVE = "is_active";
    public static final String CLAIM_FIRST_NAME = "first_name";
    public static final String CLAIM_LAST_NAME = "last_name";
    public static final String CLAIM_EMAIL_ADDRESS = "email_address";

    /**
     * JWT Service configuration
     */
    private final JwtServiceConfig config;

    /**
     * RSA Key Loader
     */
    private final RSAKeyContainer rsaKeys;

    /**
     * Initialise data-members
     *
     * @param config    the jwt service configuration
     * @param rsaKeys   the loader for our rsa keys.
     */
    public JwtGenerator(JwtServiceConfig config, RSAKeyContainer rsaKeys) {
        this.config = config;
        this.rsaKeys = rsaKeys;
    }


    /**
     * Implementation specifically geared towards claims around device features.
     *
     * @return a JWT
     */
    public String generateTokenForBloomreachAccessFeatures(JwtUserInfo info) {

        return generateToken((builder) -> {

            // set the audience
            builder.withAudience(JWT_AUDIENCE);

            // add claims
            builder.withClaim(CLAIM_USERNAME, info.getUsername());
            builder.withClaim(CLAIM_IS_ACTIVE, info.isActive());
            builder.withClaim(CLAIM_FIRST_NAME, info.getFirstName());
            builder.withClaim(CLAIM_LAST_NAME, info.getLastName());
            builder.withClaim(CLAIM_EMAIL_ADDRESS, info.getEmail());

            if (CollectionUtils.isNotEmpty(info.getGroups())) {
                builder.withArrayClaim(
                    CLAIM_USERGROUPS,
                    info.getGroups().toArray(new String[0])
                );
            }
        });
    }


    /**
     * Generate a JWT with a specific set of claims
     *
     * @param claimAdder    the claims we want to create a token for
     * @return a JWT.
     */
    public String generateToken(Consumer<JWTCreator.Builder> claimAdder) {
        KeyHolder activeKey = getRecentKey();
        Algorithm algo =
            Algorithm.RSA256(
                activeKey.getPublicKey(),
                activeKey.getPrivateKey()
            );

        Date now = new Date();
        JWTCreator.Builder tokenBuilder = emptyToken(now);
        claimAdder.accept(tokenBuilder);
        return tokenBuilder.sign(algo);
    }


    /**
     * @return an empty token
     */
    protected JWTCreator.Builder emptyToken(Date now) {
        return JWT.create()
                    .withIssuer(getIssuer())
                    .withIssuedAt(now)
                    .withKeyId(getRecentKey().getKeyIdentifier())
                    .withExpiresAt(getExpirationTime(now));
    }

    private String getIssuer() {
        return JWT_ISSUER;
    }

    /**
     * @return the expiration time for this token
     */
    protected Date getExpirationTime(Date now) {
        return new Date(now.getTime() + getExpirationSetting());
    }

    /**
     * @return the amount of milliseconds a token is valid
     */
    protected long getExpirationSetting() {
        return config.getJWTExpirationTime();
    }

    /**
     * @return the last key in the list.
     */
    protected KeyHolder getRecentKey() {
        return rsaKeys.getKeys().get(rsaKeys.getKeys().size() - 1);
    }

}
