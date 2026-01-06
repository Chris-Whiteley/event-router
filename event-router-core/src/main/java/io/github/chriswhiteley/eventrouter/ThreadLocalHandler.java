package io.github.chriswhiteley.eventrouter;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Getter
@Slf4j
public class ThreadLocalHandler extends LocalHandler {

    private final String handledEventName;
    private final BlockingQueue<NamedEvent> eventQueue =
            new ArrayBlockingQueue<>(10_000);

    private final Thread queueConsumerThread;

    @Builder
    private ThreadLocalHandler(
            String handledEventName,
            Object handlerObject,
            Method handlerMethod,
            int noOfParameters
    ) {
        super(handlerObject, handlerMethod, noOfParameters);
        this.handledEventName = handledEventName;

        // Start a virtual thread to consume events
        this.queueConsumerThread = Thread.startVirtualThread(this::runQueueConsumer);
    }

    private void runQueueConsumer() {
        Thread.currentThread()
              .setName("EVT-" + handledEventName + "-" + handlerMethod.getName());

        long lastQueueSizeReport = System.currentTimeMillis();

        try {
            while (!Thread.currentThread().isInterrupted()) {
                NamedEvent event = eventQueue.take(); // blocks efficiently
                invoke(event);

                if (eventQueue.size() > 10
                        && System.currentTimeMillis() - lastQueueSizeReport > 60_000) {
                    log.info("Event queue size is {}, handler={}",
                            eventQueue.size(), handledEventName);
                    lastQueueSizeReport = System.currentTimeMillis();
                }
            }
        } catch (InterruptedException e) {
            // Restore interrupt flag and exit cleanly
            Thread.currentThread().interrupt();
            log.debug("Queue consumer interrupted, shutting down: {}", handledEventName);
        } catch (Exception e) {
            log.error("Unhandled error in queue consumer for handler {}", handledEventName, e);
        }
    }

    @Override
    public <E extends NamedEvent> void handle(E event) {
        try {
            eventQueue.put(event); // back-pressure when full
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while enqueuing event {}", event);
        }
    }

    /**
     * Gracefully stops the handler thread.
     */
    public void shutdown() {
        queueConsumerThread.interrupt();
    }

    @Override
    public Set<Class<? extends NamedEvent>> events() {
        // No parameters → handles all events
        if (noOfParameters == 0) {
            return Set.of(NamedEvent.class);
        }

        Class<?> paramType = handlerMethod.getParameterTypes()[0];

        if (!NamedEvent.class.isAssignableFrom(paramType)) {
            throw new IllegalStateException(
                    "Handler method parameter " + paramType.getName()
                            + " is not a NamedEvent subtype"
            );
        }

        @SuppressWarnings("unchecked")
        Class<? extends NamedEvent> eventClass =
                (Class<? extends NamedEvent>) paramType;

        return Set.of(eventClass);
    }
}
