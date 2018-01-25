package io.opensphere.core.control;

import java.awt.event.InputEvent;

/**
 * <p>
 * This class is used to activate Discrete events. Discrete events are those
 * which, when triggered, perform a task that doesn't need a separate event. For
 * example, pressing keys to move the view east or west by some amount, or to
 * zoom in or out, or "fly-to" some location, are all discrete events. Events
 * that are Compound, (the opposite of Discrete), need at least two events to be
 * useful. An example of this would be the click-and-drag panning of the globe
 * with the mouse: this needs left click, drag and left release events to work.
 * </p>
 *
 * <p>
 * Most events are Discrete.
 * </p>
 *
 * <p>
 * This adapter is the intended entry point for these events. The method to
 * override for your event is
 * <code> public void eventOccurred(InputEvent event) </code>. The InputEvent
 * contains the event that occurred to trigger the callback.
 * </p>
 *
 * <p>
 * You should be careful to not filter for a keystroke in your event handler,
 * since the handler may be mapped to a different key (or control) by the user
 * than what you originally envisioned. If the execution is inside the
 * eventOccurred method at all, then the assigned key or control was invoked.
 * </p>
 *
 * <p>
 * Intended usage is as follows:
 * </p>
 *
 * <pre>
 * // context is of type ControlContext
 * context.addListener(new DiscreteEventAdapter("View", "Camera Right", "Moves the camera to the right")
 * {
 *     <code>@Override</code> public void eventOccurred(InputEvent event) {
 * getCurrentControlTranslator().viewRight(event); } }, new
 * DefaultKeyPressedBinding(KeyEvent.VK_RIGHT));
 * </pre>
 * <p>
 * <i> Remember that your discrete events may not necessarily be mapped to
 * keyboard events</i>; they may be mapped to mouse events, or some combination
 * of both, such as:
 * </p>
 *
 * <pre>
 * context.addListener(new DiscreteEventAdapter(category, "Zoom out", "Zooms out the camera")
 * {
 *     <code>@Override</code> public void eventOccurred(InputEvent event) {
 * getCurrentControlTranslator().zoomOutView(event); }
 *
 * }, new DefaultMouseWheelBinding(DefaultMouseWheelBinding.MOUSE_DOWN), new
 * DefaultKeyPressedBinding(KeyEvent.VK_DOWN,KeyEvent.SHIFT_DOWN_MASK));
 * </pre>
 *
 * @see CompoundEventAdapter
 * @see DiscreteEventListener
 */
public class DiscreteEventAdapter extends BoundEventAdapter implements DiscreteEventListener
{
    /**
     * Constructor.
     *
     * @param category the category under which this event will be displayed on
     *            the key binding frame. For example, "View".
     * @param title the title The summary title the user will see. It should be
     *            short, a few words at most.
     * @param description the description the user will see. This can be several
     *            sentences long. It is usually shown in a tooltip, so limited
     *            html is allowed. The description, once in the tooltip, will be
     *            contained between the tags: "
     *            <code>&lt;html&gt;&lt;font face=\"sansserif\"&gt;" + description + "&lt;/font&gt;&lt;/html&gt;</code>
     *            "
     */
    public DiscreteEventAdapter(String category, String title, String description)
    {
        super(category, title, description);
    }

    @Override
    public void eventOccurred(InputEvent event)
    {
    }
}
