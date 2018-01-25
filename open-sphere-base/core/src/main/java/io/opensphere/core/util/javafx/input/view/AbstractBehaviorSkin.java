package io.opensphere.core.util.javafx.input.view;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;

import io.opensphere.core.util.javafx.input.view.behavior.AbstractBehavior;

/**
 * An extension of the {@link SkinBase} class, in which behavior support is added to the skin hierarchy.
 *
 * @param <C> The type of control to which the skin is bound.
 * @param <B> The type of behavior to which the skin is bound.
 */
public class AbstractBehaviorSkin<C extends Control, B extends AbstractBehavior<C>> extends SkinBase<C>
{
    /**
     * The {@link AbstractBehavior} that encapsulates the interaction with the {@link Control} from this {@code Skin}. The
     * {@code Skin} does not modify the {@code Control} directly, but rather redirects events into the {@code AbstractBehavior}
     * which then handles the events by modifying internal state and public state in the {@code Control}. Generally, specific
     * {@code Skin} implementations will require specific {@code AbstractBehavior} implementations. For example, a ButtonSkin
     * might require a ButtonBehavior.
     */
    private B myBehavior;

    /**
     * A change listener used to simplify event propagation.
     */
    private MultiplePropertyChangeListenerHandler myChangeListenerHandler;

    /**
     * Forwards mouse events received by a MouseListener to the behavior. Note that this pattern is used to remove some of the
     * anonymous inner classes which would otherwise have to created.
     */
    private final EventHandler<MouseEvent> myMouseHandler;

    /**
     * Forwards {@link ContextMenuEvent}s received to the behavior. Note that this pattern is used to remove some of the anonymous
     * inner classes which would otherwise have to created.
     *
     */
    private final EventHandler<ContextMenuEvent> myContextMenuHandler;

    /**
     * Creates a new skin, bound to the supplied control and behavior.
     *
     * @param pControl the control to which the skin is bound.
     * @param pBehavior the behavior to which the skin is bound.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public AbstractBehaviorSkin(final C pControl, final B pBehavior)
    {
        super(pControl);
        if (pBehavior == null)
        {
            throw new IllegalArgumentException("Cannot pass null for behavior");
        }

        myBehavior = pBehavior;
        myMouseHandler = e -> processMouseEvent(e);
        myContextMenuHandler = e -> myBehavior.contextMenuRequested(e);

        pControl.addEventHandler(MouseEvent.MOUSE_ENTERED, myMouseHandler);
        pControl.addEventHandler(MouseEvent.MOUSE_EXITED, myMouseHandler);
        pControl.addEventHandler(MouseEvent.MOUSE_PRESSED, myMouseHandler);
        pControl.addEventHandler(MouseEvent.MOUSE_RELEASED, myMouseHandler);
        pControl.addEventHandler(MouseEvent.MOUSE_DRAGGED, myMouseHandler);

        pControl.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, myContextMenuHandler);
    }

    /**
     * Gets the value of the {@link #myBehavior} field.
     *
     * @return the value stored in the {@link #myBehavior} field.
     */
    public final B getBehavior()
    {
        return myBehavior;
    }

    /**
     * An event handler method used to process a mouse event, and distribute the event to the behavior.
     *
     * @param pEvent the event to process.
     * @throws AssertionError if the event type is not recognized.
     */
    protected void processMouseEvent(MouseEvent pEvent) throws AssertionError
    {
        final EventType<?> type = pEvent.getEventType();

        if (type == MouseEvent.MOUSE_ENTERED)
        {
            myBehavior.mouseEntered(pEvent);
        }
        else if (type == MouseEvent.MOUSE_EXITED)
        {
            myBehavior.mouseExited(pEvent);
        }
        else if (type == MouseEvent.MOUSE_PRESSED)
        {
            myBehavior.mousePressed(pEvent);
        }
        else if (type == MouseEvent.MOUSE_RELEASED)
        {
            myBehavior.mouseReleased(pEvent);
        }
        else if (type == MouseEvent.MOUSE_DRAGGED)
        {
            myBehavior.mouseDragged(pEvent);
        }
        else
        {
            // no op
            throw new AssertionError("Unsupported event type received");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.SkinBase#dispose()
     */
    @Override
    public void dispose()
    {
        C control = getSkinnable();
        if (control != null)
        {
            control.removeEventHandler(MouseEvent.MOUSE_ENTERED, myMouseHandler);
            control.removeEventHandler(MouseEvent.MOUSE_EXITED, myMouseHandler);
            control.removeEventHandler(MouseEvent.MOUSE_PRESSED, myMouseHandler);
            control.removeEventHandler(MouseEvent.MOUSE_RELEASED, myMouseHandler);
            control.removeEventHandler(MouseEvent.MOUSE_DRAGGED, myMouseHandler);

            control.removeEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, myContextMenuHandler);
        }

        if (myBehavior != null)
        {
            myBehavior.dispose();
            myBehavior = null;
        }

        super.dispose();
    }

    /**
     * Subclasses can invoke this method to register that we want to listen to property change events for the given property.
     *
     * @param pProperty the property to which to bind the listener handler.
     * @param pChangeEventName the name of the event that will be propagated when the property changes.
     */
    protected final void registerChangeListener(ObservableValue<?> pProperty, String pChangeEventName)
    {
        if (myChangeListenerHandler == null)
        {
            myChangeListenerHandler = new MultiplePropertyChangeListenerHandler(p -> handleControlPropertyChanged(p));
        }
        myChangeListenerHandler.registerChangeListener(pProperty, pChangeEventName);
    }

    /**
     * Skin subclasses will override this method to handle changes in corresponding control's properties.
     *
     * @param pChangeEventName the name of the event that will be propagated when the property changes.
     */
    protected void handleControlPropertyChanged(String pChangeEventName)
    {
        /* intentionally blank */
    }
}
