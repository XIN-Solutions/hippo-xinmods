package nz.xinsolutions.core.security;

import nz.xinsolutions.core.jackrabbit.AutoCloseableSession;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.core.container.ContainerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static nz.xinsolutions.core.jackrabbit.JcrSessionHelper.closeableSession;
import static nz.xinsolutions.core.jackrabbit.JcrSessionHelper.loginAdministrative;

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
public class EnsureUserRoleBlockingFilter implements Filter {

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
     * Execute the filter and stop unauthorised acces.
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

        if (METHOD_OPTIONS.equals(httpRequest.getMethod())) {
            LOG.debug("Don't care about blocking OPTIONS requests");
            chain.doFilter(request, response);
            return;
        }

        // login to repo and check memberships
        try (AutoCloseableSession adminSession = closeableSession(loginAdministrative())) {

            String remoteUser = httpRequest.getRemoteUser();
            LOG.debug("Remote user: {}", remoteUser);

            List<String> userGroups = queryUserGroups(remoteUser, adminSession);
            LOG.debug("Has groups: {}", userGroups);

            if (!isAdministrator(remoteUser) && !foundValidGroupMembership(userGroups)) {
                httpResponse.sendError(SC_FORBIDDEN, "No allowed group memberships found.");
                return;
            }

        }
        catch (Exception rEx) {
            LOG.error("Could not check user memberships, caused by: ", rEx);
        }

        LOG.debug("Successful authentication and authorisation for user.");

        // continue chain
        chain.doFilter(request, response);

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
     * @return the credentials instance supposedly stored in the session object
     */
    protected SimpleCredentials getSessionCredentials(HttpSession session) {
        return (SimpleCredentials) session.getAttribute(ContainerConstants.SUBJECT_REPO_CREDS_ATTR_NAME);
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
