package nz.xinsolutions.authentication;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      To receive authentication requests from the frontend identified by a JWT access token.
 *      Also outputs JSON with the authentication configuration as stored in the JCR.
 *
 */
public class Auth0SessionBridgeServlet extends HttpServlet {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Auth0SessionBridgeServlet.class);


    /**
     * Outputs the auth0 configuration as a JSON so we can use it in the page.
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Session session = null;
        try {
            Auth0Service authService = newAuthService();
            session = authService.loginAdministrative();
            Auth0Configuration authConfig = getAuthConfiguration(session, authService);

            StringWriter strWriter = new StringWriter();
            ObjectMapper objMap = new ObjectMapper();
            objMap.writeValue(strWriter, authConfig);

            String output = "window.auth0Config = " + strWriter.toString() + ";";

            // 5 minutes cache.
            resp.setHeader("Cache-Control", "max-age=300");
            resp.setContentType("application/json");
            resp.getOutputStream().println(output);

        }
        catch (RepositoryException rEx) {
            LOG.error("Could not output auth0 configuration", rEx);

            resp.setStatus(500);
            resp.getOutputStream().println("Invalid Configuration.");
        }
        finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }
    }

    /**
     * Intercept bridge request
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringWriter strWriter = new StringWriter();
        IOUtils.copy(req.getInputStream(), strWriter);

        Auth0Service authService = newAuthService();

        ObjectMapper objMap = new ObjectMapper();
        BridgeTokenModel tokenInfo = objMap.readValue(strWriter.toString(), BridgeTokenModel.class);

        Session session = null;
        try {
            session = authService.loginAdministrative();
            Auth0Configuration authConfig = getAuthConfiguration(session, authService);

            DecodedJWT jwt = authService.parseValidToken(authConfig, tokenInfo.getAccessToken());
            if (jwt == null) {
                LOG.error("This token is not valid (for email: {})", tokenInfo.getEmail());
                outputMessage(resp, false, "Error: This is not a valid access token.");
                return;
            }

            if (!tokenInfo.getEmailVerified()) {
                LOG.error("This email address has not been verified (email: {})", tokenInfo.getEmail());
                outputMessage(resp, false, "Error: This account has not been verified yet.");
                return;
            }


            if (!authService.userExists(session, tokenInfo.getEmail())) {
                boolean success = authService.createUser(session, tokenInfo);
                if (!success) {
                    outputMessage(resp, false, "Error: Could not create this user.");
                    return;
                }
            }

            String password = authService.refreshUserAndSetPassword(session, tokenInfo, jwt);

            outputMessage(resp, true, password);
        }
        catch (Exception ex) {
            LOG.error("Something went wrong bridging the token into brXM, caused by: ", ex);
            outputMessage(resp, false, "Error: " + ex.getMessage());
        }
        finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }
    }

    /**
     * @return the auth0 configuration
     */
    protected Auth0Configuration getAuthConfiguration(Session session, Auth0Service service) throws RepositoryException {
        return service.retrieveConfiguration(session);
    }

    /**
     * @return a new instance of the auth service.
     */
    @NotNull
    protected Auth0Service newAuthService() {
        return new Auth0Service();
    }


    /**
     * Output some information into the response object.
     *
     * @param response  the response object
     * @param success   did it work?
     * @param message   the status message
     */
    protected void outputMessage(HttpServletResponse response, boolean success, String message) {
        // prepare simple result map
        Map<String, Object> map = new HashMap<>();
        map.put("success", success);
        map.put("message", message);

        try {
            // convert to JSON
            ObjectMapper objMap = new ObjectMapper();
            StringWriter writer = new StringWriter();
            objMap.writeValue(writer, map);

            // output the JSON
            response.getOutputStream().println(writer.toString());
        }
        catch (Exception ex) {
            LOG.error("Could not marshal into JSON, caused by: ", ex);
        }
    }

}
