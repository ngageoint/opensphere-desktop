package io.opensphere.controlpanels.timeline;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.TimespanContextKey;
import io.opensphere.core.units.duration.Duration;

/**
 * Interface for a layer on a timeline.
 */
public interface TimelineLayer extends ContextMenuProvider<TimespanContextKey>
{
    /**
     * Determines whether the layer has an object that can be dragged at the
     * given point.
     *
     * @param p the point
     * @return whether a drag can occur
     */
    boolean canDrag(Point p);

    /**
     * Gets the drag priority at the given point. 0 is the default.
     *
     * @param p the point
     * @return the drag priority
     */
    int getDragPriority(Point p);

    /**
     * Performs a drag.
     *
     * @param dragObject the drag object if available
     * @param from the original point
     * @param to the destination
     * @param beginning if this is the beginning of a new drag
     * @param dragTime the drag time in milliseconds
     * @return the drag object
     */
    Object drag(Object dragObject, Point from, Point to, boolean beginning, Duration dragTime);

    /**
     * Gets a list of optional menu items for the given point.
     *
     * @param p the mouse point requesting the menu items
     * @param menuItems the return collection of menu items
     */
    void getMenuItems(Point p, List<JMenuItem> menuItems);

    /**
     * Gets the temporary layers.
     *
     * @return the temporary layers
     */
    List<TimelineLayer> getTemporaryLayers();

    /**
     * Gets the tool tip text.
     *
     * @param event the mouse event
     * @param incoming If a lower layer provided a tooltip, it will be provided
     *            here, and it's up to this layer to adjudicate while tooltip to
     *            return.
     * @return the tool tip text
     */
    String getToolTipText(MouseEvent event, String incoming);

    /**
     * Determines whether the layer has the given drag object.
     *
     * @param dragObject the drag object
     * @return whether the layer has the given drag object
     */
    boolean hasDragObject(Object dragObject);

    /**
     * Passes a mouse event to the layer.
     *
     * @param e the mouse event
     */
    void mouseEvent(MouseEvent e);

    /**
     * Paint.
     *
     * @param g2d the graphics
     */
    void paint(Graphics2D g2d);

    /**
     * Sets the UI model.
     *
     * @param model the UI model
     */
    void setUIModel(TimelineUIModel model);
}
