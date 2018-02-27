package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Collection;
import java.util.function.Predicate;

import javax.swing.AbstractButton;
import javax.swing.JComponent;

import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Utilities for AWT {@link Component}s.
 */
public final class ComponentUtilities
{
    /**
     * Gets all the components the given component's component tree (itself plus
     * all its descendants).
     *
     * @param comp the component
     * @return all the components
     */
    public static Collection<Component> getAllComponents(Component comp)
    {
        return getAllComponents(comp, null);
    }

    /**
     * Gets all the components the given component's component tree (itself plus
     * all its descendants).
     *
     * @param comp the component
     * @param filter the filter
     * @return all the components
     */
    public static Collection<Component> getAllComponents(Component comp, Predicate<? super Component> filter)
    {
        Collection<Component> components = New.list();
        flatten(comp, components, filter);
        return components;
    }

    /**
     * Returns the first parent component for the current component tree that
     * matches the filter.
     *
     * @param comp the component
     * @param filter the filter
     * @return the first ancestor of c that matches the filter
     */
    public static Component getFirstParent(Component comp, Predicate<? super Component> filter)
    {
        for (Component parent = comp; parent != null; parent = parent.getParent())
        {
            if (filter.test(parent))
            {
                return parent;
            }
        }
        return null;
    }

    /**
     * Change the opacity of the background color of a component and its
     * descendants.
     *
     * @param container The container for which to change opacity.
     * @param alpha The new opacity.
     */
    public static void setContentsBackgroundOpacity(Container container, int alpha)
    {
        if (container instanceof JComponent && !(container instanceof AbstractButton))
        {
            JComponent comp = (JComponent)container;
            if (comp.isBackgroundSet())
            {
                comp.setBackground(ColorUtilities.opacitizeColor(comp.getBackground(), alpha));
            }
        }
        for (Component child : container.getComponents())
        {
            if (child instanceof Container)
            {
                setContentsBackgroundOpacity((Container)child, alpha);
            }
        }
    }

    /**
     * Set of the components and descendant components to either opaque or
     * non-opaque.
     *
     * @param container The container for which to change opacity.
     * @param opaque Use true for opaque and false for non-opaque.
     */
    public static void setContentsOpaque(Container container, boolean opaque)
    {
        if (container instanceof JComponent && !(container instanceof AbstractButton))
        {
            ((JComponent)container).setOpaque(opaque);
        }
        for (Component child : container.getComponents())
        {
            if (child instanceof Container)
            {
                setContentsOpaque((Container)child, opaque);
            }
        }
    }

    /**
     * Set the location of the given component adjacent to (to the left or to
     * the right of) another component, keeping it within the bounds of the
     * parent.
     *
     * @param component the component on which to set the location
     * @param adjacent the relative component
     * @param parent the parent frame
     */
    public static void setLocationAdjacentTo(Component component, Component adjacent, Component parent)
    {
        if (adjacent.isVisible())
        {
            int adjacentWidth = adjacent.getWidth();
            Point adjacentLocation = adjacent.getLocationOnScreen();
            int componentWidth = component.getWidth();
            Rectangle parentBounds = parent.getBounds();

            // Try right of the component
            int x = adjacentLocation.x + adjacentWidth;
            if (x + componentWidth > parentBounds.x + parentBounds.width)
            {
                // Try left of the component
                x = adjacentLocation.x - componentWidth;
                if (x < parentBounds.x)
                {
                    // Flush with right side of parent frame
                    x = parentBounds.x + parentBounds.width - componentWidth;
                }
            }

            component.setLocation(x, adjacentLocation.y);
        }
        else if (component instanceof Window)
        {
            // Centered in the parent frame
            ((Window)component).setLocationRelativeTo(parent);
        }
    }

    /**
     * Sets the preferred width of the component, leaving the preferred height
     * the same.
     *
     * @param comp the component
     * @param width the preferred width
     */
    public static void setPreferredWidth(Component comp, int width)
    {
        comp.setPreferredSize(new Dimension(width, comp.getPreferredSize().height));
    }

    /**
     * Sets the preferred height of the component, leaving the preferred width
     * the same.
     *
     * @param comp the component
     * @param height the preferred height
     */
    public static void setPreferredHeight(Component comp, int height)
    {
        comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, height));
    }

    /**
     * Sets the minimum width of the component, leaving the minimum height
     * the same.
     *
     * @param comp the component
     * @param width the minimum width
     */
    public static void setMinimumWidth(Component comp, int width)
    {
        comp.setMinimumSize(new Dimension(width, comp.getMinimumSize().height));
    }

    /**
     * Sets the minimum height of the component, leaving the minimum width
     * the same.
     *
     * @param comp the component
     * @param height the minimum height
     */
    public static void setMinimumHeight(Component comp, int height)
    {
        comp.setMinimumSize(new Dimension(comp.getMinimumSize().width, height));
    }

    /**
     * Sets the preferred width of the narrower component to that of the wider
     * component.
     *
     * @param c1 the first component
     * @param c2 the second component
     */
    public static void setPreferredWidthsEqual(Component c1, Component c2)
    {
        Dimension s1 = c1.getPreferredSize();
        Dimension s2 = c2.getPreferredSize();
        if (s1.width < s2.width)
        {
            setPreferredWidth(c1, s2.width);
        }
        else if (s2.width < s1.width)
        {
            setPreferredWidth(c2, s1.width);
        }
    }

    /**
     * Flattens the component tree.
     *
     * @param comp the starting component
     * @param components the collection to build up
     * @param filter the filter
     */
    private static void flatten(Component comp, Collection<? super Component> components, Predicate<? super Component> filter)
    {
        if (filter == null || filter.test(comp))
        {
            components.add(comp);
        }
        if (comp instanceof Container)
        {
            for (Component child : ((Container)comp).getComponents())
            {
                flatten(child, components, filter);
            }
        }
    }

    /**
     * Private constructor.
     */
    private ComponentUtilities()
    {
    }
}
