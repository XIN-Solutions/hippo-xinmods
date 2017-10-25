package nz.xinsolutions.packages;

import nz.xinsolutions.cnd.CndEntity;
import nz.xinsolutions.cnd.CndNamespace;
import nz.xinsolutions.cnd.CndSerialiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.*;
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
        List<NodeType> types = getInterestingNodeTypes(workspace, prefix, allTypes);
        List<CndEntity> cndEntities = convertFromNodeTypes(types);
        List<CndNamespace> namespaces = new ArrayList<>();
        
        cndEntities.forEach(
            entity -> entity.getReferredNamespaces().forEach(
                ns -> {
                    ns.resolve(workspace);
                    namespaces.add(ns);
                }
            )
        );
    
        
        String output = CndSerialiser.outputAll(namespaces, cndEntities);

        LOG.info(output);

        return output;
    }
    
    /**
     * @return a list of cnd entity instances based off of a list of node type
     */
    protected List<CndEntity> convertFromNodeTypes(List<NodeType> types) {
        return types.stream().map(CndEntity::fromNodeType).collect(Collectors.toList());
    }
    
    
    /**
     * @return a list of node types that we are interesting in saving.
     */
    protected List<NodeType> getInterestingNodeTypes(Workspace workspace, String prefix, List<String> allTypes) {
        
        try {
            NodeTypeIterator ntIt = workspace.getNodeTypeManager().getAllNodeTypes();
            List<NodeType> interestingNodes = new ArrayList<>();
            while (ntIt.hasNext()) {
                NodeType nodeType = ntIt.nextNodeType();
                
                // make sure this is something we're interested in
                if (!isInterestingNodeType(allTypes, nodeType)) {
                    continue;
                }
                
                interestingNodes.add(nodeType);
            }
            return interestingNodes;
        }
        catch (Exception ex) {
            LOG.error("Could not get CND for prefix `{}`, caused by: ", prefix, ex);
        }
        
        return Collections.EMPTY_LIST;
    }
    
    /**
     * @return true if it's an interesting node type
     */
    protected boolean isInterestingNodeType(List<String> allTypes, NodeType nodeType) {
        return allTypes.contains(nodeType.getName());
    }
    
    
}
