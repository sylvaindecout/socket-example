package test.sdc.socket.server.event;

/**
 * Event triggered when new data is added to repository.
 *
 * @param <T> data type
 */
public final class DataUpdateEvent<T> {

    private final T updatedElement;

    /**
     * Constructor.
     *
     * @param updatedElement updated data element
     */
    public DataUpdateEvent(final T updatedElement) {
        this.updatedElement = updatedElement;
    }

    /**
     * Get updated data element.
     *
     * @return updated data element
     */
    public T getUpdatedElement() {
        return this.updatedElement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Data update: %s", this.updatedElement);
    }

}