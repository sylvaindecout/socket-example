package test.sdc.socket.server.interfacing;

import com.google.common.eventbus.EventBus;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.common.DataCompressionFormat;
import test.sdc.socket.common.interfacing.LeChannelInitializer;
import test.sdc.socket.model.protocol.MessageProtos.Message;

import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.SSLException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;

/**
 * Server connection.
 */
public class ServerConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnection.class);

    private final Boolean sslEnabled;
    private final Integer port;
    private final Charset encodingCharset;
    private final DataCompressionFormat compression;
    private final EventBus eventBus;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private volatile Channel channel = null;

    /**
     * Constructor.
     *
     * @param sslEnabled      enable SSL
     * @param port            port used to expose service
     * @param encodingCharset encoding character set
     * @param compression     data compression format (optional)
     * @param eventBus        event bus
     */
    @Inject
    public ServerConnection(@Named("sslEnabled") final Boolean sslEnabled,
                            @Named("port") final Integer port, final Charset encodingCharset,
                            final DataCompressionFormat compression,
                            final EventBus eventBus) {
        this.sslEnabled = sslEnabled;
        this.port = port;
        this.encodingCharset = encodingCharset;
        this.compression = compression;
        this.eventBus = eventBus;
    }

    /**
     * Initialize SSL context.
     *
     * @return SSL context
     * @throws SSLException         indicates some kind of error detected by an SSL subsystem
     * @throws CertificateException indicates one of a variety of certificate problems
     */
    private static SslContext initSslContext()
            throws CertificateException, SSLException {
        final SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }

    /**
     * Start server.
     *
     * @param handler IVEF message handler, which handles received data
     * @throws InterruptedException connection thread was interrupted
     * @throws SSLException         indicates some kind of error detected by an SSL subsystem
     * @throws CertificateException indicates one of a variety of certificate problems
     */
    public void start(final SimpleChannelInboundHandler<Message> handler)
            throws InterruptedException, CertificateException, SSLException {
        LOGGER.info("Starting server");

        // Configure SSL.
        final SslContext sslContext = this.sslEnabled ? initSslContext() : null;

        // Configure the server.
        final ServerBootstrap bootstrap = new ServerBootstrap()
                .group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.TRACE))
                .childHandler(LeChannelInitializer.forServer(sslContext, handler, this.encodingCharset, this.compression));

        // Start the server.
        final ChannelFuture f = bootstrap.bind(this.port).sync();

        LOGGER.info("Server startup completed");

        // Wait until the server socket is closed.
        f.channel().closeFuture().sync();
    }

    /**
     * Liberate resources gracefully.
     */
    public void dispose() {
        LOGGER.info("Terminating all threads");
        // Shut down all event loops to terminate all threads.
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
    }

}