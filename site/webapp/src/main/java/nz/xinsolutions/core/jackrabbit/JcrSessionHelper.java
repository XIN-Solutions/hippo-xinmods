package nz.xinsolutions.core.jackrabbit;

import nz.xinsolutions.core.Rest;
import nz.xinsolutions.core.security.BasicAuthUtility;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.onehippo.repository.security.JvmCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 29/01/18
 *
 *      Some useful JCR functions
 */
public class JcrSessionHelper implements Rest {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Logger.class);


    public static Session getAuthenticatedSession(String user, String password) throws RepositoryException {
        HippoRepository repository = HippoRepositoryFactory.getHippoRepository("vm://");
        return repository.login(user, password.toCharArray());
    }

    /**
     * Create a new administrative session based on the live user credentials ability
     * to impersonate the 'admin' user. Make sure to close the session.
     *
     * @return an administrator session
     * @throws RepositoryException
     */
    public static Session loginAdministrative() throws RepositoryException {

        // get the liveuser service user credentials
        JvmCredentials liveUserPass = JvmCredentials.getCredentials("liveuser");

        // create a new session
        Session baseSession = getAuthenticatedSession(
            liveUserPass.getUserID(),
            new String(liveUserPass.getPassword())
        );

        Session adminSession = baseSession.impersonate(new SimpleCredentials("admin", "".toCharArray()));
        baseSession.logout();
        return adminSession;
    }


    /**
     * @return an administrator session
     * @throws RepositoryException
     * @param jcrSession base JCR session to start impersonation off of.
     */
    public static Session loginAdministrative(Session jcrSession) throws RepositoryException {
        Session adminSession = jcrSession.impersonate(new SimpleCredentials("admin", "".toCharArray()));
        return adminSession;
    }

    /**
     * Make a normal session auto closeable.
     *
     * @param session
     * @return
     */
    public static AutoCloseableSession closeableSession(Session session) {
        if (session == null) {
            return null;
        }
        return new AutoCloseableSession(session);
    }

    /**
     * Try to create a session using the basic auth information.
     *
     * @param request is the incoming request
     * @return the session
     */
    public static Session getAuthenticatedSession(HttpServletRequest request) {

        try {
            SimpleCredentials creds = BasicAuthUtility.parseAuthorizationHeader(request);

            if (creds == null) {
                LOG.info("No Authorization header found on the request.");
                return null;
            }

            return getAuthenticatedSession(
                    creds.getUserID(),
                    new String(creds.getPassword())
            );
        }
        catch (Exception ex) {
            LOG.error("Could not get authentication header: {}", ex.getMessage());
            return null;
        }
    }
}
