package nz.xinsolutions.services;

import com.amazonaws.services.dynamodbv2.xspec.L;
import nz.xinsolutions.rest.model.FacetNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.content.beans.standard.HippoDocumentBean;
import org.hippoecm.hst.content.beans.standard.HippoFacetNavigationBean;
import org.hippoecm.hst.content.beans.standard.HippoResultSetBean;
import org.hippoecm.hst.content.beans.standard.facetnavigation.HippoFacetNavigation;
import org.hippoecm.hst.content.beans.standard.facetnavigation.HippoFacetSubNavigation;
import org.hippoecm.hst.restapi.ResourceContextFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 */
public class FacetedNavService {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FacetedNavService.class);

    /**
     * JCR Session
     */
    private Session session;

    /**
     * Resource context factory
     */
    private ResourceContextFactory resourceFactory;

    public FacetedNavService(Session session, ResourceContextFactory resourceFactory) {
        this.session = session;
        this.resourceFactory = resourceFactory;
    }

    /**
     * Get information about a specific faceted node.
     *
     *
     * @param objBeanMgr
     * @param facetPath the facet base path to retrieve something for
     * @param childPath the path inside the facet that we're navigating into.
     *
     * @return the facet node information
     */
    public FacetNode getFacetNodeAtPath(ObjectBeanManager objBeanMgr, String facetPath, String childPath, int offset, int limit, boolean sorted, List<String> fetch) {
        try {
            String completePath = (
                StringUtils.isBlank(childPath)
                    ? facetPath
                    : String.format("%s/%s", facetPath, childPath)
            );

            Object obj = objBeanMgr.getObject(completePath);
            if (!(obj instanceof HippoFacetNavigation)) {
                LOG.error("Did not retrieve a faceted navigation root node.");
                return null;
            }

            HippoFacetNavigation nav = (HippoFacetNavigation) obj;
            FacetNode facetNode = beanToFacetNode(objBeanMgr, nav, offset, limit, sorted, fetch);
            facetNode.setSourceFacet(facetPath);
            facetNode.setFacetPath(childPath);

            return facetNode;
        }
        catch (Exception ex) {
            LOG.error("retrieving the facet node information at {}->{} caused an error.", facetPath, childPath, ex);
        }
        return null;
    }

    /**
     * @return the facet node model representation of the hippo facet navigation bean.
     */
    protected FacetNode beanToFacetNode(ObjectBeanManager objBeanMgr, HippoFacetNavigation bean, int offset, int limit, boolean sorted, List<String> fetch) {
        FacetNode facet = new FacetNode();
        facet.setDisplayName(bean.getDisplayName());

        HippoResultSetBean resultSet = bean.getResultSet();
        int actualOffset = (int) Math.min(offset, resultSet.getCount());
        int actualLimit = (int) Math.min(offset + limit, resultSet.getCount());

        Map<String, Integer> folderMap = createChildFolderMap(bean, sorted);
        facet.setChildFacets(folderMap);

        List<HippoDocumentBean> resultsDocs = resultSet.getDocuments(actualOffset, actualLimit, sorted);
        NodeConversion nodeConversion = new NodeConversion(this.resourceFactory);

        // convert the result set beans into the json representation we're expecting
        // and also prefetch and child nodes if specified by the user.
        List<Map<String, Object>> mappedResults = convertBeansToMaps(objBeanMgr, fetch, resultsDocs, nodeConversion);

        facet.setResults(mappedResults);

        return facet;
    }

    /**
     * @return a map representation of these beans.
     */
    @NotNull
    protected List<Map<String, Object>> convertBeansToMaps(ObjectBeanManager objBeanMgr, List<String> fetch, List<HippoDocumentBean> resultsDocs, NodeConversion nodeConversion) {
        List<Map<String, Object>> mappedResults = (
            resultsDocs.stream()
                .map(nodeConversion::toMap)
                .peek(map -> {

                    // did we have instructions to enrich the results?
                    if (CollectionUtils.isNotEmpty(fetch)) {
                        ReferenceNodeFetcher refFetcher = new ReferenceNodeFetcher(nodeConversion, objBeanMgr);
                        refFetcher.fetchReferencedNodes(map, fetch);
                    }
                })
                .collect(Collectors.toList())
        );
        return mappedResults;
    }

    /**
     * Extract the child facets of the current facet bean.
     * @param bean  the current facet navigation bean
     * @param sorted are we sorting the results? otherwise use normal score.
     * @return a map of sub childnavs and the count of items in it.
     */
    @NotNull
    protected Map<String, Integer> createChildFolderMap(HippoFacetNavigation bean, boolean sorted) {
        List<HippoFacetNavigationBean> folderBeans = (
            bean.getFolders(sorted)
                .stream()
                .map(folder -> (HippoFacetNavigationBean) folder)
                .collect(Collectors.toList())
        );

        // determine the sub maps and their count
        Map<String, Integer> folderMap = new LinkedHashMap<>();
        for (HippoFacetNavigationBean folder : folderBeans) {
            folderMap.put(folder.getName(), folder.getCount().intValue());
        }
        return folderMap;
    }

}
