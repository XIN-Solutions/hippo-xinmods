package nz.xinsolutions.queries.engine.tokenise;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 */
public class RegexTokenDefinition extends TokenDefinition {
    
    private String pattern;
    
    /**
     * Initialise data-members
     * @param id
     * @param pattern
     */
    public RegexTokenDefinition(String id, String pattern) {
        super(id);
        this.pattern = pattern;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(String token) {
        return token.matches(this.pattern);
    }
}
