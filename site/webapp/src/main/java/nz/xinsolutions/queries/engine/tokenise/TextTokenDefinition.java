package nz.xinsolutions.queries.engine.tokenise;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 */
public class TextTokenDefinition extends TokenDefinition {
    
    /**
     * Match this
     */
    private String matchThis;
    
    
    /**
     * Initialise data-members
     *
     * @param id        the token identifier
     * @param matchThis the thing to match
     */
    public TextTokenDefinition(String id, String matchThis) {
        super(id);
        this.matchThis = matchThis;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(String token) {
        return token.equals(this.matchThis);
    }
}
