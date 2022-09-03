package nz.xinsolutions.packages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ZipUtil;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 23/09/17
 */
@Component
public class PackageExportService {
    
    public static final String FILE_CND_EXPORT_JSON = "cndExport.json";
    public static final String FILE_METADATA = "metadata.json";
    
    @Autowired
    private PackageListService packageListService;
    
    @Autowired
    private MultiPathExporter multiPathExporter;
    
    @Autowired
    private PartialCndExporter partialCndExporter;
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PackageExportService.class);
  
    /**
     * Build a package
     *
     * @param packageId
     * @param outStream
     * @throws PackageException
     */
    public void build(Session jcrSession, String packageId, OutputStream outStream) throws PackageException {
     
        Package pkg = packageListService.getPackage(jcrSession, packageId);
        if (pkg == null) {
            throw new PackageException("No such package with identifier: " + packageId);
        }
    
        try {
            List<Node> filterNode = getFilterNodes(jcrSession, pkg);
            File tmpBaseFolder = multiPathExporter.exportZippedContent(filterNode);
            
            String cnd =
                partialCndExporter.exportCnds(
                    jcrSession.getWorkspace(),
                    pkg.getCnds().toArray(new String[0])
                );
            
            LOG.info("CND:\n" + cnd);
            
            String packageMetadata = fromPojoToString(pkg);
            
            // write package description to file
            writeStringAsFile(tmpBaseFolder, FILE_METADATA, packageMetadata);
            
            // write CND string to file
            writeStringAsFile(tmpBaseFolder, FILE_CND_EXPORT_JSON, cnd);
    
            // pack and stream back to the user
            ZipUtil.pack(tmpBaseFolder, outStream, ZipUtil.DEFAULT_COMPRESSION_LEVEL);

            LOG.info("Attempting to delete the temporary folder in which package content was generated");
            FileUtils.deleteDirectory(tmpBaseFolder);
            LOG.info("Deleted temporary folder at " + tmpBaseFolder.getAbsolutePath());
        }
        catch (IOException ioEx) {
            LOG.error("IO Exception, caused by: ", ioEx);
        }
        catch (RepositoryException rEx) {
            LOG.error("Cannot find node, caused by: ", rEx);
        }
        
        
    }
    
    /**
     * @return the json string of <code>pojo</code>.
     */
    protected String fromPojoToString(Object pojo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter strWriter = new StringWriter();
            objectMapper.writeValue(strWriter, pojo);
            return strWriter.toString();
        }
        catch (IOException ioEx) {
            LOG.error("Could not properly marshal the pojo, caused by: ", ioEx);
        }
        return null;
    }
    
    
    /**
     * Write a string to file.
     *
     * @param folder    is the folder to write to the file to
     * @param filename  is the name of the file to create
     * @param content   is the contents of the file.
     *
     * @throws FileNotFoundException
     */
    protected void writeStringAsFile(File folder, String filename, String content) throws FileNotFoundException {
        File metainfo = new File(folder, filename);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(metainfo)));
        writer.print(content);
        writer.close();
    }
    
    /**
     * Get the jcr nodes for a package instance
     *
     * @param jcrSession
     * @param pkg
     * @return
     *
     * @throws RepositoryException
     */
    protected List<Node> getFilterNodes(Session jcrSession, Package pkg) throws RepositoryException {
        
        List<Node> filterNode = new ArrayList<>();

        List<String> rawFilters = pkg.getFilters();
        List<String> expandedFilters = expandFilters(jcrSession, rawFilters);
        
        // all filters
        for (String filter : expandedFilters) {
            
            if (!jcrSession.nodeExists(filter)) {
                LOG.info("The node at path `{}` does not exist.", filter);
                continue;
            }
            
            Node node = jcrSession.getNode(filter);
            filterNode.add(node);
        }
        
        return filterNode;
    }
    
    /**
     * Expand a set of filter paths. Usually a path will not have to be manipulated, unless the path
     * ends in an asterisk. This will cause us to read the parent node and find children that starts with
     * whatever came before the asterisk's last path element.
     *
     * @param jcrSession the jcr session to use to check node names with
     * @param rawFilters a list of filters to be expanded
     *
     * @return and expanded list of filters.
     */
    protected List<String> expandFilters(Session jcrSession, List<String> rawFilters) throws RepositoryException {
        
        List<String> result = new ArrayList<>();
        
        for (String path: rawFilters) {
            
            // regular path? just add it back in
            if (!path.endsWith("*")) {
                result.add(path);
                continue;
            }
            
            FilterExpansion expansion = new FilterExpansion(path);
    
            String basePath = expansion.getBasePath();
            String startsWith = expansion.getStartsWith();
            LOG.info("Expecting to iterate over node at basePath: {}", basePath);
            
            // does the node not exist? skip.
            if (!jcrSession.nodeExists(basePath)) {
                LOG.info("Could not find base path for wildcard expression: {}", basePath);
                continue;
            }

            // get parent node
            Node parentNode = jcrSession.getNode(basePath);
            
            // iterate over children
            NodeIterator childrenIt = parentNode.getNodes();
            while (childrenIt.hasNext()) {
                Node child = childrenIt.nextNode();
                String childPath = child.getPath();
                
                LOG.info("Iterating over: {}", childPath);
                
                // need to match something in the node's name? let's make sure it starts with that identifier, otherwise skip
                if (StringUtils.isNotBlank(startsWith) && !child.getName().startsWith(startsWith)) {
                    LOG.info("Skipping over: {}", childPath);
                    continue;
                }
                
                LOG.info("Child at path matches: {}", childPath);
                result.add(childPath);
            }
            
        }
        
        return result;
    }
    
}
