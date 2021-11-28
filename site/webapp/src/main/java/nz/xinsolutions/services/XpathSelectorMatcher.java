package nz.xinsolutions.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *   A class that helps identify whether a recursion breadcrumb through a datastructure
 *   matches a particular xpath subset syntax.
 *
 *      - /from/the/root/exact
 *      - relative/to/anywhere
 *      - /from/the/root//a-random-child
 *
 *   Constraints:
 *
 *      - you cannot have more than one use of '//'
 *      - we don't match on any attributes, only node paths
 *
 */
public class XpathSelectorMatcher {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XpathSelectorMatcher.class);
    public static final int IDX_HEAD_PART = 0;
    public static final int IDX_TAIL_PART = 1;

    /**
     * This is the function that determines whether a particular breadcrumb of node names
     * matches the xpathSelector based on the constraints outlined in the class description.
     *
     * @param pathBreadcrumb    a set of elements with path breadcrumbs
     * @param xpathSelector     the xpath selector string to match against.
     * @return
     */
    public boolean matchesXpathSelector(List<String> pathBreadcrumb, String xpathSelector) {

        if (containsBasePath(xpathSelector)) {
            LOG.debug("Found wildcard selector");
            String startMatch = getMatchHead(xpathSelector);
            String relativeEnd = getMatchTail(xpathSelector);

            if (StringUtils.isNotBlank(startMatch) && !this.isAbsoluteSelector(startMatch)) {
                throw new IllegalArgumentException("When matching on wildcard, base path should be absolute");
            }

            return (
                // if the string starts with // we can just assume the start matches.
                (StringUtils.isBlank(startMatch) || matchesFromStart(pathBreadcrumb, startMatch)) &&
                matchesFromEnd(pathBreadcrumb, relativeEnd)
            );
        }
        else if (isRelativeSelector(xpathSelector)) {
            LOG.debug("Found relative selector");
            return matchesFromEnd(pathBreadcrumb, xpathSelector);
        }
        else if (isAbsoluteSelector(xpathSelector)) {
            LOG.debug("Found absolute selector");
            return completeMatch(pathBreadcrumb, xpathSelector);
        }

        return false;
    }

    /**
     * @return true if the entire xpath selector matches the path breadcrumb
     */
    protected boolean completeMatch(List<String> pathBreadcrumb, String xpathSelector) {

        if (StringUtils.isEmpty(xpathSelector)) {
            return false;
        }

        if (pathBreadcrumb.size() != xpathSelector.split("/").length - 1) {
            return false;
        }

        return this.matchesFromStart(pathBreadcrumb, xpathSelector);
    }


    /**
     * @return true if the path breadcrumb matches from the start
     */
    protected boolean matchesFromStart(List<String> pathBreadcrumb, String startMatch) {
        // expecting the string to start with a '/', if it does, let's strip it.
        String splitThis = startMatch;
        if (startMatch.charAt(0) == '/') {
            splitThis = startMatch.substring(1);
        }

        String[] pathParts = splitThis.split("/");

        // can't possibly match the entire thing if it isn't at least the same size.
        if (pathParts.length > pathBreadcrumb.size()) {
            return false;
        }

        // iterate through all path parts and make sure they equal that in the breadcrumb.
        boolean allMatched = true;
        for (int elIdx = 0; elIdx < pathParts.length; ++elIdx) {
            String pathElement = pathParts[elIdx];
            allMatched &= pathElement.equals("*") || pathBreadcrumb.get(elIdx).equals(pathElement);
        }

        // should be true if all elements were equal, otherwise AND will have made it false.
        return allMatched;
    }

    /**
     * @return true if the end of the breadcrumb matches the xpath selector
     */
    protected boolean matchesFromEnd(List<String> pathBreadcrumb, String xpathSelector) {
        String[] pathParts = xpathSelector.split("/");

        // can't possibly match the entire thing if it isn't at least the same size.
        if (pathParts.length > pathBreadcrumb.size()) {
            return false;
        }

        // iterate through all path parts and make sure they equal that in the breadcrumb.
        boolean allMatched = true;
        for (int elIdx = pathParts.length - 1, revIdx = 1; elIdx >= 0; --elIdx, ++revIdx) {
            String pathElement = pathParts[elIdx];
            int breadcrumbElIdx = pathBreadcrumb.size() - revIdx;
            allMatched &= pathElement.equals("*") || pathBreadcrumb.get(breadcrumbElIdx).equals(pathElement);
        }

        // should be true if all elements were equal, otherwise AND will have made it false.
        return allMatched;
    }

    /**
     * @return the part of the selector that matches pre '//' (the thing that should match the absolute path at the start)
     */
    protected String getMatchHead(String xpathSelector) {
        if (StringUtils.isEmpty(xpathSelector)) {
            throw new IllegalArgumentException("Should not invoke method with empty value.");
        }
        return xpathSelector.split("//")[IDX_HEAD_PART];
    }

    /**
     * @return the part of the selector that matches past '//' (the thing that should match the end of the breadcrumb)
     */
    protected String getMatchTail(String xpathSelector) {
        if (StringUtils.isEmpty(xpathSelector)) {
            throw new IllegalArgumentException("Should not invoke method with empty value.");
        }
        String[] pathParts = xpathSelector.split("//");
        if (pathParts.length > 2) {
            throw new IllegalArgumentException("Do not support multiple wildcard match points.");
        }
        return pathParts[IDX_TAIL_PART];
    }

    /**
     * @return true if the xpathSelector is expecting an absolute path match
     */
    protected boolean isAbsoluteSelector(String xpathSelector) {
        if (StringUtils.isEmpty(xpathSelector)) {
            return false;
        }
        return xpathSelector.charAt(0) == '/';
    }

    /**
     * @return true if the xpathSelector is not expecting an absolute path match
     */
    protected boolean isRelativeSelector(String xpathSelector) {
        if (StringUtils.isEmpty(xpathSelector)) {
            return false;
        }
        return !this.isAbsoluteSelector(xpathSelector);
    }

    /**
     * @return true if the xpath selector has a wildcard path match '//' in the string
     */
    protected boolean containsBasePath(String xpathSelector) {
        if (StringUtils.isEmpty(xpathSelector)) {
            return false;
        }
        return xpathSelector.contains("//");
    }

}
