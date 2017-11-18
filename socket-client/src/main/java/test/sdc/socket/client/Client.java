package test.sdc.socket.client;

import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.client.interfacing.ClientConnection;
import test.sdc.socket.client.session.LoginManager;
import test.sdc.socket.model.protocol.MessageProtos.Message;

import javax.inject.Inject;

/**
 * Server.
 */
public final class Client
        implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final ClientConnection connection;
    private final SimpleChannelInboundHandler<Message> handler;
    private final LoginManager loginManager;

    @Inject
    public Client(final ClientConnection connection,
                  final SimpleChannelInboundHandler<Message> handler,
                  final LoginManager loginManager) {
        this.connection = connection;
        this.handler = handler;
        this.loginManager = loginManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            this.loginManager.startListening();
            Runtime.getRuntime().addShutdownHook(new Thread(connection::dispose));
            this.connection.start(this.handler);
        } catch (final Exception ex) {
            LOGGER.error("Application start-up failed", ex);
        }
    }

}