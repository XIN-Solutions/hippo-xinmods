package nz.xinsolutions.derived;

import groovy.lang.GroovyCodeSource;
import org.hippoecm.frontend.session.PluginUserSession;
import org.hippoecm.repository.ext.DerivedDataFunction;
import org.onehippo.repository.update.GroovyUpdaterClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.Map;

/*

    Simple example of a derive function script:

        import nz.xinsolutions.derived.DeriveFunction
        import org.apache.jackrabbit.value.*;

        class ExampleDerive implements DeriveFunction {

            boolean derive(Session jcrSession, Node document) {
                println "Document: ${document.path}"

                document.setProperty("xinmods:teststring", "test")
                document.setProperty("xinmods:anotherstring", 10.5)
            }

        }

 */

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *  To run a groovy script to do more complex derivation logic based on an actual node and session.
 */
public class XinGroovyDeriveFunction extends DerivedDataFunction {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XinGroovyDeriveFunction.class);

    public static final String PN_UUID = "uuid";
    public static final String PN_SCRIPT = "script";

    /**
     * Where to look for scripts.
     */
    public static final String PATH_DERIVE_NODE = "/hippo:configuration/hippo:modules/xinmods/hippo:moduleconfig/derivatives";
    public static final String PN_CACHE_SCRIPT = "cacheScript";

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Value[]> compute(Map<String, Value[]> map) {

        if (!hasUuidKey(map)) {
            LOG.error("Expected `uuid` field in derivative declaration.");
            return null;
        }

        try {
            String uuid = getUuidValue(map);
            Session jcrSession = PluginUserSession.get().getJcrSession();
            Node docNode = jcrSession.getNodeByIdentifier(uuid);

            LOG.info("Processing derivative script for: {}", docNode.getPath());

            String primaryType = docNode.getPrimaryNodeType().getName();
            String typePath = PATH_DERIVE_NODE + "/" + primaryType;

            Node xmDerive = jcrSession.getNode(typePath);
            if (xmDerive == null) {
                LOG.debug("No derive node found at: {}", typePath);
                return null;
            }

            if (!xmDerive.hasProperty(PN_SCRIPT)) {
                LOG.debug("No derive script for node with primary type: {}", primaryType);
                return null;
            }

            String scriptContents = xmDerive.getProperty(PN_SCRIPT).getString();
            boolean cacheEnabled = xmDerive.hasProperty(PN_CACHE_SCRIPT) && xmDerive.getProperty(PN_CACHE_SCRIPT).getBoolean();

            try (GroovyUpdaterClassLoader classLoader = GroovyUpdaterClassLoader.createClassLoader()) {

                GroovyCodeSource gcs = new GroovyCodeSource(scriptContents, "derive_" + primaryType, "/xinmods/derive");
                Class<? extends DeriveFunction> deriveClass = classLoader.parseClass(gcs, cacheEnabled);
                if (!DeriveFunction.class.isAssignableFrom(deriveClass)) {
                    LOG.error("Derive class should implement nz.xinsolutions.derived.DeriveFunction interface.");
                    return null;
                }

                DeriveFunction runMe = deriveClass.newInstance();
                boolean success = runMe.derive(jcrSession, docNode);
                if (!success) {
                    LOG.error("Something went wrong during processing of derive groovy script, check logs.");
                }
                return null;
            }

        }
        catch (ItemNotFoundException infEx) {
            LOG.info("Can't process a new node, skipping.");
            return null;
        }
        catch (Exception ex) {
            LOG.error("Could not process derivative function manipulation, caused by: ", ex);
            return null;
        }

    }

    protected boolean hasUuidKey(Map<String, Value[]> map) {
        return map.containsKey(PN_UUID);
    }

    protected String getUuidValue(Map<String, Value[]> map) throws RepositoryException {
        return map.get(PN_UUID)[0].getString();
    }

}
