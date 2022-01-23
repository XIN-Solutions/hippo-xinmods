package nz.xinsolutions.services;

import java.util.List;
import java.util.Map;

/**
 * Author: Marnix Kok
 * <p>
 * Purpose:
 */
@FunctionalInterface
public interface MapFilterCallback {

    /**
     * Called when a path has matched a breadcrumb (the path into a map).
     *
     * @param matchedPath   the path that matched
     * @param breadcrumb    the current breadcrumb that matched the path
     * @param nodeName      the name of the current node
     * @param currentMap    the map of the current node (we can add to this).s
     */
    boolean matchHit(String matchedPath, List<String> breadcrumb, String nodeName, Map<String, Object> currentMap);

}
