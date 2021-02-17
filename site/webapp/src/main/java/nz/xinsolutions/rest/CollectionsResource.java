package nz.xinsolutions.rest;

import nz.xinsolutions.core.jackrabbit.AutoCloseableSession;
import nz.xinsolutions.rest.model.CollectionsWriteRequest;
import nz.xinsolutions.core.Rest;
import nz.xinsolutions.services.CollectionsService;
import org.hippoecm.hst.restapi.ResourceContextFactory;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static nz.xinsolutions.core.jackrabbit.JcrSessionHelper.closeableSession;
import static nz.xinsolutions.core.jackrabbit.JcrSessionHelper.getAuthenticatedSession;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *      A set of endpoints that allow collection information to be written to
 *      and read from the repository.
 *
 */
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Path("/collections/")
public class CollectionsResource extends BaseRestResource implements Rest {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CollectionsResource.class);

    /**
     * Bound instance by spring.
     */
    private ResourceContextFactory resourceContextFactory;

    /**
     * Read a list of collections that are currently part of the repository.
     *
     * @param request   the http request
     * @return a list of collections
     */
    @GET
    @Path("/list/")
    public Response getListOfCollections(@Context HttpServletRequest request) {

        LOG.info("Retrieving a list of collections");

        try (AutoCloseableSession adminSession = closeableSession(getAuthenticatedSession(request))) {

            CollectionsService service = newCollectionService(adminSession, resourceContextFactory);
            List<String> collections = service.listCollections();

            return (
                Response
                    .status(200)
                    .entity(new LinkedHashMap<String, Object>() {{
                        put("success", true);
                        put("collections", collections);
                    }}
                ).build()
            );
        }
        catch (Exception ex) {
            LOG.error("Something went wrong, caused by: ", ex);
        }

        return null;
    }

    /**
     * Retrieve an item from a collection. If it does not exist, it will return 404. If it does
     * exist, it will return a document response much like the normal ootb documents endpoints.
     *
     * @param request       the http request
     * @param collection    the collection to get an item from
     * @param path          the path in the collection to get an item for
     * @return an item from a collection, or 404 if not found.
     */
    @GET
    @Path("/{collection}/item")
    public Response getCollectionItem(
        @Context HttpServletRequest request,
        @PathParam("collection") String collection,
        @QueryParam("path") String path
    ) {
        try (AutoCloseableSession adminSession = closeableSession(getAuthenticatedSession(request))) {

            CollectionsService service = newCollectionService(adminSession, resourceContextFactory);
            Map<String, Object> nodeContent = service.getItemAtPath(collection, path);

            return (
                Response
                    .status(200)
                    .entity(
                        new LinkedHashMap<String, Object>() {{
                            put("success", true);
                            put("item", nodeContent);
                        }}
                    ).build()
            );

        }
        catch (Exception ex) {
            LOG.error("Something went wrong, caused by: ", ex);
        }
        return null;
    }

    /**
     * Write new content into a collection. If the folders don't exist, they are created. If the item already exists, depending
     * on the "Savemode" it will be deleted and overwritten, or merged with existing values.
     *
     * @param request       the request object
     * @param collection    the collection to write to
     * @param writeRequest
     * @return
     */
    @POST
    @Path("/{collection}/item")
    public Response writeItemToCollection(
        @Context HttpServletRequest request,
        @PathParam("collection") String collection, @QueryParam("path") String path,
        CollectionsWriteRequest writeRequest
    ) {

        // TODO: make sure path doesn't contain ":" character.

        try (AutoCloseableSession adminSession = closeableSession(getAuthenticatedSession(request))) {

            // create service and try to write
            CollectionsService service = newCollectionService(adminSession, resourceContextFactory);
            boolean written = service.writeItemValues(collection, path, writeRequest);

            return (
                Response
                    .status(200)
                    .entity(
                        new LinkedHashMap<String, Object>() {{
                            put("success", written);
                        }}
                    ).build()
            );
        }
        catch (Exception ex) {

            LOG.error("Something went wrong, caused by: ", ex);

            // setup a 400 bad request
            return (
                Response
                    .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .entity(
                        new LinkedHashMap<String, Object>() {{
                            put("success", false);
                            put("message", ex.getMessage());
                        }}
                    ).build()
            );

        }
    }

    /**
     * Delete an item from the collection. If this is a leaf item, the element is deleted automatically. If there are
     * levels in the tree, then the forceDelete parameter must be set to true, otherwise an error is returned.
     *
     * @param request       the http servlet request object
     * @param collection    the collection the item should be removed from
     * @param path          the path at which the item lives that must be deleted
     * @param forceDelete   if set to true, even if the item is not a leaf item, it will be deleted.
     * @return 200 if all good, or 4xx code if something went wrong.
     */
    @DELETE
    @Path("/{collection}/item")
    public Response deleteItemFromCollection(
        @Context HttpServletRequest request,
        @PathParam("collection") String collection,
        @QueryParam("path") String path,
        @QueryParam("forceDelete") boolean forceDelete)
    {

        try (AutoCloseableSession adminSession = closeableSession(getAuthenticatedSession(request))) {

            CollectionsService service = newCollectionService(adminSession, resourceContextFactory);
            boolean removed = service.removeItemsAtPath(collection, path, forceDelete);

            return (
                Response
                    .status(200)
                    .entity(
                        new LinkedHashMap<String, Object>() {{
                            put("success", removed);
                            put("message", !removed? "Item not found or not a leaf item without forceDelete." : "Deleted.");
                        }}
                    ).build()
            );
        }
        catch (Exception ex) {
            LOG.error("Something went wrong, caused by: ", ex);
        }
        return null;
    }


    /**
     * @return a new collections service instance
     */
    protected CollectionsService newCollectionService(Session session, ResourceContextFactory resourceContextFactory) {
        return new CollectionsService(session, resourceContextFactory);
    }


    public void setResourceContextFactory(ResourceContextFactory resourceContextFactory) {
        this.resourceContextFactory = resourceContextFactory;
    }
}
