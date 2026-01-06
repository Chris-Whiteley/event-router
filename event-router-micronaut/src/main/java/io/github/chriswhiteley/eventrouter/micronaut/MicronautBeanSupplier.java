package io.github.chriswhiteley.eventrouter.micronaut;

import io.github.chriswhiteley.eventrouter.BeanSupplier;
import io.github.chriswhiteley.eventrouter.RemoteServiceEvent;
import io.github.chriswhiteley.messaging.Producer;
import io.micronaut.context.BeanContext;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.stream.Collectors;

@Singleton
public class MicronautBeanSupplier implements BeanSupplier {

    private final BeanContext beanContext;
    private final Producer<RemoteServiceEvent> producer;

    public MicronautBeanSupplier(BeanContext beanContext, Producer<RemoteServiceEvent> producer) {
        System.out.println("🔥 MicronautBeanSupplier active");
        this.beanContext = beanContext;
        this.producer = producer;
    }

    @Override
    public Collection<Object> getAllBeans() {

        return beanContext.getBeansOfType(Object.class)
                .stream()
                .filter(bean -> !bean.getClass().getPackageName().startsWith("io.micronaut"))
                .collect(Collectors.toList());

    }

}
