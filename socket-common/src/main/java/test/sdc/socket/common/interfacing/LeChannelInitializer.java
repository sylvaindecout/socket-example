package test.sdc.socket.common.interfacing;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import test.sdc.socket.common.DataCompressionFormat;
import test.sdc.socket.model.protocol.MessageProtos.Message;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import static java.util.Objects.requireNonNull;

public class LeChannelInitializer
        extends ChannelInitializer<SocketChannel> {

    private final SslContext sslContext;
    private final SimpleChannelInboundHandler<Message> messageHandler;
    private final Charset encodingCharset;
    private final DataCompressionFormat compression;
    private final InetSocketAddress serverAddress;

    /**
     * Constructor.
     *
     * @param sslContext      SSL context (null if irrelevant)
     * @param messageHandler  message handler
     * @param encodingCharset encoding character set
     * @param compression     data compression format (optional)
     * @param serverAddress   server address (null on server side)
     */
    private LeChannelInitializer(final SslContext sslContext,
                                 final SimpleChannelInboundHandler<Message> messageHandler,
                                 final Charset encodingCharset,
                                 final DataCompressionFormat compression,
                                 final InetSocketAddress serverAddress) {
        super();
        this.sslContext = sslContext;
        this.messageHandler = messageHandler;
        this.encodingCharset = encodingCharset;
        this.compression = compression;
        this.serverAddress = serverAddress;
    }

    public static LeChannelInitializer forClient(final SslContext sslContext,
                                                 final SimpleChannelInboundHandler<Message> messageHandler,
                                                 final Charset encodingCharset,
                                                 final DataCompressionFormat compression,
                                                 final InetSocketAddress serverAddress) {
        return new LeChannelInitializer(sslContext, messageHandler, encodingCharset, compression, serverAddress);
    }

    public static LeChannelInitializer forServer(final SslContext sslContext,
                                                 final SimpleChannelInboundHandler<Message> messageHandler,
                                                 final Charset encodingCharset,
                                                 final DataCompressionFormat compression) {
        return new LeChannelInitializer(sslContext, messageHandler, encodingCharset, compression, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initChannel(final SocketChannel ch) {
        requireNonNull(ch, "Socket channel must not be null");
        final ChannelPipeline pipeline = ch.pipeline();
        if (this.sslContext != null) {
            final SslHandler sslHandler = this.serverAddress == null
                    ? this.sslContext.newHandler(ch.alloc())
                    : this.sslContext.newHandler(ch.alloc(), this.serverAddress.getHostString(), this.serverAddress.getPort());
            pipeline.addLast(sslHandler);
        }
        pipeline.addLast(new LoggingHandler(LogLevel.TRACE));
        if (this.compression != null) {
            pipeline.addLast(this.compression.newEncoder());
            pipeline.addLast(this.compression.newDecoder());
        }
        pipeline
                // Outgoing messages (encoders)
                .addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender())
                .addLast("protobufEncoder", new ProtobufEncoder())
                // Incoming messages (decoders + handlers)
                .addLast("frameDecoder", new ProtobufVarint32FrameDecoder())
                .addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance()))
                .addLast("messageHandler", this.messageHandler);
    }

}