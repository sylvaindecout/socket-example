package test.sdc.socket.server.interfacing;

import com.google.common.eventbus.EventBus;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.model.protocol.MessageProtos.Message;
import test.sdc.socket.model.protocol.login.LoginResponseProtos;
import test.sdc.socket.model.protocol.login.LoginResponseProtos.LoginResponse.LoginResult;
import test.sdc.socket.server.event.LoginEvent;
import test.sdc.socket.server.session.ClientRegistry;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

/**
 * Handle incoming messages.
 */
@Sharable
public class ServerMessageHandler
        extends SimpleChannelInboundHandler<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerMessageHandler.class);

    private final ClientRegistry clientRegistry;
    private final EventBus eventBus;

    /**
     * Constructor.
     *
     * @param clientRegistry client registry
     * @param eventBus       event bus
     */
    @Inject
    public ServerMessageHandler(final ClientRegistry clientRegistry, final EventBus eventBus) {
        this.clientRegistry = clientRegistry;
        this.eventBus = eventBus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Message msg)
            throws Exception {
        requireNonNull(ctx, "Channel handler context must not be null");
        requireNonNull(msg, "Input object must not be null");
        LOGGER.info("Processing message: {}", msg);
        if (msg.hasLoginRequest()) {
            final Message response = this.onLoginRequest(ctx, msg);
            LOGGER.info("Sending response to message {}: {}", msg, response);
            ctx.writeAndFlush(response);
        }
        LOGGER.trace("Done processing message {}", msg);
    }

    /**
     * Process login request message.
     *
     * @param ctx context
     * @param msg login request message
     * @return login response message
     */
    private Message onLoginRequest(final ChannelHandlerContext ctx, final Message msg) {
        final LoginResult result;
        if (this.clientRegistry.contains(msg.getLoginRequest().getLogin())) {
            result = LoginResult.ALREADY_LOGGED;
        } else {
            result = LoginResult.SUCCESS; // FIXME: do not allow just any credentials
        }
        if (result == LoginResult.SUCCESS) {
            this.eventBus.post(new LoginEvent(msg.getLoginRequest().getLogin(), ctx.channel()));
        }
        return Message.newBuilder()
                .setMsgRefId(msg.getMsgRefId())
                .setLoginResponse(LoginResponseProtos.LoginResponse.newBuilder()
                        .setValue(result)
                        .build())
                .build();
    }

}