package nz.xinsolutions.core.security;

import org.hippoecm.hst.core.container.ContainerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Credentials;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Authentication subject persist filter grabs the authentication header and pushes it onto
 * the HTTP session for this user to accommodate the hst:subjectbasedsession. Out of the box
 * it works fine with the LoginServlet, but for some reason the Basic Authentication valve doesn't care.
 *
 * @author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 */
public class AuthenticationSubjectPersistFilter implements Filter {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationSubjectPersistFilter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Initialising the AuthenticationSubjectPersistFilter");
    }

    /**
     * Be a filter in the chain that is able to store the pushed credentials.
     *
     * @param request   the servlet request
     * @param response  the servlet response
     * @param chain     the rest of the chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        LOG.debug("Starting to persist authentication subject");


        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // has an authentication header? no need to do anything.
        if (!BasicAuthUtility.hasAuthorizationHeader(httpRequest)) {
            LOG.debug("No authentication header");
            chain.doFilter(request, response);
            return;
        }

        // if no session, create it. Storing of the credentials is only temporary until the hstfilter's
        // security valve has been invoked. Also doesn't need to make this sticky session as the
        // credentials are stored for each request.
        HttpSession httpSession = httpRequest.getSession(true);

        // parse from header
        Credentials repoCreds = BasicAuthUtility.parseAuthorizationHeader(httpRequest);

        // set in session
        httpSession.setAttribute(ContainerConstants.SUBJECT_REPO_CREDS_ATTR_NAME, repoCreds);

        chain.doFilter(request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        LOG.info("Shutting down AuthenticationSubjectPersistFilter");
    }
}
