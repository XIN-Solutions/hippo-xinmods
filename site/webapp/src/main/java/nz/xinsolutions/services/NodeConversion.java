package nz.xinsolutions.services;

import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.restapi.NodeVisitor;
import org.hippoecm.hst.restapi.ResourceContext;
import org.hippoecm.hst.restapi.ResourceContextFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jcr.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
     * Convert a jcr node to a normal Java map representation.
     * @param bean the hippo bean we're converting
     * @return the map representation.
     */
    public Map<String, Object> toMap(HippoBean bean) {

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

            NodeVisitor visitor = context.getVisitor(jcrNode);
            visitor.visit(context, jcrNode, map);

            return map;
        }
        catch (RepositoryException rEx) {
            LOG.error("Could not create a resource context, caused by: ", rEx);
        }

        return null;

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
            Property summaryValue = childNode.getProperty(HIPPOSTD_STATE_SUMMARY);


            if (stateValue.getString().equals(STATE_PUBLISHED) && summaryValue.getString().equals(SUMMARY_LIVE)) {
                return childNode;
            }
        }

        return null;
    }

}
