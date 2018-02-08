package nz.xinsolutions.extensions;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.onehippo.cms7.reports.ReportsPerspective;
import org.onehippo.cms7.reports.plugins.PortalPanelPlugin;

public class XinInjectionReportDefinition extends PortalPanelPlugin {

    /**
     * Identifier
     */
    private String id;

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
     * Initialise data-members
     *
     * @param context the plugin context
     * @param config  the plugin configuration properties
     */
    public XinInjectionReportDefinition(IPluginContext context, IPluginConfig config) {
        super(context, config);

        this.title = config.getString("title", "`title` missing");
        this.icon = config.getString("icon", null);
        this.help = config.getString("help", "`help` missing");
        this.id = config.getString("service.id", null);

        if (this.id == null) {
            throw new IllegalArgumentException("Missing `service.id` property on reporting node");
        }

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

    @Override
    public String getPortalPanelServiceId() {
        return this.id;
    }

    @Override
    public String getPanelServiceId() {
        return ReportsPerspective.REPORTING_SERVICE;
    }
}
