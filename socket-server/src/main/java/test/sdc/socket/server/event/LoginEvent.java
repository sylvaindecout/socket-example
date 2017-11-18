package test.sdc.socket.server.event;

import io.netty.channel.Channel;

/**
 * Event triggered when a client logs in.
 */
public final class LoginEvent {

    private final String login;
    private final Channel channel;

    /**
     * Constructor.
     *
     * @param login   login
     * @param channel channel
     */
    public LoginEvent(final String login, final Channel channel) {
        this.login = login;
        this.channel = channel;
    }

    /**
     * Get login.
     *
     * @return login
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * Get channel.
     *
     * @return channel
     */
    public Channel getChannel() {
        return this.channel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Client %s logged in", this.login);
    }

}