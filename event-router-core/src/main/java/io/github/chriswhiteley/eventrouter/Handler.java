package io.github.chriswhiteley.eventrouter;

import java.util.Set;

public interface Handler {
    <E extends NamedEvent> void handle(E e);

    /**
     * @return the set of NamedEvent classes this handler is responsible for
     */
    Set<Class<? extends NamedEvent>> events();
}
