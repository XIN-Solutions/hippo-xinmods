package nz.xinsolutions.rest;

import nz.xinsolutions.packages.Package;
import nz.xinsolutions.packages.*;
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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import static javax.servlet.http.HttpServletResponse.*;
import static nz.xinsolutions.core.JcrHelper.loginAdministrative;
import static nz.xinsolutions.rest.CORSHelper.enableCORS;

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
     * @return a list of all the packages
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPackages(@Context HttpServletRequest request) {
        RestContext ctx = new DefaultRestContext(this, request);
        
        try {
            LOG.info("Return a list of packages");
            Session session = getSession(ctx);
            return enableCORS(Response.ok(pkgListService.getPackages(session))).build();
        }
        catch (PackageException | RepositoryException ex) {
            LOG.error("Couldn't get all packages, caused by: ", ex);
            return errorResponse();
        }
    }
    
    
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPackage(@Context HttpServletRequest request, @PathParam("id") String packageId) {
        RestContext ctx = new DefaultRestContext(this, request);
        try {
            Session session = getSession(ctx);
            Package pkg = pkgListService.getPackage(session, packageId);
            if (pkg == null) {
                LOG.info("Package with id `{}` not found", packageId);
                return enableCORS(Response.status(404)).build();
            }
            
            return enableCORS(Response.ok(pkg)).build();
        }
        catch (Exception ex) {
            LOG.error("Something went wrong while trying to retrieve package with id `{}`", packageId);
            return errorResponse();
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
    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importPackage(
        @Context HttpServletRequest request,
        @Context HttpServletResponse response,

        @Multipart(value = "importContent", required = false) boolean importContent,
        @Multipart(value = "importCND", required = false) boolean importCND,
        @Multipart(value = "importPackageDef", required = false) boolean importPackageDef,
        @Multipart(value = "redirectTo", required = false) String redirectTo,
        
        @Multipart("file") Attachment stream)
    {
        Session jcrSession = null;
        
        try {
            // do important importing things with the administrative user.
            jcrSession = loginAdministrative();
            
            // attachment found?
            if (stream == null) {
                LOG.error("No attachment found with name `file`, aborting.");
                response.setStatus(SC_BAD_REQUEST);
                return errorResponse();
            }
            
            // attachment of the correct type?
            if (!stream.getContentType().getSubtype().equals("zip")) {
                LOG.error("Incoming stream was not of type `zip`, aborting.");
                response.setStatus(SC_BAD_REQUEST);
                return errorResponse();
            }
            
            // copy file onto disk
            File tmpAttachmentFile = File.createTempFile("attachment_" + new Date().getTime(), ".zip");
            stream.transferTo(tmpAttachmentFile);
            
            pkgImportService.importFile(
                jcrSession, tmpAttachmentFile,
                importContent, importCND, importPackageDef
            );
            
            LOG.info("Package temporarily stored at: " + tmpAttachmentFile.getCanonicalPath());
            
            return
                enableCORS(
                    Response.temporaryRedirect(URI.create(redirectTo))
                ).build()
            ;
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
        finally {
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
            }
        }
        return errorResponse();
    }
    
    protected Response emptyResponse() {
        return enableCORS(Response.ok("Success")).build();
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

        Session jcrSession = null;
        try {
            jcrSession = loginAdministrative();
    
            if (!pkgListService.packageExists(jcrSession, packageId)) {
                response.setStatus(SC_NOT_FOUND);
                CORSHelper.enableCORS(response);
            }
    
            String date = getFilenameDate(getNowStamp());
            String packageDate = packageId + "-" + date + ".zip";
            
            response.setStatus(SC_OK);
            response.setHeader("Content-Type", "application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + packageDate + "\"");
            ServletOutputStream responseOutStr = response.getOutputStream();
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
        finally {
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
            }
        }
        
    }
    
    /**
     * TODO: Implement the deletion of the package definition
     */
    @DELETE
    @Path("/{id}")
    public Response deletePackage(@PathParam("id") String packageId) {
        LOG.info("Requesting deletion of " + packageId);

        Session jcrSession = null;
        try {
            jcrSession = loginAdministrative();
        
            if (!pkgListService.packageExists(jcrSession, packageId)) {
                LOG.error("Package with ID `{}` doesn't exist", packageId);
                return enableCORS(Response.serverError()).build();
            }
        
            pkgListService.deletePackage(jcrSession, packageId);
            return enableCORS(Response.ok()).build();
        }
        catch (RepositoryException | PackageException ex) {
            LOG.error("Could not complete package creation, caused by: ", ex);
            return enableCORS(Response.serverError()).build();
        }
        finally {
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
            }
        }
        
    }
    

    
    /**
     * TODO: Create a package definition using this endpoint
     */
    @PUT
    @Path("/{id}")
    public Response createPackage(
        @Context HttpServletRequest request,
        @PathParam("id") String packageId,
        Package packageInfo
    ) {
        LOG.info("Requesting creation of new package: " + packageId);
        
        Session jcrSession = null;
        try {
            jcrSession = loginAdministrative();
            
            if (pkgListService.packageExists(jcrSession, packageId)) {
                LOG.error("Package with ID `{}` already exists, aborting.", packageId);
                return enableCORS(Response.serverError()).build();
            }
            
            packageInfo.setId(packageId);
            pkgListService.addPackage(jcrSession, packageInfo);
            return enableCORS(Response.ok()).build();
        }
        catch (RepositoryException | PackageException ex) {
            LOG.error("Could not complete package creation, caused by: ", ex);
            return enableCORS(Response.serverError()).build();
        }
        finally {
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
            }
        }
    }
    
    
    /**
     * TODO: Create a package definition using this endpoint
     */
    @POST
    @Path("/{id}")
    public Response editPackage(
        @Context HttpServletRequest request,
        @PathParam("id") String packageId,
        Package packageInfo
    ) {
        LOG.info("Requesting creation of new package: " + packageId);
    
        Session jcrSession = null;
        try {
            jcrSession = loginAdministrative();
            
            // should exist.
            if (!pkgListService.packageExists(jcrSession, packageId)) {
                LOG.error("Package with ID `{}` does not exist, aborting.", packageId);
                return enableCORS(Response.serverError()).build();
            }
            
            packageInfo.setId(packageId);

            pkgListService.deletePackage(jcrSession, packageId);
            pkgListService.addPackage(jcrSession, packageInfo);
            return enableCORS(Response.ok()).build();
        }
        catch (RepositoryException | PackageException ex) {
            LOG.error("Could not complete package creation, caused by: ", ex);
            return enableCORS(Response.serverError()).build();
        }
        finally {
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
            }
        }
    }
    
    /**
     * Make sure to respond appropriately to an OPTIONS
     * @return
     */
    @OPTIONS
    @Path("{path : .*}")
    public Response options() {
        return enableCORS(Response.ok("")).build();
    }
    
    /**
     * @return an error response
     */
    protected Response errorResponse() {
        return enableCORS(Response.serverError()).build();
    }
    
    
    protected Session getSession(RestContext ctx) throws RepositoryException {
        return ctx.getRequestContext().getSession();
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
    
}
