package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.EtchedBorder;

/**
 * The Class DynamicEtchedBorder.
 */
public class DynamicEtchedBorder extends EtchedBorder
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Border color. */
    private Color myBorderColor = Color.LIGHT_GRAY;

    /** The Control component width. */
    private final int myControlComponentWidth;

    /**
     * Instantiates a new dynamic etched border.
     *
     * @param borderColor the border color
     * @param comp the comp
     */
    public DynamicEtchedBorder(Color borderColor, Component comp)
    {
        super();
        if (borderColor != null)
        {
            myBorderColor = borderColor;
        }
        myControlComponentWidth = comp.getPreferredSize().width;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        int w = width;
        int h = height;

        g.translate(x, y);

        g.setColor(etchType == LOWERED ? getShadowColor(c) : getHighlightColor(c));
        if (myControlComponentWidth == 0)
        {
            g.drawRect(0, 0, w - 2, h - 2);
        }
        else
        {
            // Left side
            g.drawLine(0, h - 4, 0, 0);

            // Top side
            g.drawLine(0, 0, ComponentTitledBorder.LEFT_OFFSET - 1, 0);
            g.drawLine(myControlComponentWidth + ComponentTitledBorder.LEFT_OFFSET + 2, 0, w - 3, 0);

            // Bottom side
            g.drawLine(0, h - 2, w - 2, h - 2);

            // Right side
            g.drawLine(w - 2, h - 2, w - 2, 0);
        }

        g.setColor(etchType == LOWERED ? getHighlightColor(c) : getShadowColor(c));
        if (myControlComponentWidth == 0)
        {
            g.drawLine(1, h - 3, 1, 1);
            g.drawLine(1, 1, w - 3, 1);
            g.drawLine(0, h - 1, w - 1, h - 1);
            g.drawLine(w - 1, h - 1, w - 1, 0);
        }
        else
        {
            // Left side
            g.drawLine(1, h - 3, 1, 1);

            // Top side
            g.drawLine(1, 1, ComponentTitledBorder.LEFT_OFFSET - 1, 1);
            g.drawLine(myControlComponentWidth + ComponentTitledBorder.LEFT_OFFSET + 2, 1, w - 3, 1);

            // Bottom side
            g.drawLine(0, h - 1, w - 1, h - 1);

            // Right side
            g.drawLine(w - 1, h - 1, w - 1, 0);
        }

        g.translate(-x, -y);
    }

    /**
     * Sets the border highlight.
     *
     * @param highlighted on/off switch for the highlighting
     */
    public void setHighlighted(boolean highlighted)
    {
        etchType = highlighted ? 0 : 1;
        highlight = highlighted ? myBorderColor : null;
    }
}
