package io.github.chriswhiteley.eventrouter.global;

import io.github.chriswhiteley.eventrouter.RemoteServiceEvent;
import io.github.chriswhiteley.eventrouter.global.util.TopicNameHelper;
import io.github.chriswhiteley.messaging.kafka.AbstractKafkaChunkingProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@SuppressWarnings("unused") // library class for use as an implementation for cws-event-router
@Slf4j
public class EventProducer extends AbstractKafkaChunkingProducer<RemoteServiceEvent> {

    private final String eventsForServiceRootTopicName;

    public EventProducer(Properties kafkaProperties, int maxMessageSize) {
        this(null, kafkaProperties, maxMessageSize);
    }

    public EventProducer(String eventsForServiceRootTopicName, Properties kafkaProperties, int maxMessageSize) {
        super(kafkaProperties, maxMessageSize);

        this.eventsForServiceRootTopicName = TopicNameHelper.provideDefaultTopicName(
                eventsForServiceRootTopicName,
                TopicNameHelper.DEFAULT_EVENTS_FOR_SERVICE_ROOT_TOPIC
        );
    }

    @Override
    protected String getDestination(RemoteServiceEvent remoteServiceEvent) {
        var destinationServiceId = remoteServiceEvent.getRemoteServiceId();
        return TopicNameHelper.generateTopicName(eventsForServiceRootTopicName, destinationServiceId);
    }

    @Override
    protected String getMessageName(RemoteServiceEvent remoteServiceEvent) {
        return remoteServiceEvent.getEvent().getName();
    }

    @Override
    protected String encode(RemoteServiceEvent remoteServiceEvent) {
        try {
            return remoteServiceEvent.getEvent().encode();
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error encoding remote service event", e);
        }
    }
}
