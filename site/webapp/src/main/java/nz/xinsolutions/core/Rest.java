package nz.xinsolutions.core;

import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.onehippo.cms7.essentials.components.rest.ctx.DefaultRestContext;
import org.onehippo.cms7.essentials.components.rest.ctx.RestContext;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * Contains common functions you'd want to use when building rest controllers but aren't
 * worth extending the class from or using as helper functions.
 *
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 */
public interface Rest {

    /**
     * Creates and returns a typical error response.
     * @return an error response
     */
    default Response errorResponse() {
        return Response.serverError().build();
    }

    /**
     * Creates and returns a success response
     * @return an empty "success" response
     */
    default Response emptyResponse() {
        return Response.ok("Success").build();
    }

    /**
     * Get the session for the current request.
     *
     * @param resource is the rest resource to create the session from
     * @param request is the request object
     * @return a JCR session that was created for this rest resource.
     * @throws RepositoryException when the session cannot be retrieved
     */
    default Session getSession(BaseRestResource resource, HttpServletRequest request) throws RepositoryException {
        RestContext ctx = newRestContext(resource, request);
        return getSession(ctx);
    }

    /**
     * Creates a new rest context based on the current resource and request object.
     *
     * @param resource is the rest resource to create it for
     * @param request is the request object to create it with
     * @return a rest context instance
     */
    default DefaultRestContext newRestContext(BaseRestResource resource, HttpServletRequest request) {
        return new DefaultRestContext(resource, request);
    }

    /**
     * Return a session from the rest context.
     * @return the session from the current rest context
     * @throws RepositoryException when the session can't be retrieved.
     */
    default Session getSession(RestContext ctx) throws RepositoryException {
        return ctx.getRequestContext().getSession();
    }

}
