package io.opensphere.core.event;

/**
 * Interface to be implemented by those to be notified when events happen.
 *
 * @param <T> The event type.
 */
@FunctionalInterface
public interface EventListener<T>
{
    /**
     * Method called when an event begins or ends.
     *
     * @param event The event.
     */
    void notify(T event);
}
