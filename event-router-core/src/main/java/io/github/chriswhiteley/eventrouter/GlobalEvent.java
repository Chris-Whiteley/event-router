package io.github.chriswhiteley.eventrouter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;

@Slf4j
@ToString(callSuper = true, doNotUseGetters = true)
public class GlobalEvent<E> extends Event<E> {

    @Getter
    private final String fromServiceId;

    @Setter
    private String sourceJson;
    @Setter
    private Class<?> sourceClass;


    @Override
    public E getSource(TypeReference<E> sourceTypeRef) {
        if (this.source == null) {
            try {
                this.source = (E) getObjectMapper().readValue(sourceJson, sourceTypeRef);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "Failed to deserialize event source for event " + getName(), e
                );
            }
        }
        return this.source;
    }


    public E getSource() {
        if (this.source == null && sourceJson != null) {
            try {
                this.source = (E) getObjectMapper().readValue(sourceJson, sourceClass);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "Failed to deserialize event source for event " + getName(), e
                );

            }
        }

        return this.source;
    }

    public static GlobalEvent toGlobalEvent(NamedEvent e, String fromServiceId) {
        GlobalEventBuilder builder = GlobalEvent.globalBuilder();
        builder.name(e.getName());

        if (e instanceof GlobalEvent ge) {
            builder.source(ge.source);
            builder.sourceJson(ge.sourceJson);
            builder.sourceClass(ge.sourceClass);
            builder.destinationServices(ge.destinationServices);
        } else if (e instanceof Event ev) {
            builder.source(ev.getSource());
            builder.destinationServices(ev.destinationServices);
        } else {
            builder.destinationServices(Set.of());
        }

        builder.fromServiceId(fromServiceId);
        return builder.build();
    }

    @Builder(builderMethodName = "globalBuilder")
    GlobalEvent(String name, E source, Set<String> destinationServices,
                String fromServiceId, String sourceJson, Class<?> sourceClass) {

        super(name, source, destinationServices, null);

        if (fromServiceId == null || fromServiceId.isBlank()) {
            throw new IllegalArgumentException("fromServiceId must not be blank");
        }

        this.sourceJson = sourceJson;
        this.sourceClass = sourceClass;
        this.fromServiceId = fromServiceId;
    }
}
