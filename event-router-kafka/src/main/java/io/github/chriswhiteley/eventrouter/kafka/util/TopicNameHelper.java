package io.github.chriswhiteley.eventrouter.kafka.util;

public class TopicNameHelper {

    // Public constants for default topic names
    public static final String DEFAULT_EVENTS_FOR_SERVICE_ROOT_TOPIC = "events_for_service";
    public static final String DEFAULT_EVENTS_HANDLED_BY_SERVICE_TOPIC = "events_handled_by_service";

    /**
     * Provides a valid topic name. Defaults to a provided constant if the input is null or blank.
     *
     * @param topicName        User-supplied topic name
     * @param defaultTopicName Default topic name
     * @return A valid, sanitized topic name
     */
    public static String provideDefaultTopicName(String topicName, String defaultTopicName) {
        if (topicName == null || topicName.isBlank()) {
            return defaultTopicName;
        }
        return sanitizeTopicName(topicName);
    }

    /**
     * Generates a topic name by appending a serviceId to the root topic name.
     * Sanitizes both the root topic name and the serviceId.
     *
     * @param rootTopicName Root topic name
     * @param serviceId     Service identifier
     * @return A sanitized combined topic name
     */
    public static String generateTopicName(String rootTopicName, String serviceId) {
        var sanitizedRoot = sanitizeTopicName(rootTopicName);
        var sanitizedServiceId = sanitizeTopicName(serviceId);
        return sanitizedRoot + "_" + sanitizedServiceId;
    }

    /**
     * Sanitizes a topic name by replacing blanks with underscores.
     *
     * @param topicName Topic name to sanitize
     * @return Sanitized topic name
     */
    public static String sanitizeTopicName(String topicName) {
        return topicName.replaceAll("\\s+", "_");
    }
}

