package nz.xinsolutions.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.restapi.content.linking.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Purpose:
 *
 *      To match against a set of node path selectors and load references that are eligible for fetching, then
 *      push them into the payload map at that point.
 *
 */
public class ReferenceNodeFetcher {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceNodeFetcher.class);

    public static final String KEY_UUID = "id";
    public static final String PROPERTY_REF = "ref";
    public static final String PROPERTY_LINK_TYPE = "type";
    public static final String PROPERTY_LINK_ID = "id";
    public static final String PROPERTY_LINK_URL = "url";

    /**
     * Object bean manager instance we use retrieve hippo beans
     */
    private final ObjectBeanManager objBeanMgr;

    /**
     * The node conversion instance to use.
     */
    private final NodeConversion nodeConversion;

    /**
     * Initialise data-members
     *
     * @param nodeConversion    helper bean for converting hippo beans into maps
     * @param objBeanMgr        the object bean manager for the JCR session to get hippo documents with.
     */
    public ReferenceNodeFetcher(NodeConversion nodeConversion, ObjectBeanManager objBeanMgr) {
        this.nodeConversion = nodeConversion;
        this.objBeanMgr = objBeanMgr;
    }

    /**
     * Fetch nodes for 'link'-type elements highlighted by the path selectors. This
     * function does not return any value as it operates directly on the map.
     *
     * @param docMap the map to traverse
     * @param pathSelectors the path selectors to match on
     */
    public void fetchReferencedNodes(Map<String, Object> docMap, List<String> pathSelectors) {
        XpathMapFilter mapFilter = new XpathMapFilter();
        mapFilter.visitMap(docMap, pathSelectors, this::fetchForMatch);
    }

    /**
     * This is a callback function invoked by `fetchReferencedNodes` when any of the path selectors
     * matches the structure of the document map passed into it. This method then goes and tries to
     * figure out whether the matched bit is a map that has a `uuid` field. If it does, a hippobean is retrieved
     * and inserted into the map.
     *
     * @param matchedPath   the path that triggered invocation of this function
     * @param breadcrumb    the current path breadcrumb
     * @param nodeName      the name of the node we've matched against
     * @param currentMap    the map that has an entry named `nodeName` we can work on.
     */
    protected void fetchForMatch(String matchedPath, List<String> breadcrumb, String nodeName, Map<String, Object> currentMap) {
        try {
            Object node = currentMap.get(nodeName);
            if (!(node instanceof Link.LocalLink)) {
                LOG.debug("Although we matched something, it is not a map, so can't fetch information for it, skipping.");
                return;
            }

            Link.LocalLink link = (Link.LocalLink) node;

            // find matching bean
            HippoBean bean = (HippoBean) this.objBeanMgr.getObjectByUuid(link.id);

            if (bean == null) {
                LOG.info("Could not find a hippo bean with uuid `{}`, skipping.", link.id);
                return;
            }

            Map<String, Object> linkMap = new LinkedHashMap<>();
            linkMap.put(PROPERTY_LINK_TYPE, link.type);
            linkMap.put(PROPERTY_LINK_ID, link.id);
            linkMap.put(PROPERTY_LINK_URL, link.url);

            Map<String, Object> refNodeConversion = nodeConversion.toMap(bean);
            linkMap.put(PROPERTY_REF, refNodeConversion);

            currentMap.put(nodeName, linkMap);
        }
        catch (Exception ex) {
            LOG.error("Could not retrieve the referenced bean. Caused by: ", ex);
        }
    }


}
