package nz.xinsolutions.rest;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.onehippo.cms7.essentials.components.rest.ctx.DefaultRestContext;
import org.onehippo.cms7.essentials.components.rest.ctx.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
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
