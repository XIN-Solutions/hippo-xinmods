package nz.xinsolutions.queries.engine.interpret;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 23/08/17
 */
public class ValueHelper {
    
    private ValueHelper() {}
    
    public static String getStringValue(String val) {
        return val.substring(1, val.length() - 1).replace("\\'", "'");
    }
    
    public static boolean isStringValue(String val) {
        return val.startsWith("'") && val.endsWith("'");
    }
    
    
}
