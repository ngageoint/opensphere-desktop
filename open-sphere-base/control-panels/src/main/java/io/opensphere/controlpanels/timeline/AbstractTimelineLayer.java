package io.opensphere.controlpanels.timeline;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.opensphere.core.control.action.context.TimespanContextKey;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.swing.ButtonTextPredicate;

/**
 * Abstract timeline layer.
 */
public abstract class AbstractTimelineLayer implements TimelineLayer
{
    /** The dot circumference (odd number). */
    protected static final int DOT_CIRCUMFERENCE = 7;

    /** The dot padding. */
    protected static final int DOT_PADDING = DOT_CIRCUMFERENCE - 1 >> 1;

    /** Menu items. */
    private final List<Supplier<? extends Collection<? extends JMenuItem>>> myMenuItemSuppliers = New.list(0);

    /** The list of temporary layers that only exist for a single paint. */
    private final List<TimelineLayer> myTemporaryLayers = New.list();

    /** The timeline UI model. */
    private TimelineUIModel myUIModel;

    /**
     * Utility method to subtract a duration from an observable time span.
     *
     * @param timeSpan the time span
     * @param duration the duration
     */
    public static void minus(ObservableValue<TimeSpan> timeSpan, Duration duration)
    {
        timeSpan.set(timeSpan.get().minus(duration));
    }

    /**
     * Utility method to add a duration to an observable time span.
     *
     * @param timeSpan the time span
     * @param duration the duration
     */
    public static void plus(ObservableValue<TimeSpan> timeSpan, Duration duration)
    {
        timeSpan.set(timeSpan.get().plus(duration));
    }

    /**
     * Add supplier of context menu items for this layer that will be called to
     * supply menu items each time a context menu is created.
     *
     * @param menuItemSupplier The menu item supplier.
     */
    public void addMenuItemSupplier(Supplier<? extends Collection<? extends JMenuItem>> menuItemSupplier)
    {
        myMenuItemSuppliers.add(menuItemSupplier);
    }

    @Override
    public boolean canDrag(Point p)
    {
        return false;
    }

    @Override
    public int getDragPriority(Point p)
    {
        return 0;
    }

    @Override
    public Object drag(Object dragObject, Point from, Point to, boolean beginning, Duration dragTime)
    {
        return null;
    }

    @Override
    public void getMenuItems(Point p, List<JMenuItem> menuItems)
    {
        if (canDrag(p))
        {
            for (Supplier<? extends Collection<? extends JMenuItem>> supplier : myMenuItemSuppliers)
            {
                menuItems.addAll(supplier.get());
            }
        }
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, TimespanContextKey key)
    {
        return null;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    @Override
    public List<TimelineLayer> getTemporaryLayers()
    {
        return myTemporaryLayers;
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        return incoming;
    }

    /**
     * Gets the UI model.
     *
     * @return the UI model
     */
    public TimelineUIModel getUIModel()
    {
        return myUIModel;
    }

    @Override
    public boolean hasDragObject(Object dragObject)
    {
        return false;
    }

    /**
     * Determines if this layer is being dragged or can be dragged.
     *
     * @return whether this layer is being dragged or can be dragged
     */
    public boolean isDraggingOrCanDrag()
    {
        return hasDragObject(getUIModel().getDraggingObject())
                || getUIModel().getDraggingObject() == null && canDrag(getUIModel().getLastMousePoint());
    }

    @Override
    public void mouseEvent(MouseEvent e)
    {
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        assert EventQueue.isDispatchThread();
        myTemporaryLayers.clear();
    }

    @Override
    public void setUIModel(TimelineUIModel model)
    {
        myUIModel = model;
    }

    /**
     * Look for the new menu in the list of existing menu items, and if a
     * duplicate is found, transfer the children to the existing menu; otherwise
     * just add the new menu to the list.
     *
     * @param menuItems The list of existing menu items.
     * @param newMenu The new menu.
     */
    protected void deconflictMenus(List<JMenuItem> menuItems, JMenu newMenu)
    {
        JMenuItem existingMenu = StreamUtilities.filterOne(menuItems, new ButtonTextPredicate<JMenuItem>(newMenu.getText()));
        if (existingMenu == null)
        {
            menuItems.add(newMenu);
        }
        else
        {
            for (Component component : newMenu.getMenuComponents())
            {
                existingMenu.add(component);
            }
        }
    }

    /**
     * Convenience method to draw a line from the top to bottom of the timeline
     * at the given x location.
     *
     * @param g2d the graphics context
     * @param x the x location
     */
    protected void drawLine(Graphics2D g2d, int x)
    {
        int maxY = AWTUtilities.getMaxY(myUIModel.getTimelinePanelBounds()) - 1;
        g2d.drawLine(x, myUIModel.getTimelinePanelBounds().y + 1, x, maxY);
    }

    /** A dashed stroke. */
    protected static class DashedStroke extends BasicStroke
    {
        /** Constructor. */
        public DashedStroke()
        {
            super(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { 5, 3 }, 0);
        }
    }
}
