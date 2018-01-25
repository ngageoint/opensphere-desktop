package io.opensphere.core.event;

/**
 * Manager for generic events in the system. Events may occur immediately or be
 * scheduled for a future time. They may also be instantaneous or have a
 * duration. Interested parties may register to be notified when events begin or
 * end.
 */
public interface EventManager
{
    /**
     * Publish an event immediately.
     *
     * @param <T> The event type.
     * @param event The event.
     */
    <T extends Event> void publishEvent(T event);

    /**
     * Set the state of an event. This will notify subscribers.
     *
     * @param <T> The event type.
     * @param event The event.
     * @param state The new event state.
     */
    <T extends AbstractMultiStateEvent> void setEventState(T event, Event.State state);

    /**
     * Subscribe to events that are of a particular type or its descendants.
     * Only a weak reference will be held to the subscriber, so a strong
     * reference must be held elsewhere to prevent the subscriber from being
     * garbage collected.
     *
     * @param <T> The event type.
     * @param type The event type.
     * @param subscriber The listener instance.
     */
    <T extends Event> void subscribe(Class<T> type, EventListener<? super T> subscriber);

    /**
     * Unsubscribe from events that are of a particular type.
     *
     * @param <T> The event type.
     * @param type The event type.
     * @param subscriber The listener instance.
     */
    <T extends Event> void unsubscribe(Class<T> type, EventListener<? super T> subscriber);
}
