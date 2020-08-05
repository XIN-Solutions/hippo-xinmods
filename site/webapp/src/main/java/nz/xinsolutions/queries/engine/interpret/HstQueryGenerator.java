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
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static nz.xinsolutions.queries.engine.interpret.CompoundBehaviour.*;

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
                query.setFilter(filter);
                fromWhereState(settings.getWhereState(), query, queryParams, filter, Toplevel);
            }

            LOG.info("Prepared JCR query: " + query.getQueryAsString(true));

            return query;
        }
        catch (Exception ex) {
            LOG.error("Could not create hstQuery, caused by: ", ex);
            throw new IllegalStateException(ex);
        }

    }

    protected static Object typeWrapped(Object val, String type) {
        if (type == null) {
            return val;
        }

        if (type.equalsIgnoreCase("date")) {
            ZonedDateTime dateTime = ZonedDateTime.parse(val.toString());
            return GregorianCalendar.from(dateTime);
        }

        return val;
    }

    /**
     * Create a query filter from the rule state.
     * Rather confusing nesting behaviours can be found here: https://documentation.bloomreach.com/14/library/concepts/search/nesting-hstquery-filters.html
     *
     * @param queryParams
     * @param filter
     */
    protected static void fromWhereState(RuleState whereState, HstQuery query, MultivaluedMap<String, String> queryParams, Filter filter, CompoundBehaviour cBehaviour) throws FilterException {

        for (RuleState subState : whereState.subStates) {

            switch (subState.ruleName) {

                case "expr_compound": {
                    TokenElement compound = subState.findToken("op_compound");
                    switch (compound.getValue()) {

                        case "and":
                            Filter andFilter = query.createFilter();
                            fromWhereState(subState, query, queryParams, andFilter, And);
                            if (cBehaviour == Or) {
                                filter.addOrFilter(andFilter);
                            } else {
                                filter.addAndFilter(andFilter);
                            }
                            break;

                        case "or":
                            Filter orFilter = query.createFilter();
                            fromWhereState(subState, query, queryParams, orFilter, Or);

                            if (cBehaviour == Or) {
                                filter.addOrFilter(orFilter);
                            } else {
                                filter.addAndFilter(orFilter);
                            }
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
                    String type = null;
                    if (propertyName.contains("@")) {
                        String[] splitVar = propertyName.split("@");
                        type = splitVar[1];
                        propertyName = splitVar[0];
                    }

                    Filter activeFilter = query.createFilter();

                    switch (binary.getValue()) {

                        case "contains":
                            activeFilter.addContains(propertyName, sanitised.toString());
                            break;

                        case "!contains":
                            activeFilter.addNotContains(propertyName, sanitised.toString());
                            break;

                        case ">":
                        case "gt":
                            activeFilter.addGreaterThan(propertyName, typeWrapped(sanitised, type));
                            break;

                        case ">=":
                        case "gte":
                            activeFilter.addGreaterOrEqualThan(propertyName, typeWrapped(sanitised, type));
                            break;

                        case "<":
                        case "lt":
                            activeFilter.addLessThan(propertyName, typeWrapped(sanitised, type));
                            break;

                        case "<=":
                        case "lte":
                            activeFilter.addLessOrEqualThan(propertyName, typeWrapped(sanitised, type));
                            break;

                        case "=":
                        case "eq":
                            activeFilter.addEqualTo(propertyName, typeWrapped(sanitised, type));
                            break;

                        case "!=":
                        case "neq":
                            activeFilter.addNotEqualTo(propertyName, typeWrapped(sanitised, type));
                            break;

                        case "i=":
                        case "ieq":
                            activeFilter.addEqualToCaseInsensitive(propertyName, sanitised.toString());
                            break;

                        case "i!=":
                        case "ineq":
                            activeFilter.addNotEqualToCaseInsensitive(propertyName, sanitised.toString());
                            break;
                    }

                    LOG.info("Behaviour: {}", cBehaviour);
                    if (cBehaviour == Or) {
                        filter.addOrFilter(activeFilter);
                    } else {
                        filter.addAndFilter(activeFilter);
                    }
                    break;
                }

                case "expr_unary_cmp":

                    TokenElement unary = subState.findToken("op_unary");
                    TokenElement property = subState.findToken("varname");
                    String propertyValue = property.getValue();

                    Filter activeFilter = query.createFilter();

                    switch (unary.getValue()) {

                        case "null": {
                            activeFilter.addIsNull(propertyValue);
                            break;
                        }

                        case "notnull": {
                            activeFilter.addNotNull(propertyValue);
                            break;
                        }
                    }

                    LOG.info("Behaviour: {}", cBehaviour);
                    if (cBehaviour == Or) {
                        filter.addOrFilter(activeFilter);
                    } else {
                        filter.addAndFilter(activeFilter);
                    }
                    break;

            }
        }

        LOG.info(".. {}", filter.getJcrExpression());
        
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
