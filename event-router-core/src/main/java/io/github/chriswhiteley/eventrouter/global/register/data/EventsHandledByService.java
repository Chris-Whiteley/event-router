package io.github.chriswhiteley.eventrouter.global.register.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.List;

public record EventsHandledByService(
        String serviceId,
        String serviceDomain,
        Collection<String> handledEvents
) {
    @JsonCreator
    public EventsHandledByService(
            @JsonProperty("serviceId") String serviceId,
            @JsonProperty("serviceDomain") String serviceDomain,
            @JsonProperty("handledEvents") Collection<String> handledEvents
    ) {
        if (serviceId == null || serviceId.isBlank())
            throw new IllegalArgumentException("Service ID cannot be null or blank");
        if (serviceDomain == null || serviceDomain.isBlank())
            throw new IllegalArgumentException("Service domain cannot be null or blank");

        this.serviceId = serviceId;
        this.serviceDomain = serviceDomain;
        this.handledEvents = (handledEvents != null)
                ? List.copyOf(handledEvents)
                : List.of();

    }

    public int size() {
        return handledEvents.size();
    }
}
