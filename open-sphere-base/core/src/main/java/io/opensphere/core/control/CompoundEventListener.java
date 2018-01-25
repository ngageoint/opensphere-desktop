package io.opensphere.core.control;

import java.awt.event.InputEvent;

/**
 * An event listener that handles compound events.
 */
public interface CompoundEventListener extends BoundEventListener
{
    /**
     * Called when an event ends.
     *
     * @param event The event.
     */
    void eventEnded(InputEvent event);

    /**
     * Called when an event starts.
     *
     * @param event The event.
     */
    void eventStarted(InputEvent event);
}
