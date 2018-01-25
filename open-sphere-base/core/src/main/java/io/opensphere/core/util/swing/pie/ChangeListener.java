package io.opensphere.core.util.swing.pie;

import java.util.EventListener;

/**
 * Defines an object which listens for ChangeEvents.
 */
@FunctionalInterface
public interface ChangeListener extends EventListener
{
    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param changeType the change type
     * @param source the source of the change
     */
    void stateChanged(ChangeType changeType, Object source);
}
