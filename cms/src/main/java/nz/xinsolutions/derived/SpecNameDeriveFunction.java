package nz.xinsolutions.derived;

import org.apache.jackrabbit.value.StringValue;
import org.hippoecm.repository.ext.DerivedDataFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Value;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *  To test derive functions.
 *
 */
public class SpecNameDeriveFunction extends DerivedDataFunction {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(SpecNameDeriveFunction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Value[]> compute(Map<String, Value[]> map) {
        LOG.info("Called");
        Map<String, Value[]> propVals = new LinkedHashMap<>();
        propVals.put("specoutput", new Value[] { new StringValue("calculated") });
        propVals.put("specoutput2", new Value[] { new StringValue("calculated2") });
        return propVals;
    }

}
