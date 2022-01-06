package nz.xinsolutions.rest;

import nz.xinsolutions.beans.ContextVariablesBean;
import nz.xinsolutions.core.Rest;
import nz.xinsolutions.core.jackrabbit.AutoCloseableSession;
import nz.xinsolutions.rest.model.FacetNode;
import nz.xinsolutions.services.FacetedNavService;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.restapi.ResourceContextFactory;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.onehippo.cms7.essentials.components.rest.ctx.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;

import static nz.xinsolutions.core.jackrabbit.JcrSessionHelper.closeableSession;
import static nz.xinsolutions.core.jackrabbit.JcrSessionHelper.getAuthenticatedSession;

/**
 * Author: Marnix Kok
 * <p>
 * Purpose:
 */
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Path("/facets/")
public class FacetedNavResource extends BaseRestResource implements Rest {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FacetedNavResource.class);

    /**
     * Bound instance by spring.
     */
    private ResourceContextFactory resourceContextFactory;

    @GET
    @Path("/get")
    public Response getFacetedNav(
        @Context HttpServletRequest request,
        @QueryParam("facetPath") String facetPath,
        @QueryParam("childPath") String childPath,
        @QueryParam("offset") Integer offset,
        @QueryParam("limit") Integer limit,
        @QueryParam("sorted") boolean sorted,
        @QueryParam("fetch") List<String> fetch

    ) {
        if (facetPath == null) {
            return Response.status(400).entity("Requires 'facetPath'.").build();
        }

        try (AutoCloseableSession adminSession = closeableSession(getAuthenticatedSession(request))) {

            FacetedNavService fnService = newFacetedNavService(adminSession, resourceContextFactory);
            RestContext restCtx = newRestContext(this, request);

            // make sure they have values.
            offset = offset == null ?  0 : offset;
            limit = limit == null? 50 : limit;

            ObjectBeanManager objBeanMgr = restCtx.getRequestContext().getObjectBeanManager(adminSession);
            FacetNode facet = fnService.getFacetNodeAtPath(objBeanMgr, facetPath, childPath, offset, limit, sorted, fetch);
            boolean retrieved = facet != null;

            return (
                Response
                    .status(200)
                    .entity(
                        new LinkedHashMap<String, Object>() {{
                            put("success", retrieved);
                            put("message", !retrieved? "Facet element not found." : "Success.");
                            put("facet", facet);
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
     * @return a new instance of the context variables bean
     */
    public ContextVariablesBean newContextVariablesInstance(HttpServletRequest request) {
        return new ContextVariablesBean(request);
    }

    protected FacetedNavService newFacetedNavService(Session session, ResourceContextFactory resourceFactory) {
        return new FacetedNavService(session, resourceFactory);
    }


    public void setResourceContextFactory(ResourceContextFactory resourceContextFactory) {
        this.resourceContextFactory = resourceContextFactory;
    }
}
