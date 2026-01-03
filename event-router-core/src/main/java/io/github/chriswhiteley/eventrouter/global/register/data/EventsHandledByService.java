package io.github.chriswhiteley.eventrouter.global.register.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.List;

public record EventsHandledByService(
        String serviceId,
        String serviceSite,
        Collection<String> handledEvents
) {
    @JsonCreator
    public EventsHandledByService(
            @JsonProperty("serviceId") String serviceId,
            @JsonProperty("serviceSite") String serviceSite,
            @JsonProperty("handledEvents") Collection<String> handledEvents
    ) {
        if (serviceId == null || serviceId.isBlank())
            throw new IllegalArgumentException("Service ID cannot be null or blank");
        if (serviceSite == null || serviceSite.isBlank())
            throw new IllegalArgumentException("Service site cannot be null or blank");

        this.serviceId = serviceId;
        this.serviceSite = serviceSite;
        this.handledEvents = (handledEvents != null)
                ? List.copyOf(handledEvents)
                : List.of();

    }

    public int size() {
        return handledEvents.size();
    }
}
