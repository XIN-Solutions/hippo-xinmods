package nz.xinsolutions.packages;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.xinsolutions.core.jackrabbit.JcrSessionHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.onehippo.repository.util.JcrConstants.NT_UNSTRUCTURED;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 21/01/18
 *
 *      This class is able to persist information back into the JCR.
 */
@Component
public class PackageDefinitionJcrPersister {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PackageDefinitionJcrPersister.class);
    
    /**
     * Package location if not pre-configured
     */
    private static final String STORAGE_LOCATION = "content/packages";

    /**
     * Definitions property
     */
    private static final String PROP_DEFINITIONS = "definitions";

    /**
     * Persist all package definitions into the JCR at a preconfigured location
     *
     * @param session   The session to use for writing.
     * @param packages  List of packages to write the JSON of into the JCR
     */
    public void persistPackages(Session session, List<Package> packages) throws PackageException {
        
        try {
            ensurePackageNodeExists(getStorageLocation());

            if (packages == null) {
                LOG.info("Nothing to persist");
                return;
            }
            
            String packageDefJson = getPackageListAsJsonString(packages);
            Node pkgNode = getPackageNode(session, getStorageLocation());
            writeDefinitionTo(pkgNode, packageDefJson);
        }
        catch (Exception ex) {
            LOG.error("Something happened, caused by: ", ex);
            throw new PackageException(ex);
        }
        
    }
    
    /**
     * Loads a list of packages.
     *
     * @param session   Load the packages with this sessoin.
     * @throws PackageException thrown when can't read (insufficient access?)
     */
    public List<Package> loadPackages(Session session) throws PackageException {
        try {
            ensurePackageNodeExists(getStorageLocation());
            Node pkgNode = getPackageNode(session, getStorageLocation());
            String nodeDefValue = getPackageDefinitionProperty(pkgNode);
            if (StringUtils.isEmpty(nodeDefValue)) {
                return new ArrayList<>();
            }
            
            ObjectMapper objMapper = new ObjectMapper();
            return objMapper.readValue(nodeDefValue, PackageList.class);
        }
        catch (Exception ex) {
            LOG.error("Something happened, caused by: ", ex);
            throw new PackageException(ex);
        }
    }

    
    /**
     * Gets the package node
     *
     * @param session           is the JCR session we're using
     * @param storageLocation   is the location the packages node should live at
     *
     * @return the package definition node
     *
     * @throws RepositoryException when it can't find the node
     */
    protected Node getPackageNode(Session session, String storageLocation) throws RepositoryException {
        return session.getNode("/" + storageLocation);
    }
    
    /**
     * This method writes a string to the definition property of the pkgNode jcr node.
     *
     * @param pkgNode           the package node to write to
     * @param packageDefJson    the json to write into the node
     * @throws RepositoryException is thrown when something goes wrong writing it.
     */
    protected void writeDefinitionTo(Node pkgNode, String packageDefJson) throws RepositoryException {
        if (pkgNode == null) {
            LOG.error("pkgNode cannot be null");
            return;
        }
        if (StringUtils.isEmpty(packageDefJson)) {
            LOG.error("packageDefJson cannot be null");
        }
        
        LOG.info("writing {} to `{}`", packageDefJson, pkgNode.getPath());
        
        pkgNode.setProperty(PROP_DEFINITIONS, packageDefJson);
        pkgNode.getSession().save();
        
    }
    
    
    /**
     * Load the package definitions (a json string) from the passed in node
     *
     * @param pkgNode                   is the package information node.
     * @return                          a json formatted string or null.
     * @throws RepositoryException      is thrown when something goes wrong retrieving the node.
     */
    protected String getPackageDefinitionProperty(Node pkgNode) throws RepositoryException {
        if (pkgNode == null) {
            LOG.error("pkgNode cannot be null");
            return null;
        }
        if (!pkgNode.hasProperty(PROP_DEFINITIONS)) {
            LOG.error("{} not found on node", PROP_DEFINITIONS);
            return null;
        }
        
        return pkgNode.getProperty(PROP_DEFINITIONS).getString();
    }
    
    /**
     * Get a json representation of the package list.
     *
     * @param packages  is a list of packages.
     * @return a string with the json format of the packages.
     * @throws IOException when the package list cannot be mapped to JSON.
     */
    protected String getPackageListAsJsonString(List<Package> packages) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, packages);
        return writer.toString();
    }
    
    /**
     * Make sure a node of type nt:unstructured exists at the root of the repository.
     *
     * @param storageLocation       is the storage location that should exist
     * @throws RepositoryException  something went wrong in the JCR.
     */
    protected void ensurePackageNodeExists(String storageLocation) throws RepositoryException {
        Session adminSession = null;
        try {
            adminSession = JcrSessionHelper.loginAdministrative();
            
            if (!adminSession.nodeExists("/" + storageLocation)) {
                adminSession.getRootNode().addNode(storageLocation, NT_UNSTRUCTURED);
                adminSession.save();
            }
            
        }
        finally {
            if (adminSession != null && adminSession.isLive()) {
                adminSession.logout();
            }
        }
    }
    
    
    protected String getStorageLocation() {
        return STORAGE_LOCATION;
    }
    
    
}
