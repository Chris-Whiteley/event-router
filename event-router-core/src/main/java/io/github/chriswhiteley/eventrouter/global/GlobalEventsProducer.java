package io.github.chriswhiteley.eventrouter.global;

import io.github.chriswhiteley.eventrouter.EventHandler;
import io.github.chriswhiteley.eventrouter.RemoteServiceEvent;
import io.github.chriswhiteley.messaging.Producer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class GlobalEventsProducer implements GlobalEventPublisher {

    private final Producer<RemoteServiceEvent> producer;
    private final BlockingQueue<RemoteServiceEvent> retryableQueue;

    private volatile boolean messagingAvailable = true;

    private static final int RETRY_DELAY_MS = 5000;

    private ExecutorService executorService;

    public GlobalEventsProducer(Producer<RemoteServiceEvent> producer) {
        this.producer = producer;
        this.retryableQueue = new ArrayBlockingQueue<>(500_000);
    }

    @EventHandler(name = "onStartup")
    public void start() {
        startProducer();
    }

    @EventHandler(name = "onShutdown")
    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Override
    public void publish(RemoteServiceEvent event) {
        try {
            if (!event.isRetryable() && !messagingAvailable) {
                log.info("Dropping non-retryable event: {}", event);
                return;
            }

            if (!messagingAvailable) {
                retryableQueue.put(event);
            } else {
                sendEvent(event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void startProducer() {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "G-EVT-PRODUCER");
            t.setDaemon(true);
            return t;
        });
        executorService.submit(this::processEvents);
    }

    private void processEvents() {
        log.info("GlobalEventsProducer started");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                RemoteServiceEvent event = retryableQueue.take();

                if (!messagingAvailable) {
                    Thread.sleep(RETRY_DELAY_MS);
                    retryableQueue.put(event);
                } else {
                    sendEvent(event);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Unexpected error while processing events", e);
            }
        }

        log.info("GlobalEventsProducer stopped");
    }

    private void sendEvent(RemoteServiceEvent event) {
        try {
            producer.produce(event);
            messagingAvailable = true;
        } catch (Exception e) {
            log.error("Failed to send event {}", event, e);
            messagingAvailable = false;
            if (event.isRetryable()) {
                retryableQueue.offer(event);
            }
        }
    }
}
