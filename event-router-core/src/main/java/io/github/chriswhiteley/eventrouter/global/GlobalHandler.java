package io.github.chriswhiteley.eventrouter.global;

import io.github.chriswhiteley.eventrouter.GlobalEvent;
import io.github.chriswhiteley.eventrouter.NamedEvent;
import io.github.chriswhiteley.eventrouter.RemoteHandler;
import io.github.chriswhiteley.eventrouter.RemoteServiceEvent;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@EqualsAndHashCode(of = {"fromServiceId", "toServiceId"})
@ToString
@Getter
@Builder
public class GlobalHandler implements RemoteHandler {

    private final String fromServiceId;
    private final String toServiceId;
    private final String remoteServicesSite;

    /** 🔑 Explicit dependency */
    private final GlobalEventPublisher publisher;

    @Override
    public <E extends NamedEvent> void handle(E e) {
        try {
            GlobalEvent globalEvent = GlobalEvent.toGlobalEvent(e, fromServiceId);

            if (globalEvent.getDestinationServices().isEmpty()
                    || globalEvent.getDestinationServices().contains(toServiceId)) {

                RemoteServiceEvent remoteServiceEvent =
                        RemoteServiceEvent.builder()
                                .event(globalEvent)
                                .remoteServiceId(toServiceId)
                                .build();

                log.trace("Global handler {} dispatching event {}", this, e);
                publisher.publish(remoteServiceEvent);
            }
        } catch (Exception ex) {
            log.error("Error handling Global event {}", e, ex);
        }
    }

    @Override
    public Set<Class<? extends NamedEvent>> events() {
        return Set.of(NamedEvent.class);
    }

    @Override
    public String getRemoteService() {
        return toServiceId;
    }
}
