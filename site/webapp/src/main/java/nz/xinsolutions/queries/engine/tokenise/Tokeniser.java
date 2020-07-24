package nz.xinsolutions.queries.engine.tokenise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 *
 *      The tokeniser takes a tokenset and turns it into a lists of identifiers
 *
 */
public class Tokeniser {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Tokeniser.class);
    
    /**
     * The token set to use
     */
    private TokenSet tokenSet;
    
    
    /**
     * Initialise data-members
     *
     * @param tokenSet the set to use for tokenisation
     */
    public Tokeniser(TokenSet tokenSet) {
        this.tokenSet = tokenSet;
    }
    
    
    /**
     * @return the token identifier of the first token that matches <code>sub</code>.
     */
    protected String matchedToken(String sub) {
        for (Map.Entry<String, TokenDefinition> tokenEntry : this.tokenSet.getTokens().entrySet()) {
            TokenDefinition tokenDef = tokenEntry.getValue();
            
            if (tokenDef.matches(sub)) {
                return tokenDef.getId();
            }
            
        }
        
        return null;
    }
    
    
    /**
     * Tokenise a string and turn it into a list of token elements.
     *
     * @param str is the string to parse
     * @return the list of tokens that were found
     */
    public List<TokenElement> tokenise(String str) {
        List<TokenElement> result = new ArrayList<>();
        tokeniseRecurse(str, str, result);
        return result;
    }
    
    
    /**
     * Tail-recursion for the incoming string. It will be matched against
     * elements in the token set and added to the <code>result</code> list.
     *
     * @param str           is the string to parse
     * @param original      is the original string
     * @param result        is the result list
     */
    protected void tokeniseRecurse(String str, String original, List<TokenElement> result) {
        String previousMatch = null;
    
        for (int chIdx = 1; chIdx <= str.length(); ++chIdx) {
            String sub = str.substring(0, chIdx);
            String matchedTokenName = matchedToken(sub);
        
            LOG.debug("match: {}: {}", matchedTokenName, sub);
        
            if (previousMatch == null && matchedTokenName != null) {
                LOG.debug("First match: {}", matchedTokenName);
                previousMatch = matchedTokenName;
                continue;
            }
        
            // no more match, but had previous match?
            if (matchedTokenName == null && previousMatch != null) {
                result.add(new TokenElement(previousMatch, sub.substring(0, sub.length() - 1)));
                tokeniseRecurse(str.substring(chIdx - 1), str, result);
                return;
            }
        }
    
        if (previousMatch != null) {
            result.add(new TokenElement(previousMatch, str));
        } else {
            LOG.debug("Unable to match: {} at {}", original, str);
        }
    }
    
    
}
