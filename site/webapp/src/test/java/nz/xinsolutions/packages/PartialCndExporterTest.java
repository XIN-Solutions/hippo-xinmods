package nz.xinsolutions.packages;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 30/01/18.
 */
public class PartialCndExporterTest {

    @Test
    public void isInterestingNodeType() {

        PartialCndExporter exporter = new PartialCndExporter();
        List<String> types = Arrays.asList("xinmods:", "xin:type");
        assertTrue(exporter.isInterestingNodeType(types, "xin:type"));
        assertFalse(exporter.isInterestingNodeType(types, "xin:untype"));
        assertTrue(exporter.isInterestingNodeType(types, "xinmods:somerandomtype"));
    }
}