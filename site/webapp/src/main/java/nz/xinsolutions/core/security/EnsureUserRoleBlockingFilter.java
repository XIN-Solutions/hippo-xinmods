package nz.xinsolutions.core.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import nz.xinsolutions.core.jackrabbit.AutoCloseableSession;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Base64;
import org.hippoecm.hst.core.container.ContainerConstants;
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
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
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

        // don't care about blocking OPTIONS
        if (METHOD_OPTIONS.equals(httpRequest.getMethod())) {
            LOG.debug("Don't care about blocking OPTIONS requests");
            chain.doFilter(request, response);
            return;
        }

        // login to repo and check memberships
        try (AutoCloseableSession adminSession = closeableSession(loginAdministrative())) {

            if (this.hasBearerToken(httpRequest)) {

                DecodedJWT jwt = this.decodeBearerToken(httpRequest, httpResponse);

                if (jwt == null) {
                    httpResponse.sendError(SC_FORBIDDEN, "Invalid JWT");
                    return;
                }

                // extract information from token.
                String jwtUser = jwt.getClaim("username").asString();
                List<String> userGroups = jwt.getClaim("usergroups").asList(String.class);

                if (!isAdministrator(jwtUser) && !foundValidGroupMembership(userGroups)) {
                    httpResponse.sendError(SC_FORBIDDEN, "No allowed group memberships found.");
                    return;
                }

                // shim in a request wrapper that overrides the authorization header with administrative credentials
                HttpServletRequestWrapper reqWrap = new HttpServletRequestWrapper(httpRequest) {
                    @Override
                    public String getHeader(String name) {
                        if (name.equals("Authorization")) {
                            String adminHash = getDownstreamJwtRepoCredentials();
                            return "Basic " + adminHash;
                        }
                        return super.getHeader(name);
                    }
                };

                chain.doFilter(reqWrap, response);
                return;
            }

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


    }

    /**
     * @return the authorization header using the JWT credentials if set.
     */
    protected String getDownstreamJwtRepoCredentials() {
        String repoUser = System.getProperty("jwt.repo.user");
        String repoPass = System.getProperty("jwt.repo.password");

        // the repository user basic auth information to satisfy the test repository credentials filter.
        if (StringUtils.isNotEmpty(repoUser) && StringUtils.isNotEmpty(repoPass)) {
            return Base64.encode(String.format("%s:%s", repoUser, repoPass));
        }

        // if no jwt user was specified, let's assume we're using the admin user password.
        return Base64.encode("admin:" + System.getProperty("admin.password", "admin"));
    }

    /**
     * @return true if the Authorization is a bearer token.
     */
    protected boolean hasBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return !StringUtils.isEmpty(authHeader) && authHeader.startsWith("Bearer ");
    }

    /**
     * @return true if the Authorization is a bearer token.
     */
    protected String getBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return authHeader.substring("Bearer ".length());
    }

    /**
     * Make sure
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    protected DecodedJWT decodeBearerToken(HttpServletRequest request, HttpServletResponse response) {

        try {
            String token = getBearerToken(request);

            DecodedJWT jwt = JWT.decode(token);
            JwkProvider provider = new UrlJwkProvider(new URL("http://localhost:8080/cms/ws/jwks.json"));
            Jwk jwk = provider.get(jwt.getKeyId());

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(jwt);

            return jwt;
        }
        catch (Exception ex) {
            LOG.error("Could not validate JWT, caused by: {}", ex.getMessage());
            return null;
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
