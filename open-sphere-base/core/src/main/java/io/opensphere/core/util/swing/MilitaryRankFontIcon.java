package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.GraphicsEnvironment;

import javax.swing.Icon;

import io.opensphere.core.util.MilitaryRankIcon;

/**
 * A rendered text icon in which the {@link MilitaryRankIcon} is drawn as a
 * Swing-compatible {@link Icon} instance.
 */
public class MilitaryRankFontIcon extends AbstractFontIcon
{
    static
    {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(SwingUtilities.MILITARY_RANK_FONT);
    }

    /**
     * Creates a new icon.
     *
     * @param pIcon the icon to draw.
     */
    public MilitaryRankFontIcon(MilitaryRankIcon pIcon)
    {
        super(SwingUtilities.MILITARY_RANK_FONT, pIcon);
    }

    /**
     * Creates a new icon, painted with the supplied color.
     *
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     */
    public MilitaryRankFontIcon(MilitaryRankIcon pIcon, Color pColor)
    {
        super(SwingUtilities.MILITARY_RANK_FONT, pIcon, pColor);
    }

    /**
     * Creates a new icon, painted with the supplied color, at the supplied
     * size.
     *
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     * @param pSize the size of the icon.
     */
    public MilitaryRankFontIcon(MilitaryRankIcon pIcon, Color pColor, int pSize)
    {
        super(SwingUtilities.MILITARY_RANK_FONT, pIcon, pColor, pSize);
    }
}
