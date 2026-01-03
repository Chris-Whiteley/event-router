package io.github.chriswhiteley.eventrouter;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe class to manage and maintain local and global event handlers for a service.
 */
@Slf4j
public class EventHandlers {
    public final Class<? extends Annotation> END_POINT_ANNOTATION = EventHandler.class;

    private final LocalHandlerFactory handlerFactory;

    public EventHandlers(LocalHandlerFactory handlerFactory) {
        this.handlerFactory = Objects.requireNonNull(handlerFactory);
    }

    /**
     * Thread-safe map of event names to sets of handlers.
     */
    private final Map<String, Set<Handler>> handlersMap = new ConcurrentHashMap<>();

    /**
     * Thread-safe set of global events handled by the service.
     */
    @Getter
    private final Set<String> globalEventsHandled = ConcurrentHashMap.newKeySet();

    /**
     * Thread-safe map of global handlers.
     */
    private final Map<String, Set<LocalHandler>> globalHandlersMap = new ConcurrentHashMap<>();


    /* =======================================================================
       🔹 Introspection helpers (used by EventRouterHealthCheck)
       ======================================================================= */

    /** @return number of unique handlers registered (local + global) */
    public int size() {                                   // 🔹 added
        return all().size();
    }

    /**
     * @return all handlers (local + global), deduplicated.
     *         Returned collection is immutable to prevent modification.
     */
    public Collection<Handler> all() {                     // 🔹 added
        Set<Handler> all = new HashSet<>();
        handlersMap.values().forEach(all::addAll);
        globalHandlersMap.values().forEach(all::addAll);
        return Collections.unmodifiableSet(all);
    }

    /**
     * @return all event names known to this service
     */
    public Collection<String> events() {                   // 🔹 added
        Set<String> names = new HashSet<>();
        names.addAll(handlersMap.keySet());
        names.addAll(globalHandlersMap.keySet());
        return Collections.unmodifiableSet(names);
    }


    /* =======================================================================
       Existing API
       ======================================================================= */

    public Collection<Handler> get(String forEvent) {
        return handlersMap.getOrDefault(forEvent, Collections.emptySet());
    }

    public void add(String eventName, Handler handler) {
        log.trace("Adding handler for eventName = {}, handler = {}", eventName, handler);
        handlersMap.computeIfAbsent(eventName, k -> ConcurrentHashMap.newKeySet()).add(handler);
    }

    private void addGlobal(String eventName, LocalHandler localHandler) {
        log.trace("Adding global handler for eventName = {}, localHandler = {}", eventName, localHandler);
        globalHandlersMap.computeIfAbsent(eventName, k -> ConcurrentHashMap.newKeySet()).add(localHandler);
        globalEventsHandled.add(eventName);
    }

    public Collection<LocalHandler> getGlobalHandler(String forEvent) {
        return globalHandlersMap.getOrDefault(forEvent, Collections.emptySet());
    }

    public synchronized void remove(String eventName, Handler handler) {
        log.trace("Removing handler for eventName = {}, handler = {}", eventName, handler);
        Set<Handler> handlers = handlersMap.get(eventName);
        if (handlers != null) {
            handlers.remove(handler);
            if (handlers.isEmpty()) {
                handlersMap.remove(eventName);
            }
        }
    }

    public void init(BeanSupplier beanSupplier) {
        try {
            log.info("Initializing EventDispatcher - scanning for subscriber end point handlers.");
            getSubscribers(beanSupplier).forEach(this::registerSubscriber);
        } catch (Exception ex) {
            log.error("Error while scanning for subscriber end points", ex);
        }
    }

    private Collection<SubscriberEndPoint> getSubscribers(BeanSupplier beanSupplier) {
        Collection<SubscriberEndPoint> subscribers = new ArrayList<>();

        getAllEndPoints(beanSupplier).forEach(endPoint -> {
            EventHandler[] eventHandlerAnnotations = endPoint.method.getAnnotationsByType(EventHandler.class);

            for (EventHandler eventHandlerAnnotation : eventHandlerAnnotations) {
                for (String event : eventHandlerAnnotation.name()) {
                    subscribers.add(SubscriberEndPoint.builder()
                            .forEvent(event)
                            .bean(endPoint.bean)
                            .method(endPoint.method)
                            .accessSet(Arrays.asList(eventHandlerAnnotation.access()))
                            .build());
                }
            }
        });

        return subscribers;
    }

    private Collection<EndPoint> getAllEndPoints(BeanSupplier beanSupplier) {
        Collection<EndPoint> endPoints = new HashSet<>();

        beanSupplier.getAllBeans().forEach(bean ->
                getAllMethods(bean.getClass()).forEach(method -> {
                    if (method.isAnnotationPresent(END_POINT_ANNOTATION)) {
                        endPoints.add(
                                EndPoint.builder()
                                        .bean(bean)
                                        .method(method)
                                        .build()
                        );
                    }
                }));

        return endPoints;
    }

    void registerSubscriber(SubscriberEndPoint subscriberEndPoint) {
        LocalHandler localHandler = handlerFactory.newHandler(subscriberEndPoint);

        if (subscriberEndPoint.hasLocalAccess()) {
            this.add(subscriberEndPoint.getForEvent(), localHandler);
        }

        if (subscriberEndPoint.hasGlobalAccess()) {
            this.addGlobal(subscriberEndPoint.getForEvent(), localHandler);
            log.trace("Added global event {}, globalEventsHandled now contains {}",
                    subscriberEndPoint.getForEvent(), globalEventsHandled);
        }
    }

    private List<Method> getAllMethods(Class<?> forClass) throws FindEndPointException {
        try {
            List<Method> allMethods = new ArrayList<>();
            Class<?> clazz = forClass;

            while (clazz != null) {
                allMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
                clazz = clazz.getSuperclass();
            }

            return allMethods;
        } catch (Exception | NoClassDefFoundError ex) {
            String msg = String.format("Failed to obtain methods of class %s", forClass);
            throw new FindEndPointException(msg, ex);
        }
    }

    @Builder
    private static class EndPoint {
        private final Object bean;
        private final Method method;
    }
}
