package nz.xinsolutions.core;

import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 29/01/18
 *
 *      Some useful JCR functions
 */
public class JcrHelper {
    
    
    /**
     * TODO: Admin credentials should be read from somewhere safe
     *
     * @return an administrator session
     * @throws RepositoryException
     */
    public static Session loginAdministrative() throws RepositoryException {
        HippoRepository repository = HippoRepositoryFactory.getHippoRepository("vm://");
        return repository.login("admin", "admin".toCharArray());
    }
    
    
}
