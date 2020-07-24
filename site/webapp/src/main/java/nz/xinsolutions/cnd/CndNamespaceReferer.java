package nz.xinsolutions.cnd;

import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 26/10/17
 */
public interface CndNamespaceReferer {
    
    List<CndNamespace> getReferredNamespaces();
    
}
