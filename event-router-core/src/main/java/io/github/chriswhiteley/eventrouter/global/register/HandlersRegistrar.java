package io.github.chriswhiteley.eventrouter.global.register;

import io.github.chriswhiteley.eventrouter.EventHandler;
import io.github.chriswhiteley.eventrouter.EventHandlers;
import io.github.chriswhiteley.eventrouter.RemoteHandler;
import io.github.chriswhiteley.eventrouter.global.GlobalEventPublisher;
import io.github.chriswhiteley.eventrouter.global.GlobalHandler;
import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByRemoteServices;
import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByService;
import io.github.chriswhiteley.eventrouter.global.register.persistence.RemoteServiceHandledEventsStore;
import io.github.chriswhiteley.messaging.ClosableConsumer;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The HandlersRegistrar class is responsible for managing global event handlers for remote services.
 */

@Slf4j
public class HandlersRegistrar {

    private final EventHandlers eventHandlers;
    private final ClosableConsumer<EventsHandledByService> consumer;
    private final RemoteServiceHandledEventsStore handledEventsStore;
    private final String serviceId;
    private final String serviceDomainName;
    private final GlobalEventPublisher globalEventPublisher;

    private ExecutorService executor;
    private final Duration consumeTimeout = Duration.ofSeconds(5);
    private final int initialBackoff = 1000; // ms

    public HandlersRegistrar(
            EventHandlers eventHandlers,
            ClosableConsumer<EventsHandledByService> consumer,
            RemoteServiceHandledEventsStore handledEventsStore,
            String serviceId,
            String serviceDomainName,
            GlobalEventPublisher globalEventPublisher
    ) {
        this.eventHandlers = Objects.requireNonNull(eventHandlers);
        this.consumer = Objects.requireNonNull(consumer);
        this.handledEventsStore = Objects.requireNonNull(handledEventsStore);
        this.serviceId = Objects.requireNonNull(serviceId);
        this.serviceDomainName = Objects.requireNonNull(serviceDomainName);
        this.globalEventPublisher = Objects.requireNonNull(globalEventPublisher);
    }

    // ----------------------------
    // Lifecycle
    // ----------------------------

    @EventHandler(name = "onStartup")
    public void init() {
        if (serviceId.isBlank() || serviceDomainName.isBlank()) {
            throw new IllegalArgumentException("serviceId and serviceDomainName must not be blank");
        }

        log.info("Starting HandlersRegistrar consumer...");
        registerHandlers(handledEventsStore.fetch());
        startConsumerThread();
    }

    @EventHandler(name = "onShutdown")
    public void stop() {
        shutdown();
    }

    // ----------------------------
    // Consumer Thread
    // ----------------------------

    private void startConsumerThread() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "HANDLERS-CONSUMER");
            t.setDaemon(true);
            return t;
        });

        executor.submit(this::runConsumer);
    }

    private void shutdown() {
        try {
            consumer.close();
        } catch (Exception e) {
            log.warn("Error closing consumer", e);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void runConsumer() {
        log.info("HandlersRegistrar consumer started");
        final int[] backoff = { initialBackoff };
        final int maxBackoff = 16_000;

        EventsHandledByRemoteServices current = handledEventsStore.fetch();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                consumer.consume(consumeTimeout)
                        .ifPresent(eventsHandledByService -> {
                            try {
                                log.info("CONSUMED {}", eventsHandledByService);

                                if (!eventsHandledByService.serviceId().equals(this.serviceId)) {
                                    updateHandlersForService(current.get(eventsHandledByService.serviceId(), eventsHandledByService.serviceDomain()), eventsHandledByService);
                                    current.put(eventsHandledByService);
                                    handledEventsStore.save(current);
                                }

                                backoff[0] = initialBackoff; // reset backoff after success
                            } catch (Exception e) {
                                log.error("Error processing eventsHandledByService {}", eventsHandledByService, e);
                            }
                        });
            } catch (Exception e) {
                log.error("Error consuming eventsHandledByService, backing off {}ms", backoff[0], e);

                try {
                    Thread.sleep(backoff[0]);
                    backoff[0] = Math.min(maxBackoff, backoff[0] * 2);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.info("HandlersRegistrar consumer stopped");
    }

    // ----------------------------
    // Business logic (same as before)
    // ----------------------------

    private void registerHandlers(EventsHandledByRemoteServices current) {
        current.forEach(service ->
                service.handledEvents().forEach(event ->
                        eventHandlers.add(
                                event,
                                GlobalHandler.builder()
                                        .fromServiceId(serviceId)
                                        .toServiceId(service.serviceId())
                                        .remoteServicesDomain(service.serviceDomain())
                                        .publisher(globalEventPublisher)
                                        .build()
                        )
                ));
    }

    private void updateHandlersForService(
            EventsHandledByService current,
            EventsHandledByService newEvents
    ) {
        var remoteServiceId = newEvents.serviceId();
        var remoteDomainName = newEvents.serviceDomain();

        if (RemoteHandler.domainsInSameBranch(this.serviceDomainName, remoteDomainName)) {
            Set<String> addedEvents = new HashSet<>(newEvents.handledEvents());
            addedEvents.removeAll(current.handledEvents());

            Set<String> deletedEvents = new HashSet<>(current.handledEvents());
            deletedEvents.removeAll(newEvents.handledEvents());

            addedEvents.forEach(event ->
                    eventHandlers.add(
                            event,
                            GlobalHandler.builder()
                                    .fromServiceId(serviceId)
                                    .toServiceId(remoteServiceId)
                                    .remoteServicesDomain(remoteDomainName)
                                    .publisher(globalEventPublisher)
                                    .build()
                    ));

            deletedEvents.forEach(event ->
                    eventHandlers.remove(
                            event,
                            GlobalHandler.builder()
                                    .fromServiceId(serviceId)
                                    .toServiceId(remoteServiceId)
                                    .remoteServicesDomain(remoteDomainName)
                                    .build()
                    ));
        }
    }
}
