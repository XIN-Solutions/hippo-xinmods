package nz.xinsolutions.bus;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.thirdparty.apache.http.HttpEntity;
import com.amazonaws.thirdparty.apache.http.StatusLine;
import com.amazonaws.thirdparty.apache.http.client.methods.CloseableHttpResponse;
import com.amazonaws.thirdparty.apache.http.client.methods.HttpPost;
import com.amazonaws.thirdparty.apache.http.entity.ContentType;
import com.amazonaws.thirdparty.apache.http.entity.StringEntity;
import com.amazonaws.thirdparty.apache.http.impl.client.CloseableHttpClient;
import com.amazonaws.thirdparty.apache.http.impl.client.HttpClients;
import com.amazonaws.util.json.Jackson;
import nz.xinsolutions.config.XinmodsConfig;
import org.apache.commons.lang3.StringUtils;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.services.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.*;
import java.util.concurrent.*;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 2/09/17
 *
 *      Receives and distributes events
 *
 */
public class HippoEventBusListenerImpl implements HippoEventBusListener, EventListener {

    /**
     * Property or environment variable containing the hippo event bus arn.
     */
    public static final String AWS_HIPPOBUS_ARN = "AWS_HIPPOBUS_ARN";

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HippoEventBusListenerImpl.class);

    /**
     * Delay in webhook sending.
     */
    public static final int EVENT_DELAY_IN_MS = 5000;

    /**
     * SNS service
     */
    private AmazonSNS sns;

    /**
     * Configuration
     */
    private XinmodsConfig config;

    /**
     * Timer object used to schedule sending information
     */
    private ScheduledExecutorService executor;

    /**
     * Ongoing task if exists.
     */
    private ScheduledFuture<Void> scheduledTask = null;

    /**
     * A list of queued events
     */
    private List<Map> queuedEvents;

    /**
     * Initialise data-members
     */
    public HippoEventBusListenerImpl(XinmodsConfig config) {
        this.config = config;


        this.queuedEvents = new ArrayList<>();
        this.executor = Executors.newSingleThreadScheduledExecutor();

        this.sns =
            AmazonSNSClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .build()
        ;

    }


    /**
     * This method is called when a JCR event has been lodged
     *
     * @param eventIterator
     */
    @Override
    public synchronized void onEvent(EventIterator eventIterator) {

        List<Map> events = new ArrayList<>();

        while (eventIterator.hasNext()) {
            Event event = eventIterator.nextEvent();
            LOG.info("Got JCR Event: " + event.toString());

            try {
                Map<String, Object> eventVal = new LinkedHashMap<>();
                eventVal.put("_origin", "jcr");
                eventVal.put("_timestamp", System.currentTimeMillis());
                eventVal.putAll(event.getInfo());
                eventVal.put("identifier", event.getIdentifier());
                eventVal.put("user", event.getUserID());
                eventVal.put("subjectPath", event.getPath());
                events.add(eventVal);
            }
            catch (RepositoryException rEx) {
                LOG.error("Repository exception: ", rEx);
            }
        }

        // schedule the timer to send something
        if (this.queuedEvents.size() == 0) {
            LOG.info("Queue is empty, scheduling delayed sending.");
            sendDelayedQueuedEvents();
        }

        // queue events
        LOG.info("Queued {} events for processing", events.size());
        this.queuedEvents.addAll(events);

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

        Map values = new LinkedHashMap<>(event.getValues());
        values.put("_origin", "hippo");
        values.put("_timestamp", System.currentTimeMillis());


        // schedule the timer to send something
        if (this.queuedEvents.size() == 0) {
            LOG.info("Queue is empty, scheduling delayed sending.");
            sendDelayedQueuedEvents();
        }

        this.queuedEvents.add(values);
    }


    /**
     * Send a delayed queued event
     */
    public synchronized void sendDelayedQueuedEvents() {

        // send something.
        Callable<Void> send = () -> {
            LOG.info("Sending events");
            this.sendMultipleEvents(this.queuedEvents);
            return null;
        };

        // already had a timer? cancel and forget
        if (this.scheduledTask != null && !this.scheduledTask.isDone()) {
            LOG.info("Cancelled running task");
            this.scheduledTask.cancel(true);
            this.scheduledTask = null;
        }

        LOG.info("Scheduling timer for {} ms", EVENT_DELAY_IN_MS);

        // cancelling timer
        this.scheduledTask = this.executor.schedule(
            send,
            EVENT_DELAY_IN_MS,
            TimeUnit.MILLISECONDS
        );


    }

    /**
     * Send event payload in separate thread.
     *
     * @param events all the events to send.
     */
    public void sendMultipleEvents(final List<Map> events) {

        new Thread(() -> {

            String jsonMessage = Jackson.toJsonString(events);

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

            this.queuedEvents.clear();

        }).start();

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
            LOG.error("Exception sending webhook: {}", ex.getMessage());
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


}
