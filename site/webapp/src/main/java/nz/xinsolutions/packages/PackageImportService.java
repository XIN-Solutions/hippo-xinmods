package nz.xinsolutions.packages;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.xinsolutions.cnd.CndBundle;
import nz.xinsolutions.cnd.CndNamespace;
import nz.xinsolutions.cnd.CndSerialiser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.onehippo.cm.ConfigurationService;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ZipUtil;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.zeroturnaround.zip.commons.FileUtilsV2_2.readFileToString;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 25/11/17
 *
 *      Package import service allows you to import a package into the repository.
 *
 *  TODO: implement a 'force' option, and make import fail if package with same id already exists
 */
@Component
public class PackageImportService {
    
    public static final String CND_FILE = "cndExport.json";
    public static final String METADATA_FILE = "metadata.json";
    
    @Autowired private PackageListService listService;
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PackageImportService.class);
    
    /**
     * Import the package file.
     *
     * @param pkgFile
     * @throws PackageException
     */
    public void importFile(
                Session session, File pkgFile,
                boolean importContent,
                boolean importCnd,
                boolean importPackageDef) throws PackageException
    {
        
        if (!isZip(pkgFile)) {
            throw new PackageException("Imported package is not a zip");
        }
        
        try {
            File dir = extractZipFile(pkgFile);
            Package packageDesc = getPackageDescription(dir);
            CndBundle cndBundle = getCndBundle(dir);
            List<String> contentFiles = getContentFiles(dir);
    
            if (packageDesc == null) {
                throw new PackageException("No package description found (metadata.json), aborting.");
            }
            
            // if package already exists? aboooort.
            if (listService.packageExists(session, packageDesc.getId())) {
                LOG.warn("WARNING, overwriting an existing package definition that has id: " + packageDesc.getId());
                throw new PackageException(
                    String.format(
                        "Will not import same package twice, already found: `%s`",
                        packageDesc.getId()
                    )
                );
            }
            
            // check whether package requirements are met
            validatePackageAgainstRepo(packageDesc);
    
            if (importPackageDef) {
                listService.addPackage(session, packageDesc);
            } else {
                LOG.info("Not keeping package definition.");
            }
    
            // has cnd bundle definition? import it.
            if (importCnd && cndBundle != null) {
                importCnd(cndBundle, session);
            }
            else if (!importCnd) {
                LOG.info("Skipping the CND import as per import instructions.");
            }
            else {
                LOG.warn("No cndExport.json found in the package, so no CND entries added to repo is going to be imported.");
            }
           
            // has content files?
            if (importContent && !CollectionUtils.isEmpty(contentFiles)) {
                for (String contentFile : contentFiles) {
                    importZippedYaml(dir.getCanonicalPath() + "/" + contentFile, session);
                }
            } else if (!importContent) {
                LOG.info("Skipping importing the content as per import instructions.");
            }


            // clean up
            cleanUpAttachment(pkgFile, dir);
            
        }
        catch (IOException ioEx) {
            throw new PackageException("Could not load package, cause by: " + ioEx.getMessage());
        }
    }
    
    /**
     * Import the content from a zipped yaml into the JCR
     *
     * @param contentFile is the path to the file to import
     * @param session is the jcr session to write this to
     */
    protected void importZippedYaml(String contentFile, Session session) {

        File zipFile = new File(contentFile);

        try {
            // unzip zip
            ZipUtil.unpack(zipFile, zipFile.getParentFile());
            
            String contentBasePath = getNodeNameFromFile(zipFile);
            String parentPath = getParentPathOf(contentBasePath);
            LOG.info("Determined root path: " + parentPath);
            
            LOG.info("Importing: {} into {}", zipFile.getName(), parentPath);
            
            ConfigurationService configService = HippoServiceRegistry.getService(ConfigurationService.class);
            // TODO: add node exists check
            configService.importZippedContent(zipFile, session.getNode(parentPath));
            
            session.save();
            session.refresh(false);

        }
        catch (IOException | RepositoryException ex) {
            LOG.error("Something went wrong while trying to import `{}`, caused by: ", contentFile, ex);
        }
        finally {

            // clean up
            cleanUpAttachmentFiles(contentFile, zipFile);

        }
    }

    /**
     * This method cleans up after itself when the package has been properly imported
     * @param contentFile
     * @param zipFile
     * @throws IOException
     */
    protected void cleanUpAttachmentFiles(String contentFile, File zipFile)  {
        try {
            String extractedContentFile = contentFile + "_extracted";
            FileUtils.deleteDirectory(new File(extractedContentFile));
            zipFile.delete();
        }
        catch (IOException ioEx) {
            LOG.error("Could not clean up, caused by: ", ioEx);
        }
    }

    /**
     * @return the parent path (minus the last /element)
     */
    protected String getParentPathOf(String contentBasePath) {
        if (StringUtils.isEmpty(contentBasePath)) {
            return null;
        }
        
        List<String> elements =
            new ArrayList<>(
                Arrays.asList(contentBasePath.split("/"))
            );
        
        elements.remove(elements.size() - 1);
        
        return elements.stream().collect(Collectors.joining("/"));
    }
    
    
    private String getNodeNameFromFile(File zipFile) {
        return zipFile.getName()
            .replace(".zip", "")
            .replace("_", "/");
    }
    
    /**
     *
     * @param packageDesc
     * @throws PackageException
     */
    protected void validatePackageAgainstRepo(Package packageDesc) throws PackageException {
        // TODO: make sure the required cnd entries are already there
    }
    
    /**
     * Clean up the folder and attachment file
     *
     * @param pkgFile
     * @param dir
     */
    protected void cleanUpAttachment(File pkgFile, File dir) throws IOException {
        LOG.info("Cleaning up package file");
        pkgFile.delete();
        FileUtils.deleteDirectory(dir);
    }
    
    /**
     * Import the cnd bundle into the JCR so that the content that is to be imported
     * can be imported appropriately.
     *
     * @param cndBundle
     */
    protected void importCnd(CndBundle cndBundle, Session session) {
        String cndFormatted =
            new CndSerialiser().outputToCndFormat(session.getWorkspace(), cndBundle.getEntities());


        try {
            ensureAllNamespacesExist(session, cndBundle.getNamespaces());

            CndImporter.registerNodeTypes(
                new StringReader(cndFormatted),
                session,
                true
            );
        }
        catch (ParseException | RepositoryException | IOException ex) {
            LOG.error("Cannot register node types from this CND `{}`, caused by:", cndFormatted, ex);
        }
    }

    /**
     * This method adds missing namespaces as per their prefix and URI descriptions in the
     * namespaces list.
     *
     * @param session       the JCR session to operate on
     * @param namespaces    the namespaces we need to check
     */
    protected void ensureAllNamespacesExist(Session session, List<CndNamespace> namespaces) throws RepositoryException {
        NamespaceRegistry registry = session.getWorkspace().getNamespaceRegistry();
        String[] prefixes = registry.getPrefixes();

        List<CndNamespace> missing =
            namespaces
                .stream()
                .filter(ns -> !ArrayUtils.contains(prefixes, ns.getName()))
                .collect(Collectors.toList())
            ;

        for (CndNamespace ns : missing) {
            LOG.info("Registering missing namespace: `{}` at `{}`", ns.getName(), ns.getUri());
            registry.registerNamespace(ns.getName(), ns.getUri());
        }
    }

    /**
     * @return a list of content files that are part of the zip
     */
    protected List<String> getContentFiles(File dir) {
        String[] files = dir.list((FilenameFilter) new WildcardFileFilter("*.zip"));
        return Arrays.asList(files);
    }
    
    /**
     * @return the cnd bundle description
     */
    protected CndBundle getCndBundle(File dir) throws IOException {
        File cndFile = new File(dir, CND_FILE);
        String cndFileString = readFileToString(cndFile, "UTF-8");
        return new CndSerialiser().fromJson(cndFileString);
    }
    
    /**
     * @return the package instance of the metadata.json file
     */
    protected Package getPackageDescription(File dir) throws IOException {
        File packageFile = new File(dir, METADATA_FILE);
        String packageFileString = readFileToString(packageFile, "UTF-8");
        ObjectMapper map = new ObjectMapper();
        return map.readValue(packageFileString, Package.class);
    }
    
    protected boolean isZip(File file) {
        // TODO: implement
        return true;
    }
    
    
    /**
     * @return the directory it was unzipped in
     */
    protected File extractZipFile(File pkgFile) {
        try {
            String pathname = pkgFile.getCanonicalPath() + "_extracted";
            File tmpDir = new File(pathname);
            tmpDir.mkdir();
            ZipUtil.unpack(pkgFile, tmpDir);
            return tmpDir;
        }
        catch (IOException ioEx) {
            LOG.error("Can't unpack the zip file: ", ioEx);
        }
        return null;
    }
    
    
    
}
