package test.sdc.socket.server;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.model.protocol.MessageProtos.Message;
import test.sdc.socket.server.data.Simulation;
import test.sdc.socket.server.interfacing.DataUpdateManager;
import test.sdc.socket.server.interfacing.ServerConnection;

import javax.inject.Inject;
import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

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
    private final MetricRegistry metrics;

    /**
     * Constructor.
     *
     * @param connection connection
     * @param handler    message handler
     * @param simulation simulation
     * @param dataUpdateManager data update sender
     * @param metrics metric registry
     */
    @Inject
    public Server(final ServerConnection connection,
                  final SimpleChannelInboundHandler<Message> handler,
                  final Simulation simulation, final DataUpdateManager dataUpdateManager,
                  final MetricRegistry metrics) {
        this.connection = connection;
        this.handler = handler;
        this.simulation = simulation;
        this.dataUpdateManager = dataUpdateManager;
        this.metrics = metrics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            this.startMonitoring();
            this.simulation.start();
            this.startServer();
        } catch (final Exception ex) {
            LOGGER.error("Application start-up failed", ex);
        }
    }

    /**
     * Start server (blocking).
     *
     * @throws InterruptedException connection thread was interrupted
     * @throws SSLException         indicates some kind of error detected by an SSL subsystem
     * @throws CertificateException indicates one of a variety of certificate problems
     */
    private void startServer()
            throws InterruptedException, SSLException, CertificateException {
        this.dataUpdateManager.startListening();
        Runtime.getRuntime().addShutdownHook(new Thread(this.connection::dispose));
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