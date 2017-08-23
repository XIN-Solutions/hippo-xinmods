package nz.xinsolutions.queries.engine.interpret;

import nz.xinsolutions.queries.engine.parse.RuleState;
import nz.xinsolutions.queries.engine.tokenise.TokenElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.exceptions.FilterException;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 22/08/17
 *
 *      Create an hst instance from a query settings instance
 *
 */
public class HstQueryGenerator {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HstQueryGenerator.class);
    
    /**
     * Create an hst query for the settings object that was pushed in
     *
     * @param qMgr      is the query manager we're using to setup
     * @param settings  query settings instance
     * @param queryParams
     * @return is the hst query instance
     */
    public static HstQuery createQueryFromSettings(HstQueryManager qMgr, QuerySettings settings, MultivaluedMap<String, String> queryParams) {
    
        try {
            HstQuery query = qMgr.createQuery((Node) null, settings.isUseSubtypes(), settings.getBeanName());
            
            // set limit
            if (settings.getLimit() != null) {
                query.setLimit(settings.getLimit());
            }
            
            // set offset
            if (settings.getOffset() != null) {
                query.setOffset(settings.getOffset());
            }

            // add scopes as setup
            query.addScopes(getScopeBeans(settings.getIncludeScopes(), qMgr, settings));
            query.excludeScopes(getScopeBeans(settings.getExcludeScopes(), qMgr, settings));

            // is sort field filled out? what direction? set it.
            if (StringUtils.isNotEmpty(settings.getSortField())) {
                if (settings.getSortOrder().startsWith("a")) {
                    query.addOrderByAscending(settings.getSortField());
                }
                else if (settings.getSortOrder().startsWith("d")) {
                    query.addOrderByDescending(settings.getSortField());
                }
            }

            // have some where clause?
            if (settings.getWhereState() != null) {
                Filter filter = query.createFilter();
                fromWhereState(settings.getWhereState(), query, queryParams, filter);
                query.setFilter(filter);
            }
            
            LOG.info("Prepared JCR query: " + query.getQueryAsString(true));
            
            return query;
        }
        catch (Exception ex) {
            LOG.error("Could not create hstQuery, caused by: ", ex);
            throw new IllegalStateException(ex);
        }
        
    }
    
    /**
     * Create a query filter from the rule state
     *
     * @param queryParams
     * @param filter
     */
    protected static void fromWhereState(RuleState whereState, HstQuery query, MultivaluedMap<String, String> queryParams, Filter filter) throws FilterException {

        for (RuleState subState : whereState.subStates) {
            
            switch (subState.ruleName) {
                
                case "expr_compound": {
                    TokenElement compound = subState.findToken("op_compound");
                    switch (compound.getValue()) {
                        
                        case "and":
                            Filter andFilter = query.createFilter();
                            fromWhereState(subState, query, queryParams, andFilter);
                            filter.addAndFilter(andFilter);
                            break;
                            
                        case "or":
                            Filter orFilter = query.createFilter();
                            fromWhereState(subState, query, queryParams, orFilter);
                            filter.addOrFilter(orFilter);
                            break;
    
                    }
                    break;
                }
                
                case "expr_binary_cmp": {
                    TokenElement binary = subState.findToken("op_binary");
                    TokenElement property = subState.findToken("varname");
                    TokenElement value = subState.findToken("value");

                    Object sanitised = sanitise(value, queryParams);
                    String propertyName = property.getValue();
                    
                    switch (binary.getValue()) {
                        case "contains":
                            filter.addContains(propertyName, sanitised.toString());
                            break;
                            
                        case "!contains":
                            filter.addNotContains(propertyName, sanitised.toString());
                            
                        case ">":
                            filter.addGreaterThan(propertyName, sanitised);
                            break;
                            
                        case ">=":
                            filter.addGreaterOrEqualThan(propertyName, sanitised);
                            break;
                            
                        case "<":
                            filter.addLessThan(propertyName, sanitised);
                            break;
                            
                        case "<=":
                            filter.addLessOrEqualThan(propertyName, sanitised);
                            break;
                        
                        case "=":
                            filter.addEqualTo(propertyName, sanitised);
                            break;
                            
                        case "!=":
                            filter.addNotEqualTo(propertyName, sanitised);
                            break;
                            
                        case "i=":
                            filter.addEqualToCaseInsensitive(propertyName, sanitised.toString());
                            break;
                            
                        case "i!=":
                            filter.addNotEqualToCaseInsensitive(propertyName, sanitised.toString());
                            break;
                    }
                    break;
                }
                
                case "expr_unary_cmp":

                    TokenElement unary = subState.findToken("op_unary");
                    TokenElement property = subState.findToken("varname");
                    String propertyValue = property.getValue();
                    
                    switch (unary.getValue()) {
                        
                        case "null": {
                            filter.addIsNull(propertyValue);
                            break;
                        }
                        
                        case "notnull": {
                            filter.addNotNull(propertyValue);
                            break;
                        }
                    }
                    
                    break;
                
            }
        }
        
    }
    
    /**
     * @return the sanitised value
     */
    protected static Object sanitise(TokenElement value, MultivaluedMap<String, String> queryParams) {
        if (ValueHelper.isStringValue(value.getValue())) {
            return ValueHelper.getStringValue(value.getValue());
        }
        else if (NumberUtils.isDigits(value.getValue())) {
            return Integer.parseInt(value.getValue());
        }
        else if ("false".equals(value.getValue())) {
            return false;
        }
        else if ("true".equals(value.getValue())) {
            return true;
        }
        else if (queryParams != null && value.getValue().length() > 1 && value.getValue().startsWith("$")) {
            String paramValue = queryParams.getFirst(value.getValue().substring(1));
            TokenElement fakeToken = new TokenElement("", String.format("'%s'", paramValue));
            return sanitise(fakeToken, null);
        }
        
        return value.getValue();
    }
    
    
    /**
     * @return the list of hippo beans for a list of paths
     */
    protected static List<HippoBean> getScopeBeans(List<String> scopes, HstQueryManager qMgr, QuerySettings settings) {
        if (CollectionUtils.isEmpty(scopes)) {
            return Collections.EMPTY_LIST;
        }
        return scopes.stream()
            .map(inclPath -> getBeanFromPath(qMgr, inclPath))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * @return the hippo bean for a path <code>inclPath</code>
     */
    protected static HippoBean getBeanFromPath(HstQueryManager qMgr, String inclPath) {
        try {
            return (HippoBean) qMgr.getObjectConverter().getObject(qMgr.getSession(), inclPath);
        }
        catch (ObjectBeanManagerException obmEx) {
            LOG.error("Can't get bean at path: ", obmEx);
        }
        return null;
    }
    
    /**
     * @return the scope path
     */
    protected static String getMainScope(QuerySettings settings) {
        List<String> scopes = settings.getIncludeScopes();
        if (CollectionUtils.isEmpty(scopes)) {
            return "/content/documents";
        }
        else {
            return scopes.get(0);
        }
    }
    
}
