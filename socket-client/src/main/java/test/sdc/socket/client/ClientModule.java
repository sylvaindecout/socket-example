package test.sdc.socket.client;

import com.google.common.base.Charsets;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import dagger.Module;
import dagger.Provides;
import io.netty.channel.SimpleChannelInboundHandler;
import test.sdc.socket.client.interfacing.ClientConnection;
import test.sdc.socket.client.interfacing.ClientMessageHandler;
import test.sdc.socket.client.session.LoginManager;
import test.sdc.socket.common.DataCompressionFormat;
import test.sdc.socket.model.protocol.MessageProtos;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

/**
 * Module in charge of building the dependency graph of the application.
 */
@Module(injects = {Client.class})
final class ClientModule {

    @Provides
    InetSocketAddress provideServerAddress() {
        return new InetSocketAddress(12345);
    }

    @Named("login")
    @Provides
    String provideLogin() {
        return "sdc";
    }

    @Named("password")
    @Provides
    String providePassword() {
        return "";
    }

    @Named("sslEnabled")
    @Provides
    Boolean provideSslActivation() {
        return false;
    }

    @Provides
    Charset provideEncodingCharset() {
        return Charsets.UTF_8;
    }

    @Provides
    DataCompressionFormat provideDataCompressionFormat() {
        return null;
    }

    @Provides
    SimpleChannelInboundHandler<MessageProtos.Message> provideMessageHandler(final ClientMessageHandler handler) {
        return handler;
    }

    @Provides
    @Singleton
    ClientConnection provideConnection(@Named("sslEnabled") final Boolean sslEnabled,
                                       final InetSocketAddress serverAddress, final Charset encodingCharset,
                                       final DataCompressionFormat compression,
                                       final EventBus eventBus) {
        return new ClientConnection(sslEnabled, serverAddress, encodingCharset, compression, eventBus);
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        return new AsyncEventBus(Executors.newCachedThreadPool());
    }

    @Provides
    @Singleton
    LoginManager provideLoginManager(@Named("login") final String login, @Named("password") final String password,
                                     final ClientConnection connection, final EventBus eventBus) {
        return new LoginManager(login, password, connection, eventBus);
    }

}