package nz.xinsolutions.queries;

import nz.xinsolutions.queries.engine.parse.ParseRuleFactory.Rule;
import nz.xinsolutions.queries.engine.parse.ParseRuleSet;
import nz.xinsolutions.queries.engine.tokenise.TokenSet;

import static nz.xinsolutions.queries.engine.parse.ParseRuleFactory.Token;
import static nz.xinsolutions.queries.engine.parse.ParseRuleFactory.rule;
import static nz.xinsolutions.queries.engine.tokenise.TokenFactory.regex;
import static nz.xinsolutions.queries.engine.tokenise.TokenFactory.text;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 *
 *      Query parser for prefix jcr sql strings
 *
 */
public class QueryParser {
    
    private TokenSet tokenSet;
    private ParseRuleSet ruleSet;
    
    /**
     * Initialise data-members
     */
    public QueryParser() {
        
        this.tokenSet = initialiseTokenSet();
        this.ruleSet = initialiseRules();
        
    }
    
    /**
     * Initialise the rule set
     * @return a set of rules
     */
    protected ParseRuleSet initialiseRules() {
        return new ParseRuleSet(
            
            rule("whitespace", Token.m("ws")),
           
            rule("query",
                Token.m("expr_start"), Token.o("ws"),
                    Token.m("expr_query"), Token.o("ws"),
                        Rule.o("q_offset"), Token.o("ws"),
                        Rule.o("q_limit"), Token.o("ws"),
                        Rule.o("q_scope"), Token.o("ws"),
                        Rule.m("q_where"), Token.o("ws"),
                        Rule.o("q_sortby"), Token.o("ws"),
                Token.m("expr_stop")
            ),
            
            rule("q_offset",
                Token.m("expr_start"), Token.o("ws"),
                    Token.m("expr_offset"), Token.o("ws"),
                    Token.m("value"), Token.o("ws"),
                Token.m("expr_stop")
            ),
    
            rule("q_limit",
                Token.m("expr_start"), Token.o("ws"),
                Token.m("expr_limit"), Token.o("ws"),
                Token.m("value"), Token.o("ws"),
                Token.m("expr_stop")
            ),

            rule("q_scope",
                Token.m("expr_start"), Token.o("ws"),
                Token.m("expr_scopes"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                    Rule.o("scope_include", "scope_exclude"), Token.o("ws"),
                Token.m("expr_stop")
            ),
            
            rule("scope_include",
                Token.m("expr_start"), Token.o("ws"),
                    Token.m("scope_include"), Token.o("ws"),
                    Token.m("value"), Token.o("ws"),
                Token.m("expr_stop")
            ),
    
            rule("scope_exclude",
                Token.m("expr_start"), Token.o("ws"),
                    Token.m("scope_exclude"), Token.o("ws"),
                    Token.m("value"), Token.o("ws"),
                Token.m("expr_stop")
            ),

            
            rule("q_sortby",
                Token.m("expr_start"), Token.o("ws"),
                    Token.m("expr_sortby"), Token.o("ws"),
                    Token.m("prop_start"), Token.m("varname"), Token.m("prop_end"), Token.o("ws"),
                    Token.m("order_direction"), Token.o("ws"),
                Token.m("expr_stop")
            ),
            
            rule("q_where",
                Token.m("expr_start"), Token.o("ws"),
                    Token.m("op_where"), Token.o("ws"),
                    Rule.m("expr_unary_cmp", "expr_binary_cmp", "expr_compound"), Token.o("ws"),
                Token.m("expr_stop")
            ),
            
            rule("expr_compound",
                
                Token.m("expr_start"),      // (
                Token.o("ws"),
                Token.m("op_compound"),     // and,or
                Token.o("ws"),
    
                // at least two expressions
                Rule.m("expr_unary_cmp", "expr_binary_cmp", "expr_compound"), Token.o("ws"),
                Rule.m("expr_unary_cmp", "expr_binary_cmp", "expr_compound"), Token.o("ws"),
                
                // rest would be optional
                Rule.o("expr_unary_cmp", "expr_binary_cmp", "expr_compound"), Token.o("ws"),
                Rule.o("expr_unary_cmp", "expr_binary_cmp", "expr_compound"), Token.o("ws"),
                Rule.o("expr_unary_cmp", "expr_binary_cmp", "expr_compound"), Token.o("ws"),
                Rule.o("expr_unary_cmp", "expr_binary_cmp", "expr_compound"), Token.o("ws"),
                Rule.o("expr_unary_cmp", "expr_binary_cmp", "expr_compound"), Token.o("ws"),
                Rule.o("expr_unary_cmp", "expr_binary_cmp", "expr_compound"), Token.o("ws"),
                
                Token.m("expr_stop")        // )
                
            ),
            
            rule("expr_unary_cmp",
                
                Token.m("expr_start"), Token.o("ws"),
                Token.m("op_unary"), Token.o("ws"),
                Token.m("prop_start"), Token.m("varname"), Token.m("prop_end"),
                Token.m("expr_stop")
                
            ),

            rule("expr_binary_cmp",
    
                Token.m("expr_start"), Token.o("ws"),   // (
                    Token.m("op_binary"), Token.o("ws"),           // notnull|null
                    
                    // [varname]
                    Token.m("prop_start"),
                        Token.m("varname"),
                    Token.m("prop_end"), Token.o("ws"),
                    
                    // 10 'string' $variable
                    Token.m("value"), Token.o("ws"),
                
                Token.m("expr_stop")        // )
                
            )
        );
    }
    
    
    /**
     * @return the token set
     */
    protected TokenSet initialiseTokenSet() {
        return new TokenSet(
            regex("ws", "[\\s]+"),
             text("expr_start", "("),
             text("expr_stop", ")"),
            
             text("expr_query", "query"),
             text("expr_offset", "offset"),
             text("expr_limit", "limit"),
             text("expr_scopes", "scopes"),
             text("expr_sortby", "sortby"),
            
             text("scope_include", "include"),
             text("scope_exclude", "exclude"),
            
            regex("order_direction", "asc|ascending|desc|descending"),
            
             text("op_where", "where"),
            regex("op_unary", "null|notnull|where"),
            regex("op_compound", "and|or"),
            regex("op_binary", "contains|<|>|<=|>=|="),
            
             text("prop_start", "["),
             text("prop_end", "]"),
            
            regex("varname", "[A-Za-z]+:[A-Za-z]+"),
            regex("value", "'[^']*?'|[0-9]+|true|false|\\$[A-Za-z][A-Za-z0-9]*")
        
        );
    }
    
    
}
