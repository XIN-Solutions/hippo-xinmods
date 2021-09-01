package nz.xinsolutions.authentication;

import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.login.DefaultLoginPlugin;
import org.hippoecm.frontend.plugins.login.LoginConfig;
import org.hippoecm.frontend.plugins.login.LoginHandler;
import org.hippoecm.frontend.plugins.login.LoginPanel;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      To extend the DefaultLoginPlugin and add some wicket:child content that presents an auth0
 *      Social Login integration.
 */
public class Auth0LoginPlugin extends DefaultLoginPlugin {

    /**
     * Initialise data-members
     *
     * @param context
     * @param config
     */
    public Auth0LoginPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
    }

    /**
     * Initialise a login panel that has an extra button
     *
     * @param id        the identifier
     * @param config    the login configuration
     * @param handler   the login handler
     *
     * @return the social version of the login panel
     */
    @Override
    protected LoginPanel createLoginPanel(String id, LoginConfig config, LoginHandler handler) {
        return new Auth0LoginPanel(id, config, handler);
    }

}
