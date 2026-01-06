package io.github.chriswhiteley.eventrouter;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;


/**
 * Value class used to pass events round
 *
 * @param <E> the event payload type
 */


@Getter
@ToString(callSuper = true, doNotUseGetters = true)
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Event<E> extends NamedEvent {
    protected E source;

    public Event(String name, E source) {
        super(name);
        this.source = source;
    }

    @Builder
    public Event(String name, E source, @Singular Set <String> destinationServices, String domainInContext) {
        super(name);
        this.source = source;
        this.destinationServices =
                (destinationServices == null) ? Set.of() : destinationServices;
        this.domainInContext = (domainInContext == null)?"":domainInContext;
    }

    /**
     * Returns the event source deserialized using the provided {@link TypeReference}.
     * <p>
     * This method exists to handle cases where the event payload contains generic types
     * (e.g. {@code List<Foo>}). Due to Java type erasure, Jackson cannot infer the generic
     * type information during deserialization and will otherwise deserialize collections
     * as {@code Map} instances (e.g. {@code LinkedHashMap}).
     * <p>
     * Supplying a {@link TypeReference} preserves the full generic type information and
     * allows correct deserialization of complex payloads.
     *
     * @param sourceTypeRef the Jackson type reference describing the expected source type
     * @return the deserialized event source
     */
    public E getSource(TypeReference<E> sourceTypeRef) {
        throw new UnsupportedOperationException(
                "TypeReference-based deserialization is not supported for events of type " + getClass().getName() + "."
        );
    }



    public Event(String name, E source, String destinationService) {
        this(name, source, Set.of(destinationService), null);
    }

}
