package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * The Class ColorCircleIcon.
 */
public class ColorCircleIcon implements Icon
{
    /** The circle color. */
    private Color myColor = Color.RED;

    /** The circle diameter. */
    private int myDiameter = 10;

    /**
     * Constructor.
     *
     * @param color The color.
     */
    public ColorCircleIcon(Color color)
    {
        myColor = color;
    }

    /**
     * Constructor.
     *
     * @param color The color.
     * @param diameter the diameter
     */
    public ColorCircleIcon(Color color, int diameter)
    {
        myColor = color;
        myDiameter = diameter;
    }

    /**
     * Standard getter.
     *
     * @return The color.
     */
    public Color getColor()
    {
        return myColor;
    }

    @Override
    public int getIconHeight()
    {
        return myDiameter;
    }

    @Override
    public int getIconWidth()
    {
        return myDiameter;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setColor(myColor);
        g2d.fillOval(x, y, myDiameter, myDiameter);
        g2d.dispose();
    }

    /**
     * Sets the color.
     *
     * @param color the new color
     */
    public void setColor(Color color)
    {
        myColor = color;
    }

    /**
     * Standard setter.
     *
     * @param diameter The diameter.
     */
    public void setDiameter(int diameter)
    {
        myDiameter = diameter;
    }
}
