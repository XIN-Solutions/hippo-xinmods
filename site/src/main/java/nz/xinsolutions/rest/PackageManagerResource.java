package nz.xinsolutions.rest;

import nz.xinsolutions.packages.Package;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

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
    
    /**
     * Retrieve the HTML for the angular application
     *
     * @return  the input stream
     * @throws FileNotFoundException
     */
    @GET
    @Path("/index.html")
    @Produces(MediaType.TEXT_HTML)
    public InputStream packageIndex() throws FileNotFoundException {
        File file = new File("/home/marnix/tmp/hippo/xinmods/packageManager.html");
        return new FileInputStream(file);
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Package> getAllPackages() {
        LOG.info("Return a list of packages");
        return Collections.EMPTY_LIST;
    }
    
    
    /**
     * Build a package that was already defined
     * @param packageId
     * @return
     */
    @GET
    @Path("/{id}/build")
    @Produces(MediaType.APPLICATION_JSON)
    public Void buildPackage(@PathParam("id") String packageId) {
        LOG.info("Requesting build of " + packageId);
        return null;
    }
    
    
    @DELETE
    @Path("/{id}")
    public Void deletePackage(@PathParam("id") String packageId) {
        LOG.info("Requesting deletion of " + packageId);
        return null;
    }
    
    @PUT
    @Path("/{id}")
    public Void createPackage(@PathParam("id") String packageId) {
        LOG.info("Requesting creation of new package: " + packageId);
        return null;
    }

}
