package io.opensphere.core.util.javafx.input.view.behavior;

import javafx.scene.control.Control;

/**
 * A behavior extension used to handle focus traversal events.
 *
 * @param <C> the control type for which the behavior is implemented.
 */
public interface FocusBehavior<C extends Control> extends Behavior<C>
{
    /**
     * Calls the focus traversal engine and indicates that traversal should go
     * the next focusTraversable Node above the current one.
     */
    void traverseUp();

    /**
     * Calls the focus traversal engine and indicates that traversal should go
     * the next focusTraversable Node below the current one.
     */
    void traverseDown();

    /**
     * Calls the focus traversal engine and indicates that traversal should go
     * the next focusTraversable Node left of the current one.
     */
    void traverseLeft();

    /**
     * Calls the focus traversal engine and indicates that traversal should go
     * the next focusTraversable Node right of the current one.
     */
    void traverseRight();

    /**
     * Calls the focus traversal engine and indicates that traversal should go
     * the next focusTraversable Node in the focus traversal cycle.
     */
    void traverseNext();

    /**
     * Calls the focus traversal engine and indicates that traversal should go
     * the previous focusTraversable Node in the focus traversal cycle.
     */
    void traversePrevious();
}
