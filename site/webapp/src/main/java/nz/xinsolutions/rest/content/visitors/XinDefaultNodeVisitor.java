package nz.xinsolutions.rest.content.visitors;

import org.hippoecm.hst.restapi.ResourceContext;
import org.hippoecm.hst.restapi.content.visitors.DefaultNodeVisitor;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Map;

public class XinDefaultNodeVisitor extends DefaultNodeVisitor {
    
    @Override
    protected void visitNode(ResourceContext context, Node node, Map<String, Object> response) throws RepositoryException {
        super.visitNode(context, node, response);
        response.put("id", node.getIdentifier());
        response.put("path", node.getPath());
    }
}
