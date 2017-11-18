package test.sdc.socket.server.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.socket.server.event.DataUpdateEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Data repository.
 *
 * @param <T> data type
 */
abstract class GenericDataRepository<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericDataRepository.class);

    private final List<T> elements = new ArrayList<>();
    private final EventBus eventBus;

    /**
     * Constructor.
     *
     * @param eventBus event bus
     */
    public GenericDataRepository(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Replace list of elements.
     *
     * @param elements list of elements
     */
    public void setElements(final List<T> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    /**
     * Add element.
     *
     * @param element element
     */
    public void add(final T element) {
        this.elements.add(element);
        this.eventBus.post(new DataUpdateEvent<>(element));
    }

    /**
     * Get list of available elements.
     *
     * @return list of elements
     */
    public ImmutableCollection<T> findAll() {
        return ImmutableList.copyOf(this.elements);
    }

}