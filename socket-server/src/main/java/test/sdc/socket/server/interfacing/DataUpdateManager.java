package test.sdc.socket.server.interfacing;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.model.protocol.MessageProtos.Message;
import test.sdc.socket.model.protocol.data.DataUpdateProtos.DataUpdate;
import test.sdc.socket.server.event.DataUpdateEvent;
import test.sdc.socket.server.session.ClientRegistry;

import javax.inject.Inject;
import java.util.UUID;

/**
 * In charge of deciding when to send data updates.
 */
public final class DataUpdateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataUpdateManager.class);

    private final ClientRegistry clientRegistry;
    private final EventBus eventBus;

    /**
     * Constructor.
     *
     * @param clientRegistry client registry
     * @param eventBus       event bus
     */
    @Inject
    public DataUpdateManager(final ClientRegistry clientRegistry, final EventBus eventBus) {
        this.clientRegistry = clientRegistry;
        this.eventBus = eventBus;
    }

    /**
     * Start listening to data updates.
     */
    public void startListening() {
        this.eventBus.register(this);
    }

    /**
     * Listen to failed login events.
     *
     * @param event failed login event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void onDataUpdate(final DataUpdateEvent<String> event) {
        try {
            LOGGER.trace("Data update event: {}", event);
            final String data = event.getUpdatedElement();
            this.sendData(data);
        } catch (final Exception ex) {
            LOGGER.error("Failed to process data update event", ex);
        }
    }

    /**
     * Send data to clients.
     *
     * @param data data
     */
    private void sendData(final String data) {
        LOGGER.debug("Sending data to clients: {}", data);
        final Message msg = Message.newBuilder()
                .setMsgRefId(UUID.randomUUID().toString())
                .setDataUpdate(DataUpdate.newBuilder()
                        .setLabel(data)
                        .build())
                .build();
        this.clientRegistry.findAll()
                .forEach(channel -> channel.writeAndFlush(msg));
    }

}