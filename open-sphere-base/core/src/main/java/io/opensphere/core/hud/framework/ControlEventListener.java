package io.opensphere.core.hud.framework;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import io.opensphere.core.geometry.Geometry;

/**
 * Interface for listeners for control events from a
 * <code>ControlEventSupport</code>.
 */
public interface ControlEventListener
{
    /**
     * Handle a mouse clicked event.
     *
     * @param geom The geometry which is picked at the time of the event.
     * @param event The event which has occurred.
     */
    void mouseClicked(Geometry geom, MouseEvent event);

    /**
     * Handle a mouse dragged event.
     *
     * @param geom The geometry which is picked at the time of the event.
     * @param dragStart The location where the drag started.
     * @param event The event which has occurred.
     */
    void mouseDragged(Geometry geom, Point dragStart, MouseEvent event);

    /**
     * Handle a mouse clicked event.
     *
     * @param geom The geometry which is picked at the time of the event.
     * @param location Location of the cursor at the time of the event.
     */
    void mouseEntered(Geometry geom, Point location);

    /**
     * Handle a mouse exited event.
     *
     * @param geom The geometry which is picked at the time of the event.
     * @param location Location of the cursor at the time of the event.
     */
    void mouseExited(Geometry geom, Point location);

    /**
     * Handle a mouse moved event.
     *
     * @param geom The geometry which is picked at the time of the event.
     * @param event The event which has occurred.
     */
    void mouseMoved(Geometry geom, MouseEvent event);

    /**
     * Handle a mouse pressed event.
     *
     * @param geom The geometry which is picked at the time of the event.
     * @param event The event which has occurred.
     */
    void mousePressed(Geometry geom, MouseEvent event);

    /**
     * Handle a mouse released event.
     *
     * @param geom The geometry which is picked at the time of the event.
     * @param event The event which has occurred.
     */
    void mouseReleased(Geometry geom, MouseEvent event);

    /**
     * Handle a mouse moved event.
     *
     * @param geom The geometry which is picked at the time of the event.
     * @param event The event which has occurred.
     */
    void mouseWheelMoved(Geometry geom, MouseWheelEvent event);
}
