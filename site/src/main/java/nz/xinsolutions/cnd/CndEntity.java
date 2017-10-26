package nz.xinsolutions.cnd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.collections.CollectionUtils;

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 25/10/17
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CndEntity implements CndNamespaceReferer {
    
    private String name;
    private boolean mixin;
    private boolean abstrakt;
    private boolean orderable;
    private List<String> superTypes;
    private List<CndProperty> properties;
    private List<CndProperty> childNodes;
    
    /**
     * @return a cnd entity instance from a node type
     */
    public static CndEntity fromNodeType(NodeType nodeType) {
        return new CndEntity() {{
            setName(nodeType.getName());
            setChildNodes(childNodesAsEntities(nodeType.getDeclaredChildNodeDefinitions()));
            setProperties(transformProperties(nodeType.getDeclaredPropertyDefinitions()));
            setSuperTypes(Arrays.asList(nodeType.getDeclaredSupertypeNames()));
            setAbstrakt(nodeType.isAbstract());
            setMixin(nodeType.isMixin());
            setOrderable(nodeType.hasOrderableChildNodes());
        }};
    }
    

    /**
     * @return a list of cndproperty DTOs
     */
    protected static List<CndProperty> transformProperties(PropertyDefinition[] definitions) {
        return (
            Arrays
                .stream(definitions)
                .map(CndProperty::fromPropertyDefinition)
                .collect(Collectors.toList())
        );
    }
    
    
    protected static List<CndProperty> childNodesAsEntities(NodeDefinition[] definitions) {
        return (
            Arrays
                .stream(definitions)
                .map(CndProperty::fromNodeDefinition)
                .collect(Collectors.toList())
        );
    }
    
    
    
    /**
     * @return a list of all cnd namespaces
     */
    @Override
    @JsonIgnore
    public List<CndNamespace> getReferredNamespaces() {
        List<CndNamespace> nsList = new ArrayList<>();
        
        // from type name
        nsList.add(CndNamespace.fromType(this.name));
        
        // super types have types too you know!
        if (CollectionUtils.isNotEmpty(this.getSuperTypes())) {
            nsList.addAll(
                this.getSuperTypes().stream()
                    .map(CndNamespace::fromType)
                    .collect(Collectors.toList())
            );
        }
        
        getProperties()
            .stream()
            .map( prop -> prop.getReferredNamespaces() )
            .forEach( list -> nsList.addAll(list) );
        
        getChildNodes()
            .stream()
            .map( child -> child.getReferredNamespaces() )
            .forEach( list -> nsList.addAll(list) );
        
        return nsList;
    }
    
    
    // ---------------------------------------------------------------------------------
    //      Accessors
    // ---------------------------------------------------------------------------------
    
    
    public boolean isOrderable() {
        return orderable;
    }
    
    public void setOrderable(boolean orderable) {
        this.orderable = orderable;
    }
    
    public boolean isMixin() {
        return mixin;
    }
    
    public void setMixin(boolean mixin) {
        this.mixin = mixin;
    }
    
    public boolean isAbstrakt() {
        return abstrakt;
    }
    
    public void setAbstrakt(boolean abstrakt) {
        this.abstrakt = abstrakt;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getSuperTypes() {
        return superTypes;
    }
    
    public void setSuperTypes(List<String> superTypes) {
        this.superTypes = superTypes;
    }
    
    public List<CndProperty> getProperties() {
        return properties;
    }
    
    public void setProperties(List<CndProperty> properties) {
        this.properties = properties;
    }
    
    public List<CndProperty> getChildNodes() {
        return childNodes;
    }
    
    public void setChildNodes(List<CndProperty> childNodes) {
        this.childNodes = childNodes;
    }
    
}
