package io.opensphere.controlpanels.layers.util;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.GenericFontIcon;

/** Icon utilities for layers. */
public final class IconUtilities
{
    /**
     * Gets a colorized version of the icon, if possible.
     *
     * @param icon the icon
     * @param color the color
     * @return the colorized icon
     */
    public static Icon getColorizedIcon(Icon icon, Color color)
    {
        Icon colorizedIcon = icon;
        if (icon instanceof ImageIcon)
        {
            colorizedIcon = IconUtil.getColorizedIcon((ImageIcon)icon, color);
        }
        else if (icon instanceof GenericFontIcon)
        {
            colorizedIcon = ((GenericFontIcon)icon).withColor(color);
        }
        return colorizedIcon;
    }

    /** Disallow instantiation. */
    private IconUtilities()
    {
    }
}
