package test.sdc.socket.client.session;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.client.event.ConnectionEstablishedEvent;
import test.sdc.socket.client.event.LoginFailureEvent;
import test.sdc.socket.client.interfacing.ClientConnection;
import test.sdc.socket.model.protocol.MessageProtos.Message;
import test.sdc.socket.model.protocol.login.LoginRequestProtos.LoginRequest;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * In charge of deciding when to send login requests.
 */
public final class LoginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginManager.class);

    private static final Duration DELAY_UNTIL_NEXT_ATTEMPT = Duration.ofMinutes(1L);

    private final String login;
    private final String password;
    private final ClientConnection connection;
    private final EventBus eventBus;

    /**
     * Constructor.
     *
     * @param login      login
     * @param password   password
     * @param connection connection
     * @param eventBus   event bus
     */
    @Inject
    public LoginManager(@Named("login") final String login, @Named("password") final String password, final ClientConnection connection, final EventBus eventBus) {
        this.login = login;
        this.password = password;
        this.connection = connection;
        this.eventBus = eventBus;
    }

    /**
     * Start listening to events.
     */
    public void startListening() {
        this.eventBus.register(this);
    }

    /**
     * Listen to connection established events.
     *
     * @param event connection established event
     */
    @Subscribe
    public void onConnectionEstablished(final ConnectionEstablishedEvent event) {
        try {
            LOGGER.trace("Connection established event: {}", event);
            this.sendLoginRequest();
        } catch (final Exception ex) {
            LOGGER.error("Failed to process connection established event", ex);
        }
    }

    /**
     * Listen to failed login events.
     *
     * @param event failed login event
     */
    @Subscribe
    public void onLoginFailure(final LoginFailureEvent event) {
        try {
            LOGGER.trace("Login failure event: {}", event);
            Executors.newSingleThreadScheduledExecutor().schedule(this::sendLoginRequest,
                    DELAY_UNTIL_NEXT_ATTEMPT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (final Exception ex) {
            LOGGER.error("Failed to process login failure event", ex);
        }
    }

    /**
     * Send login request to server.
     */
    private void sendLoginRequest() {
        final Message request = Message.newBuilder()
                .setMsgRefId(UUID.randomUUID().toString())
                .setLoginRequest(LoginRequest.newBuilder()
                        .setLogin("sdc")
                        .setPassword("")
                        .build())
                .build();
        LOGGER.debug("Sending login request to server: {}", request);
        try {
            this.connection.send(request);
        } catch (final InterruptedException ex) {
            LOGGER.warn("Login request emission failed: {}", ex.getMessage());
            this.eventBus.post(new LoginFailureEvent());
        }
    }

}