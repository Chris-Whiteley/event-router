package io.github.chriswhiteley.eventrouter.global.register.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public record EventsHandledByRemoteServices(
        Map<String, EventsHandledByService> handledEventsMap
) implements Iterable<EventsHandledByService> {

    public EventsHandledByRemoteServices() {
        this(new HashMap<>());
    }

    public EventsHandledByService get(String serviceId, String serviceSite) {
        if (handledEventsMap.containsKey(serviceId)) {
            return handledEventsMap.get(serviceId);
        }
        return new EventsHandledByService(serviceId, serviceSite, null);
    }

    public void put(EventsHandledByService remoteServiceHandledEvents) {
        handledEventsMap.put(remoteServiceHandledEvents.serviceId(), remoteServiceHandledEvents);
    }

    @Override
    public Iterator<EventsHandledByService> iterator() {
        return handledEventsMap.values().iterator();
    }

    @Override
    public String toString() {
        return "EventsHandledByRemoteServices{" +
                "handledEventsMap=" + handledEventsMap +
                '}';
    }
}
