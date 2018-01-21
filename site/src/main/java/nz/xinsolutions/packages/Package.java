package nz.xinsolutions.packages;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 10/09/17
 *
 * Package definition in manager
 */
@XmlRootElement
public class Package {
    
    private String id;
    private List<String> filters;
    private List<String> cnds;
    private List<String> requiredCnds;
    
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public List<String> getFilters() {
        return filters;
    }
    
    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
    
    public List<String> getCnds() {
        return cnds;
    }
    
    public void setCnds(List<String> cnds) {
        this.cnds = cnds;
    }
    
    public List<String> getRequiredCnds() {
        return requiredCnds;
    }
    
    public void setRequiredCnds(List<String> requiredCnds) {
        this.requiredCnds = requiredCnds;
    }
}
