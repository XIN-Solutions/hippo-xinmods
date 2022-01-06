package nz.xinsolutions.rest.model;

import java.util.List;
import java.util.Map;

/**
 * Author: Marnix Kok
 * <p>
 * Purpose:
 */
public class FacetNode {

    private String sourceFacet;
    private String facetPath;
    private String displayName;
    private Map<String, Integer> childFacets;
    private List<Map<String, Object>> results;
    private int totalCount;

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, Integer> getChildFacets() {
        return childFacets;
    }

    public void setChildFacets(Map<String, Integer> childFacets) {
        this.childFacets = childFacets;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public void setResults(List<Map<String, Object>> results) {
        this.results = results;
    }

    public String getSourceFacet() {
        return sourceFacet;
    }

    public void setSourceFacet(String sourceFacet) {
        this.sourceFacet = sourceFacet;
    }

    public String getFacetPath() {
        return facetPath;
    }

    public void setFacetPath(String facetPath) {
        this.facetPath = facetPath;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
