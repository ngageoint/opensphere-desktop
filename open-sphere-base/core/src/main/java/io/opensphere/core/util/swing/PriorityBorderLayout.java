package io.opensphere.core.util.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.core.util.collections.New;

/**
 * Extension to {@link BorderLayout} that allows specification of which borders
 * take precedence.
 */
@SuppressWarnings("PMD.GodClass")
public class PriorityBorderLayout extends BorderLayout
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The border order. */
    private final List<String> myBorderOrder;

    /** If the center panel should be centered if possible. */
    private final boolean myCenteredCenter;

    /**
     * Constructor.
     *
     * @param centeredCenter If the center region should be centered in the
     *            layout when unconstrained.
     * @param borderOrder The border order.
     */
    public PriorityBorderLayout(boolean centeredCenter, List<String> borderOrder)
    {
        myCenteredCenter = centeredCenter;
        int lastx = Math.max(borderOrder.indexOf(WEST), borderOrder.indexOf(EAST));
        int lasty = Math.max(borderOrder.indexOf(SOUTH), borderOrder.indexOf(NORTH));
        int center = borderOrder.indexOf(CENTER);
        if (center > -1 && lastx > center && lasty > center)
        {
            throw new UnsupportedOperationException("Prioritizing CENTER for both axes is not supported.");
        }
        List<String> borders = New.list(NORTH, SOUTH, EAST, WEST, CENTER);
        borders.removeAll(borderOrder);
        borders.addAll(0, borderOrder);
        myBorderOrder = Collections.unmodifiableList(borders);
    }

    /**
     * Constructor.
     *
     * @param centeredCenter If the center region should be centered in the
     *            layout when unconstrained.
     * @param borderOrder The border order.
     */
    public PriorityBorderLayout(boolean centeredCenter, String... borderOrder)
    {
        this(centeredCenter, Arrays.asList(borderOrder));
    }

    /**
     * Set the x coordinate and width for the center component when the east
     * component is yet to be done.
     *
     * @param containerBounds The bounds of the container being laid out.
     * @param bounds The empty bounds.
     * @param c The component.
     */
    public void doCenterXBeforeEast(Rectangle containerBounds, Rectangle bounds, Component c)
    {
        int w = Math.min(c.getPreferredSize().width, bounds.width);
        int x;
        if (myCenteredCenter)
        {
            w = Math.min(w, containerBounds.width - 2 * bounds.x);
            x = (containerBounds.width - w) / 2;
        }
        else
        {
            x = bounds.x;
        }
        c.setBounds(x, c.getY(), w, c.getHeight());
        bounds.width -= x - bounds.x + w + getHgap();
        bounds.x = x + w + getHgap();
    }

    /**
     * Set the x coordinate and width for the center component when the west
     * component is yet to be done.
     *
     * @param containerBounds The bounds of the container being laid out.
     * @param bounds The empty bounds.
     * @param c The component.
     */
    public void doCenterXBeforeWest(Rectangle containerBounds, Rectangle bounds, Component c)
    {
        int w = Math.min(c.getPreferredSize().width, bounds.width);
        int x;
        if (myCenteredCenter)
        {
            w = Math.min(w, 2 * bounds.width + 2 * bounds.x - containerBounds.width);
            x = (containerBounds.width - w) / 2;
        }
        else
        {
            x = bounds.width - w;
        }
        c.setBounds(x, c.getY(), w, c.getHeight());
        bounds.width = x - bounds.x - getHgap();
    }

    /**
     * Set the y coordinate and height for the center component when the north
     * component is yet to be done.
     *
     * @param containerBounds The bounds of the container being laid out.
     * @param bounds The empty bounds.
     * @param c The component.
     */
    public void doCenterYBeforeNorth(Rectangle containerBounds, Rectangle bounds, Component c)
    {
        int h = Math.min(c.getPreferredSize().height, bounds.height);
        int y;
        if (myCenteredCenter)
        {
            h = Math.min(h, 2 * bounds.height + 2 * bounds.y - containerBounds.height);
            y = (containerBounds.height - h) / 2;
        }
        else
        {
            y = bounds.height - h;
        }
        c.setBounds(c.getX(), y, c.getWidth(), h);
        bounds.height = y - bounds.y - getVgap();
    }

    /**
     * Set the y coordinate and height for the center component when the south
     * component is yet to be done.
     *
     * @param containerBounds The bounds of the container being laid out.
     * @param bounds The empty bounds.
     * @param c The component.
     */
    public void doCenterYBeforeSouth(Rectangle containerBounds, Rectangle bounds, Component c)
    {
        int h = Math.min(c.getPreferredSize().height, bounds.height);
        int y;
        if (myCenteredCenter)
        {
            h = Math.min(h, containerBounds.height - 2 * bounds.y);
            y = (containerBounds.height - h) / 2;
        }
        else
        {
            y = bounds.y;
        }
        c.setBounds(c.getX(), y, c.getWidth(), h);
        bounds.height -= y - bounds.y + h + getVgap();
        bounds.y = y + h + getVgap();
    }

    @Override
    public void layoutContainer(Container target)
    {
        synchronized (target.getTreeLock())
        {
            Insets insets = target.getInsets();
            Rectangle bounds = new Rectangle(insets.left, insets.top, target.getWidth(), target.getHeight());

            boolean ltr = target.getComponentOrientation().isLeftToRight();

            LinkedHashMap<String, Component> todo = new LinkedHashMap<>();
            for (String border : myBorderOrder)
            {
                Component comp = getChild(border, ltr);
                if (comp != null)
                {
                    todo.put(border, comp);
                }
            }

            while (!todo.isEmpty())
            {
                Iterator<Entry<String, Component>> iter = todo.entrySet().iterator();
                Entry<String, Component> entry = iter.next();
                iter.remove();

                String border = entry.getKey();
                Component c = entry.getValue();
                if (NORTH.equals(border))
                {
                    if (todo.containsKey(SOUTH) || todo.containsKey(CENTER))
                    {
                        doNorth(bounds, c);
                    }
                    else
                    {
                        setToBoundsY(bounds, c);
                        setToBoundsX(bounds, c);
                    }
                }
                else if (SOUTH.equals(border))
                {
                    if (todo.containsKey(NORTH) || todo.containsKey(CENTER))
                    {
                        doSouth(bounds, c);
                    }
                    else
                    {
                        setToBoundsX(bounds, c);
                        setToBoundsY(bounds, c);
                    }
                }
                else if (EAST.equals(border))
                {
                    if (todo.containsKey(WEST) || todo.containsKey(CENTER))
                    {
                        doEast(bounds, c);
                    }
                    else
                    {
                        setToBoundsX(bounds, c);
                        setToBoundsY(bounds, c);
                    }
                }
                else if (WEST.equals(border))
                {
                    if (todo.containsKey(EAST) || todo.containsKey(CENTER))
                    {
                        doWest(bounds, c);
                    }
                    else
                    {
                        setToBoundsX(bounds, c);
                        setToBoundsY(bounds, c);
                    }
                }
                else if (CENTER.equals(border))
                {
                    doCenter(target.getBounds(), bounds, c, todo);
                }
            }
        }
    }

    /**
     * Set the x coordinate and width of the component from the empty bounds.
     *
     * @param bounds The empty bounds.
     * @param c The component.
     */
    public void setToBoundsX(Rectangle bounds, Component c)
    {
        c.setBounds(bounds.x, c.getY(), bounds.width, c.getHeight());
    }

    /**
     * Set the y coordinate and height of the component from the empty bounds.
     *
     * @param bounds The empty bounds.
     * @param c The component.
     */
    public void setToBoundsY(Rectangle bounds, Component c)
    {
        c.setBounds(c.getX(), bounds.y, c.getWidth(), bounds.height);
    }

    /**
     * Set the bounds for the center component.
     *
     * @param containerBounds The bounds of the container being laid out.
     * @param bounds The empty bounds.
     * @param c The component.
     * @param todo The map of border tags to components that are yet to be laid
     *            out.
     */
    private void doCenter(Rectangle containerBounds, Rectangle bounds, Component c, Map<String, Component> todo)
    {
        doCenterX(containerBounds, bounds, c, todo);
        doCenterY(containerBounds, bounds, c, todo);
    }

    /**
     * Set the x coordinate and width for the center component.
     *
     * @param containerBounds The bounds of the container being laid out.
     * @param bounds The empty bounds.
     * @param c The component.
     * @param todo The map of border tags to components that are yet to be laid
     *            out.
     */
    private void doCenterX(Rectangle containerBounds, Rectangle bounds, Component c, Map<String, Component> todo)
    {
        if (todo.containsKey(EAST))
        {
            if (todo.containsKey(WEST))
            {
                if (EAST.equals(todo.keySet().stream().filter(k -> EAST.equals(k) || WEST.equals(k)).findFirst().get()))
                {
                    doEast(bounds, todo.remove(EAST));
                    doCenterXBeforeWest(containerBounds, bounds, c);
                }
                else
                {
                    doWest(bounds, todo.remove(WEST));
                    doCenterXBeforeEast(containerBounds, bounds, c);
                }
            }
            else
            {
                doCenterXBeforeEast(containerBounds, bounds, c);
            }
        }
        else if (todo.containsKey(WEST))
        {
            doCenterXBeforeWest(containerBounds, bounds, c);
        }
        else
        {
            setToBoundsX(bounds, c);
        }
    }

    /**
     * Set the y coordinate and height for the center component.
     *
     * @param containerBounds The bounds of the container being laid out.
     * @param bounds The empty bounds.
     * @param c The component.
     * @param todo The map of border tags to components that are yet to be laid
     *            out.
     */
    private void doCenterY(Rectangle containerBounds, Rectangle bounds, Component c, Map<String, Component> todo)
    {
        if (todo.containsKey(SOUTH))
        {
            if (todo.containsKey(NORTH))
            {
                if (SOUTH.equals(todo.keySet().stream().filter(k -> NORTH.equals(k) || SOUTH.equals(k)).findFirst().get()))
                {
                    doSouth(bounds, todo.remove(SOUTH));
                    doCenterYBeforeNorth(containerBounds, bounds, c);
                }
                else
                {
                    doNorth(bounds, todo.remove(NORTH));
                    doCenterYBeforeSouth(containerBounds, bounds, c);
                }
            }
            else
            {
                doCenterYBeforeSouth(containerBounds, bounds, c);
            }
        }
        else if (todo.containsKey(NORTH))
        {
            doCenterYBeforeNorth(containerBounds, bounds, c);
        }
        else
        {
            setToBoundsY(bounds, c);
        }
    }

    /**
     * Set the bounds for the east component.
     *
     * @param bounds The empty bounds.
     * @param c The component.
     */
    private void doEast(Rectangle bounds, Component c)
    {
        c.setSize(c.getWidth(), bounds.height);
        int w = Math.min(c.getPreferredSize().width, Math.max(0, bounds.width - getHgap()));
        c.setBounds(bounds.x + bounds.width - w, bounds.y, w, bounds.height);
        bounds.width -= w + getHgap();
    }

    /**
     * Set the bounds for the north component.
     *
     * @param bounds The empty bounds.
     * @param c The component.
     */
    private void doNorth(Rectangle bounds, Component c)
    {
        c.setSize(bounds.width, c.getHeight());
        int h = Math.min(c.getPreferredSize().height, Math.max(0, bounds.height - getVgap()));
        c.setBounds(bounds.x, bounds.y, bounds.width, h);
        bounds.y += h + getVgap();
        bounds.height -= h + getVgap();
    }

    /**
     * Set the bounds for the south component.
     *
     * @param bounds The empty bounds.
     * @param c The component.
     */
    private void doSouth(Rectangle bounds, Component c)
    {
        c.setSize(bounds.width, c.getHeight());
        int h = Math.min(c.getPreferredSize().height, Math.max(0, bounds.height - getVgap()));
        c.setBounds(bounds.x, bounds.y + bounds.height - h, bounds.width, h);
        bounds.height -= h + getVgap();
    }

    /**
     * Set the bounds for the west component.
     *
     * @param bounds The empty bounds.
     * @param c The component.
     */
    private void doWest(Rectangle bounds, Component c)
    {
        c.setSize(c.getWidth(), bounds.height);
        int w = Math.min(c.getPreferredSize().width, Math.max(0, bounds.width - getHgap()));
        c.setBounds(bounds.x, bounds.y, w, bounds.height);
        bounds.x += w + getHgap();
        bounds.width -= w + getHgap();
    }

    /**
     * Get the component that corresponds to the given constraint location.
     *
     * @param key The desired absolute position, either NORTH, SOUTH, EAST, or
     *            WEST.
     * @param ltr Is the component line direction left-to-right?
     * @return The component or null.
     */
    private Component getChild(String key, boolean ltr)
    {
        Component result = null;

        if (key.equals(NORTH))
        {
            Component pageStart = getLayoutComponent(PAGE_START);
            result = pageStart != null ? pageStart : getLayoutComponent(NORTH);
        }
        else if (key.equals(SOUTH))
        {
            Component pageEnd = getLayoutComponent(PAGE_END);
            result = pageEnd != null ? pageEnd : getLayoutComponent(SOUTH);
        }
        else if (key.equals(WEST))
        {
            result = ltr ? getLayoutComponent(LINE_START) : getLayoutComponent(LINE_END);
            if (result == null)
            {
                result = getLayoutComponent(WEST);
            }
        }
        else if (key.equals(EAST))
        {
            result = ltr ? getLayoutComponent(LINE_END) : getLayoutComponent(LINE_START);
            if (result == null)
            {
                result = getLayoutComponent(EAST);
            }
        }
        else if (key.equals(CENTER))
        {
            result = getLayoutComponent(CENTER);
        }
        if (result != null && !result.isVisible())
        {
            result = null;
        }
        return result;
    }
}
