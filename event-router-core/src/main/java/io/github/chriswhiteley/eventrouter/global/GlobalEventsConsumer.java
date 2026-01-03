package io.github.chriswhiteley.eventrouter.global;

import io.github.chriswhiteley.eventrouter.EventDispatcher;
import io.github.chriswhiteley.eventrouter.EventHandler;
import io.github.chriswhiteley.eventrouter.NamedEvent;
import io.github.chriswhiteley.messaging.ClosableConsumer;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class GlobalEventsConsumer {

    private final ClosableConsumer<NamedEvent> consumer;
    private final EventDispatcher eventDispatcher;

    private final Duration consumeTimeout;
    private final int initialBackoff;

    private ExecutorService executorService;

    public GlobalEventsConsumer(
            ClosableConsumer<NamedEvent> consumer,
            EventDispatcher eventDispatcher
    ) {
        this.consumer = consumer;
        this.eventDispatcher = eventDispatcher;
        this.consumeTimeout = Duration.ofSeconds(5);
        this.initialBackoff = 1000;
    }

    @EventHandler(name = "onStartup")
    public void start() {
        startConsumerThread();
    }

    @EventHandler(name = "onShutdown")
    public void stop() {
        shutdown();
    }

    private void startConsumerThread() {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "G-EVT-CONSUMER");
            t.setDaemon(true);
            return t;
        });
        executorService.submit(this::runConsumer);
    }

    private void shutdown() {
        try {
            consumer.close();
        } catch (Exception e) {
            log.warn("Error while closing consumer", e);
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void runConsumer() {
        log.info("GlobalEventsConsumer started");

        final int[] backoff = {initialBackoff};
        final int maxBackoff = 16_000;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                consumer.consume(consumeTimeout)
                        .ifPresent(event -> {
                            try {
                                eventDispatcher.dispatchGlobalEventLocally(event);
                                backoff[0] = initialBackoff;
                            } catch (Exception e) {
                                log.error("Error dispatching event {}", event, e);
                            }
                        });
            } catch (Exception e) {
                log.error("Error consuming event, backing off {}ms", backoff[0], e);
                try {
                    Thread.sleep(backoff[0]);
                    backoff[0] = Math.min(maxBackoff, backoff[0] * 2);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.info("GlobalEventsConsumer stopped");
    }
}
