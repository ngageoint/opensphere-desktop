package io.opensphere.core.iconlegend;

import javax.swing.Icon;

/**
 * The Interface IconRegistry. Allows icons to be added to the icon legend.
 */
public interface IconLegendRegistry
{
    /**
     * Adds the icon legend listener.
     *
     * @param listener the listener
     */
    void addIconLegendListener(IconLegendListener listener);

    /**
     * Adds the icon to legend.
     *
     * @param icon the icon
     * @param iconName the icon name
     * @param description the description
     */
    void addIconToLegend(Icon icon, String iconName, String description);

    /**
     * Removes the icon legend listener.
     *
     * @param listener the listener
     */
    void removeIconLegendListener(IconLegendListener listener);
}
