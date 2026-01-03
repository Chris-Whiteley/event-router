package io.github.chriswhiteley.eventrouter;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class EventDispatcher {

    private final EventHandlers eventHandlers;

    public EventDispatcher(EventHandlers eventHandlers) {
        this.eventHandlers = eventHandlers;
        log.info("EventDispatcher created (DI version)");
    }

    // ------------------------
    // Registration
    // ------------------------

    public void registerLocalHandler(Object handlerBean, String methodName, String eventName)
            throws NoSuchMethodException {
        registerHandler(handlerBean, methodName, eventName, false);
    }

    public void registerGlobalHandler(Object handlerBean, String methodName, String eventName)
            throws NoSuchMethodException {
        registerHandler(handlerBean, methodName, eventName, true);
    }

    private void registerHandler(
            Object handlerBean,
            String methodName,
            String eventName,
            boolean global
    ) throws NoSuchMethodException {

        if (handlerBean == null) throw new NullPointerException("handlerObject is null");
        if (methodName == null || methodName.isEmpty())
            throw new IllegalArgumentException("method name is null or empty");
        if (eventName == null || eventName.isEmpty())
            throw new IllegalArgumentException("eventName is null or empty");

        Method handlerMethod;
        try {
            handlerMethod = handlerBean.getClass().getMethod(methodName, Event.class);
        } catch (NoSuchMethodException ex) {
            handlerMethod = handlerBean.getClass().getMethod(methodName);
        }

        var subscriber = SubscriberEndPoint.builder()
                .forEvent(eventName)
                .bean(handlerBean)
                .method(handlerMethod)
                .access(global ? Access.GLOBAL : Access.LOCAL)
                .build();

        eventHandlers.registerSubscriber(subscriber);

        if (global) {
            dispatchEvent(new Event("GlobalEventsHandledUpdated", eventName));
        }
    }

    // ------------------------
    // Dispatching
    // ------------------------

    public DispatchNote generateDispatchNote(NamedEvent event) {
        var builder = DispatchNote.builder();

        eventHandlers.get(event.getName())
                .stream()
                .filter(handler -> handlerInContext(handler, event))
                .forEach(handler -> fillInDispatchNote(handler, builder));

        builder.event(event);
        return builder.build();
    }

    public DispatchNote dispatchEvent(NamedEvent event) {
        var builder = DispatchNote.builder();

        eventHandlers.get(event.getName())
                .stream()
                .filter(handler -> handlerInContext(handler, event))
                .forEach(handler -> {
                    log.trace("dispatching event {} to handler {}", event, handler);
                    handler.handle(event);
                    fillInDispatchNote(handler, builder);
                });

        builder.event(event);
        return builder.build();
    }

    public void dispatchGlobalEventLocally(NamedEvent event) {
        eventHandlers.getGlobalHandler(event.getName())
                .forEach(handler -> {
                    log.trace("dispatching global event {} to local handler {}", event, handler);
                    handler.handle(event);
                });
    }

    // ------------------------
    // Internal helpers
    // ------------------------

    private boolean handlerInContext(Handler handler, NamedEvent event) {
        if (handler instanceof LocalHandler) return true;

        if (handler instanceof RemoteHandler remoteHandler) {
            if (event.getDestinationServices() != null && !event.getDestinationServices().isEmpty()) {
                return event.getDestinationServices().contains(remoteHandler.getRemoteService());
            }

            if (event.getSiteInContext().isBlank()) return true;

            return RemoteHandler.sitesInSameBranch(
                    remoteHandler.getRemoteServicesSite(),
                    event.getSiteInContext()
            );
        }

        return true;
    }

    private void fillInDispatchNote(Handler handler, DispatchNote.DispatchNoteBuilder builder) {
        if (handler instanceof LocalHandler local) {
            builder.localEndPoint(local.getLocalEndPoint());
        } else if (handler instanceof RemoteHandler remote) {
            builder.remoteService(remote.getRemoteService());
        }
    }
}
