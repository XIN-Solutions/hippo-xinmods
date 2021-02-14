package nz.xinsolutions.jwt.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import nz.xinsolutions.jwt.JwtServiceConfig;
import nz.xinsolutions.jwt.services.JwksGenerator;
import nz.xinsolutions.jwt.services.RSAKeyContainer;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *      To expose the JWKS mapping to anyone.
 */
public class JWKSMapServlet extends HttpServlet {


    /**
     * When called output the JWKS mapping.
     *
     * @param req   the request instance
     * @param resp  the response instance for this request
     *
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setHeader("Content-Type", "application/json");
        resp.setHeader("Cache-Control", "max-age=3600");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        // create service configuration
        JwtServiceConfig jwtCfg = newServiceConfigInstance(req.getServletContext());
        RSAKeyContainer keyContainer = newRSAKeyContainer(jwtCfg);
        keyContainer.initialiseKeys();

        JwksGenerator jwksGenerator = newJwksGeneratorInstance(keyContainer);

        // create map
        Map<String, List<JSONObject>> jwksMap = jwksGenerator.createJwksMap();

        // write to response
        ObjectMapper map = new ObjectMapper();
        map.writeValue(resp.getWriter(), jwksMap);
    }

    /**
     * @return a jwks generator instance
     */
    protected JwksGenerator newJwksGeneratorInstance(RSAKeyContainer keyContainer) {
        return new JwksGenerator(keyContainer);
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
