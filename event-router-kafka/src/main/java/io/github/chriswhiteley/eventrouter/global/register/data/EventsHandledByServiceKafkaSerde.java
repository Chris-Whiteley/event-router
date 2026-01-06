package io.github.chriswhiteley.eventrouter.global.register.data;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
@SuppressWarnings("unused") // library class for use by messaging implementation
public class EventsHandledByServiceKafkaSerde implements Serde<EventsHandledByService> {

    private final Serializer<EventsHandledByService> serializer;
    private final Deserializer<EventsHandledByService> deserializer;

    public EventsHandledByServiceKafkaSerde() {
        this.serializer = new EventsHandledByServiceSerializer();
        this.deserializer = new EventsHandledByServiceDeserializer();
    }

    @Override
    public Serializer<EventsHandledByService> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<EventsHandledByService> deserializer() {
        return deserializer;
    }

    /**
     * Serializer implementation for EventsHandledByService.
     */
    public static class EventsHandledByServiceSerializer implements Serializer<EventsHandledByService> {

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // No specific configuration needed
        }

        @Override
        public byte[] serialize(String topic, EventsHandledByService data) {
            if (data == null) {
                log.warn("Null data received for serialization");
                return null;
            }
            try {
                String jsonString = EventsHandledByServiceSerde.serialize(data);
                return jsonString.getBytes();
            } catch (Exception e) {
                log.error("Serialization failed for topic [{}] and data [{}]", topic, data, e);
                throw new RuntimeException("Failed to serialize EventsHandledByService", e);
            }
        }

        @Override
        public void close() {
            // No resources to close
        }
    }

    /**
     * Deserializer implementation for EventsHandledByService.
     */
    public static class EventsHandledByServiceDeserializer implements Deserializer<EventsHandledByService> {

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // No specific configuration needed
        }

        @Override
        public EventsHandledByService deserialize(String topic, byte[] data) {
            if (data == null || data.length == 0) {
                log.warn("Null or empty data received for deserialization");
                return null;
            }
            try {
                String jsonString = new String(data);
                return EventsHandledByServiceSerde.deserialize(jsonString);
            } catch (Exception e) {
                log.error("Deserialization failed for topic [{}] and data [{}]", topic, new String(data), e);
                throw new RuntimeException("Failed to deserialize EventsHandledByService", e);
            }
        }

        @Override
        public void close() {
            // No resources to close
        }
    }
}
