package io.opensphere.core.event;

/**
 * Abstract implementation of an event that only has one state:
 * {@link Event.State#COMPLETED}.
 */
public abstract class AbstractSingleStateEvent implements Event
{
    @Override
    public final State getState()
    {
        return Event.State.COMPLETED;
    }

    @Override
    public <T extends Event> void notifyListener(EventListener<T> listener)
    {
        @SuppressWarnings("unchecked")
        T event = (T)this;

        listener.notify(event);
    }
}
