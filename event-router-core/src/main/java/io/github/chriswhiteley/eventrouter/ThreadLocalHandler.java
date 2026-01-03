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
    private final BlockingQueue<NamedEvent> eventQueue = new ArrayBlockingQueue<>(10_0000);


    @Builder
    private ThreadLocalHandler(String handledEventName, Object handlerObject, Method handlerMethod, int noOfParameters) {
       super(handlerObject, handlerMethod, noOfParameters);
       this.handledEventName = handledEventName;

        // now set up queue monitor thread to monitor the queue for events
        Thread queueConsumerThread = new Thread(() -> runQueueConsumer());
        queueConsumerThread.start();
    }

    private void runQueueConsumer() {
        Thread.currentThread().setName("EVT-" + handledEventName + "-" + handlerMethod.getName());

        var timeLastQueueSizeReport = System.currentTimeMillis();

        while (!Thread.interrupted()) {
            NamedEvent event = null;
            try {
                event = eventQueue.take();
                invoke(event);

                if (eventQueue.size() > 10 && (System.currentTimeMillis() - timeLastQueueSizeReport) > 60_000) {
                    log.info("event queue size is > 10, size is {}", eventQueue.size());
                    timeLastQueueSizeReport = System.currentTimeMillis();
                }


            } catch (InterruptedException e) {
            } catch (Exception e) {
                log.error ("error processing event from queue, {}", event);
            }
        }
    }

    @Override
    public <E extends NamedEvent> void handle(E e){
        try {
             eventQueue.put(e);
        } catch (InterruptedException interruptedException) {
        }
    }

    @Override
    public Set<Class<? extends NamedEvent>> events() {
        // if the handler method takes no parameters, we can't infer the event type;
        // so fall back to NamedEvent (means "handles all")
        if (noOfParameters == 0) {
            return Set.of(NamedEvent.class);
        }

        // otherwise extract from the single method parameter
        Class<?> paramType = handlerMethod.getParameterTypes()[0];

        if (!NamedEvent.class.isAssignableFrom(paramType)) {
            throw new IllegalStateException(
                    "Handler method parameter " + paramType.getName()
                            + " is not a NamedEvent subtype");
        }

        @SuppressWarnings("unchecked")
        Class<? extends NamedEvent> eventClass =
                (Class<? extends NamedEvent>) paramType;

        return Set.of(eventClass);
    }

}
