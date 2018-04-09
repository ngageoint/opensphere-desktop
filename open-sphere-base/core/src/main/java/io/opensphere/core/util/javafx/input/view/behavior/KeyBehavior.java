package io.opensphere.core.util.javafx.input.view.behavior;

import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A behavior extension used to handle key events.
 *
 * @param <C> the control type for which the behavior is implemented.
 */
public interface KeyBehavior<C extends Control> extends Behavior<C>
{
    /**
     * Adds the supplied binding to the behavior.
     *
     * @param pBinding the binding to add.
     */
    void addBinding(KeyActionBinding pBinding);

    /**
     * Removes the supplied binding from the behavior, if present. If not
     * present, no action is taken.
     *
     * @param pBinding the binding to remove.
     */
    void removeBinding(KeyActionBinding pBinding);

    /**
     * Creates a new binding and adds it to the behavior, binding the key to the
     * action.
     *
     * @param pCode the key code to bind to the action.
     * @param pAction the action to be bound to the key.
     */
    void bind(KeyCode pCode, String pAction);

    /**
     * Creates a new binding and adds it to the behavior, binding the key to the
     * action.
     *
     * @param pCode the key code to bind to the action.
     * @param pAction the action to be bound to the key.
     * @param pModifiers the set of modifier keys to apply to the binding.
     * @throws IllegalArgumentException if one or more of the supplied modifiers
     *             is represented by a {@link KeyCode} that returns false for a
     *             call to the {@link KeyCode#isModifierKey()} method.
     */
    void bind(KeyCode pCode, String pAction, KeyCode... pModifiers);

    /**
     * Removes the binding from the behavior, if present.
     *
     * @param pCode the binding to remove.
     */
    void removeBinding(KeyCode pCode);

    /**
     * Called when a key event is fired. The event will be distributed to all
     * bound actions, if present.
     *
     * @param pEvent the event to process.
     */
    void keyEvent(KeyEvent pEvent);

    /**
     * Invokes the action. Callers should override this method to process
     * actions in which they are interested.
     *
     * @param pAction the action to invoke.
     */
    void actionPerformed(String pAction);
}
