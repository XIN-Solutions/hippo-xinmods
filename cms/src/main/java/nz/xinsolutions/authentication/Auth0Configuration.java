package nz.xinsolutions.authentication;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      To describe parts of the auth0 configuration for use in front and backend.
 *
 */
public class Auth0Configuration {

    private String appTitle;
    private String domain;
    private String clientId;
    private String afterLoginUrl;
    private String afterLogoutUrl;
    private String jwksUrl;
    private String requiredAudience;


    /**
     * Create a new instance of this configuration object
     *
     * @param node the node to interrogate for settings
     * @return
     */
    public static Auth0Configuration newFromNode(Node node) throws RepositoryException {
        Auth0Configuration cfg = new Auth0Configuration();

        cfg.setAppTitle(ifPropertyOr(node, "auth0.appName"));
        cfg.setDomain(ifPropertyOr(node, "auth0.domain"));
        cfg.setClientId(ifPropertyOr(node, "auth0.clientId"));
        cfg.setAfterLoginUrl(ifPropertyOr(node, "auth0.afterLoginUrl"));
        cfg.setAfterLogoutUrl(ifPropertyOr(node, "auth0.afterLogoutUrl"));
        cfg.setJwksUrl(ifPropertyOr(node, "auth0.jwksUrl"));
        cfg.setRequiredAudience(ifPropertyOr(node, "auth0.audience"));

        return cfg;
    }


    /**
     * Simple convenience function for retrieving a property value.
     * @param node the node to get it from
     * @param property the name of the property
     * @return the value or null if it doesn't exist.
     * @throws RepositoryException
     */
    protected static String ifPropertyOr(Node node, String property) throws RepositoryException {
        if (node.hasProperty(property)) {
            return node.getProperty(property).getString();
        }
        return null;
    }


    public String getAppTitle() {
        return appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAfterLoginUrl() {
        return afterLoginUrl;
    }

    public void setAfterLoginUrl(String afterLoginUrl) {
        this.afterLoginUrl = afterLoginUrl;
    }

    public String getAfterLogoutUrl() {
        return afterLogoutUrl;
    }

    public void setAfterLogoutUrl(String afterLogoutUrl) {
        this.afterLogoutUrl = afterLogoutUrl;
    }

    public String getJwksUrl() {
        return jwksUrl;
    }

    public void setJwksUrl(String jwksUrl) {
        this.jwksUrl = jwksUrl;
    }

    public String getRequiredAudience() {
        return requiredAudience;
    }

    public void setRequiredAudience(String requiredAudience) {
        this.requiredAudience = requiredAudience;
    }
}
