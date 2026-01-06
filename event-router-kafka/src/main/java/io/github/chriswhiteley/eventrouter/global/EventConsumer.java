package io.github.chriswhiteley.eventrouter.global;

import io.github.chriswhiteley.eventrouter.Event;
import io.github.chriswhiteley.eventrouter.NamedEvent;
import io.github.chriswhiteley.eventrouter.global.util.TopicNameHelper;
import io.github.chriswhiteley.messaging.kafka.AbstractKafkaChunkingConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@SuppressWarnings("unused") // library class for use as an implementation for cws-event-router
@Slf4j
public class EventConsumer extends AbstractKafkaChunkingConsumer<NamedEvent> {

    public EventConsumer(Properties properties, String serviceId) {
        this(null, properties, serviceId);
    }

    public EventConsumer(String eventsForServiceRootTopicName, Properties properties, String serviceId) {
        super(properties,
                TopicNameHelper.generateTopicName(
                        TopicNameHelper.provideDefaultTopicName(eventsForServiceRootTopicName, TopicNameHelper.DEFAULT_EVENTS_FOR_SERVICE_ROOT_TOPIC)
                        , serviceId
                ),
                serviceId
        );
    }

    @Override
    protected NamedEvent decode(String encodedEvent) {
        return Event.decode(encodedEvent);
    }
}


