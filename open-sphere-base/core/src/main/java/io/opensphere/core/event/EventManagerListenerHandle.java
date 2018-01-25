package io.opensphere.core.event;

import java.util.concurrent.Executor;

import io.opensphere.core.util.Service;

/**
 * Listener handle for EventManager events.
 *
 * @param <T> The event type.
 */
public class EventManagerListenerHandle<T extends Event> implements Service
{
    /** The event manager. */
    private final EventManager myEventManager;

    /** The event type. */
    private Class<T> myType;

    /** The listener instance. */
    private EventListener<? super T> mySubscriber;

    /**
     * Constructor.
     *
     * @param eventManager the event manager.
     * @param type The event type.
     * @param subscriber The listener instance.
     */
    public EventManagerListenerHandle(EventManager eventManager, Class<T> type, EventListener<? super T> subscriber)
    {
        this(eventManager, type, subscriber, null);
    }

    /**
     * Constructor.
     *
     * @param eventManager the event manager.
     * @param type The event type.
     * @param subscriber The listener instance.
     * @param executor The executor on which to notify the subscriber
     */
    public EventManagerListenerHandle(EventManager eventManager, Class<T> type, EventListener<? super T> subscriber,
            Executor executor)
    {
        myEventManager = eventManager;
        myType = type;
        mySubscriber = executor == null ? subscriber : event -> executor.execute(() -> subscriber.notify(event));
    }

    /**
     * Constructor.
     *
     * @param eventManager the event manager.
     */
    protected EventManagerListenerHandle(EventManager eventManager)
    {
        this(eventManager, null, null);
    }

    @Override
    public void open()
    {
        myEventManager.subscribe(myType, mySubscriber);
    }

    @Override
    public void close()
    {
        myEventManager.unsubscribe(myType, mySubscriber);
    }

    /**
     * Sets the subscriber.
     *
     * @param type The event type.
     * @param subscriber The listener instance.
     */
    protected void setSubscriber(Class<T> type, EventListener<? super T> subscriber)
    {
        myType = type;
        mySubscriber = subscriber;
    }
}
