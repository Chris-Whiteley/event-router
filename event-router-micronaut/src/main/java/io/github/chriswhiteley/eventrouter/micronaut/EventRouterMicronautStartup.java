package io.github.chriswhiteley.eventrouter.micronaut;

import io.github.chriswhiteley.eventrouter.Startup;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class EventRouterMicronautStartup {

    private final Startup startup;

    public EventRouterMicronautStartup(Startup startup) {
        this.startup = startup;
    }

    @EventListener
    void onStartup(StartupEvent event) {
        log.info("Micronaut started — initializing EventRouter");
        startup.start();
    }
}
