package nz.xinsolutions.core.jackrabbit;

import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.onehippo.repository.security.JvmCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 29/01/18
 *
 *      Some useful JCR functions
 */
public class JcrSessionHelper {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Logger.class);


    /**
     * Create a new JCR session based on the user and password provided
     *
     * @param user      user to create a session for
     * @param password  the password to create a session with
     * @return the new session
     * @throws RepositoryException
     */
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

}
