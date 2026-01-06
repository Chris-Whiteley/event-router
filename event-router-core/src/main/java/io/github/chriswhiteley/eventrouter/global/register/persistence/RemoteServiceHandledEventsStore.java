package io.github.chriswhiteley.eventrouter.global.register.persistence;

import io.github.chriswhiteley.eventrouter.global.register.data.EventsHandledByRemoteServices;

public interface RemoteServiceHandledEventsStore {
    EventsHandledByRemoteServices fetch();
    void save (EventsHandledByRemoteServices eventsHandledByRemoteServices);
}
