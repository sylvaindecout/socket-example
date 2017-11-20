package test.sdc.socket.client.event;

/**
 * Event triggered when login succeeded.
 */
public final class LoginSuccessEvent {

    private String msgRefId;

    /**
     * Constructor.
     *
     * @param msgRefId message reference, which can be used to associate response to request
     */
    public LoginSuccessEvent(final String msgRefId) {
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