package io.opensphere.core.control;

import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.opensphere.core.geometry.Geometry;

/**
 * This represents a 'context' of key/mouse bindings. This is necessary because
 * there are different contexts in which keyboard bindings are meaningful. For
 * example, when the user is interacting with the globe, there is one set of
 * keystrokes they expect to perform globe-related functions. These are
 * completely separate from the keystrokes one expects when interacting with,
 * say, a TextField.
 */
public interface ControlContext extends KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
    /**
     * Add a listener for control events. The listener will be stored as a weak
     * reference, so the registering class will need to maintain a reference to
     * the listener. Registering compound events is only useful for click, drag
     * and release actions. If you register a binding for MOUSE_PRESSED, you
     * will get event start, event end and mouse dragged notifications.
     *
     * @param listener listener.
     * @param bindings The bindings that describe the actions of which the
     *            listener would like to be notified.
     */
    void addListener(CompoundEventListener listener, DefaultBinding... bindings);

    /**
     * Add a listener for control events. The listener will be stored as a weak
     * reference, so the registering class will need to maintain a reference to
     * the listener. Listeners should be removed by setting the hard references
     * to null.
     *
     * @param listener listener.
     * @param bindings The bindings that describe the actions of which the
     *            listener would like to be notified.
     */
    void addListener(DiscreteEventListener listener, DefaultBinding... bindings);

    /**
     * Add a listener for pick events. The listener will be stored as a weak
     * reference, so the registering class will need to maintain a reference to
     * the listener.
     *
     * @param listen listener
     */
    void addPickListener(PickListener listen);

    /**
     * Gets the key event descriptions by category. This method allows the
     * user-facing UI elements to describe the current registered Key Event
     * Descriptions organized into their categories, in insertion order.
     *
     * @return the key event descriptions by category
     */
    Map<String, List<BindingsToListener>> getEventListenersByCategory();

    /**
     * Get my context name.
     *
     * @return the name of this context.
     */
    String getName();

    /**
     * Notify listeners of a pick event.
     *
     * @param pickedGeom Geometry which now picked.
     * @param position Location of the cursor.
     */
    void notifyPicked(Geometry pickedGeom, Point position);

    /**
     * Remove the registration for my listeners and bindings.
     *
     * @param listener listener to remove.
     */
    void removeListener(BoundEventListener listener);

    /**
     * Remove the registration for the listeners and bindings.
     *
     * @param listeners listeners to remove.
     */
    void removeListeners(Collection<? extends BoundEventListener> listeners);

    /**
     * Remove the registration for the listeners.
     *
     * @param listen listener to remove.
     */
    void removePickListener(PickListener listen);
}
