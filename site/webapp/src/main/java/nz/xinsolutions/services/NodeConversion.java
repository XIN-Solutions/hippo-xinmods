package nz.xinsolutions.services;

import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.restapi.NodeVisitor;
import org.hippoecm.hst.restapi.ResourceContext;
import org.hippoecm.hst.restapi.ResourceContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Marnix Kok <marnix@elevate.net.nz>
 * <p>
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
        Node jcrNode = bean.getNode();
        try {

            ResourceContext context =
                resFactory.createResourceContext(Collections.emptyList(), true);

            Map<String, Object> map = new LinkedHashMap<>();

            map.put("id", getUuid(bean));
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

}
