package nz.xinsolutions.extensions;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.onehippo.cms7.reports.AbstractExtRenderPlugin;
import org.wicketstuff.js.ext.ExtComponent;
import org.wicketstuff.js.ext.ExtPanel;

/**
 * Inject a reporting plugin into the hippo admin dashboard
 */
public class XinInjectionReportPlugin extends AbstractExtRenderPlugin {

    private ExtComponent panel;

    /**
     * Initialise data-members
     *
     * @param context
     * @param config
     */
    public XinInjectionReportPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        setOutputMarkupId(true);
        setRenderBodyOnly(true);

        this.panel = new XinReportPanel(config);
        add(this.panel);

    }

    @Override
    public ExtComponent getExtComponent() {
        return this.panel;
    }

    /**
     * The reporting panel that fits snugly within the
     */
    public static class XinReportPanel extends ExtPanel {

        /**
         * Injection information
         */
        private InjectDto inject;

        /**
         * Initialise data-members
         * @param config
         */
        public XinReportPanel(IPluginConfig config) {
            super();
            this.inject = InjectDto.fromConfig(config);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRender() {
            super.onRender();
            getWebResponse().write(this.inject.getContentsString());
        }


        /**
         * Add the proper JS and CSS tags at the top.
         */
        @Override
        public void renderHead(IHeaderResponse response) {
            super.renderHead(response);
            this.inject.renderHead(response);
        }

    }

}
