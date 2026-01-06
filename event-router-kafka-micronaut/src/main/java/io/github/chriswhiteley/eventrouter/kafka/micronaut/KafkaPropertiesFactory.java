package io.github.chriswhiteley.eventrouter.kafka.micronaut;

import jakarta.inject.Singleton;

import java.util.Properties;

@Singleton
public class KafkaPropertiesFactory {

    @Singleton
    public Properties kafkaProperties(KafkaMicronautConfig config) {
        return config.toKafkaProperties();
    }
}