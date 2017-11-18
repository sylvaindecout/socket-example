package test.sdc.socket.server.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simulation: initialize repository with an initial list of values, then add another one every second. Values are incremented integers.
 */
public final class Simulation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Simulation.class);

    private static final List<String> INITIAL_STATE = Arrays.asList("1", "2", "3", "4");

    private final DataRepository repository;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Constructor.
     *
     * @param repository data repository
     */
    @Inject
    public Simulation(final DataRepository repository) {
        this.repository = repository;
    }

    /**
     * Start simulation.
     */
    public void start() {
        LOGGER.info("Starting simulation...");
        this.repository.setElements(INITIAL_STATE);
        addNext("5", Duration.ofSeconds(1));
    }

    /**
     * Schedule next update.
     *
     * @param element element element that is to be added
     * @param delay   delay delay before next update
     */
    private void addNext(final String element, final Duration delay) {
        this.executor.schedule(() -> updateRepository(element, delay),
                delay.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Update data repository.
     *
     * @param element element that is to be added
     * @param delay   delay before next update
     */
    private void updateRepository(final String element, final Duration delay) {
        repository.add(element);
        final Integer currentValue = Integer.parseInt(element);
        if (currentValue < 5000) {
            final String nextElement = String.valueOf(currentValue + 1);
            this.addNext(nextElement, delay);
        } else {
            LOGGER.info("Simulation completed");
            this.start();
        }
    }

}