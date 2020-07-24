package nz.xinsolutions.packages;

import nz.xinsolutions.cnd.CndEntity;
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
    public String exportCnds(Workspace workspace, String[] typeNames) {
        List<NodeType> types = getInterestingNodeTypes(workspace, Arrays.asList(typeNames));
        List<CndEntity> cndEntities = convertFromNodeTypes(types);
    
        // output
        CndSerialiser serialiser = new CndSerialiser();
        return serialiser.outputToJson(workspace, cndEntities);
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
    protected List<NodeType> getInterestingNodeTypes(Workspace workspace, List<String> allTypes) {
        
        try {
            NodeTypeIterator ntIt = workspace.getNodeTypeManager().getAllNodeTypes();
            List<NodeType> interestingNodes = new ArrayList<>();
            while (ntIt.hasNext()) {
                NodeType nodeType = ntIt.nextNodeType();
                
                // make sure this is something we're interested in
                if (!isInterestingNodeType(allTypes, nodeType.getName())) {
                    continue;
                }
                
                interestingNodes.add(nodeType);
            }
            return interestingNodes;
        }
        catch (Exception ex) {
            LOG.error("Could not get CND, caused by: ", ex);
        }
        
        return Collections.EMPTY_LIST;
    }
    
    /**
     * @return true if it's an interesting node type
     */
    protected boolean isInterestingNodeType(List<String> allTypes, String nodeTypeName) {

        if (allTypes.contains(nodeTypeName)) {
            return true;
        }

        for (String typeDesc: allTypes) {
            // wildcard specific
            if (typeDesc.endsWith(":") && nodeTypeName.startsWith(typeDesc)) {
                return true;
            }
        }

        return false;
    }
    
    
}
