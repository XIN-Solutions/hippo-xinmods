package nz.xinsolutions.cnd;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 25/11/17
 *
 *  A bundle of CND information
 *
 */
public class CndBundle {

    private List<CndNamespace> namespaces;
    private List<CndEntity> entities;
    
    /**
     * Initialise an empty cnd bundle
     */
    public CndBundle() {
        this(
            new ArrayList<>(),
            new ArrayList<>()
        );
    }
    
    /**
     * Initialise data-members
     *
     * @param namespaces    the namespaces that are contained within the bundle
     * @param entities      the entities contained within this bundle
     */
    public CndBundle(List<CndNamespace> namespaces, List<CndEntity> entities) {
        this.namespaces = namespaces;
        this.entities = entities;
    }
    
    public List<CndNamespace> getNamespaces() {
        return namespaces;
    }
    
    public void setNamespaces(List<CndNamespace> namespaces) {
        this.namespaces = namespaces;
    }
    
    public List<CndEntity> getEntities() {
        return entities;
    }
    
    public void setEntities(List<CndEntity> entities) {
        this.entities = entities;
    }
}
