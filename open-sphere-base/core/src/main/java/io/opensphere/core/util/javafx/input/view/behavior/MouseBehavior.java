package io.opensphere.core.util.javafx.input.view.behavior;

import javafx.scene.control.Control;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;

/**
 * An interface defining the methods needed for a behavior to process mouse
 * events.
 *
 * @param <C> The type of control supported by the behavior.
 */
public interface MouseBehavior<C extends Control> extends Behavior<C>
{
    /**
     * Invoked by a Skin when the body of the control has been pressed by the
     * mouse. Subclasses should be sure to call super unless they intend to
     * disable any built-in support.
     *
     * @param e the mouse event
     */
    void mousePressed(MouseEvent e);

    /**
     * Invoked by a Skin when the body of the control has been dragged by the
     * mouse. Subclasses should be sure to call super unless they intend to
     * disable any built-in support (for example, for tooltips).
     *
     * @param e the mouse event
     */
    void mouseDragged(MouseEvent e);

    /**
     * Invoked by a Skin when the body of the control has been released by the
     * mouse. Subclasses should be sure to call super unless they intend to
     * disable any built-in support (for example, for tooltips).
     *
     * @param e the mouse event
     */
    void mouseReleased(MouseEvent e);

    /**
     * Invoked by a Skin when the body of the control has been entered by the
     * mouse. Subclasses should be sure to call super unless they intend to
     * disable any built-in support.
     *
     * @param e the mouse event
     */
    void mouseEntered(MouseEvent e);

    /**
     * Invoked by a Skin when the body of the control has been exited by the
     * mouse. Subclasses should be sure to call super unless they intend to
     * disable any built-in support.
     *
     * @param e the mouse event
     */
    void mouseExited(MouseEvent e);

    /**
     * Invoked by a Skin when the control has had its context menu requested,
     * most commonly by right-clicking on the control. Subclasses should be sure
     * to call super unless they intend to disable any built-in support.
     *
     * @param e the context menu event
     */
    void contextMenuRequested(ContextMenuEvent e);
}
