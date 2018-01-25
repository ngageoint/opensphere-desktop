package io.opensphere.core.iconlegend;

import javax.swing.Icon;

/**
 * A simple listener that allows adding of icons to the icon legend.
 *
 */
@FunctionalInterface
public interface IconLegendListener
{
    /**
     * Icon legend icon added.
     *
     * @param icon the icon
     * @param iconName the icon name
     * @param description the description
     */
    void iconLegendIconAdded(Icon icon, String iconName, String description);
}
