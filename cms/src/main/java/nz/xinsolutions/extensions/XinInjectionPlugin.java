package nz.xinsolutions.extensions;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 3/02/18
 *
 *     XIN module extensions allowing the loading of HTML in-place and JS/CSS elements in the header
 *
 */
public class XinInjectionPlugin extends RenderPlugin implements IHeaderContributor {
    
    /**
     * Lgoger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XinInjectionPlugin.class);
    
    /**
     * Injection information
     */
    private InjectDto injection;
    
    /**
     * Initialise data-members
     *
     * @param context   is the plugin context
     * @param config
     */
    public XinInjectionPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
        
        LOG.info("Initialising external link dashboard plugin");
        this.injection = InjectDto.fromConfig(config);
        setRenderBodyOnly(true);

    }
    
    @Override
    public void onRender() {
        getWebResponse().write(this.injection.getContentsString());
        super.onRender();
    }

    
    /**
     * Add the proper JS and CSS tags at the top.
     */
    @Override
    public void renderHead(IHeaderResponse response) {
        this.injection.renderHead(response);
    }
}