package nz.xinsolutions.queries.engine.tokenise;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 */
abstract public class TokenDefinition {
    
    String id;
    
    /**
     * Initialise data-members
     *
     * @param id  the identifier used for this token
     */
    public TokenDefinition(String id) {
        this.id = id;
    }
    
    /**
      * @return the token identifier
     */
    public String getId() {
        return id;
    }
    
    /**
     * @return true if some incoming text in <code>token</code> matches this token's identification
     */
    abstract public boolean matches(String token);
    
}
