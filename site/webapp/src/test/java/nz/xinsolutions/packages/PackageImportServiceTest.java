package nz.xinsolutions.packages;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 29/01/18
 */
public class PackageImportServiceTest {
    @Test
    public void getParentPathOf() throws Exception {
        
        String path = "/content/my/path/is/cool";
        PackageImportService service = new PackageImportService();
        assertEquals("/content/my/path/is", service.getParentPathOf(path));
    }
    
}