package nz.xinsolutions.packages;

import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class FilterExpansionTest extends TestCase {

    public void testRequiresWildcard() {
        try {
            FilterExpansion fEx = new FilterExpansion("/content/documents");
            assertTrue(false);
        }
        catch (IllegalArgumentException iaEx) {
            assertTrue(true);
        }
    }
    
    public void testRequiresMultipleChildren() {
        try {
            FilterExpansion fEx = new FilterExpansion("/documents*");
            assertTrue(false);
        }
        catch (IllegalArgumentException iaEx) {
            assertTrue(true);
        }
    }
    
    /**
     * Test that both usecases of filter all, or partial names result in proper
     * filter expension pojo state.
     */
    public void testExpansion() {
        FilterExpansion fEx;
        
        fEx = new FilterExpansion("/content/documents/*");
        assertEquals("/content/documents", fEx.getBasePath());
        assertTrue(StringUtils.isBlank(fEx.getStartsWith()));
        
        fEx = new FilterExpansion("/content/documents/shouldhavethis*");
        assertEquals("/content/documents", fEx.getBasePath());
        assertEquals("shouldhavethis", fEx.getStartsWith());
    }
    
}