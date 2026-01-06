package io.github.chriswhiteley.eventrouter.micronaut;

import io.github.chriswhiteley.eventrouter.*;
import io.github.chriswhiteley.eventrouter.global.GlobalEventPublisher;
import io.github.chriswhiteley.eventrouter.global.GlobalEventsConsumer;
import io.github.chriswhiteley.eventrouter.global.GlobalEventsProducer;
import io.github.chriswhiteley.eventrouter.global.register.HandledProducer;
import io.github.chriswhiteley.eventrouter.global.register.HandlersRegistrar;
import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByService;
import io.github.chriswhiteley.eventrouter.global.register.persistence.RemoteServiceHandledEventsStore;
import io.github.chriswhiteley.messaging.ClosableConsumer;
import io.github.chriswhiteley.messaging.Producer;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Factory
public class EventRouterFactory {

    @Singleton
    LocalHandlerFactory localHandlerFactory() {
        return new ThreadLocalHandlerFactory();
    }

    @Singleton
    EventHandlers eventHandlers(LocalHandlerFactory handlerFactory) {
        return new EventHandlers(handlerFactory);
    }

    @Singleton
    EventDispatcher eventDispatcher(EventHandlers eventHandlers) {
        return new EventDispatcher(eventHandlers);
    }

    @Singleton
    GlobalEventsProducer globalEventsProducer(Producer<RemoteServiceEvent> producer) {
        return new GlobalEventsProducer(producer);
    }

    @Singleton
    GlobalEventsConsumer globalEventsConsumer(ClosableConsumer<NamedEvent> consumer, EventDispatcher eventDispatcher) {
        return new GlobalEventsConsumer(consumer, eventDispatcher);
    }

    @Singleton
    Startup eventRouterStartup(
            EventHandlers handlers,
            BeanSupplier beanSupplier,
            EventDispatcher dispatcher
    ) {
        return new Startup(handlers, beanSupplier, dispatcher);
    }

    @Singleton
    HandledProducer handledProducer(
            Producer<EventsHandledByService> producer,
            EventHandlers eventHandlers,
            @Named("serviceId") String serviceId,
            @Named("serviceSiteName") String serviceSiteName
    ) {
        return new HandledProducer(producer, eventHandlers, serviceId, serviceSiteName);
    }

    @Singleton
    HandlersRegistrar handlersRegistrar(
            EventHandlers eventHandlers,
            ClosableConsumer<EventsHandledByService> consumer,
            RemoteServiceHandledEventsStore handledEventsStore,
            @Named("serviceId") String serviceId,
            @Named("serviceSiteName") String serviceSiteName,
            GlobalEventPublisher globalEventPublisher
    ) {
        return new HandlersRegistrar(
                eventHandlers,
                consumer,
                handledEventsStore,
                serviceId,
                serviceSiteName,
                globalEventPublisher
        );
    }
}
