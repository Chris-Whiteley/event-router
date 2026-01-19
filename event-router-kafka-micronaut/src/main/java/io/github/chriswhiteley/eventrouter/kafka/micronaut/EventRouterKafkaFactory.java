package io.github.chriswhiteley.eventrouter.kafka.micronaut;

import io.github.chriswhiteley.eventrouter.NamedEvent;
import io.github.chriswhiteley.eventrouter.RemoteServiceEvent;
import io.github.chriswhiteley.eventrouter.kafka.EventConsumer;
import io.github.chriswhiteley.eventrouter.kafka.EventProducer;
import io.github.chriswhiteley.eventrouter.kafka.register.EventsHandledByServiceConsumer;
import io.github.chriswhiteley.eventrouter.kafka.register.EventsHandledByServiceProducer;
import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByService;
import io.github.chriswhiteley.messaging.ClosableConsumer;
import io.github.chriswhiteley.messaging.Producer;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Properties;

@Factory
public class EventRouterKafkaFactory {

    @Singleton
    Properties kafkaProperties(KafkaMicronautConfig config) {
        return config.toKafkaProperties();
    }

    @Singleton
    Producer<EventsHandledByService> eventsHandledByServiceProducer(Properties kafkaProperties) {
        return new EventsHandledByServiceProducer(kafkaProperties);
    }

    @Singleton
    ClosableConsumer<EventsHandledByService> eventsHandledByServiceConsumer(Properties kafkaProperties, @Named("serviceId") String serviceId) {
        return new EventsHandledByServiceConsumer(kafkaProperties, serviceId);
    }

    @Singleton
    Producer<RemoteServiceEvent> remoteServiceEventProducer(Properties kafkaProperties) {
        return new EventProducer(kafkaProperties, 1024 * 1024); // 1MB chunking
    }

    @Singleton
    ClosableConsumer<NamedEvent> remoteServiceEventConsumer(Properties kafkaProperties, @Named("serviceId") String serviceId) {
        return new EventConsumer(kafkaProperties, serviceId);
    }
}
