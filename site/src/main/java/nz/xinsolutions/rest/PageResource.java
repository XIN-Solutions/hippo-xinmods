package nz.xinsolutions.rest;

import nz.xinsolutions.beans.Page;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.onehippo.cms7.essentials.components.paging.Pageable;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.onehippo.cms7.essentials.components.rest.ctx.DefaultRestContext;
import org.onehippo.cms7.essentials.components.rest.ctx.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * @version "$Id$"
 */

@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
@Path("/page/")
public class PageResource extends BaseRestResource {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PageResource.class);
    
    @GET
    @Path("/")
    public Pageable<Page> index(@Context HttpServletRequest request) {
        return findBeans(newContext(request), Page.class);
    }
    
    private DefaultRestContext newContext(@Context HttpServletRequest request) {
        return new DefaultRestContext(this, request);
    }
    
    @GET
    @Path("/page/{page}")
    public Pageable<Page> page(@Context HttpServletRequest request, @PathParam("page") int page) {
        return findBeans(new DefaultRestContext(this, request, page, DefaultRestContext.PAGE_SIZE), Page.class);
    }

    @GET
    @Path("/page/{page}/{pageSize}")
    public Pageable<Page> pageForSize(@Context HttpServletRequest request, @PathParam("page") int page, @PathParam("pageSize") int pageSize) {
        return findBeans(new DefaultRestContext(this, request, page, pageSize), Page.class);
    }

    @GET
    @Path("/document/by-path/")
    public Object pageByPath(@Context HttpServletRequest request, @RequestParam("path") String path) {
        RestContext ctx = newContext(request);
        try {
            return ctx.getRequestContext().getObjectBeanManager().getObject(path);
        }
        catch (Exception ex) {
            LOG.error("Something happened while retrieving object at `" + path + "`, caused by: ", ex);
            return null;
        }
    }
    
    
    @GET
    @Path("/by-title/{title}")
    public Page pageWithTitle(@Context HttpServletRequest request, @PathParam("title") String title) {
        
        RestContext context = newContext(request);
        try {
            HstQuery query = createQuery(context, Page.class, Subtypes.INCLUDE);
            Filter filter = query.createFilter();
            filter.addJCRExpression("fn:name() = '" + title + "'");
            query.setFilter(filter);
            
            HstQueryResult qResult = query.execute();
            if (qResult.getSize() == 0) {
                return null;
            } else {
                return (Page) qResult.getHippoBeans().nextHippoBean();
            }
         }
        catch (Exception ex) {
            LOG.error("Something went horribleh wr0ng, caused by: ", ex);
        }
        
        return null;
    }
    
    private String getQueryString(String title) {
        return "//element(*, xinmods:page)[hippostd:state='published' and fn:name()='" + title + "']";
    }
    
    private QueryManager getJcrQueryManager(RestContext context) throws RepositoryException {
        return context.getRequestContext().getSession().getWorkspace().getQueryManager();
    }
    
}
