package io.github.chriswhiteley.eventrouter.kafka.register;

import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByService;
import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByServiceSerde;
import io.github.chriswhiteley.eventrouter.kafka.util.TopicNameHelper;
import io.github.chriswhiteley.messaging.kafka.AbstractKafkaConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@SuppressWarnings("unused") // library class for use as an implementation for cws-event-router
@Slf4j
public class EventsHandledByServiceConsumer extends AbstractKafkaConsumer<EventsHandledByService> {

    public EventsHandledByServiceConsumer(Properties kafkaProperties, String serviceId) {
        this(null, kafkaProperties, serviceId);
    }

    public EventsHandledByServiceConsumer(String eventsHandledByServiceTopicName,  Properties properties, String serviceId) {
        super(properties,
                TopicNameHelper.sanitizeTopicName(
                        TopicNameHelper.provideDefaultTopicName(eventsHandledByServiceTopicName, TopicNameHelper.DEFAULT_EVENTS_HANDLED_BY_SERVICE_TOPIC)
                ),
                serviceId
        );
    }

    @Override
    protected EventsHandledByService decode(String s) {
        return EventsHandledByServiceSerde.deserialize(s);
    }
}
