package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.GraphicsEnvironment;

import javax.swing.Icon;

import io.opensphere.core.util.GovIcon;

/**
 * A rendered text icon in which the {@link GovIcon} is drawn as a
 * Swing-compatible {@link Icon} instance.
 */
public class GovIconFontIcon extends AbstractFontIcon
{
    static
    {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(SwingUtilities.GOVICONS_FONT);
    }

    /**
     * Creates a new icon.
     *
     * @param pIcon the icon to draw.
     */
    public GovIconFontIcon(GovIcon pIcon)
    {
        super(SwingUtilities.GOVICONS_FONT, pIcon);
    }

    /**
     * Creates a new icon, painted with the supplied color.
     *
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     */
    public GovIconFontIcon(GovIcon pIcon, Color pColor)
    {
        super(SwingUtilities.GOVICONS_FONT, pIcon, pColor);
    }

    /**
     * Creates a new icon, painted with the supplied color, at the supplied
     * size.
     *
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     * @param pSize the size of the icon.
     */
    public GovIconFontIcon(GovIcon pIcon, Color pColor, int pSize)
    {
        super(SwingUtilities.GOVICONS_FONT, pIcon, pColor, pSize);
    }
}
