package io.opensphere.core.messaging;

import java.util.Collection;

/**
 * Interface for a subscriber.
 *
 * @param <E> The type of objects received by this subscriber.
 */
@FunctionalInterface
public interface GenericSubscriber<E>
{
    /**
     * Receive objects.
     *
     * @param source The source of the objects.
     * @param adds The added objects.
     * @param removes The removed objects.
     */
    void receiveObjects(Object source, Collection<? extends E> adds, Collection<? extends E> removes);
}
