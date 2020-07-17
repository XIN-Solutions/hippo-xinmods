package nz.xinsolutions.bus;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.util.json.Jackson;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.services.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 2/09/17
 *
 *      Receives and distributes events
 *
 */
public class HippoEventBusListener {

    public static final String AWS_HIPPOBUS_ARN = "AWS_HIPPOBUS_ARN";

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HippoEventBusListener.class);

    /**
     * SNS service
     */
    private AmazonSNS sns;

    /**
     * Initialise data-members
     */
    public HippoEventBusListener() {
        this.sns =
            AmazonSNSClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)     // dat sydney
                .build()
        ;
        
    }
    
    /**
     * Called when an event is to be handled.
     *
     * @param event is the event that was surfaced
     */
    @Subscribe
    public void handleEvent(HippoEvent event) {

        LOG.debug(
            "PUBLISH EVENT: " +
            ", cat " + event.category() +
            ", action: " + event.action() +
            ", application: " + event.application() +
            ", message: " + event.message() +
            ", user: " + event.user()
        );
    
        Map<String, Object> map = new LinkedHashMap<>(event.getValues());
        
        map.put("_origin", "hippo");
        map.put("_timestamp", System.currentTimeMillis());
    
        String jsonMessage = Jackson.toJsonString(map);


        String topicArn = getAwsSnsTopic();
        if (StringUtils.isBlank(topicArn)) {
            LOG.debug("No SNS Arn set, not going to tell anyone what happened.");
            return;
        }

        try {
            LOG.info("Sending JSON to topic: " + jsonMessage);
            this.sns.publish(topicArn, jsonMessage);
        }
        catch (Exception ex) {
            LOG.error("Could not send message to SNS `{}`.", topicArn);
        }
    }

    /**
     * @return the AWS SNS topic to send to, can be null or emtpy
     */
    @NotNull
    protected String getAwsSnsTopic() {
        return System.getProperty(AWS_HIPPOBUS_ARN, System.getenv(AWS_HIPPOBUS_ARN));
    }


}
