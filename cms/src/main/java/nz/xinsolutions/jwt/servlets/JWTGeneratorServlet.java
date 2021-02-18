package nz.xinsolutions.jwt.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.xinsolutions.jwt.JwtServiceConfig;
import nz.xinsolutions.jwt.models.JwtUserInfo;
import nz.xinsolutions.jwt.services.JwtGenerator;
import nz.xinsolutions.jwt.services.RSAKeyContainer;
import nz.xinsolutions.jwt.services.UserInfoGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
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

        resp.setHeader("Access-Control-Allow-Origin", getSourceParameter(req));
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        if (!this.isUserLoggedIn(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setHeader("Content-Type", "application/json");

            Map<String, Object> outputMap = new LinkedHashMap<>();
            outputMap.put("success", false);
            outputMap.put("message", "User not logged in to CMS");

            ObjectMapper map = new ObjectMapper();
            map.writeValue(resp.getWriter(), outputMap);

            return;
        }


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
