package nz.xinsolutions.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Author: Marnix Kok
 * <p>
 * Purpose:
 */
public class XpathMapFilterTest {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XpathMapFilterTest.class);

    private int hits = 0;

    /**
     * Test the visit mechanism.
     */
    @Test
    public void visitMap() {

        Map<String, Object> testMap = loadMap();
        assert testMap != null;

        hits = 0;
        XpathMapFilter mapFilter = new XpathMapFilter();
        mapFilter.visitMap(
            testMap,
            Arrays.asList(
                "embedLinks/*/items/link",
                "embedLinks/*/items/thumbnail"
            ),
            (matchedPath, breadcrumb, nodeName, currentMap) -> {
                LOG.info("Matched breadcrumb: {}\nNode name: {}", breadcrumb, nodeName);
                ++hits;
                return true;
            }
        );

        assertTrue(hits > 0);

    }

    /**
     * @return the test map with realistic json
     */
    private Map<String, Object> loadMap() {
        try {
            ObjectMapper objMapper = new ObjectMapper();
            return objMapper.readValue(this.getClass().getResourceAsStream("/xpath/test.json"), Map.class);
        }
        catch (Exception ex) {
            LOG.error("Could not read test map, caused by: ", ex);
            return null;
        }
    }
}