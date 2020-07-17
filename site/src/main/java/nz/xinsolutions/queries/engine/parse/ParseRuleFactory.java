package nz.xinsolutions.queries.engine.parse;

import java.util.Arrays;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 *
 *      Contains functions that help generate parse rules
 */
public class ParseRuleFactory {
    
    private ParseRuleFactory() {}
    
    public static ParseRule rule(String id, ParseRuleElement... elements) {
        return new ParseRule(id, Arrays.asList(elements));
    }
    
    public interface Token {
    
        static ParseRuleElement m(String token) {
            return new ParseRuleElement(true, false, token);
        }
        
        static ParseRuleElement o(String token) {
            return new ParseRuleElement(false, false, token);
        }
    
    }
    
    public interface Rule {
        
        static ParseRuleElement m(String... rules) {
            return new ParseRuleElement(true, true, rules);
        }
        
        static ParseRuleElement o(String... rules) {
            return new ParseRuleElement(false, true, rules);
        }
    }
    
}
