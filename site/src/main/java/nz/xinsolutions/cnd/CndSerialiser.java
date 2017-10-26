package nz.xinsolutions.cnd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.Workspace;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 25/10/17
 *
 *  This class is able to serialise a list of cnd entities into a certain format
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
    public String outputToJson(Workspace workspace, List<CndEntity> entities) {
        try {
            ObjectMapper objMap = new ObjectMapper();
            StringWriter strWriter = new StringWriter();
           
            List<CndNamespace> namespaces = uniqueNamespaces(workspace, entities);
            
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
    
    
    /**
     * Output the cnd entities in a format understandable to the cdn importer
     *
     * @param workspace     the workspace to do retrieve namespace uris for
     * @param entities      the entities to transform
     * @return
     */
    public String outputToCndFormat(Workspace workspace, List<CndEntity> entities) {
        List<CndNamespace> namespaces = uniqueNamespaces(workspace, entities);
    
        StringBuilder builder = new StringBuilder();
        
        builder
            .append(namespacesToString(namespaces))
            .append("\n\n\n")
            .append(entitiesToString(entities));
        
        return builder.toString();
    }
    
    /**
     * @return a string of namespace descriptions in the cnd format
     */
    protected String entitiesToString(List<CndEntity> namespaces) {
        return namespaces.stream().map(this::entityToCndString).collect(Collectors.joining("\n\n"));
    }
    
    protected String entityToCndString(CndEntity entity) {
        StringBuilder strBld = new StringBuilder();
        strBld.append(String.format("[%s] > %s\n", entity.getName(), concatSuperTypes(entity)));
        
        if (entity.isOrderable()) {
            strBld.append("  orderable\n");
        }
        
        if (entity.isAbstrakt()) {
            strBld.append("  abstract\n");
        }
        
        if (CollectionUtils.isNotEmpty(entity.getProperties())) {
            strBld.append(
                entity.getProperties()
                    .stream()
                    .map(prop -> String.format(
                        "  - %s (%s) %s",
                        prop.getName(),
                        getPropertyTypeName(prop),
                        prop.isMandatory() ? "mandatory" : (prop.isMultiple() ? "multiple" : "")
                    ))
                    .collect(Collectors.joining("\n"))
            );
        }
    
        if (CollectionUtils.isNotEmpty(entity.getChildNodes())) {
            strBld.append(
                entity.getChildNodes()
                    .stream()
                    .map(prop -> String.format(
                        "  + %s (%s) %s",
                        prop.getName(),
                        getPropertyTypeName(prop),
                        prop.isMandatory() ? "mandatory" : (prop.isMultiple() ? "multiple" : "")
                    ))
                    .collect(Collectors.joining("\n"))
            );
        }

        return strBld.toString();
    }
    
    
    protected String getPropertyTypeName(CndProperty property) {
        if (property.getType() == null) {
            if (ArrayUtils.isNotEmpty(property.getPrimaryTypes())) {
                return Arrays.stream(property.getPrimaryTypes()).collect(Collectors.joining(","));
            } else {
                return "";
            }
        }

        
        switch (property.getType()) {
            case PropertyType.STRING: return "string";
            case PropertyType.DATE: return "date";
            case PropertyType.BINARY: return "binary";
            case PropertyType.DOUBLE: return "double";
            case PropertyType.DECIMAL: return "decimal";
            case PropertyType.LONG: return "long";
            case PropertyType.BOOLEAN: return "boolean";
            case PropertyType.NAME: return "name";
            case PropertyType.PATH: return "path";
            case PropertyType.URI: return "uri";
            case PropertyType.REFERENCE: return "reference";
            case PropertyType.WEAKREFERENCE: return "weakreference";
            default: return "undefined";
        }
    }
    
    protected String concatSuperTypes(CndEntity entity) {
        return entity.getSuperTypes().stream().collect(Collectors.joining(", "));
    }
    
    /**
     * @return a string of namespace descriptions in the cnd format
     */
    protected String namespacesToString(List<CndNamespace> namespaces) {
        return namespaces.stream().map(this::nsToCndString).collect(Collectors.joining("\n"));
    }
    
    protected String nsToCndString(CndNamespace namespace) {
        return String.format("<'%s'='%s'>", namespace.getName(), namespace.getUri());
    }
    
    /**
     * @return the list of unique namespaces to be found in the list of cnd entities
     */
    protected List<CndNamespace> uniqueNamespaces(Workspace workspace, List<CndEntity> cndEntities) {
        
        List<CndNamespace> namespaces = new ArrayList<>();
    
        // collate
        cndEntities.forEach(
            entity -> entity.getReferredNamespaces().forEach(
                ns -> {
                    ns.resolve(workspace);
                    namespaces.add(ns);
                }
            )
        );
    
        // dedupe
        return dedupeNamespaces(namespaces);
    }
    
    
    /**
     * @return a unique of cnd namespaces that are unique from each other
     */
    protected List<CndNamespace> dedupeNamespaces(List<CndNamespace> namespaces) {
        Map<String, CndNamespace> nsMap = new HashMap<String, CndNamespace>();
        namespaces.forEach( ns -> nsMap.put(ns.getName(), ns) );
        return new ArrayList<>(nsMap.values());
    }
    
}
