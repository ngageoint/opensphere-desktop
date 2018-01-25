package io.opensphere.core.control;

import java.awt.event.InputEvent;

/**
 * Listener for a discrete event.
 *
 * @see DiscreteEventAdapter
 */
public interface DiscreteEventListener extends BoundEventListener
{
    /**
     * Method called when an event occurs.
     *
     * @param event The event.
     */
    void eventOccurred(InputEvent event);
}
