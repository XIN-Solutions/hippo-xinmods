package nz.xinsolutions.core.security;

import org.apache.jackrabbit.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BasicAuthUtility {


    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String BASIC_AUTH_PREFIX = "Basic "; // intentional trailing space
    private static final int BASIC_AUTH_PREFIX_LENGTH = BASIC_AUTH_PREFIX.length();

    private static final Logger log = LoggerFactory.getLogger(BasicAuthUtility.class);

    /**
     * @return true if an authorization header was specified
     */
    public static boolean hasAuthorizationHeader(HttpServletRequest request) {
        return (getAuthorizationHeader(request) != null);
    }

    /**
     * @return parse the credentials from the header into a simple credentials instance
     */
    public static SimpleCredentials parseAuthorizationHeader(HttpServletRequest request) {
        String authHeader = getAuthorizationHeader(request);
        if (authHeader == null) {
            return null;
        }

        String decoded = base64DecodeAuthHeader(authHeader);
        String[] creds = getUsernamePasswordFromAuth(decoded);
        return new SimpleCredentials(creds[0], creds[1].toCharArray());
    }

    /**
     * @return the Authorization header for parsing
     */
    public static String getAuthorizationHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.length() < BASIC_AUTH_PREFIX_LENGTH) {
            log.info("Authorization header not found.");
            return null;
        }
        return authHeader;
    }

    /**
     * @return the decoded version of the authentication
     */
    protected static String base64DecodeAuthHeader(String authHeader) {
        if (authHeader == null || authHeader.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Base64.decode(authHeader.substring(BASIC_AUTH_PREFIX_LENGTH), out);
            return new String(out.toByteArray(), "UTF-8");
        } catch (IOException e) {
            log.warn("Unable to decode auth header '" + authHeader + "' : " + e.getMessage());
            log.debug("Decode error:", e);
        }
        return null;
    }

    public static String[] getUsernamePasswordFromAuth(String decoded) {
        int split = decoded.indexOf(':');
        if (split < 1) {
            log.warn("Invalid authorization header found '{}'.", decoded);
            return null;
        }
        return new String[] { decoded.substring(0, split), decoded.substring(split + 1) };
    }
}
