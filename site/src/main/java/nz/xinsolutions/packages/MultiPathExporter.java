package nz.xinsolutions.packages;

import com.github.fge.filesystem.MoreFiles;
import com.github.fge.filesystem.RecursionMode;
import org.onehippo.cm.ConfigurationService;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 26/09/17
 */
@Component
public class MultiPathExporter {

    private static final String HCM_CONTENT = "hcm-content";
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
    public File exportZippedContent(List<Node> nodesToExport) throws RepositoryException, IOException, PackageException {
        
        final File dirToZip = com.google.common.io.Files.createTempDir();
    
        LOG.info("Trying to create something here: " + dirToZip.getPath());
        
        for (Node node : nodesToExport) {
    
            // create a file
            File partialFile = new File(dirToZip.getCanonicalFile() + "/" + node.getPath().replace("/", "_") + ".zip");
            
            File zipFile = exportZippedContent(node);
            LOG.info(
                "zip file: " + zipFile.getCanonicalPath() + "\n" +
                "moving to: " + partialFile.getCanonicalPath()
            );
            

            //
            // move the folders into hcm-content
            //
            try (FileSystem zipFs = getZipFilesystem(zipFile)) {
                List<Path> folders = getFolderNames(zipFs);
                createRootFolder(zipFs, HCM_CONTENT);
                moveFolders(zipFs, folders, HCM_CONTENT);
            }
            catch (IOException ioEx) {
                LOG.error("Something went wrong manipulating the zip fs, caused by: ", ioEx);
                throw new PackageException("Could not properly export content, aborting.");
            }

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


    /**
     * @return a zip based filesystem to operate on
     * @throws IOException
     */
    protected FileSystem getZipFilesystem(File zipFile) throws IOException {

        Map<String, String> env = new HashMap<>();
        env.put("create", "false");
        URI uri = URI.create("jar:file:" + zipFile.getAbsolutePath());

        return FileSystems.newFileSystem(uri, env);
    }

    /**
     * Create a new root-folder
     * @param zipFs         is the zip filesystem
     * @param folderName    is the new folder to create
     */
    protected void createRootFolder(FileSystem zipFs, String folderName) throws IOException {
        Path newDirPath = zipFs.getPath("/" + folderName);
        Files.createDirectory(newDirPath);
    }

    /**
     * Move all the folders into <code>allInto</code>.
     *
     * @param zipFs     is the zip filesystem to work with
     * @param folders   are the list of folders to move
     * @param allInto   the folder name to move it into
     * @throws IOException
     */
    protected void moveFolders(FileSystem zipFs, List<Path> folders, String allInto) throws IOException {
        LOG.info("Moving all folders into `{}`", allInto);


        for (Path folder : folders) {

            Path folderName = folder.getName(0);
            Path source = zipFs.getPath("/" + folderName);
            Path destination = zipFs.getPath("/" + allInto + "/" + folderName);

            // move by copying and delete.
            MoreFiles.copyRecursive(source, destination, RecursionMode.FAIL_FAST, StandardCopyOption.ATOMIC_MOVE);
            MoreFiles.deleteRecursive(source, RecursionMode.KEEP_GOING);
        }
    }

    /**
     * @return a list of folder names
     */
    protected List<Path> getFolderNames(FileSystem zipFs) throws IOException {
        // get root folder
        Path rootFolder =
            StreamSupport
                .stream(zipFs.getRootDirectories().spliterator(), false)
                .collect(Collectors.toList())
                .get(0);

        // get directories in /
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(rootFolder, Files::isDirectory)) {
            return (
                StreamSupport
                    .stream(dirStream.spliterator(), false)
                    .collect(Collectors.toList())
            );
        }
        catch (IOException ioEx) {
            LOG.error("Could not get root folders, caused by: ", ioEx);
            return null;
        }

    }

}
