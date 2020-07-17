package nz.xinsolutions.extensions;

import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.cms.admin.AdminPanelPlugin;
import org.hippoecm.frontend.plugins.standards.panelperspective.breadcrumb.PanelPluginBreadCrumbPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 4/02/18
 */
public class XinInjectionAdminPanelDefinition extends AdminPanelPlugin {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XinInjectionAdminPanelDefinition.class);

    /**
     * Icon
     */
    private String icon;

    /**
     * Title
     */
    private String title;

    /**
     * Help
     */
    private String help;

    /**
     * Injection information
     */
    private InjectDto inject;

    /**
     * Initialise data-members
     *
     * @param context   is the plugin context
     * @param config    is the plugin configuration (properties on node)
     */
    public XinInjectionAdminPanelDefinition(IPluginContext context, IPluginConfig config) {
        super(context, config);

        this.title = config.getString("title", "`title` missing");
        this.icon = config.getString("icon", null);
        this.help = config.getString("help", "`help` missing");

        inject = InjectDto.fromConfig(config);

    }

    /**
     * @return an object to an external image resource
     */
    @Override
    public ResourceReference getImage() {
        if (this.icon == null) {
            return null;
        }
        return new UrlResourceReference(Url.parse(this.icon));
    }

    /**
     * @return the title of the admin panel
     */
    @Override
    public IModel<String> getTitle() {
        return new Model<>(this.title);
    }

    /**
     * @return the help text of the admin panel
     */
    @Override
    public IModel<String> getHelp() {
        return new Model<>(this.help);
    }

    /**
     * @return the instance of the panel to show when the admin button is clicked.
     */
    @Override
    public PanelPluginBreadCrumbPanel create(String componentId, IBreadCrumbModel breadCrumbModel) {
        return new XinInjectionAdminPlugin(componentId, breadCrumbModel, this.title, this.inject);
    }

}
