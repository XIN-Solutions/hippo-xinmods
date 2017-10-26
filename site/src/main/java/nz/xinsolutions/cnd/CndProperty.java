package nz.xinsolutions.cnd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang.ArrayUtils;

import javax.jcr.nodetype.NodeDefinition;
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
public class CndProperty implements CndNamespaceReferer {
    
    private String name;
    private String[] primaryTypes;
    private Integer type;
    private boolean childNodes;
    private boolean multiple;
    private boolean mandatory;
    
    public static CndProperty fromPropertyDefinition(PropertyDefinition property) {
        return new CndProperty() {{
            setType(property.getRequiredType());
            setMandatory(property.isMandatory());
            setMultiple(property.isMultiple());
            setChildNodes(false);
            setName(property.getName());
        }};
    }
    
    /**
     * @return the cnd entity for a child node
     */
    public static CndProperty fromNodeDefinition(NodeDefinition nodeDefinition) {
        return new CndProperty() {{
            setName(nodeDefinition.getName());
            setPrimaryTypes(nodeDefinition.getRequiredPrimaryTypeNames());
            setChildNodes(true);
            setMultiple(nodeDefinition.allowsSameNameSiblings());
        }};
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public List<CndNamespace> getReferredNamespaces() {
        List<CndNamespace> namespaces = new ArrayList<CndNamespace>();
        
        if (this.name.contains(":")) {
            namespaces.add(CndNamespace.fromType(this.name));
        }
        
        // add possible namespaces from supertypes
        if (ArrayUtils.isNotEmpty(this.primaryTypes)) {
            namespaces.addAll(
                Arrays.stream(this.primaryTypes)
                    .map(CndNamespace::fromType)
                    .collect(Collectors.toList()
                )
            );
        }
        
        
        return namespaces;
    }
    
    // --------------------------------------------------------
    //      Accessors
    // --------------------------------------------------------
    
    
    public String[] getPrimaryTypes() {
        return primaryTypes;
    }
    
    public void setPrimaryTypes(String[] primaryTypes) {
        this.primaryTypes = primaryTypes;
    }
    
    public void setType(Integer type) {
        this.type = type;
    }
    
    public Integer getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isMultiple() {
        return multiple;
    }
    
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
    
    public boolean isChildNodes() {
        return childNodes;
    }
    
    public void setChildNodes(boolean childNodes) {
        this.childNodes = childNodes;
    }
}
