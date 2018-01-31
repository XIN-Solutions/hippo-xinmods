package nz.xinsolutions.bus;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.util.json.Jackson;
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
    
    public static final String AWS_SNS_TOPIC = "arn:aws:sns:ap-southeast-2:741354369915:hippo-dev-topic";
    
    /**
     * SNS service
     */
    private AmazonSNS sns;
    
    /**
     * Logger
     */
    private Logger LOG = LoggerFactory.getLogger(HippoEventBusListener.class);
    
    
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

        LOG.info(
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

        LOG.info("Sending JSON to topic: " + jsonMessage);

        try {

            this.sns.publish(AWS_SNS_TOPIC, jsonMessage);
        }
        catch (Exception ex) {
            LOG.error("Could not send message to SNS `{}`.", AWS_SNS_TOPIC);
        }
    }
    
    

}
