package test.sdc.socket.client;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.client.interfacing.ClientConnection;
import test.sdc.socket.client.session.LoginManager;
import test.sdc.socket.model.protocol.MessageProtos.Message;

import javax.inject.Inject;
import javax.net.ssl.SSLException;

/**
 * Server.
 */
public final class Client
        implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final ClientConnection connection;
    private final SimpleChannelInboundHandler<Message> handler;
    private final LoginManager loginManager;
    private final MetricRegistry metrics;

    @Inject
    public Client(final ClientConnection connection,
                  final SimpleChannelInboundHandler<Message> handler,
                  final LoginManager loginManager, final MetricRegistry metrics) {
        this.connection = connection;
        this.handler = handler;
        this.loginManager = loginManager;
        this.metrics = metrics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            this.startMonitoring();
            this.startConnection();
        } catch (final Exception ex) {
            LOGGER.error("Application start-up failed", ex);
        }
    }

    /**
     * Connect to server.
     *
     * @throws InterruptedException connection thread was interrupted
     * @throws SSLException         indicates some kind of error detected by an SSL subsystem
     */
    private void startConnection()
            throws SSLException, InterruptedException {
        this.loginManager.startListening();
        Runtime.getRuntime().addShutdownHook(new Thread(connection::dispose));
        this.connection.start(this.handler);
    }

    /**
     * Activate JMX monitoring.
     */
    private void startMonitoring() {
        final JmxReporter reporter = JmxReporter.forRegistry(this.metrics).build();
        Runtime.getRuntime().addShutdownHook(new Thread(reporter::stop));
        reporter.start();
    }

}