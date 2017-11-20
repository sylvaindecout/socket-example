package test.sdc.socket.client.interfacing;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.EventBus;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.client.event.LoginFailureEvent;
import test.sdc.socket.client.event.LoginSuccessEvent;
import test.sdc.socket.model.protocol.MessageProtos.Message;
import test.sdc.socket.model.protocol.login.LoginResponseProtos.LoginResponse;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

/**
 * Handle incoming messages.
 */
@Sharable
public class ClientMessageHandler
        extends SimpleChannelInboundHandler<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientMessageHandler.class);

    private final EventBus eventBus;
    private final Meter receivedDataUpdates;

    /**
     * Constructor.
     *
     * @param eventBus event bus
     * @param metrics metric registry
     */
    @Inject
    public ClientMessageHandler(final EventBus eventBus, final MetricRegistry metrics) {
        this.eventBus = eventBus;
        this.receivedDataUpdates = metrics.meter("receivedDataUpdates");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Message msg)
            throws Exception {
        requireNonNull(ctx, "Channel handler context must not be null");
        requireNonNull(msg, "Input object must not be null");
        LOGGER.trace("Processing message: {}", msg);
        if (msg.hasLoginResponse()) {
            this.onLoginResponse(msg);
        }
        if (msg.hasDataUpdate()) {
            this.onDataUpdate(msg);
        }
        LOGGER.trace("Done processing message {}", msg);
    }

    /**
     * Process login response.
     *
     * @param msg login response message
     */
    private void onLoginResponse(final Message msg) {
        if (msg.getLoginResponse().getValue() == LoginResponse.LoginResult.SUCCESS) {
            LOGGER.info("User login succeeded - waiting for data...");
            this.eventBus.post(new LoginSuccessEvent(msg.getMsgRefId()));
        } else {
            LOGGER.warn("Login failed ({}) - trying again in {}", msg.getLoginResponse().getValue());
            this.eventBus.post(new LoginFailureEvent(msg.getMsgRefId()));
        }
    }

    /**
     * Process data update message.
     *
     * @param msg data update message
     */
    private void onDataUpdate(final Message msg) {
        LOGGER.info(msg.getDataUpdate().getLabel());
        this.receivedDataUpdates.mark();
    }

}