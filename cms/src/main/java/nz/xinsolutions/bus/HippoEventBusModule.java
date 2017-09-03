package nz.xinsolutions.bus;

import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.HippoEventBus;
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
        this.eventBusListener = new HippoEventBusListener();
        HippoServiceRegistry.registerService(this.eventBusListener, HippoEventBus.class);
    }
    
    /**
     * Unregister the service
     */
    @Override
    public void shutdown() {
        HippoServiceRegistry.unregisterService(this.eventBusListener, HippoEventBus.class);
    }
}
