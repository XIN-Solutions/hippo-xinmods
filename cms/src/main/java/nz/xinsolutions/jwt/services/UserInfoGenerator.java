package nz.xinsolutions.jwt.services;

import nz.xinsolutions.core.jackrabbit.AutoCloseableSession;
import nz.xinsolutions.jwt.models.JwtUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

import static nz.xinsolutions.core.jackrabbit.JcrSessionHelper.closeableSession;
import static nz.xinsolutions.core.jackrabbit.JcrSessionHelper.loginAdministrative;

/**
 * Author: Marnix Kok <marnix@elevate.net.nz>
 * <p>
 * Purpose:
 */
public class UserInfoGenerator {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(UserInfoGenerator.class);

    /**
     * Query to run to get all group memberships
     */
    public static final String QUERY_GROUP_MEMBERSHIPS = "//element(*, hipposys:group)[jcr:contains(@hipposys:members, '%s')]";
    public static final String HIPPOSYS_LASTNAME = "hipposys:lastname";
    public static final String HIPPOSYS_FIRSTNAME = "hipposys:firstname";
    public static final String HIPPOSYS_EMAIL = "hipposys:email";
    public static final String HIPPOSYS_ACTIVE = "hipposys:active";
    public static final String USER_BASE_PATH = "/hippo:configuration/hippo:users/";

    /**
     * Populate a JwtUser information object based on the information related to the
     * currently logged in user.
     *
     * @param request   the request object
     * @return
     */
    public JwtUserInfo createUserInfo(HttpServletRequest request) {
        JwtUserInfo userInfo = new JwtUserInfo();

        HttpSession httpSession = request.getSession(false);
        String username = httpSession.getAttribute("hippo:username").toString();

        userInfo.setUsername(username);

        // login to repo and check memberships
        try (AutoCloseableSession adminSession = closeableSession(loginAdministrative())) {
            List<String> userGroups = queryUserGroups(adminSession, username);
            userInfo.setGroups(userGroups);

            Node node = getNodeForUsername(adminSession, username);

            // populate jwt user info object with personal information
            if (node.hasProperty(HIPPOSYS_EMAIL)) {
                userInfo.setEmail(node.getProperty(HIPPOSYS_EMAIL).getString());
            }

            if (node.hasProperty(HIPPOSYS_FIRSTNAME)) {
                userInfo.setFirstName(node.getProperty(HIPPOSYS_FIRSTNAME).getString());
            }

            if (node.hasProperty(HIPPOSYS_LASTNAME)) {
                userInfo.setLastName(node.getProperty(HIPPOSYS_LASTNAME).getString());
            }

            if (node.hasProperty(HIPPOSYS_ACTIVE)) {
                userInfo.setActive(node.getProperty(HIPPOSYS_ACTIVE).getBoolean());
            }

        }
        catch (Exception rEx) {
            LOG.error("Could not check user memberships, caused by: ", rEx);
        }

        return userInfo;
    }

    /**
     * Find the user node for <code>username</code>
     *
     * @param session the session to query with
     * @param username  the username to look for
     * @return the node or null if it doesn't exist.
     * @throws RepositoryException
     */
    protected Node getNodeForUsername(Session session, String username) throws RepositoryException {
        return session.getNode(USER_BASE_PATH + username);
    }

    /**
     * Retrieve a list of groups the current user is directly
     *
     * @param adminSession  the session to use for the query (should be administrative session).
     * @param userName      is the username to find the groups for
     * @return a list of declared group memberships
     * @throws RepositoryException
     */
    protected List<String> queryUserGroups(Session adminSession, String userName) throws RepositoryException {

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

}
