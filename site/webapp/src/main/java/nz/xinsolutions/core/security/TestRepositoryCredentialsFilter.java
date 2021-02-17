package nz.xinsolutions.core.security;

import nz.xinsolutions.core.jackrabbit.JcrSessionHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *      To check the authorization header populated through JWT parsing
 *      or by direct basic authentication injection.
 *
 */
public class TestRepositoryCredentialsFilter implements Filter {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(TestRepositoryCredentialsFilter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Initialising credentials tester.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String authHeader = httpRequest.getHeader("Authorization");
        if (StringUtils.isEmpty(authHeader)) {
            LOG.debug("No credentials found, returning 401.");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        // make sure the credentials are correct.
        Session session = JcrSessionHelper.getAuthenticatedSession(httpRequest);

        if (session == null) {
            LOG.debug("Could not create session with these credentials, 403.");
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        session.logout();

        filterChain.doFilter(servletRequest, servletResponse);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        LOG.info("Destroying credentials tester.");

    }
}
