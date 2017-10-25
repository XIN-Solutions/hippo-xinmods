package nz.xinsolutions.cnd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 25/10/17
 */
public class CndSerialiser {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CndSerialiser.class);
    
    /**
     * Output a list of cnd entities to string and return their json
     *
     * @param entities  the list to write
     * @return
     */
    public static String outputAll(List<CndNamespace> namespaces, List<CndEntity> entities) {
        try {
            ObjectMapper objMap = new ObjectMapper();
            StringWriter strWriter = new StringWriter();
            objMap.writeValue(strWriter, new LinkedHashMap<String, Object>() {{
                put("namespaces", namespaces);
                put("entities", entities);
            }});
            
            return strWriter.toString();
        }
        catch (IOException ioEx) {
            LOG.error("Could not properly serialise the cnd entities");
        }
        return null;
    }
}
