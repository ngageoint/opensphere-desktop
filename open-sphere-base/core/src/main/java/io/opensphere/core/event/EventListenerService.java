package io.opensphere.core.event;

import java.util.concurrent.Executor;

import io.opensphere.core.util.ObservableValueService;

/**
 * A service that simplifies creating a controller that listens to events.
 */
public class EventListenerService extends ObservableValueService
{
    /** The event manager. */
    private final EventManager myEventManager;

    /**
     * Constructor.
     *
     * @param eventManager The event manager.
     */
    public EventListenerService(EventManager eventManager)
    {
        super();
        myEventManager = eventManager;
    }

    /**
     * Constructor.
     *
     * @param eventManager The event manager.
     * @param size the expected number of services (for memory savings)
     */
    public EventListenerService(EventManager eventManager, int size)
    {
        super(size);
        myEventManager = eventManager;
    }

    /**
     * Adds a listener handle to this service that will bind the subscriber to
     * the given event type when the service is opened.
     *
     * @param <T> The event type.
     * @param type The event type.
     * @param subscriber The listener instance.
     */
    public final <T extends Event> void bindEvent(Class<T> type, EventListener<? super T> subscriber)
    {
        addService(new EventManagerListenerHandle<>(myEventManager, type, subscriber));
    }

    /**
     * Adds a listener handle to this service that will bind the subscriber to
     * the given event type when the service is opened.
     *
     * @param <T> The event type.
     * @param type The event type.
     * @param subscriber The listener instance.
     * @param executor The executor on which to notify the subscriber.
     */
    public final <T extends Event> void bindEvent(Class<T> type, EventListener<? super T> subscriber, Executor executor)
    {
        addService(new EventManagerListenerHandle<>(myEventManager, type, subscriber, executor));
    }
}
