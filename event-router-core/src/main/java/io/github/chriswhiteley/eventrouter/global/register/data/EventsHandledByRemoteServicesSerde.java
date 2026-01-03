package io.github.chriswhiteley.eventrouter.global.register.data;

import com.cwsoft.eventrouter.global.SerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unused") // Utility class for serialization/deserialization
public class EventsHandledByRemoteServicesSerde {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // Configure as needed

    /**
     * Serializes an EventsHandledByRemoteServices object to a JSON string.
     *
     * @param eventsHandledByRemoteServices the object to serialize
     * @return the JSON string representation of the object
     * @throws SerializationException if serialization fails
     */
    public static String serialize(EventsHandledByRemoteServices eventsHandledByRemoteServices) {
        if (eventsHandledByRemoteServices == null) {
            log.error("Cannot serialize a null EventsHandledByRemoteServices object");
            throw new IllegalArgumentException("EventsHandledByRemoteServices cannot be null");
        }
        try {
            return objectMapper.writeValueAsString(eventsHandledByRemoteServices);
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Serialization error for EventsHandledByRemoteServices: {}", eventsHandledByRemoteServices, e);
            throw new SerializationException("Failed to serialize EventsHandledByRemoteServices", e);
        }
    }

    /**
     * Deserializes a JSON string to an EventsHandledByRemoteServices object.
     *
     * @param json the JSON string
     * @return the deserialized EventsHandledByRemoteServices object
     * @throws SerializationException if deserialization fails
     */
    public static EventsHandledByRemoteServices deserialize(String json) {
        if (json == null || json.isEmpty()) {
            log.error("Cannot deserialize a null or empty JSON string");
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        try {
            return objectMapper.readValue(json, EventsHandledByRemoteServices.class);
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Deserialization error for JSON: {}", json, e);
            throw new SerializationException("Failed to deserialize to EventsHandledByRemoteServices object", e);
        }
    }
}
