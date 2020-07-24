package nz.xinsolutions.queries.engine.interpret;

import nz.xinsolutions.queries.engine.parse.RuleState;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 21/08/17
 */
public class QuerySettings {
    
    private String beanName;
    private boolean useSubtypes;
    private Integer limit = null;
    private Integer offset = null;
    private List<String> includeScopes = Arrays.asList("/content/documents");
    private List<String> excludeScopes;
    private String sortField;
    private String sortOrder;
    private RuleState whereState;
    
    public String getBeanName() {
        return beanName;
    }
    
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
    
    public boolean isUseSubtypes() {
        return useSubtypes;
    }
    
    public void setUseSubtypes(boolean useSubtypes) {
        this.useSubtypes = useSubtypes;
    }
    
    public List<String> getIncludeScopes() {
        return includeScopes;
    }
    
    public void setIncludeScopes(List<String> includeScopes) {
        this.includeScopes = includeScopes;
    }
    
    public List<String> getExcludeScopes() {
        return excludeScopes;
    }
    
    public void setExcludeScopes(List<String> excludeScopes) {
        this.excludeScopes = excludeScopes;
    }
    
    public String getSortField() {
        return sortField;
    }
    
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }
    
    public String getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public Integer getOffset() {
        return offset;
    }
    
    public void setOffset(Integer offset) {
        this.offset = offset;
    }
    
    public RuleState getWhereState() {
        return whereState;
    }
    
    public void setWhereState(RuleState whereState) {
        this.whereState = whereState;
    }
}
