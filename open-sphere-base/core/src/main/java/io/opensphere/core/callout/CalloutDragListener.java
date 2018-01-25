package io.opensphere.core.callout;

import io.opensphere.core.math.Vector2i;

/**
 * Listener to be notified when a callout is dragged.
 *
 * @param <E> The type of the key associated with the callout.
 */
@FunctionalInterface
public interface CalloutDragListener<E>
{
    /**
     * Method called when a callout is dragged.
     *
     * @param key The key associated with the callout.
     * @param offset The new anchor offset.
     * @param index The index of the callout.
     */
    void calloutDragged(E key, Vector2i offset, int index);
}
