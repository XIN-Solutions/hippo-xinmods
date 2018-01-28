package nz.xinsolutions.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 28/01/18
 *
 *  A small helper class that is able to 
 */
public class CORSHelper {
    
    /**
     * Add some CORS headers to the response builder.
     *
     * @param responseBuilder   is the builder to
     * @return
     */
    public static Response.ResponseBuilder enableCORS(Response.ResponseBuilder responseBuilder) {
    
        return (
            responseBuilder
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Credentials", "true")
            .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
        );
        
    }
    
    /**
     * Add some CORS headers to a normal response object
     *
     * @param response  is the response instance
     */
    public static void enableCORS(HttpServletResponse response) {
        
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
