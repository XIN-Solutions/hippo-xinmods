package nz.xinsolutions.packages;

import org.apache.commons.lang3.StringUtils;

/**
 * Simple POJO to contain information regarding an expanded path filter.
 */
public class FilterExpansion {
    
    private String basePath;
    private String startsWith;
    
    
    public FilterExpansion(String path) {

        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("Expected a path value.");
        }
        if (!path.endsWith("*")) {
            throw new IllegalArgumentException("Expected a path value to end with an asterisk");
        }
        if (path.indexOf("/", 1) == -1) {
            throw new IllegalArgumentException("Will not be able to determine parent without proper path names");
        }
        
        // this means it ends in a wildcard
        int lastSlash = path.lastIndexOf("/");
        this.basePath = path.substring(0, lastSlash);

        // extract the part of the last path element before the wildcard
        this.startsWith = path.substring(lastSlash + 1, path.length() - 1);
    
    }
    
    
    public String getBasePath() {
        return basePath;
    }
    
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    public String getStartsWith() {
        return startsWith;
    }
    
    public void setStartsWith(String startsWith) {
        this.startsWith = startsWith;
    }
}
