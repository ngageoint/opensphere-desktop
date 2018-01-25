package io.opensphere.core.util.swing.pie;

import java.awt.Color;

import io.opensphere.core.util.MathUtil;

/**
 * This holds a start angle, an end angle, and the color the arc should be
 * drawn.
 */
public class Arc
{
    /** The Start angle. */
    private final double myStartAngle;

    /** The Stop angle. */
    private final double myStopAngle;

    /** The Color. */
    private final Color myColor;

    /**
     * Instantiates a new arc.
     *
     * @param startAngle The start angle in degrees, 0 is up.
     * @param stopAngle The stop angle in degrees.
     * @param color The arc color.
     */
    public Arc(double startAngle, double stopAngle, Color color)
    {
        myStartAngle = startAngle;
        myStopAngle = stopAngle;
        myColor = color;
    }

    /**
     * Gets the color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Get the extent of the arc; i.e., the angle subtended, positive being
     * clockwise.
     *
     * @return The extent of the arc.
     */
    public int getExtent()
    {
        return (int)Math.round(MathUtil.normalize(myStopAngle - myStartAngle, 0, 360));
    }

    /**
     * Gets the start angle.
     *
     * @return the start angle
     */
    public double getStartAngle()
    {
        return myStartAngle;
    }

    /**
     * Gets the stop angle.
     *
     * @return the stop angle
     */
    public double getStopAngle()
    {
        return myStopAngle;
    }
}
