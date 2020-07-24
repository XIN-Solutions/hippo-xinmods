package nz.xinsolutions.queries.engine.parse;

import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 */
public class ParseRule {
    
    private String id;
    private List<ParseRuleElement> ruleElements;
    
    /**
     * Initialise data-members
     *
      *@param id           identifier
     * @param ruleElements  rule elements
     */
    public ParseRule(String id, List<ParseRuleElement> ruleElements) {
        this.id = id;
        this.ruleElements = ruleElements;
    }
    
    public String getId() {
        return id;
    }
    
    public List<ParseRuleElement> getRuleElements() {
        return ruleElements;
    }
}
