package nz.xinsolutions.rest;

import nz.xinsolutions.queries.QueryParser;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.onehippo.cms7.essentials.components.rest.ctx.DefaultRestContext;
import org.onehippo.cms7.essentials.components.rest.ctx.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @version "$Id$"
 */

@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Path("/content/")
public class ContentResource extends BaseRestResource {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ContentResource.class);
    
    
    @GET
    @Path("/query/")
    public Response performQuery(@Context UriInfo uriInfo, @Context HttpServletRequest request, @QueryParam(value = "query") String query) {
    
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    
        RestContext ctx = newContext(request);
        
        try {
            if (StringUtils.isEmpty(query)) {
                throw new IllegalArgumentException("`query` parameter is empty");
            }

            HstQueryManager qMgr = ctx.getRequestContext().getQueryManager();
            HstQuery hstQuery = new QueryParser().createFromString(qMgr, query, queryParams);
            
            HstQueryResult queryResult = hstQuery.execute();
            int totalItems = queryResult.getTotalSize();

            // uuid lists
            List<String> uuids = new ArrayList<>();
            HippoBeanIterator it = queryResult.getHippoBeans();
            while (it.hasNext()) {
                HippoBean bean = it.nextHippoBean();
                if (bean instanceof HippoDocument) {
                    uuids.add( ((HippoDocument) bean).getCanonicalHandleUUID() );
                } else {
                    uuids.add(bean.getCanonicalUUID());
                }
            }
            
            // respond
            return
                Response.status(200)
                    .entity(
                        new LinkedHashMap<String, Object>() {{
                            put("success", true);
                            put("msg", "Result found");
                            put("uuids", uuids);
                            put("totalSize", totalItems);
                        }}
                    )
                    .build()
                ;
        }
        catch (Exception ex) {
        
            LOG.error("Could not parse a query properly, caused by: ", ex);

            return
                Response.status(501)
                    .entity(
                        new LinkedHashMap<String, Object>() {{
                            put("success", false);
                            put("msg", "Error: " + ex.getMessage());
                        }}
                    ).build()
            ;
        }
        
    }
    
    /**
     * Convert a UUID
     *
     * @param request
     * @param path
     * @return
     */
    @GET
    @Path("/path-to-uuid/")
    public Response uuidToPath(@Context HttpServletRequest request, @QueryParam(value = "path") String path) {
    
        RestContext ctx = newContext(request);
        
        try {
            if (StringUtils.isEmpty(path)) {
                LOG.info("The path is empty");
                return null;
            }

            HippoBean bean = (HippoBean) ctx.getRequestContext().getObjectBeanManager().getObject(path);

            if (bean == null) {
                return notFoundResponse();
            }
            
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("success", true);
            result.put("message", "found");
            result.put("type", bean.getNode().getPrimaryNodeType().getName());
            result.put("path", path);
            result.put("uuid", bean.getCanonicalUUID());
            result.put("children",
                    bean.getChildBeans(HippoBean.class)
                        .stream()
                        .map(this::beanToJson)
                        .collect(Collectors.toList())
                );
            
            return (
                Response
                    .status(200)
                    .entity(result)
                    .build()
            );
        }
        catch (Exception ex) {
            LOG.error("Something happened while retrieving object at `" + path + "`, caused by: ", ex);
            return null;
        }
    }
    
    /**
     * @return a structure describing a bean
     */
    protected Map<String, String> beanToJson(HippoBean bean) {
        try {
            return new LinkedHashMap<String, String>() {{
                if (bean instanceof HippoDocument) {
                    HippoDocument doc = (HippoDocument) bean;
                    put("uuid", doc.getCanonicalHandleUUID());
                    put("path", doc.getCanonicalHandlePath());
                } else {
                    put("path", bean.getCanonicalPath());
                    put("uuid", bean.getCanonicalUUID());
                }
                put("type", bean.getNode().getPrimaryNodeType().getName());
            }};
        }
        catch (RepositoryException rEx) {
            LOG.error("Could not get information from " + bean.getPath());
        }
        return null;
    }
    
    
    
    /**
     * @return the not found response
     */
    protected Response notFoundResponse() {
        return (
            Response
                .status(200)
                .entity(new LinkedHashMap<String, Object>() {{
                    put("success", false);
                    put("message", "not found");
                }})
                .build()
        );
    }
    
    
    /**
     * @return the context we need.
     */
    protected DefaultRestContext newContext(@Context HttpServletRequest request) {
        return new DefaultRestContext(this, request);
    }
}
