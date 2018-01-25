package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * Simple icon implementation that just draws a square in a constant color.
 */
public class ColorIcon implements Icon
{
    /** Width of the icon. */
    private int myIconWidth = 24;

    /** Height of the icon. */
    private int myIconHeight = 24;

    /** Current Color of the icon. */
    private Color myColor = Color.WHITE;

    /**
     * Instantiates a new color icon.
     *
     */
    public ColorIcon()
    {
    }

    /**
     * Instantiates a new color icon.
     *
     * @param c the {@link Color}
     */
    public ColorIcon(Color c)
    {
        myColor = c;
    }

    /**
     * Instantiates a new color icon.
     *
     * @param c the {@link Color}
     * @param width the width
     * @param height the height
     */
    public ColorIcon(Color c, int width, int height)
    {
        myColor = c;
        myIconHeight = height;
        myIconWidth = width;
    }

    /**
     * Get the color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor;
    }

    @Override
    public int getIconHeight()
    {
        return myIconHeight;
    }

    @Override
    public int getIconWidth()
    {
        return myIconWidth;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setColor(myColor);
        g2d.fillRect(x, y, myIconWidth, myIconHeight);
        g2d.dispose();
    }

    /**
     * Set the color.
     *
     * @param color the color to set
     */
    public void setColor(Color color)
    {
        myColor = color;
    }

    /**
     * Set the height of the icon.
     *
     * @param height iconHeight to set
     */
    public void setIconHeight(int height)
    {
        myIconHeight = height;
    }

    /**
     * Set the width of the icon.
     *
     * @param width iconWidth to set
     */
    public void setIconWidth(int width)
    {
        myIconWidth = width;
    }
}
