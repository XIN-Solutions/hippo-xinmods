package nz.xinsolutions.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * <p>
 * Purpose:
 *
 *  Simple wrapper object to read configuration information stored in the JCR.
 */
public class SiteXinmodsConfig {

    /**
     * Logging
     */
    private static final Logger LOG = LoggerFactory.getLogger(SiteXinmodsConfig.class);

    /**
     * Configuration location
     */
    public static final String XINMODS_CONFIG_PATH = "/hippo:configuration/hippo:modules/xinmods/hippo:moduleconfig";

    public static final String PROP_WEBHOOKS = "webhooks";
    public static final String PROP_SNS_TOPICS = "snsTopics";
    public static final String PROP_ASSET_CACHE_LENGTH = "assetCacheLength";

    /**
     * JCR Session
     */
    private Session session;

    /**
     * Jcr node where configuration is stored
     */
    private Node config;


    /**
     * Initialise data-members
     *
     * @param session   the JCR session to request the configuration with
     */
    public SiteXinmodsConfig(Session session) {
        this.session = session;

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


    public boolean isValid() {
        return this.config != null;
    }

    /**
     * @return the default cache time.
     */
    public long getAssetCacheLength(long defaultTime) {
        if (!this.isValid()) {
            return defaultTime;
        }

        try {
            return (
                this.config.hasProperty(PROP_ASSET_CACHE_LENGTH)
                    ? this.config.getProperty(PROP_ASSET_CACHE_LENGTH).getLong()
                    : defaultTime
            );
        }
        catch (RepositoryException rEx) {
            LOG.error("Couldn't get asset cache length from module configuration, caused by:", rEx);
            return defaultTime;
        }
    }
}
