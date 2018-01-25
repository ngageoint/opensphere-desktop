package io.opensphere.core.event;

/**
 * Abstract implementation that provides a mutable state.
 */
public abstract class AbstractMultiStateEvent extends AbstractEvent
{
    /** The current state of the event. */
    private Event.State myState = Event.State.STARTED;

    @Override
    public synchronized State getState()
    {
        return myState;
    }

    /**
     * Set the current state of the event. This should only be called from the
     * {@link EventManager}.
     *
     * @param state The new state of the event.
     */
    synchronized void setState(Event.State state)
    {
        myState = state;
    }
}
