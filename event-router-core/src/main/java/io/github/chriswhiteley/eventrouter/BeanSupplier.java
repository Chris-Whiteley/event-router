package io.github.chriswhiteley.eventrouter;

import java.util.Collection;

public interface BeanSupplier {
    Collection<Object> getAllBeans();
}
