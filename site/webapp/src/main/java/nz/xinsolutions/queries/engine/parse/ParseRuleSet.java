package nz.xinsolutions.queries.engine.parse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 */
public class ParseRuleSet {
    
    private Map<String, ParseRule> rules;
    
    public ParseRuleSet() {
        this.rules = new LinkedHashMap<String, ParseRule>();
    }
    
    /**
     * Initialise with a set of rules
     *
     * @param rules
     */
    public ParseRuleSet(ParseRule... rules) {
        this();
        Arrays.stream(rules).forEach(this::addRule);
    }
    
    /**
     * Add the rule.
     *
     * @param rule  rule instance
     */
    public void addRule(ParseRule rule) {
        this.rules.put(rule.getId(), rule);
    }
    
    public Map<String, ParseRule> getRules() {
        return rules;
    }
}
