package nz.xinsolutions.queries.engine.parse;

import nz.xinsolutions.queries.engine.tokenise.TokenElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 *
 *      Uncapsulated DTO.
 *
 */
public class RuleState {
    
    public String ruleName;
    public boolean matched;
    public int consumedTokens;
    public List<RuleState> subStates = new ArrayList<>();
    public List<TokenElement> tokens;
    
    
    
    /**
     * @return rules with a certain name
     */
    public List<RuleState> findRules(String... ruleNames) {
        
        if (CollectionUtils.isEmpty(subStates)) {
            return Collections.EMPTY_LIST;
        } else {
            return subStates
                        .stream()
                        .filter( state -> ArrayUtils.contains(ruleNames, state.ruleName))
                        .collect(Collectors.toList())
                    ;
        }
        
    }
    
    /**
     * @return the first token with name <code>tokenId</code>.
     */
    public TokenElement findToken(String tokenId) {
        return findToken(tokenId, 1);
    }
    
    /**
     * @return the nth token of a specific name
     */
    public TokenElement findToken(String tokenId, int nOccurance) {
        
        if (CollectionUtils.isEmpty(tokens)) {
            return null;
        }
        
        List<TokenElement> matchedTokens =
            this.tokens
                    .stream()
                    .filter( tok -> tok.getName().equals(tokenId) )
                    .collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(matchedTokens)) {
            return null;
        }
        
        if (matchedTokens.size() < nOccurance) {
            return null;
        } else {
            return matchedTokens.get(nOccurance - 1);
        }
    }
    
    
    
    // ------------------------------------------------------------------------------------------
    //      Accessors.
    // ------------------------------------------------------------------------------------------
    
}
