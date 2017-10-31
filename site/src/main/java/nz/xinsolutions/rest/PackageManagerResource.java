package nz.xinsolutions.rest;

import nz.xinsolutions.packages.PackageException;
import nz.xinsolutions.packages.PackageService;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.onehippo.cms7.essentials.components.rest.ctx.DefaultRestContext;
import org.onehippo.cms7.essentials.components.rest.ctx.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 10/09/17
 */
@Path("/packages/")
@Consumes(MediaType.APPLICATION_JSON)
public class PackageManagerResource extends BaseRestResource {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PackageManagerResource.class);
    public static final String DATE_PATTERN = "Y-M-d";
    
    /**
     * Package service bound here
     */
    @Autowired private PackageService pkgService;
    
    /**
     * Retrieve the HTML for the angular application
     *
     * @return  the input stream
     * @throws FileNotFoundException
     */
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public InputStream packageIndex() throws FileNotFoundException {
        File file = new File("/home/marnix/tmp/hippo/xinmods/packageManager.html");
        return new FileInputStream(file);
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPackages() {
        try {
            LOG.info("Return a list of packages");
            return Response.ok(pkgService.getPackages()).build();
        }
        catch (PackageException ex) {
            LOG.error("Couldn't get all packages, caused by: ", ex);
            return Response.serverError().build();
        }
    }
    
    
    /**
     * Build a package that was already defined
     *
     * @param packageId
     * @return
     */
    @POST
    @Path("/{id}/build")
    @Produces(MediaType.APPLICATION_JSON)
    public void buildPackage(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("id") String packageId) {
        LOG.info("Requesting build of " + packageId);
        
        RestContext ctx = new DefaultRestContext(this, request);
    
        try {
    
            if (!pkgService.packageExists(packageId)) {
                response.setStatus(SC_NOT_FOUND);
                return;
            }
    
            String date = getFilenameDate(getNowStamp());
            String packageDate = packageId + "-" + date + ".zip";
            
            response.setStatus(SC_OK);
            response.setHeader("Content-Type", "application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + packageDate + "\"");
            ServletOutputStream responseOutStr = response.getOutputStream();
            Session jcrSession = ctx.getRequestContext().getSession();
            pkgService.build(jcrSession, packageId, responseOutStr);
            
        }
        catch (IOException ioEx) {
            LOG.error("Could not retrieve output stream for response object, caused by: ", ioEx);
        }
        catch (RepositoryException rEx) {
            LOG.error("Repository exception, caused by: ", rEx);
        }
        catch (PackageException pkgEx) {
            LOG.error("Something went wrong, caused by: ", pkgEx);
        }
        
    }
    
    
    /**
     * @return the now date
     */
    protected Date getNowStamp() {
        return new Date();
    }
    
    /**
     * @return the formatted date for the filename for <code>date</code>.
     */
    protected String getFilenameDate(Date date) {
        return new SimpleDateFormat(DATE_PATTERN).format(date);
    }
    
    /**
     *
     * @param packageId
     * @return
     */
    @DELETE
    @Path("/{id}")
    public Void deletePackage(@PathParam("id") String packageId) {
        LOG.info("Requesting deletion of " + packageId);
        return null;
    }
    
    /**
     *
     * @param packageId
     * @return
     */
    @PUT
    @Path("/{id}")
    public Void createPackage(@PathParam("id") String packageId) {
        LOG.info("Requesting creation of new package: " + packageId);
        return null;
    }

}
