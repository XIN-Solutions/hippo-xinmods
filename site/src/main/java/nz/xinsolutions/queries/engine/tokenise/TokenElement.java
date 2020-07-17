package nz.xinsolutions.queries.engine.tokenise;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 */
public class TokenElement {
    
    private String name;
    private String value;
    
    public TokenElement(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public String toString() {
        return String.format("%s(%s)", this.name, this.value);
    }
    
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
}
