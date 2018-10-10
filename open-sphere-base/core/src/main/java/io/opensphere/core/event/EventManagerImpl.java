package io.opensphere.core.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.opensphere.core.event.Event.State;
import io.opensphere.core.util.concurrent.FixedThreadPoolExecutor;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.ref.WeakReference;

/**
 * Implementation of {@link EventManager}.
 */
public class EventManagerImpl implements EventManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(EventManagerImpl.class);

    /** Executor used to publish events. */
    private final Executor myExecutor;

    /** My event listeners. */
    private final Map<Class<? extends Event>, Collection<WeakReference<EventListener<?>>>> mySubscribers = Collections
            .synchronizedMap(new HashMap<Class<? extends Event>, Collection<WeakReference<EventListener<?>>>>());

    /** Constructor. */
    public EventManagerImpl()
    {
        int priority = 1;
        int maxPriority = 1;
        NamedThreadFactory factory = new NamedThreadFactory("EventManager", priority, maxPriority);
        myExecutor = new FixedThreadPoolExecutor(1, factory);
    }

    @Override
    public <T extends Event> void publishEvent(final T event)
    {
        Class<?> type = event.getClass();

        // Start with the concrete event type
        while (Event.class.isAssignableFrom(type))
        {
            @SuppressWarnings("unchecked")
            Class<? extends Event> typedType = (Class<? extends Event>)type;
            publishEvent(event, typedType);

            for (Class<?> intf : type.getInterfaces())
            {
                if (Event.class.isAssignableFrom(intf))
                {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> typedIntf = (Class<? extends Event>)intf;
                    publishEvent(event, typedIntf);
                }
            }

            type = type.getSuperclass();
        }
    }

    @Override
    public <T extends AbstractMultiStateEvent> void setEventState(T event, State state)
    {
        event.setState(state);
        publishEvent(event);
    }

    @Override
    public <T extends Event> void subscribe(Class<T> type, EventListener<? super T> subscriber)
    {
        Collection<WeakReference<EventListener<?>>> subscribers;
        synchronized (mySubscribers)
        {
            subscribers = mySubscribers.get(type);
            if (subscribers == null)
            {
                subscribers = new ArrayList<>();
                mySubscribers.put(type, subscribers);
            }
        }
        synchronized (subscribers)
        {
            subscribers.add(new WeakReference<EventListener<?>>(subscriber));
        }
    }

    @Override
    public <T extends Event> void unsubscribe(Class<T> type, EventListener<? super T> subscriber)
    {
        Collection<WeakReference<EventListener<?>>> subscribers = mySubscribers.get(type);
        if (subscribers != null)
        {
            synchronized (subscribers)
            {
                for (Iterator<WeakReference<EventListener<?>>> iter = subscribers.iterator(); iter.hasNext();)
                {
                    WeakReference<EventListener<?>> ref = iter.next();
                    if (ref.get() == subscriber)
                    {
                        iter.remove();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Get the subscribers for a particular event type.
     *
     * @param <T> The event type.
     * @param type The event type.
     * @return The subscribers.
     */
    protected <T extends Event> Collection<EventListener<T>> getSubscribers(Class<T> type)
    {
        // Remove any that have been garbage-collected.
        Collection<EventListener<T>> subscribers;
        Collection<WeakReference<EventListener<?>>> refs = mySubscribers.get(type);
        if (refs != null)
        {
            synchronized (refs)
            {
                subscribers = new ArrayList<>(refs.size());
                for (Iterator<WeakReference<EventListener<?>>> iter = refs.iterator(); iter.hasNext();)
                {
                    WeakReference<EventListener<?>> ref = iter.next();
                    @SuppressWarnings("unchecked")
                    EventListener<T> subscriber = (EventListener<T>)ref.get();
                    if (subscriber == null)
                    {
                        iter.remove();
                    }
                    else
                    {
                        subscribers.add(subscriber);
                    }
                }
            }
        }
        else
        {
            subscribers = Collections.emptySet();
        }
        return subscribers;
    }

    /**
     * Publish an event to a specific set of subscribers.
     *
     * @param <S> The declared type of the event.
     * @param <T> The type of listener. This must be assignable from S.
     * @param event The event.
     * @param type The type of listener.
     */
    protected <S extends Event, T extends Event> void publishEvent(final S event, Class<T> type)
    {
        final Collection<EventListener<T>> subscribers = getSubscribers(type);

        if (!subscribers.isEmpty())
        {
            for (final EventListener<? super T> subscriber : subscribers)
            {
                myExecutor.execute(() ->
                {
                    try
                    {
                        long start = System.nanoTime();

                        // This is safe as long as T is assignable from S.
                        @SuppressWarnings("unchecked")
                        T castEvent = (T)event;
                        subscriber.notify(castEvent);

                        /* Check for subscribers that take a long time to handle
                         * the event */
                        long deltaNS = System.nanoTime() - start;
                        if (deltaNS > 200_000_000)
                        {
                            Object subscriberMsgProxy = LOGGER.isTraceEnabled() ? subscriber : subscriber.getClass().getName();
                            String message = StringUtilities.formatTimingMessage(subscriberMsgProxy + " took ", deltaNS)
                                    + " to handle event notification for " + castEvent;
                            LOGGER.log(deltaNS > 1_000_000_000 ? Level.ERROR : Level.WARN, message);
                        }
                    }
                    catch (RuntimeException e)
                    {
                        LOGGER.error("Exception while publishing an event: " + e, e);
                    }
                });
            }
        }
    }
}
