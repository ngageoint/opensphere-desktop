package io.opensphere.core.util.swing.tags;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.AbstractBorder;

/**
 * A border with rounded corners.
 */
public class RoundedBorder extends AbstractBorder
{
    /** The unique identifier used for serialization. */
    private static final long serialVersionUID = 4806604281876296421L;

    /** The color of the border. */
    private final Color myColor;

    /** The insets of the border. */
    private final Insets myInsets;

    /**
     * Creates a default rounded border.
     */
    public RoundedBorder()
    {
        this(new Color(0, 0, 0, 220));
    }

    /**
     * Creates a rounded border with the supplied color.
     *
     * @param color the color of the border.
     */
    public RoundedBorder(Color color)
    {
        myColor = color;
        myInsets = new Insets(3, 3, 3, 3);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D)g).setColor(myColor);
        ((Graphics2D)g).drawRoundRect(x, y, width - 1, height - 1, 12, 12);
    }

    @Override
    public Insets getBorderInsets(Component c)
    {
        return myInsets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets)
    {
        insets.top = myInsets.top;
        insets.left = myInsets.left;
        insets.right = myInsets.right;
        insets.bottom = myInsets.bottom;
        return insets;
    }
}