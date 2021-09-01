package nz.xinsolutions.authentication;

import org.hippoecm.frontend.plugins.login.LoginConfig;
import org.hippoecm.frontend.plugins.login.LoginHandler;
import org.hippoecm.frontend.plugins.login.LoginPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Marnix Kok
 * <p>
 * Purpose:
 */
public class Auth0LoginPanel extends LoginPanel {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Auth0LoginPanel.class);

    public Auth0LoginPanel(String id, LoginConfig config, LoginHandler handler) {
        super(id, config, handler);
    }

}
