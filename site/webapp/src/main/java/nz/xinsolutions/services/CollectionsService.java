package nz.xinsolutions.services;

import nz.xinsolutions.rest.model.CollectionsWriteRequest;
import nz.xinsolutions.rest.model.ItemProperty;
import nz.xinsolutions.rest.model.ItemSaveMode;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.util.Text;
import org.hippoecm.hst.restapi.ResourceContextFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *      A number of services that help us interact with collections in the repository.
 *
 */
public class CollectionsService {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CollectionsService.class);

    /**
     * Where does collections root node live?
     */
    public static final String PATH_COLLECTIONS_ROOT = "/content/collections";

    /**
     * Primary type of hippo folder
     */
    public static final String TYPE_FOLDER = "hippostd:folder";
    public static final String TYPE_HIPPO_HANDLE = "hippo:handle";

    /**
     * JCR session
     */
    private Session session;

    /**
     * Resource context factory
     */
    private ResourceContextFactory resourceContextFactory;

    /**
     * Initialise data-members
     *
     * @param session   the jcr session to use for collections
     * @param resourceContextFactory the context factory required for node conversion
     */
    public CollectionsService(Session session, ResourceContextFactory resourceContextFactory) {
        this.session = session;
        this.resourceContextFactory = resourceContextFactory;
    }

    /**
     * List all collection names.
     *
     * @return a list of collection
     * @throws RepositoryException
     */
    public List<String> listCollections() throws RepositoryException {

        if (hasCollectionsRootNode()) {
            LOG.debug("Collection root node does not exist");
            return Collections.emptyList();
        }

        // get collection root
        Node collRoot = getCollectionsRootNode();
        NodeIterator nIterator = collRoot.getNodes();

        List<String> collectionNames = new ArrayList<>();

        while (nIterator.hasNext()) {
            Node item = nIterator.nextNode();

            // if it's a folder, list it.
            if (item.getPrimaryNodeType().getName().equals(TYPE_FOLDER)) {
                collectionNames.add(item.getName());
            }
        }

        return collectionNames;
    }


    /**
     * Get a map of values from the node at `collection` and `path`
     *
     * @param collection    is the collection to look inside
     * @param path          is the path to get the item from
     * @return a map with value responses.
     */
    public Map<String, Object> getItemAtPath(String collection, String path) throws RepositoryException {

        String itemPath = getItemContentPath(collection, path);

        if (!this.session.nodeExists(itemPath)) {
            LOG.info("There is no node at that path: {}", itemPath);
            return null;
        }

        Node itemNode = this.session.getNode(itemPath);
        String primaryType = itemNode.getPrimaryNodeType().getName();
        if (!primaryType.equals(TYPE_HIPPO_HANDLE)) {
            LOG.info("The node is not of the handle type.");
            return null;
        }

        Node liveNode = itemNode.getNode(itemNode.getName());
        NodeConversion nodeConversion = new NodeConversion(this.resourceContextFactory);
        return nodeConversion.toCollectionMap(liveNode, true);
    }



    /**
     * Write item values
     *
     * @param collection    the collection to write to
     * @param path          the path to write to
     * @param writeRequest  the requests to write
     * @return
     */
    public boolean writeItemValues(String collection, String path, CollectionsWriteRequest writeRequest) throws RepositoryException {
        if (!this.collectionExists(collection)) {
            throw new IllegalArgumentException("Collection does not exist: " + collection);
        }

        String itemFolderPath = getItemPath(collection, path);
        String itemContentPath = getItemContentPath(collection, path);

        boolean folderNodeExists = this.session.nodeExists(itemFolderPath);
        boolean contentNodeExists = this.session.nodeExists(itemContentPath);

        // does content node exist and not allowed to override?
        if (contentNodeExists && writeRequest.getSaveMode() == ItemSaveMode.FailIfExists) {
            throw new IllegalArgumentException("Item exists, instructed to fail if found.");
        }

        // get the folder node
        Node folderNode = (
            folderNodeExists
                ? this.session.getNode(itemFolderPath)
                : ensureFolderExists(itemFolderPath)
        );

        if (!folderNodeExists) {
            this.session.save();
        }

        // if overwrite and content node exists, remove it so it can be recreated.
        if (contentNodeExists && writeRequest.getSaveMode() == ItemSaveMode.Overwrite) {
            this.session.getNode(itemContentPath).remove();
            contentNodeExists = false;
            this.session.save();
        }

        Node contentNode = (
            contentNodeExists
                ? this.session.getNode(itemContentPath + "/jcr:content")
                : createContentNode(folderNode)
        );

        contentNode.setProperty("hippostdpubwf:lastModifiedBy", "admin");
        contentNode.setProperty("hippostdpubwf:lastModificationDate", Calendar.getInstance());
        contentNode.setProperty("hippostdpubwf:publicationDate", Calendar.getInstance());

        // iterate over incoming values and write them to the node
        for (Map.Entry<String, ItemProperty> entry : writeRequest.getValues().entrySet()) {

            String propertyName = "xinmods:" + Text.escapeIllegalJcrChars(entry.getKey());
            ItemProperty itemProp = entry.getValue();
            String value = itemProp.getValue().toString();

            // depending on the incoming property type push into content Node
            switch (itemProp.getType()) {

                case Boolean:
                    contentNode.setProperty(propertyName, Boolean.parseBoolean(value));
                    break;

                case String:
                    contentNode.setProperty(propertyName, value);
                    break;

                case Long:
                    contentNode.setProperty(propertyName, Long.parseLong(value));
                    break;

                case Double:
                    contentNode.setProperty(propertyName, Double.parseDouble(value));
                    break;


                case Date:
                    try {
                        ZonedDateTime dateTime = ZonedDateTime.parse(value);
                        GregorianCalendar cal = GregorianCalendar.from(dateTime);
                        contentNode.setProperty(propertyName, cal);
                    }
                    catch (DateTimeParseException dtpEx) {
                        LOG.error("Could not parse date `{}`, caused by: ", value, dtpEx);
                    }
                    break;

            }

        }

        session.save();

        return true;
    }

    /**
     * Create a content node hippo:handle and xinmods:collectionitem live version.
     *
     * @param folderNode the parent folder in which a jcr:content node should be created.
     * @return the new content node.
     */
    protected Node createContentNode(Node folderNode) throws RepositoryException {

        Node handle = folderNode.addNode("jcr:content", "hippo:handle");

        handle.addMixin("mix:referenceable");
        handle.addMixin("hippo:named");
        handle.addMixin("hippostd:relaxed");

        handle.setProperty("hippo:name", folderNode.getName());

        Node content = handle.addNode("jcr:content", "xinmods:collectionitem");
        content.addMixin("mix:referenceable");
        content.addMixin("hippostd:container");
        content.addMixin("hippostd:publishableSummary");
        content.addMixin("hippostd:relaxed");
        content.addMixin("hippostdpubwf:document");

        content.setProperty("hippo:availability", new String[] {"live"});
        content.setProperty("hippostd:state", "published");
        content.setProperty("hippostd:stateSummary", "live");
        content.setProperty("hippostdpubwf:createdBy", "admin");
        content.setProperty("hippostdpubwf:creationDate", Calendar.getInstance());
        content.setProperty("hippostdpubwf:lastModificationDate", Calendar.getInstance());
        content.setProperty("hippostdpubwf:lastModifiedBy", "admin");
        content.setProperty("hippostdpubwf:publicationDate", Calendar.getInstance());

        // iterate from folder node until root node.
        // setup hippo:paths on jcr:content/jcr:content node
        Node current = folderNode;
        List<String> allPaths = new ArrayList<>();

        while (true) {
            allPaths.add(current.getIdentifier());

            if (current.isSame(session.getRootNode())) {
                break;
            }
            current = current.getParent();
        }

        // write hippo paths property
        content.setProperty("hippo:paths", allPaths.toArray(new String[0]));

        return content;
    }

    /**
     * This method makes sure a folder exists. If one of the path elements doesn't exist, it will
     * make a new hippostd:folder node.
     *
     * @param itemFolderPath the folder path that should be created.
     * @return the node for the folder path that was just created.
     */
    protected Node ensureFolderExists(String itemFolderPath) throws RepositoryException {
        List<String> itemElements = (
            Arrays.stream(itemFolderPath.split("/"))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList())
        ) ;

        Node current = this.session.getRootNode();
        for (String pathElement : itemElements) {
            if (current.hasNode(pathElement)) {
                current = current.getNode(pathElement);
                continue;
            }

            Node childNode =
                current.addNode(
                    Text.escapeIllegalJcrChars(pathElement),
                    "hippostd:folder"
                );

            childNode.addMixin("hippostd:relaxed");
            current = childNode;
        }

        return current;
    }

    /**
     * Remove an item at a path. If there are other folders at that path, we must have 'forceDelete' set
     * to true, otherwise it won't work
     *
     * @param collection
     * @param path
     * @param forceDelete
     * @return
     */
    public boolean removeItemsAtPath(String collection, String path, boolean forceDelete) throws RepositoryException {
        if (!this.collectionExists(collection)) {
            LOG.info("Collection with name '{}' does not exist.", collection);
            return false;
        }

        String itemFolderPath = getItemPath(collection, path);
        String itemContentPath = getItemContentPath(collection, path);

        // make sure the folder to the content exists.
        if (!session.nodeExists(itemFolderPath)) {
            LOG.info("The path '{}' in collection '{}' does not exist.", path, collection);
            return false;
        }

        // if content node path doesn't exist, and we're not forcing to delete a tree, go back.
        if (!session.nodeExists(itemContentPath) && !forceDelete) {
            LOG.info("There is no value at '{}' path and no force delete selected, won't write.", itemContentPath);
            return false;
        }

        // get folder container node
        Node folderNode = session.getNode(itemFolderPath);
        boolean hasChildFolders = hasChildFolders(folderNode);

        if (hasChildFolders && !forceDelete) {
            LOG.info("Found child folders at `{}`, but not allowed to recursively delete", itemFolderPath);
            return false;
        }

        folderNode.remove();
        session.save();

        return true;
    }


    /**
     * @return true if <code>folderNode</code> has other folders as children.
     * @throws RepositoryException
     */
    protected boolean hasChildFolders(Node folderNode) throws RepositoryException {

        NodeIterator childNodeIt = folderNode.getNodes();

        while (childNodeIt.hasNext()) {
            Node childNode = childNodeIt.nextNode();
            if (childNode.getPrimaryNodeType().getName().equals(TYPE_FOLDER)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if <code>name</code> exists as a collection.
     * @throws RepositoryException
     */
    public boolean collectionExists(String name) throws RepositoryException {
        return this.listCollections().contains(name);
    }

    /**
     * @return the collections root node
     * @throws RepositoryException
     */
    public Node getCollectionsRootNode() throws RepositoryException {
        return this.session.getNode(PATH_COLLECTIONS_ROOT);
    }

    /**
     * @return true if a root node exists
     * @throws RepositoryException
     */
    public boolean hasCollectionsRootNode() throws RepositoryException {
        return !session.nodeExists(PATH_COLLECTIONS_ROOT);
    }

    /**
     * @return the path to the item's parent folder path
     */
    protected String getItemPath(String collection, String path) {
        return PATH_COLLECTIONS_ROOT + "/" + collection + "/" + path;
    }

    /**
     * @return the path to the content node of an item
     */
    protected String getItemContentPath(String collection, String path) {
        return PATH_COLLECTIONS_ROOT + "/" + collection + "/" + path + "/jcr:content";
    }

}
