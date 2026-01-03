package io.github.chriswhiteley.eventrouter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Startup {

    private final EventHandlers handlers;
    private final BeanSupplier beanSupplier;
    private final EventDispatcher eventDispatcher;

    public Startup(
            EventHandlers handlers,
            BeanSupplier beanSupplier,
            EventDispatcher eventDispatcher
    ) {
        if (handlers == null || beanSupplier == null || eventDispatcher == null) {
            throw new IllegalArgumentException("arguments must not be null");
        }
        this.handlers = handlers;
        this.beanSupplier = beanSupplier;
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * Called once the application context has been fully initialised.
     */
    public void start() {
        log.info("Starting EventRouter initialization...");

        handlers.init(beanSupplier);

        log.info("EventRouter initialized successfully.");

        NamedEvent onStartupEvent = new NamedEvent("onStartup");
        eventDispatcher.dispatchEvent(onStartupEvent);

        log.info("Dispatched event: {}", onStartupEvent.getName());
    }
}
