package io.opensphere.core.control;

import java.awt.event.InputEvent;

/**
 * <p>
 * This class is used to activate Compound events. Compound Events need at least
 * two events to be useful. An example of this would be the click-and-drag
 * panning of the globe with the mouse: this needs left click, drag and left
 * release events to work. If you only need one event to activate your event,
 * you should be using the <code>DiscreteEventAdapter</code> instead.
 * </p>
 *
 * This adapter is targeted to only handle non-mouse related compound events. To
 * allow the user more latitude in control assignments, you may choose to use
 * the subclass <code>CompoundEventMouseAdapter</code> instead.
 *
 * <p>
 * This adapter is the intended entry point for these events. The methods to
 * override to control your event are: <code>
 * public void eventStarted(InputEvent event)
 *  and
 * public void eventEnded(InputEvent event)
 * </code>. The InputEvent contains the event that occurred to trigger the
 * callback.
 * </p>
 *
 * <p>
 * You should be careful to not filter for a keystroke in your event handler,
 * since the handler may be mapped to a different key by the user than you
 * originally envisioned. If the execution is inside the eventOccurred method at
 * all, then the assigned key was invoked.
 * </p>
 *
 * @see CompoundEventMouseAdapter
 * @see CompoundEventListener
 */
public class CompoundEventAdapter extends BoundEventAdapter implements CompoundEventListener
{
    /**
     * Construct a compound event adapter.
     *
     * @param category The category of the adapter.
     * @param title The title for the adapter.
     * @param description The description of the adapter.
     */
    protected CompoundEventAdapter(String category, String title, String description)
    {
        super(category, title, description);
    }

    @Override
    public void eventEnded(InputEvent event)
    {
    }

    @Override
    public void eventStarted(InputEvent event)
    {
    }
}
