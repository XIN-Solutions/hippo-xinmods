package nz.xinsolutions.derived;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import java.util.Map;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      Contract for what a groovy script that implements derive-behaviours should implement.
 *
 */
public interface DeriveFunction {

    /**
     * Derive function definition
     *
     * @param jcrSession    the session to manipulate with
     * @param document      the document that needs processing.
     */
    boolean derive(Session jcrSession, Node document);
}
