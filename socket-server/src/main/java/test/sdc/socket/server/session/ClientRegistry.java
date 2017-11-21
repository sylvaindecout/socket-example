package test.sdc.socket.server.session;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.server.event.LoginEvent;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Register all clients that are currently connected and logged in.
 */
public final class ClientRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRegistry.class);

    private final Map<Channel, String> channels = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param eventBus event bus
     */
    @Inject
    public ClientRegistry(final EventBus eventBus) {
        eventBus.register(this);
    }

    /**
     * Get list of channels.
     *
     * @return list of channels
     */
    public Collection<Channel> findAll() {
        return this.channels.keySet();
    }

    /**
     * Check if registry contains input login.
     *
     * @param login login
     * @return registry contains login
     */
    public Boolean contains(final String login) {
        return this.channels.containsValue(login);
    }

    /**
     * Listen to login events.
     *
     * @param event login event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void onLogin(final LoginEvent event) {
        try {
            LOGGER.trace("Login event: {}", event);
            this.channels.put(event.getChannel(), event.getLogin());
            LOGGER.info("List of clients has been updated: {}", this.channels.values());
        } catch (final Exception ex) {
            LOGGER.error("Failed to process login event", ex);
        }
    }

}