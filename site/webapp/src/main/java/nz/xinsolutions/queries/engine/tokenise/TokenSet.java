package nz.xinsolutions.queries.engine.tokenise;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 *
 *      A set of tokens.
 *
 */
public class TokenSet {
    
    
    Map<String, TokenDefinition> tokens;
    
    /**
     * Initialise data-members
     */
    public TokenSet() {
        this.tokens = new LinkedHashMap<String, TokenDefinition>();
    }
    
    /**
     * Initialise data-members with a number of tokens
     *
     * @param tokens    the tokens to initialise with
     */
    public TokenSet(TokenDefinition... tokens) {
        this();
        Arrays.stream(tokens).forEach(this::addToken);
    }
    
    /**
     * Adds a token to the set.
     *
     * @param token is the token identifier to add
     */
    public void addToken(TokenDefinition token) {
        tokens.put(token.getId(), token);
    }
    
    public Map<String, TokenDefinition> getTokens() {
        return tokens;
    }
}
