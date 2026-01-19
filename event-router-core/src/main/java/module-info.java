module io.github.chriswhiteley.eventrouter.core {

    requires static lombok;
    requires org.slf4j;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires messaging.core;

    // ===== Public API =====
    exports io.github.chriswhiteley.eventrouter;
    exports io.github.chriswhiteley.eventrouter.global;
    exports io.github.chriswhiteley.eventrouter.global.register;
    exports io.github.chriswhiteley.eventrouter.global.register.data;
}
