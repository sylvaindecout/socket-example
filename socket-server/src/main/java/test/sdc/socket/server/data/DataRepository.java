package test.sdc.socket.server.data;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
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
     * @param metrics metric registry
     */
    @Inject
    public DataRepository(final EventBus eventBus, final MetricRegistry metrics) {
        super(eventBus);
        metrics.register(MetricRegistry.name(this.getClass(), "size"),
                (Gauge<Integer>) DataRepository.this.elements::size);
    }

}