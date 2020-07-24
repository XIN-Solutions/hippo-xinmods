package nz.xinsolutions.bus;

import org.onehippo.cms7.event.HippoEvent;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * <p>
 * Purpose:
 *
 *     Interface for the hippo event bus service
 *
 */
public interface HippoEventBusListener {

    void handleEvent(HippoEvent event);

}
