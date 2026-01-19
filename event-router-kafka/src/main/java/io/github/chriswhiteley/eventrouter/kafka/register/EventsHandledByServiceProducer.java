package io.github.chriswhiteley.eventrouter.kafka.register;

import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByService;
import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByServiceSerde;
import io.github.chriswhiteley.eventrouter.kafka.util.TopicNameHelper;
import io.github.chriswhiteley.messaging.kafka.AbstractKafkaProducer;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@SuppressWarnings("unused") // library class for use as implementation for cws-event-router
@Slf4j
public class EventsHandledByServiceProducer extends AbstractKafkaProducer<EventsHandledByService> {
    private final String topicName;

    public EventsHandledByServiceProducer(Properties kafkaProperties) {
        this (null, kafkaProperties);
    }


    public EventsHandledByServiceProducer(String eventsHandledByServiceTopicName, Properties kafkaProperties) {
        super(kafkaProperties);

        this.topicName = TopicNameHelper.provideDefaultTopicName(
                eventsHandledByServiceTopicName,
                TopicNameHelper.DEFAULT_EVENTS_HANDLED_BY_SERVICE_TOPIC
        );
    }

    @Override
    public String getDestination(EventsHandledByService eventsHandledByService) {
        return topicName;
    }

    @Override
    public String getMessageName(EventsHandledByService eventsHandledByService) {
        return TopicNameHelper.DEFAULT_EVENTS_HANDLED_BY_SERVICE_TOPIC;
    }

    @Override
    public String encode(EventsHandledByService eventsHandledByService) {
        if (eventsHandledByService == null) {
            log.error("Cannot encode a null EventsHandledByService object");
            throw new IllegalArgumentException("EventsHandledByService cannot be null");
        }

        // Serialize the message using the Serde
        String serializedMessage = EventsHandledByServiceSerde.serialize(eventsHandledByService);
        log.debug("Encoded message for topic [{}]: {}", topicName, serializedMessage);
        return serializedMessage;
    }
}
