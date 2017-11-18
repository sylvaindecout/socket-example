package test.sdc.socket.client.interfacing;

import com.google.common.eventbus.EventBus;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.client.event.ConnectionEstablishedEvent;
import test.sdc.socket.client.event.ConnectionLossEvent;
import test.sdc.socket.common.DataCompressionFormat;
import test.sdc.socket.common.interfacing.LeChannelInitializer;
import test.sdc.socket.model.protocol.MessageProtos.Message;

import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import static java.util.Objects.requireNonNull;

/**
 * Client connection.
 */
public class ClientConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnection.class);

    private final Boolean sslEnabled;
    private final InetSocketAddress serverAddress;
    private final Charset encodingCharset;
    private final DataCompressionFormat compression;
    private final EventBus eventBus;

    private final EventLoopGroup group = new NioEventLoopGroup();
    private volatile Channel channel = null;

    /**
     * Constructor.
     *
     * @param sslEnabled      enable SSL
     * @param serverAddress   server address
     * @param encodingCharset encoding character set
     * @param compression     data compression format (optional)
     * @param eventBus        event bus
     */
    @Inject
    public ClientConnection(@Named("sslEnabled") final Boolean sslEnabled,
                            final InetSocketAddress serverAddress, final Charset encodingCharset,
                            final DataCompressionFormat compression,
                            final EventBus eventBus) {
        this.sslEnabled = sslEnabled;
        this.serverAddress = serverAddress;
        this.encodingCharset = encodingCharset;
        this.compression = compression;
        this.eventBus = eventBus;
    }

    /**
     * Initialize SSL context.
     *
     * @return SSL context
     * @throws SSLException indicates some kind of error detected by an SSL subsystem
     */
    private static SslContext initSslContext()
            throws SSLException {
        return SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }

    /**
     * Connect to server.
     *
     * @param messageHandler message handler, which receives data from connection
     * @throws InterruptedException connection thread was interrupted
     * @throws SSLException         indicates some kind of error detected by an SSL subsystem
     */
    public void start(final SimpleChannelInboundHandler<Message> messageHandler)
            throws InterruptedException, SSLException {
        requireNonNull(messageHandler, "Message handler must not be null");
        LOGGER.info("Starting connection to {}", this.serverAddress);
        // Configure SSL
        final SslContext sslContext = this.sslEnabled ? initSslContext() : null;

        // Configure the client.
        final ChannelInitializer<SocketChannel> channelInitializer = LeChannelInitializer.forClient(
                sslContext, messageHandler, this.encodingCharset, this.compression, this.serverAddress);
        final Bootstrap bootstrap = new Bootstrap()
                .group(this.group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(channelInitializer);

        // Start the client.
        this.channel = bootstrap.connect(this.serverAddress).sync().channel();
        this.eventBus.post(new ConnectionEstablishedEvent());
        this.channel.closeFuture().addListener(new ChannelClosureListener());
        LOGGER.info("Connection startup completed successfully");
    }

    /**
     * Send message to server.
     *
     * @param message message
     * @throws InterruptedException message emission thread was interrupted
     */
    public void send(final Message message)
            throws InterruptedException {
        requireNonNull(message, "Input message must not be null");
        LOGGER.info("Sending message...");
        this.channel.writeAndFlush(message).sync();
    }

    /**
     * Liberate resources gracefully.
     */
    public void dispose() {
        LOGGER.info("Stopping client connection...");
        // Shut down the event loop to terminate all threads.
        this.group.shutdownGracefully();
    }

    /**
     * Connection channel closure listener.
     */
    private class ChannelClosureListener
            implements ChannelFutureListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void operationComplete(final ChannelFuture future)
                throws Exception {
            requireNonNull(future, "Result of an asynchronous Channel I/O operation must not be null");
            LOGGER.info("Connection to server was lost");
            ClientConnection.this.eventBus.post(new ConnectionLossEvent());
        }
    }

}