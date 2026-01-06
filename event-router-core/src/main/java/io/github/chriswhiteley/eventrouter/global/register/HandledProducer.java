package io.github.chriswhiteley.eventrouter.global.register;

import io.github.chriswhiteley.eventrouter.EventHandler;
import io.github.chriswhiteley.eventrouter.EventHandlers;
import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByService;
import io.github.chriswhiteley.messaging.Producer;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Produces messages about global events handled by this service,
 * intended to inform other services about this service's event-handling capabilities.
 */
@Slf4j
public class HandledProducer {

    private final Producer<EventsHandledByService> producer;
    private final EventHandlers eventHandlers;
    private final String serviceId;
    private final String serviceDomain;

    private volatile EventsHandledByService latestData;

    private final ScheduledExecutorService retryExecutor =
            Executors.newSingleThreadScheduledExecutor();

    private static final long INITIAL_RETRY_INTERVAL_MS = 5_000;
    private static final long MAX_RETRY_INTERVAL_MS = 60_000;

    private long currentRetryIntervalMs = INITIAL_RETRY_INTERVAL_MS;

    public HandledProducer(
            Producer<EventsHandledByService> producer,
            EventHandlers eventHandlers,
            String serviceId,
            String serviceDomain
    ) {
        this.producer = Objects.requireNonNull(producer, "producer must not be null");
        this.eventHandlers = Objects.requireNonNull(eventHandlers, "eventHandlers must not be null");
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId must not be null");
        this.serviceDomain = Objects.requireNonNull(serviceDomain, "serviceDomain must not be null");
    }

    @EventHandler(name = "onStartup")
    public void init() {
        if (serviceId.isBlank()) {
            throw new IllegalArgumentException("serviceId must not be blank");
        }

        if (serviceDomain.isBlank()) {
            throw new IllegalArgumentException("serviceDomain must not be blank");
        }

        log.info("Initializing HandledProducer...");
        produceGlobalEventsHandledByThisService();
        scheduleRetryTask();
    }

    @EventHandler(name = "onShutdown")
    public void onShutdown() {
        log.info("Shutting down HandledProducer...");
        retryExecutor.shutdown();
        try {
            if (!retryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                retryExecutor.shutdownNow();
                log.warn("Forced shutdown of retry executor");
            }
        } catch (InterruptedException e) {
            retryExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @EventHandler(name = "GlobalEventsHandledUpdated")
    public void produceGlobalEventsHandledByThisService() {
        latestData = createGlobalEventsHandled();
        attemptProduce();
    }

    private EventsHandledByService createGlobalEventsHandled() {
        return new EventsHandledByService(
                serviceId,
                serviceDomain,
                eventHandlers.getGlobalEventsHandled()
        );
    }

    private void attemptProduce() {
        if (latestData == null) {
            return;
        }

        try {
            producer.produce(latestData);
            log.info("Successfully produced global events handled by this service.");
            latestData = null;
            currentRetryIntervalMs = INITIAL_RETRY_INTERVAL_MS;
        } catch (Exception e) {
            log.warn("Failed to produce message, will retry later. Error: {}", e.getMessage());
        }
    }

    private void scheduleRetryTask() {
        retryExecutor.scheduleWithFixedDelay(
                () -> {
                    if (latestData != null) {
                        log.info("Retrying to produce global events...");
                        attemptProduceWithExponentialBackoff();
                    }
                },
                INITIAL_RETRY_INTERVAL_MS,
                INITIAL_RETRY_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    private void attemptProduceWithExponentialBackoff() {
        if (latestData == null) {
            return;
        }

        try {
            producer.produce(latestData);
            log.info("Successfully retried and produced global events.");
            latestData = null;
            currentRetryIntervalMs = INITIAL_RETRY_INTERVAL_MS;
        } catch (Exception e) {
            log.warn("Retry failed: {}", e.getMessage());
            increaseRetryInterval();
        }
    }

    private void increaseRetryInterval() {
        currentRetryIntervalMs =
                Math.min(currentRetryIntervalMs * 2, MAX_RETRY_INTERVAL_MS);

        log.info("Increasing retry interval to {} ms", currentRetryIntervalMs);

        retryExecutor.schedule(
                this::retryProduce,
                currentRetryIntervalMs,
                TimeUnit.MILLISECONDS
        );
    }

    private void retryProduce() {
        if (latestData != null) {
            log.info("Retrying to produce global events...");
            attemptProduceWithExponentialBackoff();
        }
    }
}
