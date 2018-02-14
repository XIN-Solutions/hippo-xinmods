package nz.xinsolutions.core.jackrabbit;

import nz.xinsolutions.core.security.BasicAuthUtility;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
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


    public static Session getAuthenticatedSession(String user, String password) throws RepositoryException {
        HippoRepository repository = HippoRepositoryFactory.getHippoRepository("vm://");
        return repository.login(user, password.toCharArray());
    }

    /**
     * @return an administrator session basi
     * @throws RepositoryException
     */
    public static Session loginAdministrative() throws RepositoryException {
        return getAuthenticatedSession("admin", System.getProperty("admin.password", "admin"));
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
            return getAuthenticatedSession(
                    creds.getUserID(),
                    new String(creds.getPassword())
            );
        }
        catch (Exception ex) {
            LOG.error("Could not get authentication header");
            return null;
        }
    }
}
