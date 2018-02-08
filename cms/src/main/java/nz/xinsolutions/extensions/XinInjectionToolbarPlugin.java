package nz.xinsolutions.extensions;

import org.apache.commons.lang.ArrayUtils;
import org.hippoecm.addon.workflow.WorkflowDescriptorModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.reviewedactions.AbstractDocumentWorkflowPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 4/02/18
 *
 *      Can add a toolbar element to the editing toolbar.
 */
public class XinInjectionToolbarPlugin extends AbstractDocumentWorkflowPlugin {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XinInjectionToolbarPlugin.class);
    
    
    /**
     * Initialise data-members
     *
     * @param context   is the plugin context
     * @param config
     */
    public XinInjectionToolbarPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
    
        // get button information
        String title = getTitle(config);
        String iconId = getIconId(config);
        String subMenu = getSubMenu(config);
        String action = getAction(config);
        String[] validTypes = getValidTypes(config);
        
        try {
            Node handle = ((WorkflowDescriptorModel) getDefaultModel()).getNode();
            String docType = ((Node)handle.getNodes().next()).getPrimaryNodeType().getName();
    
            LOG.info("Toolbar for doc: " + handle.getPath());
    
            // add a button
            this.add(
                new JavascriptEnabledStdWorkflow(config.getName(), subMenu, iconId, title,  action, handle.getPath()) {

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        // no restrictions?
                        if (validTypes == null) {
                            return true;
                        }

                        return ArrayUtils.contains(validTypes, docType);
                    }

                }
            );
        }
        catch (RepositoryException rEx) {
            LOG.error("Could not retrieve handler, caused by: ", rEx);
        }
    }

    private String getAction(IPluginConfig config) {
        return (String) config.get("action");
    }


    /**
     * @return a list of valid types
     */
    protected String[] getValidTypes(IPluginConfig config) {
        return config.containsKey("types")
                    ? (String[]) config.get("types")
                    : null;
    }
    
    /**
     * @return the submenu in which the item is placed
     */
    protected String getSubMenu(IPluginConfig config) {
        return config.containsKey("submenu") ? (String) config.get("submenu") : "top";
    }
    
    /**
     * @return the icon identifier or GEAR when not specified
     */
    protected String getIconId(IPluginConfig config) {
        return config.containsKey("icon")
                    ? ((String) config.get("icon")).toUpperCase()
                    : "GEAR";
    }
    
    /**
     * @return the button title
     */
    protected String getTitle(IPluginConfig config) {
        return config.containsKey("title")
                    ? (String) config.get("title")
                    : "Unnamed";
    }


    
}