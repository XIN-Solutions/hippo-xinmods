package nz.xinsolutions.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * <p>
 * Purpose:
 *
 *  Simple wrapper object to read configuration information stored in the JCR.
 */
public class XinmodsConfig {

    /**
     * Logging
     */
    private static final Logger LOG = LoggerFactory.getLogger(XinmodsConfig.class);

    /**
     * Configuration location
     */
    public static final String XINMODS_CONFIG_PATH = "/hippo:configuration/hippo:modules/xinmods/hippo:moduleconfig";
    public static final String PROP_WEBHOOKS = "webhooks";
    public static final String PROP_SNS_TOPICS = "snsTopics";
    public static final String PROP_JWT_WHITELIST = "jwt.whitelist";


    /**
     * Jcr node where configuration is stored
     */
    private Node config;


    /**
     * Initialise data-members
     *
     * @param session   the JCR session to request the configuration with
     */
    public XinmodsConfig(Session session) {

        try {

            if (session.nodeExists(XINMODS_CONFIG_PATH)) {
                this.config = session.getNode(XINMODS_CONFIG_PATH);
            }
            else {
                LOG.info("Could not find the xinmods configuration at " + XINMODS_CONFIG_PATH);
            }
        }
        catch (RepositoryException rEx) {
            LOG.error("Exception trying to retrieve the xinmods configuration, caused by:", rEx);
        }
    }

    public List<String> getJwtSourceWhitelist() {
        if (config == null) {
            return Collections.EMPTY_LIST;
        }

        try {
            if (!this.config.hasProperty(PROP_JWT_WHITELIST)) {
                return Collections.EMPTY_LIST;
            }


            Value[] whitelistValues = this.config.getProperty(PROP_JWT_WHITELIST).getValues();
            List<String> whitelist = new ArrayList<>();

            for (Value snsVal : whitelistValues) {
                whitelist.add(snsVal.getString());
            }

            return whitelist;
        }
        catch (RepositoryException rEx) {
            LOG.error("Something went wrong trying to retrieve the white list", rEx);
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * Extract a set of webhooks from the xinmods configuration
     * @return a set of webhook strings as configured in the node
     */
    public List<String> getSNSArns() {
        if (config == null) {
            return Collections.EMPTY_LIST;
        }

        try {
            if (!this.config.hasProperty(PROP_SNS_TOPICS)) {
                return Collections.EMPTY_LIST;
            }

            Value[] snsValues = this.config.getProperty(PROP_SNS_TOPICS).getValues();
            List<String> topics = new ArrayList<>();

            for (Value snsVal : snsValues) {
                topics.add(snsVal.getString());
            }

            return topics;
        }
        catch (Exception ex) {
            LOG.error("Problem retrieving sns topics configuration", ex);
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * Extract a set of webhooks from the xinmods configuration
     * @return a set of webhook strings as configured in the node
     */
    public List<String> getWebhookUrls() {
        if (config == null) {
            return Collections.EMPTY_LIST;
        }

        try {
            if (!this.config.hasProperty(PROP_WEBHOOKS)) {
                return Collections.EMPTY_LIST;
            }

            Value[] whValues = this.config.getProperty(PROP_WEBHOOKS).getValues();
            List<String> hooks = new ArrayList<>();
            for (Value whVal : whValues) {
                hooks.add(whVal.getString());
            }
            return hooks;
        }
        catch (Exception ex) {
            LOG.error("Problem retrieving webhooks configuration", ex);
            return Collections.EMPTY_LIST;
        }
    }


    public boolean isValid() {
        return this.config != null;
    }

}
