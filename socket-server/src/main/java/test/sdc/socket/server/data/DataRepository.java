package test.sdc.socket.server.data;

import com.google.common.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Data repository.
 */
public final class DataRepository
        extends GenericDataRepository<String> {

    /**
     * Constructor.
     *
     * @param eventBus event bus
     */
    @Inject
    public DataRepository(final EventBus eventBus) {
        super(eventBus);
    }

}