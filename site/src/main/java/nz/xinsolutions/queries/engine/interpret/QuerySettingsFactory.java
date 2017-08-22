package nz.xinsolutions.queries.engine.interpret;

import nz.xinsolutions.queries.engine.parse.RuleState;
import nz.xinsolutions.queries.engine.tokenise.TokenElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 21/08/17
 *
 *      This factory is able to create a query settings object
 *
 */
public class QuerySettingsFactory {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(QuerySettingsFactory.class);
    
    /**
     * @return a query settings instance
     */
    public static QuerySettings fromRuleState(RuleState queryState) {
        
        if (CollectionUtils.isEmpty(queryState.subStates)) {
            return null;
        }

        QuerySettings settings = new QuerySettings();
    
        RuleState qExpr = findFirstOf(queryState, "query");
        if (qExpr == null) {
            return null;
        }
        
        RuleState qOffset = findFirstOf(qExpr, "q_offset");
        RuleState qLimit = findFirstOf(qExpr, "q_limit");
        RuleState qSortOrder = findFirstOf(qExpr, "q_sortby");
        RuleState qScope = findFirstOf(qExpr, "q_scope");
        RuleState qType = findFirstOf(qExpr, "q_type");
        
        extractIntegerFromTokens(settings, qOffset, settings::setOffset);
        extractIntegerFromTokens(settings, qLimit, settings::setLimit);
        
        if (qSortOrder != null) {
            String varName = qSortOrder.findToken("varname").getValue();
            String direction = qSortOrder.findToken("order_direction").getValue();
            
            settings.setSortField(varName);
            settings.setSortOrder(direction);
        }
        
        if (qScope != null) {
            List<RuleState> includeStates = qScope.findRules("scope_include");
            List<RuleState> excludeStates = qScope.findRules("scope_exclude");
            
            settings.setIncludeScopes(getValuesFromScopeStates(includeStates));
            settings.setExcludeScopes(getValuesFromScopeStates(excludeStates));
        }
        
        if (qType != null) {
            boolean alsoIncludeSubtypes = qType.findToken("expr_subtypes") != null;

            // make sure the value is a string
            TokenElement typeValue = qType.findToken("value");
            if (typeValue == null || !isStringValue(typeValue.getValue())) {
                throw new IllegalStateException("Query does not specify a type of string value");
            }
            
            
            settings.setBeanName(getStringValue(typeValue.getValue()));
            settings.setUseSubtypes(alsoIncludeSubtypes);
            
        }
        
        settings.setWhereState(findFirstOf(qExpr, "q_where"));
        
        return settings;
        
    }
    
    /**
     * @return the scope values in the set of rules passed in
     */
    protected static List<String> getValuesFromScopeStates(List<RuleState> scopeStates) {
        return scopeStates.stream()
            .map( state -> state.findToken("value").getValue() )
            .filter(QuerySettingsFactory::isStringValue)
            .map(QuerySettingsFactory::getStringValue)
            .collect(Collectors.toList());
    }
    
    private static String getStringValue(String val) {
        return val.substring(1, val.length() - 1).replace("\\'", "'");
    }
    
    private static boolean isStringValue(String val) {
        return val.startsWith("'") && val.endsWith("'");
    }
    
    /**
     * Get the integer value and consume it
     */
    protected static void extractIntegerFromTokens(QuerySettings settings, RuleState state, Consumer<Integer> consume) {
        
        if (state == null) {
            return;
        }
        
        TokenElement valueEl = state.findToken("value");
        if (NumberUtils.isDigits(valueEl.getValue())) {
            int value = Integer.parseInt(valueEl.getValue());
            consume.accept(value);
        }
    }
    
    /**
     * @return the first rule of a specific set of rule types
     */
    protected static RuleState findFirstOf(RuleState queryState, String... rules) {
        List<RuleState> queryExprList = queryState.findRules(rules);
        if (CollectionUtils.isEmpty(queryExprList)) {
            LOG.info("No query rule state found.");
            return null;
        }
        return queryExprList.get(0);
    }
    
}
