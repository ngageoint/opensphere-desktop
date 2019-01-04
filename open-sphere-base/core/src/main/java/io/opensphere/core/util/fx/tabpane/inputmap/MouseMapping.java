package io.opensphere.core.util.fx.tabpane.inputmap;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

/**
 * The MouseMapping class provides API to specify {@link Mapping mappings}
 * related to mouse input.
 */
public class MouseMapping extends Mapping<MouseEvent>
{
    /**
     * Creates a new KeyMapping instance that will fire when the given
     * {@link KeyCode} is entered into the application by the user, and this
     * will result in the given {@link EventHandler} being fired. The eventType
     * argument can be any of the {@link MouseEvent} event types, but typically
     * it is one of the following:
     *
     * <ul>
     * <li>{@link MouseEvent#ANY}</li>
     * <li>{@link MouseEvent#MOUSE_PRESSED}</li>
     * <li>{@link MouseEvent#MOUSE_CLICKED}</li>
     * <li>{@link MouseEvent#MOUSE_RELEASED}</li>
     * </ul>
     *
     * @param eventType The type of {@link MouseEvent} to listen for.
     * @param eventHandler The {@link EventHandler} to fire when the
     *            {@link MouseEvent} is observed.
     */
    public MouseMapping(final EventType<MouseEvent> eventType, final EventHandler<MouseEvent> eventHandler)
    {
        super(eventType, eventHandler);
        if (eventType == null)
        {
            throw new IllegalArgumentException("MouseMapping eventType constructor argument can not be null");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.fx.tabpane.inputmap.Mapping#getSpecificity(javafx.event.Event)
     */
    @Override
    public int getSpecificity(final Event e)
    {
        if (isDisabled())
        {
            return 0;
        }
        if (!(e instanceof MouseEvent))
        {
            return 0;
        }
        final EventType<MouseEvent> et = getEventType();

        int s = 0;
        if (e.getEventType() == MouseEvent.MOUSE_CLICKED && et != MouseEvent.MOUSE_CLICKED)
        {
            return 0;
        }
        s++;
        if (e.getEventType() == MouseEvent.MOUSE_DRAGGED && et != MouseEvent.MOUSE_DRAGGED)
        {
            return 0;
        }
        s++;
        if (e.getEventType() == MouseEvent.MOUSE_ENTERED && et != MouseEvent.MOUSE_ENTERED)
        {
            return 0;
        }
        s++;
        if (e.getEventType() == MouseEvent.MOUSE_ENTERED_TARGET && et != MouseEvent.MOUSE_ENTERED_TARGET)
        {
            return 0;
        }
        s++;
        if (e.getEventType() == MouseEvent.MOUSE_EXITED && et != MouseEvent.MOUSE_EXITED)
        {
            return 0;
        }
        s++;
        if (e.getEventType() == MouseEvent.MOUSE_EXITED_TARGET && et != MouseEvent.MOUSE_EXITED_TARGET)
        {
            return 0;
        }
        s++;
        if (e.getEventType() == MouseEvent.MOUSE_MOVED && et != MouseEvent.MOUSE_MOVED)
        {
            return 0;
        }
        s++;
        if (e.getEventType() == MouseEvent.MOUSE_PRESSED && et != MouseEvent.MOUSE_PRESSED)
        {
            return 0;
        }
        s++;
        if (e.getEventType() == MouseEvent.MOUSE_RELEASED && et != MouseEvent.MOUSE_RELEASED)
        {
            return 0;
        }
        s++;

        return s;
    }
}
