module io.github.chriswhiteley.eventrouter.kafka {

    // ===== Core router API =====
    requires io.github.chriswhiteley.eventrouter.core;

    // ===== Messaging =====
    requires messaging.core;
    requires kafka.messaging;

    // ===== Kafka client =====
    requires kafka.clients;

    // ===== JSON =====
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    // ===== Logging =====
    requires org.slf4j;

    // ===== Compile-time only =====
    requires static lombok;

    // ===== Exports =====
    exports io.github.chriswhiteley.eventrouter.kafka;
    exports io.github.chriswhiteley.eventrouter.kafka.register;
    exports io.github.chriswhiteley.eventrouter.kafka.register.data;
}
