package nz.xinsolutions.rest;

import nz.xinsolutions.packages.PackageException;
import nz.xinsolutions.packages.PackageExportService;
import nz.xinsolutions.packages.PackageImportService;
import nz.xinsolutions.packages.PackageListService;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
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

import static javax.servlet.http.HttpServletResponse.*;

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
     * Package list service bound here
     */
    @Autowired private PackageListService pkgListService;
    
    /**
     * Package service bound here
     */
    @Autowired private PackageExportService pkgExportService;
    
    /**
     * Package import service bound here
     */
    @Autowired private PackageImportService pkgImportService;
    
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
        // TODO: Make this less shitty
        File file = new File("/home/marnix/tmp/hippo/xinmods/packageManager.html");
        return new FileInputStream(file);
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPackages() {
        try {
            LOG.info("Return a list of packages");
            return Response.ok(pkgListService.getPackages()).build();
        }
        catch (PackageException ex) {
            LOG.error("Couldn't get all packages, caused by: ", ex);
            return Response.serverError().build();
        }
    }
    
    
    /**
     * This package imports the contents and CND elements of a package in the zip file for the <code>file</code> query parameter.
     * If there is 'autorun.groovy' in the package, it will be executed in the context of this request as well with access to
     * the repository.
     *
     * @param request
     * @param response
     * @param stream
     */
    @PUT
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void importPackage(
        @Context HttpServletRequest request,
        @Context HttpServletResponse response,
        @QueryParam("careful") boolean careful,
        @Multipart("file") Attachment stream)
    {
        RestContext ctx = new DefaultRestContext(this, request);
    
        try {
            
            // attachment found?
            if (stream == null) {
                LOG.error("No attachment found with name `file`, aborting.");
                response.setStatus(SC_BAD_REQUEST);
                return;
            }
            
            // attachment of the correct type?
            if (!stream.getContentType().getSubtype().equals("zip")) {
                LOG.error("Incoming stream was not of type `zip`, aborting.");
                response.setStatus(SC_BAD_REQUEST);
                return;
            }
            
            // copy file onto disk
            File tmpAttachmentFile = File.createTempFile("attachment_" + new Date().getTime(), ".zip");
            stream.transferTo(tmpAttachmentFile);
            
            pkgImportService.importFile(ctx.getRequestContext().getSession(), tmpAttachmentFile, false);
            
            LOG.info("Package temporarily stored at: " + tmpAttachmentFile.getCanonicalPath());
        }
        catch (RepositoryException rEx) {
            LOG.error("Repo exception caused by: ", rEx);
        }
        catch (PackageException pEx) {
            LOG.error("Could not create package, caused by: ", pEx);
        }
        catch (IOException ioEx) {
            LOG.error("Could not parse the incoming attachment, caused by: ", ioEx);
        }
    
    
    }
    
    /**
     * Build a package that was already defined
     *
     * @param packageId
     * @return
     */
    @GET
    @Path("/{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    public void exportPackage(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("id") String packageId) {
        LOG.info("Requesting build of " + packageId);
        
        RestContext ctx = new DefaultRestContext(this, request);
    
        try {
    
            if (!pkgListService.packageExists(packageId)) {
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
            pkgExportService.build(jcrSession, packageId, responseOutStr);
            
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
     * TODO: Implement the deletion of the package definition
     */
    @DELETE
    @Path("/{id}")
    public Void deletePackage(@PathParam("id") String packageId) {
        LOG.info("Requesting deletion of " + packageId);
        return null;
    }
    
    /**
     * TODO: Create a package definition using this endpoint
     */
    @PUT
    @Path("/{id}")
    public Void createPackage(@PathParam("id") String packageId) {
        LOG.info("Requesting creation of new package: " + packageId);
        return null;
    }

}
