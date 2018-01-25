package io.opensphere.core.util.swing.input.model;

import java.util.EventListener;

/**
 * Defines an object which listens for PropertyChangeEvents.
 */
@FunctionalInterface
public interface PropertyChangeListener extends EventListener
{
    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e a PropertyChangeEvent object
     */
    void stateChanged(PropertyChangeEvent e);
}
