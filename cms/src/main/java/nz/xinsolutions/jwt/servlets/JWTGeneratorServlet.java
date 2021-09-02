package nz.xinsolutions.jwt.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.xinsolutions.config.XinmodsConfig;
import nz.xinsolutions.core.jackrabbit.JcrSessionHelper;
import nz.xinsolutions.jwt.JwtServiceConfig;
import nz.xinsolutions.jwt.models.JwtUserInfo;
import nz.xinsolutions.jwt.services.JwtGenerator;
import nz.xinsolutions.jwt.services.RSAKeyContainer;
import nz.xinsolutions.jwt.services.UserInfoGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Marnix Kok <marnix@elevate.net.nz>
 *
 * Purpose:
 *
 *      Returns a new JWT if the user is logged in.
 */
public class JWTGeneratorServlet extends HttpServlet {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(JWTGeneratorServlet.class);
    public static final String PARAM_SOURCE = "source";

    /**
     * When called output the JWT Token.
     *
     * @param req      the request object for this request
     * @param resp     the response object for this request
     *
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String source = getSourceParameter(req);

        if (StringUtils.isEmpty(source)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            returnErrorMessage(resp, "No `source` parameter specified");
            return;
        }

        if (!this.isUserLoggedIn(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            returnErrorMessage(resp, "User not logged in to CMS");
            return;
        }


        Session session = null;
        try {
            session = JcrSessionHelper.loginAdministrative();
            if (!this.validSource(session, source)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                returnErrorMessage(resp, "Invalid request source");
                return;
            }


            resp.setHeader("Access-Control-Allow-Origin", source);
            resp.setHeader("Access-Control-Allow-Credentials", "true");

            // create service configuration
            JwtServiceConfig jwtCfg = newServiceConfigInstance(req.getServletContext());
            RSAKeyContainer keyContainer = newRSAKeyContainer(jwtCfg);
            keyContainer.initialiseKeys();

            JwtGenerator jwtGenerator = newJwtGeneratorInstance(jwtCfg, keyContainer);
            UserInfoGenerator userInfoGen = newUserInfoGenerator();

            JwtUserInfo userInfo = userInfoGen.createUserInfo(req);
            String token = jwtGenerator.generateTokenForBloomreachAccessFeatures(userInfo);

            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().print(String.format("\"%s\"", token));
            resp.flushBuffer();
        }
        catch (RepositoryException ex) {
            LOG.error("Could not determine source parameter validity.");

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnErrorMessage(resp, "Could not determine source");
        }
        finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }

    }


    /**
     * Determine source validity by reading the xinmods module configuration
     *
     * @param session   JCR session
     * @param source    the source we hopefully find.
     * @return true if it's a valid source.
     */
    protected boolean validSource(Session session, String source) {

        XinmodsConfig xinCfg = new XinmodsConfig(session);
        List<String> whitelist = xinCfg.getJwtSourceWhitelist();
        return whitelist.contains(source);

    }

    /**
     * Return an error message to the user.
     *
     * @param resp  the response object to write ot
     * @param message the message to return
     * @throws IOException
     */
    protected void returnErrorMessage(HttpServletResponse resp, String message) throws IOException {
        resp.setHeader("Content-Type", "application/json");
        Map<String, Object> outputMap = new LinkedHashMap<>();
        outputMap.put("success", false);
        outputMap.put("message", message);

        ObjectMapper map = new ObjectMapper();
        map.writeValue(resp.getWriter(), outputMap);
    }

    /**
     * Get the source to use in the response header.
     * @param req the request parameter
     * @return the access allow origin hostname
     */
    protected String getSourceParameter(HttpServletRequest req) {
        if (!StringUtils.isBlank(req.getParameter(PARAM_SOURCE))) {
            return req.getParameter(PARAM_SOURCE);
        }
        return "*";
    }

    @NotNull
    private UserInfoGenerator newUserInfoGenerator() {
        return new UserInfoGenerator();
    }

    @NotNull
    private JwtGenerator newJwtGeneratorInstance(JwtServiceConfig jwtCfg, RSAKeyContainer keyContainer) {
        return new JwtGenerator(jwtCfg, keyContainer);
    }


    /**
     * @return true if the user has logged in.
     */
    protected boolean isUserLoggedIn(HttpServletRequest req) {
        HttpSession httpSession = req.getSession(false);
        return httpSession != null && httpSession.getAttribute("hippo:username") != null;
    }


    /**
     * @return a RSA Key Container instance based on <code>jwtCfg</code>
     */
    protected RSAKeyContainer newRSAKeyContainer(JwtServiceConfig jwtCfg) {
        return new RSAKeyContainer(jwtCfg);
    }

    /**
     * @return a jwt service config based on the servlet context
     */
    protected JwtServiceConfig newServiceConfigInstance(ServletContext context) {
        return JwtServiceConfig.fromWebContext(context);
    }

}
