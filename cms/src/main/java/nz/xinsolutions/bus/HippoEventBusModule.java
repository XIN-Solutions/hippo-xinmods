package nz.xinsolutions.bus;

import nz.xinsolutions.config.XinmodsConfig;
import org.apache.jackrabbit.api.observation.JackrabbitEventFilter;
import org.apache.jackrabbit.api.observation.JackrabbitObservationManager;
import org.jetbrains.annotations.NotNull;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.HippoEventListenerRegistry;
import org.onehippo.repository.modules.DaemonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 2/09/17
 *
 *      Registers the hippo event bus
 *
 */
public class HippoEventBusModule implements DaemonModule {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HippoEventBusModule.class);
    public static final int ALL_EVENTS = 255;
    public static final String CONTENT_GALLERY = "/content/gallery";

    /**
     * Listener instance
     */
    private HippoEventBusListener eventBusListener;

    /**
     * Xinmods configuration
     */
    private XinmodsConfig config;

    /**
     * Add the event bus listener to the hippo service registry
     *
     * @param session
     * @throws RepositoryException
     */
    @Override
    public void initialize(Session session) throws RepositoryException {
        this.config = newConfigInstance(session);
        this.eventBusListener = newListenerInstance();
        HippoEventListenerRegistry.get().register(this.eventBusListener);

        ObservationManager obsMgr = session.getWorkspace().getObservationManager();

        JackrabbitEventFilter filter = new JackrabbitEventFilter();
        filter.setAbsPath("/content/");
        filter.setIsDeep(true);

        // also wanna hear about things that change in the gallery.
        obsMgr.addEventListener((EventListener) this.eventBusListener, ALL_EVENTS, CONTENT_GALLERY, true, null, null, false);
    }


    @NotNull
    protected XinmodsConfig newConfigInstance(Session session) {
        return new XinmodsConfig(session);
    }

    @NotNull
    protected HippoEventBusListenerImpl newListenerInstance() {
        return new HippoEventBusListenerImpl(this.config);
    }

    /**
     * Unregister the service
     */
    @Override
    public void shutdown() {
        HippoEventListenerRegistry.get().unregister(this.eventBusListener);
    }
}
