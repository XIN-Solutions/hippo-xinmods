package nz.xinsolutions.core.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import nz.xinsolutions.core.Rest;
import nz.xinsolutions.core.jackrabbit.AutoCloseableSession;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Base64;
import org.hippoecm.hst.core.container.ContainerConstants;
import org.jetbrains.annotations.NotNull;
import org.onehippo.cms7.essentials.components.rest.ctx.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static nz.xinsolutions.core.jackrabbit.JcrSessionHelper.*;

/**
 * Blocks further calls by setting 403 if incoming requests aren't authenticated, and
 * authenticated users aren't in one of the groups specified in the init-param <code>groups</code>.
 * The filter will return 401 if no authentication found, or 403 if user does not have
 * the correct group memberships.
 *
 * Expects to be used in conjunction with {@link AuthenticationSubjectPersistFilter}.
 *
 * @author: Marnix Kok <marnix@xinsolutions.co.nz>
 */
public class EnsureUserRoleBlockingFilter implements Filter, Rest {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(EnsureUserRoleBlockingFilter.class);

    /**
     * Init-param name to specify groups with.
     */
    public static final String INIT_PARAM_GROUPS = "groups";
    public static final String QUERY_GROUP_MEMBERSHIPS = "//element(*, hipposys:group)[jcr:contains(@hipposys:members, '%s')]";
    public static final String METHOD_OPTIONS = "OPTIONS";

    /**
     * Valid groups
     */
    private String[] validGroups;

    /**
     * Initialise the filter.
     *
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String groupParamValue = filterConfig.getInitParameter(INIT_PARAM_GROUPS);
        if (StringUtils.isBlank(groupParamValue)) {
            throw new IllegalStateException("Expected the EnsureUserRoleBlockingFilter to have a `groups` init-param");
        }

        this.validGroups = groupParamValue.split(",");
    }

    /**
     * Execute the filter and stop unauthorised access.
     *
     * @param request   is the incoming request instance
     * @param response  is the response instance
     * @param chain     is the rest of the chain
     *
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // don't care about blocking OPTIONS
        if (METHOD_OPTIONS.equals(httpRequest.getMethod())) {
            LOG.debug("Don't care about blocking OPTIONS requests");
            chain.doFilter(request, response);
            return;
        }

        // create a session based on the auth credentials in the request
        Session requestSession = getAuthenticatedSession(httpRequest);
        if (requestSession == null) {
            LOG.error("No authentication information available, can never adhere to check. 403");
            httpResponse.sendError(SC_FORBIDDEN, "No authentication information found.");
            return;
        }

        // impersonate admin
        try (AutoCloseableSession adminSession = closeableSession(loginAdministrative(requestSession))) {

            // assuming it's using BASIC authentication
            SimpleCredentials basicAuthCreds = BasicAuthUtility.parseAuthorizationHeader(httpRequest);

            if (basicAuthCreds == null) {
                httpResponse.sendError(SC_FORBIDDEN, "Username credentials required.");
                return;
            }

            // check that there is a user .
            String remoteUser = basicAuthCreds.getUserID();
            if (StringUtils.isEmpty(remoteUser)) {
                httpResponse.sendError(SC_FORBIDDEN, "Username credentials required.");
                return;
            }
            else {

                if (StringUtils.isEmpty(remoteUser)) {
                    httpResponse.sendError(SC_FORBIDDEN, "Username credentials required.");
                    return;
                }

                List<String> userGroups = queryUserGroups(remoteUser, adminSession);
                LOG.debug("Remote user: {}", remoteUser);
                LOG.debug("Has groups: {}", userGroups);

                if (!isAdministrator(remoteUser) && !foundValidGroupMembership(userGroups)) {
                    httpResponse.sendError(SC_FORBIDDEN, "No allowed group memberships found.");
                    return;
                }
            }

            LOG.debug("Successful authentication and authorisation for user.");

            // continue chain
            chain.doFilter(request, response);

        }
        catch (Exception rEx) {
            LOG.error("Could not check user memberships, caused by: ", rEx);
        }
        finally {
            if (requestSession.isLive()) {
                requestSession.logout();
            }
        }
    }

    /**
     * @return true if it's an administrator
     */
    protected boolean isAdministrator(String remoteUser) {
        return "admin".equals(remoteUser);
    }

    /**
     * Retrieve a list of groups the current user is directly
     *
     * @param userName      is the username to find the groups for
     * @param adminSession  the session to use for the query (should be administrative session).
     * @return a list of declared group memberships
     * @throws RepositoryException
     */
    protected List<String> queryUserGroups(String userName, Session adminSession) throws RepositoryException {

        QueryManager qMgr = adminSession.getWorkspace().getQueryManager();

        String escaped = userName.replace("'", "''");

        // create query
        Query jcrQuery =
                qMgr.createQuery(
                    String.format(QUERY_GROUP_MEMBERSHIPS, escaped),
                    Query.XPATH
                );

        QueryResult results = jcrQuery.execute();
        NodeIterator nIterator = results.getNodes();

        List<String> userGroups = new ArrayList<>();

        while (nIterator.hasNext()) {
            Node groupNode = nIterator.nextNode();
            String groupName = groupNode.getName();
            userGroups.add(groupName);
        }

        return userGroups;
    }

    /**
     * @return true if <code>groups</code> contains a group mentioned in <code>this.validGroups</code>
     */
    protected boolean foundValidGroupMembership(Collection<String> groups) {
        boolean foundValidGroup = false;
        for (String validGroup : this.validGroups) {
            foundValidGroup |= groups.contains(validGroup);
        }
        return foundValidGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        LOG.info("Shutting down EnsureUserRoleBlockingFilter");
    }
}
