package nz.xinsolutions.bus;

import org.jetbrains.annotations.NotNull;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.HippoEventListenerRegistry;
import org.onehippo.repository.modules.DaemonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
    
    /**
     * Listener instance
     */
    private HippoEventBusListener eventBusListener;
    
    /**
     * Add the event bus listener to the hippo service registry
     *
     * @param session
     * @throws RepositoryException
     */
    @Override
    public void initialize(Session session) throws RepositoryException {
        this.eventBusListener = newListenerInstance();
        HippoEventListenerRegistry.get().register(this.eventBusListener);
    }

    @NotNull
    protected HippoEventBusListenerImpl newListenerInstance() {
        return new HippoEventBusListenerImpl();
    }

    /**
     * Unregister the service
     */
    @Override
    public void shutdown() {
        HippoEventListenerRegistry.get().unregister(this.eventBusListener);
    }
}
