package io.github.chriswhiteley.eventrouter.kafka.micronaut;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ConfigurationProperties("event-router.kafka")
public class KafkaMicronautConfig {

    @NonNull
    private String bootstrapServers = "localhost:9092";

    @NonNull
    private Map<String, String> extraProperties = new HashMap<>();

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public Map<String, String> getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Map<String, String> extraProperties) {
        this.extraProperties = extraProperties;
    }

    /**
     * Convert to kafka client Properties
     */
    public Properties toKafkaProperties() {
        Properties props = new Properties();

        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // Allow overriding / adding anything
        props.putAll(extraProperties);

        return props;
    }
}
