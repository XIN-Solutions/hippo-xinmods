package nz.xinsolutions.cnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 26/10/17
 */
public class CndNamespace {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CndNamespace.class);


    private String name;
    private String uri;
    
    
    public CndNamespace() {
    
    }
    
    /**
     * @return cnd namespace instance based on a typename
     */
    public static CndNamespace fromType(String type) {
        if (type.contains(":")) {
            int colonIdx = type.indexOf(":");
            String namespace = type.substring(0, colonIdx);
            return CndNamespace.partial(namespace);
        }
        
        return null;
    }
    
    
    public static CndNamespace partial(String name) {
        return new CndNamespace() {{
            setName(name);
        }};
    }
    
    
    public static CndNamespace fromPlain(String name, String uri) {
        return new CndNamespace() {{
            setName(name);
            setUri(uri);
        }};
    }
    
    
    /**
     * Fills in the blanks with regards to the URI for the namespace prefix
     * @param workspace
     * @throws RepositoryException
     */
    public void resolve(Workspace workspace)  {
        try {
            setUri(workspace.getNamespaceRegistry().getURI(this.name));
        } catch (RepositoryException e) {
            LOG.error("Could not resolve prefix `{}`", this.name);
        }
    }
    
    
    // -------------------------------------------------------------------------------------------
    //      Accessors
    // -------------------------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
}
