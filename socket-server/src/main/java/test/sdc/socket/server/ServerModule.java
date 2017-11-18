package test.sdc.socket.server;

import com.google.common.base.Charsets;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import dagger.Module;
import dagger.Provides;
import io.netty.channel.SimpleChannelInboundHandler;
import test.sdc.socket.common.DataCompressionFormat;
import test.sdc.socket.model.protocol.MessageProtos.Message;
import test.sdc.socket.server.data.DataRepository;
import test.sdc.socket.server.interfacing.DataUpdateManager;
import test.sdc.socket.server.interfacing.ServerMessageHandler;
import test.sdc.socket.server.session.ClientRegistry;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

/**
 * Module in charge of building the dependency graph of the application.
 */
@Module(injects = {Server.class})
final class ServerModule {

    @Named("port")
    @Provides
    Integer provideServerPort() {
        return 12345;
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
    SimpleChannelInboundHandler<Message> provideMessageHandler(final ServerMessageHandler handler) {
        return handler;
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        return new AsyncEventBus(Executors.newCachedThreadPool());
    }

    @Provides
    @Singleton
    ClientRegistry provideClientRegistry(final EventBus eventBus) {
        return new ClientRegistry(eventBus);
    }

    @Provides
    @Singleton
    DataRepository provideDataRepository(final EventBus eventBus) {
        return new DataRepository(eventBus);
    }

    @Provides
    @Singleton
    DataUpdateManager provideDataUpdateManager(final ClientRegistry registry, final EventBus eventBus) {
        return new DataUpdateManager(registry, eventBus);
    }

}