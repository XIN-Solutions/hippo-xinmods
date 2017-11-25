package nz.xinsolutions.packages;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 25/11/17
 *
 *
 *
 */
@Component
public class PackageListService {
    
    /**
     * Fake list of packages
     */
    private List<Package> packages = new ArrayList<Package>() {{
        
        add(new Package() {{
            setId("blog-package");
            setFilters(Arrays.asList(
                "/content/documents/blog",
                "/hippo:namespaces/xinmods",
                "/hippo:configuration/hippo:queries/hippo:templates/new-xinmods-genericimageset-folder",
                "/hippo:configuration/hippo:queries/hippo:templates/new-xinmods-genericimageset-image"));
            setCnds(Arrays.asList(
                "xinmods:basedocument",
                "xinmods:content",
                "xinmods:postdetails",
                "xinmods:eventdetails",
                "xinmods:menuitem",
                "xinmods:menu"
            ));
        }});
        
        add(new Package() {{
            setId("shop-package");
            setRequiredCnds(Arrays.asList("xinmods:content"));
            setFilters(Arrays.asList("/content/documents/shop"));
            setCnds(Arrays.asList("xinmods:content", "xinmods:postdetails", "xinmods:eventdetails"));
        }});
        
    }};
    
    
    /**
     * @return a list of all pages
     * @throws PackageException
     */
    public List<Package> getPackages() throws PackageException {
        return packages;
    }
    
    
    /**
     * Get package information for package with id <code>id</code>
     *
     * @param id is the package identifier to retrieve
     * @return the package instance or null if not found
     * @throws PackageException
     */
    public Package getPackage(String id) throws PackageException {
        return getPackages()
            .stream()
            .filter( pkg -> pkg.getId().equals(id) )
            .findFirst()
            .orElse(null)
            ;
    }
    
    /**
     * Make sure that a package with `packageId` exists.
     *
     * @param packageId is the identifier of the package
     * @throws PackageException thrown when something goes wrong
     * @return true if the package exists
     */
    public boolean packageExists(String packageId) throws PackageException {
        return getPackage(packageId) != null;
    }
    
}
