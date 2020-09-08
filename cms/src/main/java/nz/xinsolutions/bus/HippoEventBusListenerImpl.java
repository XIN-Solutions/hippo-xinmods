package nz.xinsolutions.bus;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.thirdparty.apache.http.HttpEntity;
import com.amazonaws.thirdparty.apache.http.StatusLine;
import com.amazonaws.thirdparty.apache.http.client.HttpClient;
import com.amazonaws.thirdparty.apache.http.client.methods.CloseableHttpResponse;
import com.amazonaws.thirdparty.apache.http.client.methods.HttpPost;
import com.amazonaws.thirdparty.apache.http.entity.ContentType;
import com.amazonaws.thirdparty.apache.http.entity.StringEntity;
import com.amazonaws.thirdparty.apache.http.impl.client.CloseableHttpClient;
import com.amazonaws.thirdparty.apache.http.impl.client.HttpClientBuilder;
import com.amazonaws.thirdparty.apache.http.impl.client.HttpClients;
import com.amazonaws.util.json.Jackson;
import nz.xinsolutions.config.XinmodsConfig;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.services.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 2/09/17
 *
 *      Receives and distributes events
 *
 */
public class HippoEventBusListenerImpl implements HippoEventBusListener {

    /**
     * Property or environment variable containing the hippo event bus arn.
     */
    public static final String AWS_HIPPOBUS_ARN = "AWS_HIPPOBUS_ARN";

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HippoEventBusListenerImpl.class);

    /**
     * SNS service
     */
    private AmazonSNS sns;

    /**
     * Configuration
     */
    private XinmodsConfig config;

    /**
     * Initialise data-members
     */
    public HippoEventBusListenerImpl(XinmodsConfig config) {
        this.config = config;

        this.sns =
            AmazonSNSClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
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

        if (!config.isValid()) {
            LOG.info("The xinmods configuration was not valid so no messages will be sent.");
            return;
        }

        for (String topicArn: config.getSNSArns()) {
            attemptSendToSNS(topicArn, jsonMessage);
        }

        for (String webhook : config.getWebhookUrls()) {
            attemptSendToWebhook(webhook, jsonMessage);
        }

    }

    /**
     * Try to send the hippo message to a webhook. The webhook will be expected to accept
     * application/json in the payload of a POST request.
     *
     * @param url   the url to post to.
     * @param payload the payload to embed in the body.
     */
    protected void attemptSendToWebhook(String url, String payload) {

        LOG.info("About to send to webhook: {}", url);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            HttpEntity body = new StringEntity(payload, ContentType.APPLICATION_JSON);
            request.setEntity(body);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                StatusLine statusLine = response.getStatusLine();
                LOG.info(
                    "Webhook response status code: {} => {}",
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase()
                );
            }
        }
        catch (Exception ex) {
            LOG.error("Exception sending webhook: ", ex);
        }
    }

    /**
     * Try to send the hippo bus message to an SNS. This will only happen if one was configured
     * on the command line.
     *
     * @param jsonMessage
     */
    protected void attemptSendToSNS(String topicArn, String jsonMessage) {
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
