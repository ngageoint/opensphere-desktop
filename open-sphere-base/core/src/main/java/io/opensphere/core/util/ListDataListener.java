package io.opensphere.core.util;

import java.util.EventListener;

/**
 * Listener for ListDataEvent events.
 *
 * @param <E> the type of elements in the list
 */
public interface ListDataListener<E> extends EventListener
{
    /**
     * Called after elements have been added.
     *
     * @param e a <code>ListDataEvent</code> encapsulating the event information
     */
    void elementsAdded(ListDataEvent<E> e);

    /**
     * Called after elements have been removed.
     *
     * @param e a <code>ListDataEvent</code> encapsulating the event information
     */
    void elementsRemoved(ListDataEvent<E> e);

    /**
     * Called after elements have been changed.
     *
     * @param e a <code>ListDataEvent</code> encapsulating the event information
     */
    void elementsChanged(ListDataEvent<E> e);
}
