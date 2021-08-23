package nz.xinsolutions.services;

import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.restapi.NodeVisitor;
import org.hippoecm.hst.restapi.ResourceContext;
import org.hippoecm.hst.restapi.ResourceContextFactory;
import org.hippoecm.hst.restapi.content.visitors.HippoPublicationWorkflowDocumentVisitor;
import org.hippoecm.repository.util.PropertyIterable;
import org.jetbrains.annotations.NotNull;
import org.onehippo.cms7.services.contenttype.ContentType;
import org.onehippo.cms7.services.contenttype.ContentTypeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.*;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *  To convert a JCR node into a map that can be added to the response. It will use the same logic
 *  as the hst-restapi dependency's DocumentsResource implementation.
 */
public class NodeConversion {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(NodeConversion.class);

    public static final String HIPPOSTD_STATE = "hippostd:state";
    public static final String HIPPOSTD_STATE_SUMMARY = "hippostd:stateSummary";

    public static final String STATE_PUBLISHED = "published";
    public static final String SUMMARY_LIVE = "live";

    /**
     * Resource factory
     */
    private ResourceContextFactory resFactory;

    /**
     * Initialise data-members
     * @param resFactory
     */
    public NodeConversion(ResourceContextFactory resFactory) {
        this.resFactory = resFactory;
    }


    /**
     * @return the uuid for a bean
     */
    public String getUuid(HippoBean bean) {
        if (bean instanceof HippoDocument) {
          return ((HippoDocument) bean).getCanonicalHandleUUID();
        }
        return bean.getCanonicalUUID();
    }


    /**
     * Convert a jcr node to a normal Java map representation, using the normal node visitor.
     *
     * @param bean the hippo bean we're converting
     * @return the map representation.
     */
    public Map<String, Object> toMap(HippoBean bean) {
        try {
            boolean isCollectionItem = bean.getNode().getPrimaryNodeType().getName().equals("xinmods:collectionitem");
            return this.toMap(bean, isCollectionItem);
        }
        catch (RepositoryException rEx) {
            LOG.info("Could not determine primary type, converting like normal");
            return this.toMap(bean, false);
        }
    }


    /**
     * Convert a jcr node to a normal Java map representation.
     *
     * @param jcrNode the node we're converting
     * @param fullExport if set to true, a visitor shim is used that does not require a property
     *                   to be part of the content type as declared in the document type.
     * @return the map representation.
     */
    public Map<String, Object> toCollectionMap(Node jcrNode, boolean fullExport) {

        try {

            Node parentNode = jcrNode.getParent().getParent();

            ResourceContext context =
                resFactory.createResourceContext(Collections.emptyList(), true);

            NodeVisitor visitor =
                fullExport
                    ? getShimmedHippoDocumentVisitor()
                    : context.getVisitor(jcrNode)
                ;

            Map<String, Object> map = new LinkedHashMap<>();
            visitor.visit(context, jcrNode, map);

            Map<String, Object> values = (Map<String, Object>) map.get("items");

            Map<String, Object> finalMap = new LinkedHashMap<>();

            finalMap.put("_id", jcrNode.getIdentifier());
            finalMap.put("_name", parentNode.getName());
            finalMap.put("_path", parentNode.getPath());

            // iterate over all the elements and remove the namespace
            for (Map.Entry<String, Object> entry: values.entrySet()) {

                String keyName = entry.getKey();

                // remove namespace if exists
                if (entry.getKey().indexOf(":") != -1) {
                    keyName = entry.getKey().split(":")[1];
                }


                finalMap.put(keyName, entry.getValue());
            }

            finalMap.remove("availability");

            return finalMap;
        }
        catch (RepositoryException rEx) {
            LOG.error("Could not create a resource context, caused by: ", rEx);
        }

        return null;
    }

    /**
     * Convert a jcr node to a normal Java map representation.
     *
     * @param bean the hippo bean we're converting
     * @param fullExport if set to true, a visitor shim is used that does not require a property
     *                   to be part of the content type as declared in the document type.
     * @return the map representation.
     */
    public Map<String, Object> toMap(HippoBean bean, boolean fullExport) {

        try {

            Node jcrNode;

            if (bean instanceof HippoDocument) {
                HippoDocument doc = (HippoDocument) bean;
                jcrNode = findPublishedDocument(doc);
                if (jcrNode == null) {
                    return null;
                }
            }
            else {
                jcrNode = bean.getNode();
            }

            ResourceContext context =
                resFactory.createResourceContext(Collections.emptyList(), true);

            Map<String, Object> map = new LinkedHashMap<>();

            map.put("id", getUuid(bean));
            map.put("name", bean.getName());
            map.put("displayName", bean.getDisplayName());
            map.put("path", bean.getNode().getParent().getPath());

            NodeVisitor visitor =
                fullExport
                    ? getShimmedHippoDocumentVisitor()
                    : context.getVisitor(jcrNode)
                ;

            visitor.visit(context, jcrNode, map);

            return map;
        }
        catch (RepositoryException rEx) {
            LOG.error("Could not create a resource context, caused by: ", rEx);
        }

        return null;
    }

    @NotNull
    private HippoPublicationWorkflowDocumentVisitor getShimmedHippoDocumentVisitor() {
        return new HippoPublicationWorkflowDocumentVisitor() {

            /**
             * Override the visitProperties implementation to no longer ignore properties that aren't explicitly
             * declared on the content type. Instead process it, because it's probably been added with a reason.
             *
             * @param context   the resource context
             * @param node      the node we're visiting
             * @param response  the response object we're inserting into.
             *
             * @throws RepositoryException
             */
            protected void visitProperties(final ResourceContext context, final Node node, final Map<String, Object> response)
                throws RepositoryException {
                for (Property property : new PropertyIterable(node.getProperties())) {
                    final ContentType parentContentType = context.getContentTypes().getContentTypeForNode(property.getParent());
                    String propertyName = property.getName();
                    final ContentTypeProperty propertyType = parentContentType.getProperties().get(propertyName);

                    // either property type not found in cache, or defined and not a inherited item, or in the list of skippable properties.
                    if (isCustomNamespace(propertyName) && (
                            propertyType == null ||
                            (!propertyType.isDerivedItem() && !skipProperty(context, propertyType, property)
                        )))
                    {
                        visitProperty(context, propertyType, property, response);
                    }
                }
            }
        };
    }

    protected boolean isCustomNamespace(String propertyName) {
        if (propertyName.indexOf(":") == -1) {
            return true;
        }
        List<String> skipThese = Arrays.asList("hippo", "hippostd", "hippostdpubwf", "jcr");
        String ns = propertyName.split(":")[0];

        return !skipThese.contains(ns);
    }

    /**
     * Find one of the hippo document's variations that is set to be the published version.
     *
     * @param doc   the hippo document to interrogate the nodes of
     * @return the node of the published version, or null if not found
     * @throws RepositoryException
     */
    protected Node findPublishedDocument(HippoDocument doc) throws RepositoryException {

        Session session = doc.getNode().getSession();
        Node parentNode = session.getNodeByIdentifier(doc.getCanonicalHandleUUID());

        NodeIterator nIt = parentNode.getNodes();
        while (nIt.hasNext()) {
            Node childNode = nIt.nextNode();

            if (!childNode.hasProperty(HIPPOSTD_STATE) || !childNode.hasProperty(HIPPOSTD_STATE_SUMMARY)) {
                continue;
            }

            Property stateValue = childNode.getProperty(HIPPOSTD_STATE);

            if (stateValue.getString().equals(STATE_PUBLISHED)) {
                return childNode;
            }
        }

        return null;
    }

}
