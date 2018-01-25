package io.opensphere.core.event;

/**
 * Basic interface for an event in the system.
 *
 * @see AbstractSingleStateEvent
 * @see AbstractMultiStateEvent
 */
public interface Event
{
    /**
     * Get a description of this event.
     *
     * @return The description.
     */
    String getDescription();

    /**
     * Get the current state of the event.
     *
     * @return The state of the event.
     */
    State getState();

    /**
     * Notify a listener about an event.
     *
     * @param <T> The event type.
     * @param listener The listener.
     */
    <T extends Event> void notifyListener(EventListener<T> listener);

    /** Enumeration of event states. */
    enum State
    {
        /** State indicating an event has started. */
        STARTED,

        /** State indicating an event is completed. */
        COMPLETED,

        /** State indicating an event has failed. */
        FAILED,

        /** State indicating an event was cancelled. */
        CANCELLED,
    }
}
