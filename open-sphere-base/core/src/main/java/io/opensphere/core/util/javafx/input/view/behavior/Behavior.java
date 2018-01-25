package io.opensphere.core.util.javafx.input.view.behavior;

import javafx.scene.control.Control;

/**
 * The root behavior interface, from which all behaviors extend / implement.
 *
 * @param <C> the type of control supported by the behavior.
 */
public interface Behavior<C extends Control>
{
    /**
     * Called by a Skin when the Skin is disposed. This method allows a Behavior to implement any logic necessary to clean up
     * itself after the Behavior is no longer needed. Calling dispose twice has no effect. This method is intended to be
     * overridden by subclasses, although all subclasses must call super.dispose() or a potential memory leak will result.
     */
    void dispose();

    /**
     * Gets the control associated with this behavior. Even after the BehaviorBase is disposed, this reference will be non-null.
     *
     * @return The control for this Behavior.
     */
    C getControl();

    /**
     * Called whenever the focus on the control has changed. This method is intended to be overridden by subclasses that are
     * interested in focus change events.
     */
    void focusChanged();
}
