package test.sdc.socket.server;

import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.model.protocol.MessageProtos.Message;
import test.sdc.socket.server.data.Simulation;
import test.sdc.socket.server.interfacing.DataUpdateManager;
import test.sdc.socket.server.interfacing.ServerConnection;

import javax.inject.Inject;

/**
 * Server.
 */
public final class Server
        implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final ServerConnection connection;
    private final SimpleChannelInboundHandler<Message> handler;
    private final Simulation simulation;
    private DataUpdateManager dataUpdateManager;

    /**
     * Constructor.
     *
     * @param connection connection
     * @param handler    message handler
     * @param simulation simulation
     */
    @Inject
    public Server(final ServerConnection connection,
                  final SimpleChannelInboundHandler<Message> handler,
                  final Simulation simulation, final DataUpdateManager dataUpdateManager) {
        this.connection = connection;
        this.handler = handler;
        this.simulation = simulation;
        this.dataUpdateManager = dataUpdateManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            this.dataUpdateManager.startListening();
            this.simulation.start();
            Runtime.getRuntime().addShutdownHook(new Thread(this.connection::dispose));
            this.connection.start(this.handler);
        } catch (final Exception ex) {
            LOGGER.error("Application start-up failed", ex);
        }
    }

}