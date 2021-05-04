package nz.xinsolutions.rest;

import nz.xinsolutions.core.Rest;
import nz.xinsolutions.packages.Package;
import nz.xinsolutions.packages.*;
import nz.xinsolutions.beans.ClonePackagePayload;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
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
import java.util.Date;
import java.util.HashMap;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 10/09/17
 */
@Path("/packages/")
@Consumes(MediaType.APPLICATION_JSON)
public class PackageManagerResource extends BaseRestResource implements Rest {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PackageManagerResource.class);

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

        Session session = null;
        try {
            Session reqSession = getSession(this, request);
            session = impersonateFromRequest(reqSession, request);

            LOG.info("Return a list of packages");
            return Response.ok(pkgListService.getPackages(session)).build();
        }
        catch (Exception ex) {
            LOG.error("Couldn't get all packages, caused by: ", ex);
            return errorResponse();
        }
        finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }
    }



    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPackage(@Context HttpServletRequest request, @PathParam("id") String packageId) {

        Session session = null;
        try {
            Session reqSession = getSession(this, request);
            session = impersonateFromRequest(reqSession, request);

            Package pkg = pkgListService.getPackage(session, packageId);
            if (pkg == null) {
                LOG.info("Package with id `{}` not found", packageId);
                return Response.status(404).build();
            }

            return Response.ok(pkg).build();
        }
        catch (Exception ex) {
            LOG.error("Something went wrong while trying to retrieve package with id `{}`", packageId);
            return errorResponse();
        }
        finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }
    }

    /**
     * This package imports the contents and CND elements of a package in the zip file for
     * the <code>file</code> query parameter.
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
            Session reqSession = getSession(this, request);
            jcrSession = impersonateFromRequest(reqSession, request);

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

            return Response.ok(
                    "Package was imported succesfully, you can " +
                           "<a href='#' onclick='window.close();'>close this window</a>."
                        ).build();
        }
        catch (RepositoryException rEx) {
            LOG.error("Repo exception caused by: ", rEx);
        }
        catch (IOException ioEx) {
            LOG.error("Could not parse the incoming attachment, caused by: ", ioEx);
        }
        catch (Exception pEx) {
            LOG.error("Could not create package, caused by: ", pEx);
        }
        finally {
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
            }
        }

        return errorResponse();
    }



    /**
     * Clone a package definition
     *
     * @param payload clone instructions information
     * @return a response object
     */
    @POST
    @Path("/clone")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clonePackage(@Context HttpServletRequest request, ClonePackagePayload payload) {

        String dstName = payload.getToPackage();
        String srcName = payload.getFromPackage();

        LOG.info("Trying to clone " + srcName + " to " + dstName);

        Session jcrSession = null;
        try {
            Session reqSession = getSession(this, request);
            jcrSession = impersonateFromRequest(reqSession, request);

            // source doesn't exists?
            if (!pkgListService.packageExists(jcrSession, srcName)) {
                LOG.error("Package `{}` does not exist", srcName);
                return errorResponse();
            }

            // destination exists?
            if (pkgListService.packageExists(jcrSession, dstName)) {
                LOG.error("Package `{}` already exists", dstName);
                return errorResponse();
            }

            Package fromPackage = pkgListService.getPackage(jcrSession, srcName);
            Package newPackage = fromPackage.cloneTo(dstName);
            pkgListService.addPackage(jcrSession, newPackage);

            return (
                    Response.ok(
                        new HashMap<String, String>() {{
                            put("msg", "Cloning success");
                        }}
                    ).build()
                );
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

        return Response.ok().build();
    }

    /**
     * Build a package that was already defined
     *
     * @param packageId is the package to export.
     * @param request is the request object
     * @param response is the response object
     * @param postfix is the information to add to the filename
     */
    @GET
    @Path("/{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("VoidMethodAnnotatedWithGET")
    public void exportPackage(
                @Context HttpServletRequest request,
                @Context HttpServletResponse response,
                @PathParam("id") String packageId,
                @QueryParam("postfix") String postfix)
    {
        LOG.info("Requesting build of " + packageId);

        Session jcrSession = null;
        try {
            Session reqSession = getSession(this, request);
            jcrSession = impersonateFromRequest(reqSession, request);

            if (!pkgListService.packageExists(jcrSession, packageId)) {
                response.setStatus(SC_NOT_FOUND);
                return;
            }

            String pkgFilename = getPkgFilename(packageId, postfix);

            response.setStatus(SC_OK);
            response.setHeader("Content-Type", "application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + pkgFilename + "\"");
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

    }


    /**
     * Delete the package definition
     *
     * @param packageId     is the package id to delete
     * @return  a response object
     */
    @DELETE
    @Path("/{id}")
    public Response deletePackage(@Context HttpServletRequest request, @PathParam("id") String packageId) {
        LOG.info("Requesting deletion of " + packageId);

        Session jcrSession = null;
        try {
            Session reqSession = getSession(this, request);
            jcrSession = impersonateFromRequest(reqSession, request);

            if (!pkgListService.packageExists(jcrSession, packageId)) {
                LOG.error("Package with ID `{}` doesn't exist", packageId);
                return Response.serverError().build();
            }

            pkgListService.deletePackage(jcrSession, packageId);
            return Response.ok().build();
        }
        catch (RepositoryException | PackageException ex) {
            LOG.error("Could not complete package creation, caused by: ", ex);
            return Response.serverError().build();
        }

    }



    /**
     * Create a new package
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
            Session reqSession = getSession(this, request);
            jcrSession = impersonateFromRequest(reqSession, request);

            if (pkgListService.packageExists(jcrSession, packageId)) {
                LOG.error("Package with ID `{}` already exists, aborting.", packageId);
                return Response.serverError().build();
            }

            packageInfo.setId(packageId);
            pkgListService.addPackage(jcrSession, packageInfo);

            return Response.ok().build();
        }
        catch (RepositoryException | PackageException ex) {
            LOG.error("Could not complete package creation, caused by: ", ex);
            return Response.serverError().build();
        }
        finally {
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
            }
        }
    }


    /**
     * Edit a package definition
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
            Session reqSession = getSession(this, request);
            jcrSession = impersonateFromRequest(reqSession, request);

            // should exist.
            if (!pkgListService.packageExists(jcrSession, packageId)) {
                LOG.error("Package with ID `{}` does not exist, aborting.", packageId);
                return Response.serverError().build();
            }

            pkgListService.deletePackage(jcrSession, packageId);
            pkgListService.addPackage(jcrSession, packageInfo);
            return Response.ok().build();
        }
        catch (RepositoryException | PackageException ex) {
            LOG.error("Could not complete package creation, caused by: ", ex);
            return Response.serverError().build();
        }
        finally {
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
            }
        }
    }

    /**
     * @return the package filename when exporting.
     */
    protected String getPkgFilename(String packageId, String postfix) {
        return packageId + (StringUtils.isNotBlank(postfix) ? ("-" + postfix) : "") + ".zip";
    }

}
