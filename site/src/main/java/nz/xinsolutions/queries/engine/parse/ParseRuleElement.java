package nz.xinsolutions.queries.engine.parse;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 */
public class ParseRuleElement {
    
    private boolean mandatory;
    private boolean subRules;
    private List<String> ids;
    
    /**
     * Initialise data-members
     */
    public ParseRuleElement(boolean mandatory, boolean subRules, String... ids) {
        this.mandatory = mandatory;
        this.subRules = subRules;
        this.ids = Arrays.asList(ids);
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public boolean isSubRules() {
        return subRules;
    }
    
    public List<String> getIds() {
        return ids;
    }
    
    public String getId() {
        return ids.get(0);
    }
}
