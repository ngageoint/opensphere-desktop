package io.opensphere.core.event;

/**
 * Abstract implementation that provides a listener notification method.
 */
public abstract class AbstractEvent implements Event
{
    @Override
    public <T extends Event> void notifyListener(EventListener<T> listener)
    {
        @SuppressWarnings("unchecked")
        T event = (T)this;

        listener.notify(event);
    }
}
