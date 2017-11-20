package test.sdc.socket.client.event;

/**
 * Event triggered when login failed.
 */
public final class LoginFailureEvent {

    private String msgRefId;

    /**
     * Constructor.
     *
     * @param msgRefId message reference, which can be used to associate response to request
     */
    public LoginFailureEvent(final String msgRefId) {
        this.msgRefId = msgRefId;
    }

    /**
     * Get message reference.
     *
     * @return message reference, which can be used to associate response to request
     */
    public String getMsgRefId() {
        return this.msgRefId;
    }

}