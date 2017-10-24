package nz.xinsolutions.packages;

import org.hippoecm.repository.util.JcrCompactNodeTypeDefWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 19/10/17
 *
 *      Partial CDN Exporter is able to use the existing CDN Export functionality and
 *      reduce the entries that are exported.
 *
 */
@Component
public class PartialCndExporter {
    
    private static final Logger LOG = LoggerFactory.getLogger(PartialCndExporter.class);
    
    /**
     * @return a list of sanitised CDN descriptions
     */
    public List<String> exportCnds(Workspace workspace, String[] types) {
        Set<String> namespaces = getPrefixes(types);
        List<String> cdnExports = getFullExports(workspace, namespaces, Arrays.asList(types));
        
        return cdnExports;
    }
    
    
    /**
     * @return a set of namespaces used in the types we're interested in
     */
    protected Set<String> getPrefixes(String[] types) {
        return Arrays.stream(types)
                .filter(type -> type.indexOf(":") != -1)
                .map(type -> type.substring(0, type.indexOf(":")))
                .collect(Collectors.toSet())
            ;
    }
    
    /**
     * @return a list of full exports for a set of namespaces
     */
    protected List<String> getFullExports(Workspace workspace, Set<String> namespaces, List<String> allTypes) {
        return namespaces
                .stream()
                .map(prefix -> toFullCnd(workspace, prefix, allTypes))
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
            ;
    }
    
    /**
     * @return a cnd export for a namespace
     */
    protected String toFullCnd(Workspace workspace, String prefix, List<String> allTypes) {
        try {
            return JcrCompactNodeTypeDefWriter.compactNodeTypeDef(workspace, prefix);
        }
        catch (RepositoryException | IOException ex) {
            LOG.error("Could not get CND for prefix `{}`, caused by: ", prefix, ex);
        }
        return null;
    }
    
    
}
