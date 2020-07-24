package nz.xinsolutions.queries.engine.tokenise;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 */
public class TokenFactory {
    
    private TokenFactory() {}
    
    public static TokenDefinition regex(String id, String pattern) {
        return new RegexTokenDefinition(id, pattern);
    }
    
    public static TokenDefinition text(String id, String text) {
        return new TextTokenDefinition(id, text);
    }
    
}
