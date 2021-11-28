package nz.xinsolutions.services;

import com.amazonaws.services.dynamodbv2.xspec.M;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      To recurse through a map of objects (potentially other maps) and
 *      hand off to a functional interface once a particular xpath selector triggers
 *      the current path.
 *
 */
public class XpathMapFilter {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XpathMapFilter.class);

    /**
     * Start visiting a map
     *
     * @param map            the map to get through
     * @param pathSelectors  the selectors we have to match
     * @param callback       a callback when something hits
     */
    public void visitMap(Map<String, Object> map, List<String> pathSelectors, MapFilterCallback callback) {
        this.visitMap(map, pathSelectors, new ArrayList<>(), callback);
    }

    /**
     * Start visiting a map
     *
     * @param map            the map to get through
     * @param pathSelectors  the selectors we have to match
     * @param callback       a callback when something hits
     */
    public void visitMap(Map<String, Object> map, List<String> pathSelectors, List<String> breadcrumb, MapFilterCallback callback) {

        XpathSelectorMatcher matcher = new XpathSelectorMatcher();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            // create new list with additional breadcrumb for this entry
            List<String> scopedBreadcrumb = new ArrayList<>(breadcrumb);
            scopedBreadcrumb.add(entry.getKey());

            LOG.debug("visiting: {} => {}", entry.getKey(), scopedBreadcrumb);

            // match the breadcrumb against all path selectors
            String firstMatchedSelector = (
                pathSelectors
                    .stream()
                    .filter(selector -> matcher.matchesXpathSelector(scopedBreadcrumb, selector))
                    .findAny()
                    .orElse(null)
            );

            // something matched? execute callback so we can "enrich" the node.
            if (firstMatchedSelector != null) {
                callback.matchHit(firstMatchedSelector, scopedBreadcrumb, entry.getKey(), map);
            }

            // should we step into the child node?
            if (entry.getValue() instanceof Map) {
                this.visitMap((Map) entry.getValue(), pathSelectors, scopedBreadcrumb, callback);
            }

            // if it's a list value, let's find any maps inside.
            if (entry.getValue() instanceof Collection) {

                int elIdx = 0;

                // iterate over elements
                for (Object valElement : (Collection) entry.getValue()) {

                    // map? recurse.
                    if (valElement instanceof Map) {

                        // create new list with additional breadcrumb for this entry (adds current index integer)
                        List<String> listElBreadcrumb = new ArrayList<>(scopedBreadcrumb);
                        listElBreadcrumb.add(String.format("%d", elIdx));
                        this.visitMap((Map) valElement, pathSelectors, listElBreadcrumb, callback);

                        ++elIdx;
                    }

                }
            }
        }
    }

}
