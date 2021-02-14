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

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
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
            List<String> userGroups = queryUserGroups(username, adminSession);
            userInfo.setGroups(userGroups);
        }
        catch (Exception rEx) {
            LOG.error("Could not check user memberships, caused by: ", rEx);
        }

        return userInfo;
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

}
