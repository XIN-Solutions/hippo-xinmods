package nz.xinsolutions.queries;

import nz.xinsolutions.queries.engine.QueryParserException;
import nz.xinsolutions.queries.engine.parse.ParseRuleSet;
import nz.xinsolutions.queries.engine.parse.RuleMatching;
import nz.xinsolutions.queries.engine.parse.RuleState;
import nz.xinsolutions.queries.engine.tokenise.TokenElement;
import nz.xinsolutions.queries.engine.tokenise.TokenSet;
import nz.xinsolutions.queries.engine.tokenise.Tokeniser;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 20/08/17
 */
public class QueryParserTest {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(QueryParserTest.class);
    
    /*
    
            This tests that we can do the types of queries we want to be doing:
            
            (query
                (offset 10)
                (limit 10)
                (scopes
                    (include '/content/documents/xin')
                    (include '/content/documents/configuration')
                    (exclude '/content/documents/xin/secret')
                )
                (where
                    (and
                        (= [mods:onsale] true)
                        (or
                            (> [mods:price] 100)
                            (<= [mods:price] 1000)
                        )
                    )
                )
                (sortby [xinmods:publishedDate] desc)
            )
    
     */
    
    public static final String EXPR_SIMPLE = "(< [mods:price] 10)";
    public static final String QUERY_SIMPLE =
        "(where\n" +
            "                    (and\n" +
            "                        (= [mods:onsale] true)\n" +
            "                        (or\n" +
            "                            (> [mods:price] 100)\n" +
            "                            (<= [mods:price] 1000)\n" +
            "                        )\n" +
            "                    )\n" +
            "                )";
    
    public static final String QUERY_COMPLETE =
        "             (query\n" +
        "                (type with-subtypes 'xinmods:page')\n" +
        "                (offset 100)\n" +
        "                (limit 10)\n" +
        "                (scopes\n" +
        "                    (include '/content/documents/xin')\n" +
        "                    (include '/content/documents/configuration')\n" +
        "                    (exclude '/content/documents/xin/secret')\n" +
        "                )\n" +
        "                (where\n" +
        "                    (and\n" +
        "                        (= [mods:onsale] true)\n" +
        "                        (or\n" +
        "                            (> [mods:price] 100)\n" +
        "                            (<= [mods:price] 1000)\n" +
        "                        )\n" +
        "                    )\n" +
        "                )\n" +
        "                (sortby [xinmods:publishedDate])\n" +
        "            )";
    
    QueryParser parser = new QueryParser();
    
    
    @Test public void testQueryHstGenerator() {
        try {
            QueryParser qParser = new QueryParser();
            qParser.createFromString(null, QUERY_COMPLETE, new MultivaluedHashMap<>());
        }
        catch (QueryParserException e) {
            LOG.error("Something happened, caused by: ", e);
        }
    }
    
    @Test public void testAllTokens() {
        TokenSet tokenSet = parser.initialiseTokenSet();
        
        Tokeniser tksr = new Tokeniser(tokenSet);
        List<TokenElement> elements = tksr.tokenise(QUERY_COMPLETE);
        Assert.assertNotNull(elements);
        
        
    }
    
    @Test
    public void testTokeniser() {
        TokenSet tokenSet = parser.initialiseTokenSet();
        ParseRuleSet rules = parser.initialiseRules();
//
//        RuleState state = RuleMatching.parseString(rules, tokenSet, EXPR_SIMPLE, "expr_binary_cmp");
//        Assert.assertNotNull(state);
//
        RuleState state = RuleMatching.parseString(rules, tokenSet, QUERY_COMPLETE, "whitespace", "query");
        Assert.assertNotNull(state);

    }
    
}