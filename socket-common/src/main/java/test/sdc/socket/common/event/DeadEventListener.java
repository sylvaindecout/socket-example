package test.sdc.socket.common.event;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle events with no registered listeners.
 */
public final class DeadEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeadEventListener.class);

    /**
     * Process dead event.
     *
     * @param event dead event
     */
    @Subscribe
    public void onDeadEvent(final DeadEvent event) {
        LOGGER.debug("{} event has not been processed.", event.getEvent());
    }

}