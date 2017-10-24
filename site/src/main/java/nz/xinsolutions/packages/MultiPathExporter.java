package nz.xinsolutions.packages;

import com.google.common.io.Files;
import org.onehippo.cm.ConfigurationService;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 26/09/17
 */
@Component
public class MultiPathExporter {
    
    /**
     * Logger
     */
    private Logger LOG = LoggerFactory.getLogger(MultiPathExporter.class);
    
    /**
     * Export a list of nodes into a temporary folder
     *
     * @param nodesToExport     a list of nodes to export
     *
     * @return
     * @throws RepositoryException
     * @throws IOException
     */
    public File exportZippedContent(List<Node> nodesToExport) throws RepositoryException, IOException {
        
        final File dirToZip = Files.createTempDir();
    
        LOG.info("Trying to create something here: " + dirToZip.getPath());
        
        for (Node node : nodesToExport) {
    
            // create a file
            File partialFile = new File(dirToZip.getCanonicalFile() + "/" + node.getPath().replace("/", "_") + ".zip");
            
            File zipFile = exportZippedContent(node);
            LOG.info(
                "zip file: " + zipFile.getCanonicalPath() + "\n" +
                "moving to: " + partialFile.getCanonicalPath()
            );
    
            zipFile.renameTo(partialFile);
        }
        
        return dirToZip;
    }
    
    
    /**
     * Export the node content
     *
     * @param nodeToExport
     * @return
     *
     * @throws RepositoryException
     * @throws IOException
     */
    protected File exportZippedContent(Node nodeToExport) throws RepositoryException, IOException {
        ConfigurationService configService = HippoServiceRegistry.getService(ConfigurationService.class);
        File zipFile = configService.exportZippedContent(nodeToExport);
        return zipFile;
    }
    
    
}
