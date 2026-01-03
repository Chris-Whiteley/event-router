package io.github.chriswhiteley.eventrouter;

public interface LocalHandlerFactory {
    LocalHandler newHandler (SubscriberEndPoint subscriberEndPoint);
}
