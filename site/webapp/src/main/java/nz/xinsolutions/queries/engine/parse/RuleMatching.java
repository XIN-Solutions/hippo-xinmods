package nz.xinsolutions.queries.engine.parse;

import nz.xinsolutions.queries.engine.tokenise.TokenElement;
import nz.xinsolutions.queries.engine.tokenise.TokenSet;
import nz.xinsolutions.queries.engine.tokenise.Tokeniser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 *
 *  Match a bunch of token elements against parse rules.
 */
public class RuleMatching {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(RuleMatching.class);
    
    /**
     * Set of rules to match with
     */
    private ParseRuleSet rules;
    
    /**
     * Initialise data-members
     *
     * @param rules     the set of rules
     */
    public RuleMatching(ParseRuleSet rules) {
        this.rules = rules;
    }
    
    
    /**
     * Recursive algorithm to match forward against a set of rules
     *
     * @param rule         is the rule we're matching against
     * @param current      is the current list of token elements to match with
     * @param parentState  is the state we're storing information into
     * @return the rule state
     */
    public RuleState tokensMatchRule(ParseRule rule, List<TokenElement> current, RuleState parentState) {
    
        boolean matches = false;
        int currentIdx = 0;
        int ruleTokenIdx = 0;
    
        RuleState state = new RuleState();
    
        for (ParseRuleElement expected : rule.getRuleElements()) {
        
            // the expected rule is a list of possible expected subrules
            // the first element in the rule list is ? or !, causing the match
            // to be mandatory.
            if (expected.isSubRules()) {
                List<String> subValidRules = expected.getIds();
            
                LOG.debug("Subtype: ${subType}, tries for rules: ${subValidRules}");
            
                // try to match on the tokens that havent been consumed yet, one of the rules specified in the list
                RuleState subMatch = this.match(current.subList(currentIdx, current.size()), subValidRules, state);
            
                // no submatch but required? return false
                if (subMatch == null && expected.isMandatory()) {
                    LOG.error("Did not find a match for subrules: {}", subValidRules);
                    return new RuleState() {{ matched = false; }};
                }
                else if (subMatch != null && subMatch.matched) {
                    currentIdx += subMatch.consumedTokens;
                    LOG.debug("Yes, Submatched!, remaining tokens: {}", rule.getRuleElements().size() - ruleTokenIdx);
                }
            }
            else {
            
                String expectedToken = expected.getId();
            
                TokenElement token = current.get(currentIdx);
                LOG.debug("Mandatory: {}, token: {}, lookingat: {}", expected.isMandatory(), expected.getId(), token);
            
                // found the token we need?
                if (expectedToken.equals(token.getName())) {
                    ++currentIdx;
                    LOG.debug("Matched {}, on `{}`", expectedToken, token.getValue());
                }
                
                // if not, fail if the token was mandatory
                else if (expected.isMandatory()) {
                    LOG.debug("Expected: {} found `{}`", expectedToken, token.getName());
                    RuleState returnState = new RuleState();
                    returnState.matched = false;
                    returnState.consumedTokens = currentIdx;
                    return returnState;
                }
            
            }
        
            ++ruleTokenIdx;
        
            // if current index has reached the size limit of the available tokens
            if (currentIdx >= current.size()) {
            
                // determine if we were able to match the entire rule (which indicates a match)
                RuleState returnState = new RuleState();
                returnState.matched = (ruleTokenIdx == rule.getRuleElements().size());
                returnState.consumedTokens = currentIdx;
                returnState.subStates = state.subStates;
                returnState.tokens = current;
                return returnState;
            }
        
        
        }
    
        LOG.debug("currentidx: {} / {}", currentIdx, current.size());

        RuleState returnState = new RuleState();
        returnState.matched = (ruleTokenIdx == rule.getRuleElements().size());
        returnState.consumedTokens = currentIdx;
        returnState.subStates = state.subStates;
        returnState.tokens = current.subList(0, currentIdx);

        return returnState;
    }
    
    
    
    
    /**
     * Match a set of tokens onto one or more possible rules
     *
     * @param tokens the tokens to match
     * @param allowedRules the names of the rules that are valid
     * @return
     */
    RuleState match(List<TokenElement> tokens, List<String> allowedRules, RuleState parentState) {
        
        for (String ruleName : allowedRules) {
            LOG.debug("Testing for: {} \n .. with tokens: {}", ruleName, rules.getRules().get(ruleName));
            RuleState ruleState = tokensMatchRule(rules.getRules().get(ruleName), tokens, parentState);
            
            LOG.debug("Returned: {}", ruleState.matched);
            
            if (!ruleState.matched) {
                continue;
            }

            LOG.debug("matches: {} => {}", ruleName, tokens.subList(0, ruleState.consumedTokens));

            ruleState.ruleName = ruleName;
            if (parentState != null) {
                parentState.subStates.add(ruleState);
                return ruleState;
            }
        }
        
        return null;
    }
    
    
    public static RuleState parseString(ParseRuleSet ruleSet, TokenSet tokenSet, String text, String... startingRules) {

        Tokeniser tokeniser = new Tokeniser(tokenSet);
        List<TokenElement> elements = tokeniser.tokenise(text);
        
        RuleMatching matcher = new RuleMatching(ruleSet);
        RuleState rootState = new RuleState();
        
        while (true) {
            
            RuleState mResult = matcher.match(elements, Arrays.asList(startingRules), rootState);
            
            if (mResult == null || !mResult.matched) {
                LOG.debug("No match");
                break;
            }

            if (mResult.consumedTokens == elements.size()) {
                LOG.debug(
                    "All tokens were consumed",
                    mResult.ruleName,
                    mResult.consumedTokens,
                    elements.size()
                );
                break;
            }
            
            elements = elements.subList(mResult.consumedTokens, elements.size());
            
        }
        
        return rootState;
    }
    
}
