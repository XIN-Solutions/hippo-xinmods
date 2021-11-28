package nz.xinsolutions.services;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Purpose:
 *
 *      To do some testing on the xpath selector matcher class.
 *
 */
public class XpathSelectorMatcherTest {

    /**
     * Matcher instance to interrogate
     */
    private XpathSelectorMatcher matcher = new XpathSelectorMatcher();

    @Test
    public void testMatchesXpathSelector() {
        List<String> breadcrumb = Arrays.asList("item", "model", "component", "image", "thumbnail", "link");

        // -- absolute

        // 1. should match entire path
        assertTrue(matcher.matchesXpathSelector(breadcrumb, "/item/model/component/image/thumbnail/link"));

        // 2. should match entire path
        assertTrue(matcher.matchesXpathSelector(breadcrumb, "/item/model/component/image/*/link"));

        // 3. should not match on partial path
        assertFalse(matcher.matchesXpathSelector(breadcrumb, "/item/model/component/image/thumbnail"));

        // 4. shouldn't match a path that doesn't exist
        assertFalse(matcher.matchesXpathSelector(breadcrumb, "/ite/model/component/image/thumbnail"));

        // -- relative
        // 1. should match relative for entire path
        assertTrue(matcher.matchesXpathSelector(breadcrumb, "item/model/component/image/thumbnail/link"));
        // 2. should match relative for just a part of it
        assertTrue(matcher.matchesXpathSelector(breadcrumb, "image/thumbnail/link"));
        // 3. shouldn't work if not all elements are there
        assertFalse(matcher.matchesXpathSelector(breadcrumb, "image/thumbnail"));

        // -- wildcard match
        // 1. should match for a few partials
        assertTrue(matcher.matchesXpathSelector(breadcrumb, "/item/model//thumbnail/link"));
        assertTrue(matcher.matchesXpathSelector(breadcrumb, "//thumbnail/link"));
        assertTrue(matcher.matchesXpathSelector(breadcrumb, "/item/model/component/image//thumbnail/link"));
        // 2. shouldn't match partial node names
        assertFalse(matcher.matchesXpathSelector(breadcrumb, "/item/mode//thumbnail/link"));
        assertFalse(matcher.matchesXpathSelector(breadcrumb, "/untrue/item/model//thumbnail/link"));
    }

    @Test
    public void testAbsoluteSelector() {
        assertTrue(matcher.isAbsoluteSelector("/my/absolute/selector"));
        assertFalse(matcher.isAbsoluteSelector("my/relative/selector"));
        assertFalse(matcher.isAbsoluteSelector(""));
        assertFalse(matcher.isAbsoluteSelector("relative-selector"));
    }

    @Test
    public void testRelativeSelector() {
        assertTrue(matcher.isRelativeSelector("my/relative/selector"));
        assertTrue(matcher.isRelativeSelector("relative-selector"));
        assertFalse(matcher.isRelativeSelector(""));
        assertFalse(matcher.isRelativeSelector("/my/absolute/selector"));
    }

    @Test
    public void testWildcardExpressionTester() {
        assertTrue(matcher.containsBasePath("//thumbnail/link"));
        assertFalse(matcher.containsBasePath("my/relative/selector"));
        assertTrue(matcher.containsBasePath("/item/model/component/image//thumbnail/link"));
    }

    @Test
    public void testWildcardExtraction() {
        String path = "/item/model/component/image//thumbnail/link";
        assertEquals("/item/model/component/image", matcher.getMatchHead(path));
        assertEquals("thumbnail/link", matcher.getMatchTail(path));
    }

    @Test
    public void testCompleteMatch() {
        List<String> breadcrumb = Arrays.asList("item", "model", "component", "image", "thumbnail", "link");
        String path = "/item/model/component/image/thumbnail/link";
        assertTrue(matcher.completeMatch(breadcrumb, path));

        path = "/item/model/component/image/thumbnail/link/too-long";
        assertFalse(matcher.completeMatch(breadcrumb, path));

        path = "item/model/component/image/thumbnail/link/";
        assertFalse(matcher.completeMatch(breadcrumb, path));

        path = "/item_incorrect/model/component/image/thumbnail/link";
        assertFalse(matcher.completeMatch(breadcrumb, path));
    }


    @Test
    public void testMatchesFromEnd() {
        List<String> breadcrumb = Arrays.asList("item", "model", "component", "image", "thumbnail", "link");

        String path = "image/thumbnail/link";
        assertTrue(matcher.matchesFromEnd(breadcrumb, path));

        path = "image/thumbnail/link-incorrect";
        assertFalse(matcher.matchesFromEnd(breadcrumb, path));

        path = "image/thumbnail-incorrect/link";
        assertFalse(matcher.matchesFromEnd(breadcrumb, path));

        path = "image/link";
        assertFalse(matcher.matchesFromEnd(breadcrumb, path));

    }

}