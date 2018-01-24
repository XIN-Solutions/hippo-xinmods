package nz.xinsolutions.packages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 25/11/17
 *
 *  The package list service is able to retrieve and parse packages from the data source
 *
 */
@Component
public class PackageListService {
    
    private static final Logger LOG = LoggerFactory.getLogger(PackageListService.class);
    
    /**
     * Persister implementation
     */
    @Autowired private PackageDefinitionJcrPersister jcrPersister;
    
    private List<Package> packages;
    
//
//    /**
//     * Fake list of packages
//     */
//    private List<Package> packages = new ArrayList<Package>() {{
//
//        add(new Package() {{
//            setId("blog-package");
//            setFilters(Arrays.asList(
//                "/content/documents/blog",
//                "/hippo:namespaces/xinmods",
//                "/hippo:configuration/hippo:queries/hippo:templates/new-xinmods-genericimageset-folder",
//                "/hippo:configuration/hippo:queries/hippo:templates/new-xinmods-genericimageset-image"));
//            setCnds(Arrays.asList(
//                "xinmods:basedocument",
//                "xinmods:categorydetails",
//                "xinmods:content",
//                "xinmods:galleryComponent",
//                "xinmods:imageComponent",
//                "xinmods:listComponent",
//                "xinmods:menu",
//                "xinmods:menuitem",
//                "xinmods:postdetails",
//                "xinmods:siteconfig",
//                "xinmods:textComponent",
//                "xinmods:textImageComponent",
//                "xinmods:twoColumn"
//            ));
//        }});
//
//        add(new Package() {{
//            setId("shop-package");
//            setRequiredCnds(Arrays.asList("xinmods:content"));
//            setFilters(Arrays.asList("/content/documents/shop"));
//            setCnds(Arrays.asList("xinmods:content", "xinmods:postdetails", "xinmods:eventdetails"));
//        }});
//
//    }};
//
//
    /**
     * Get a list of all the packages
     *
     * @return a list of all pages
     * @throws PackageException
     */
    public List<Package> getPackages(Session session) throws PackageException {
        return jcrPersister.loadPackages(session);
    }
    
    
    /**
     * This method adds a package to the list of package definitions
     * @param session
     * @param pkg
     * @throws PackageException
     */
    public void addPackage(Session session, Package pkg) throws PackageException {
        if (pkg == null) {
            LOG.error("Cannot store a null package, aborting.");
        }
        
        // retrieve current state
        List<Package> packages = getPackages(session);
        
        // not created yet?
        if (packages == null) {
            packages = new ArrayList<>();
        }
        
        // add the new package
        packages.add(pkg);
        
        // store
        this.jcrPersister.persistPackages(session, packages);
    }
    
    /**
     * Delete a package with id <code>packageId</code> from the list of packages, then persist it.
     *
     * @param jcrSession    is the session to get the packages with
     * @param packageId     is the identifier to get
     * @throws PackageException
     */
    public void deletePackage(Session jcrSession, String packageId) throws PackageException {
        
        List<Package> packages =
            getPackages(jcrSession)
                .stream()
                .filter(pkg -> !pkg.getId().equals(packageId))
                .collect(Collectors.toList())
            ;
        
        this.jcrPersister.persistPackages(jcrSession, packages);
        
    }
    
    /**
     * Get package information for package with id <code>id</code>
     *
     * @param id is the package identifier to retrieve
     * @return the package instance or null if not found
     * @throws PackageException
     */
    public Package getPackage(Session session, String id) throws PackageException {
        return getPackages(session)
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
    public boolean packageExists(Session session, String packageId) throws PackageException {
        return getPackage(session, packageId) != null;
    }

}
