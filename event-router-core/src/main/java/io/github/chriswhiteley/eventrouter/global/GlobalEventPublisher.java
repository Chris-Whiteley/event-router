package io.github.chriswhiteley.eventrouter.global;

import io.github.chriswhiteley.eventrouter.RemoteServiceEvent;

public interface GlobalEventPublisher {
    void publish(RemoteServiceEvent event);
}
