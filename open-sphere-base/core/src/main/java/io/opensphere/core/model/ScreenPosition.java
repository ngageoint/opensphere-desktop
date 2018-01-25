package io.opensphere.core.model;

import java.awt.Point;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;

/**
 * A position on the screen (window coordinates).
 */
public class ScreenPosition implements Position
{
    /** The origin. */
    public static final ScreenPosition ZERO = new ScreenPosition(0., 0.);

    /** The horizontal coordinate. */
    private final double myX;

    /** The vertical coordinate. */
    private final double myY;

    /**
     * Create a screen position from a simple string.
     *
     * @see ScreenPosition#toSimpleString()
     * @param simpleString The simple string
     * @return the newly created screen position.
     */
    public static ScreenPosition fromSimpleString(String simpleString)
    {
        String[] coords = simpleString.split("/");
        return new ScreenPosition(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
    }

    /**
     * Create the position on the screen, with (0,0) being the top-left corner.
     * Negative coordinates loop around to the bottom-right such that (-1,-1) is
     * the bottom-right corner.
     *
     * @param x The horizontal coordinate.
     * @param y The vertical coordinate.
     */
    public ScreenPosition(double x, double y)
    {
        myX = x;
        myY = y;
    }

    /**
     * Create the position on the screen, with (0,0) being the top-left corner.
     * Negative coordinates loop around to the bottom-right such that (-1,-1) is
     * the bottom-right corner.
     *
     * @param x The horizontal coordinate.
     * @param y The vertical coordinate.
     */
    public ScreenPosition(int x, int y)
    {
        myX = x;
        myY = y;
    }

    /**
     * Create a screen position using a 2-D vector.
     *
     * @see #ScreenPosition(double, double)
     * @param vec The input vector.
     */
    public ScreenPosition(Vector2d vec)
    {
        myX = vec.getX();
        myY = vec.getY();
    }

    @Override
    public ScreenPosition add(Position pos)
    {
        if (pos instanceof ScreenPosition)
        {
            ScreenPosition scrPos = (ScreenPosition)pos;
            return new ScreenPosition(getX() + scrPos.getX(), getY() + scrPos.getY());
        }
        return null;
    }

    @Override
    public ScreenPosition add(Vector3d vec)
    {
        return new ScreenPosition(getX() + vec.getX(), getY() + vec.getY());
    }

    @Override
    public Vector3d asFlatVector3d()
    {
        return asVector3d();
    }

    /**
     * Get this position as a {@link Point}, truncating the coordinates to
     * integers.
     *
     * @return The point.
     */
    public Point asPoint()
    {
        return new Point((int)myX, (int)myY);
    }

    /**
     * Get this position as a 2-D vector.
     *
     * @return The vector.
     */
    @Override
    public Vector2d asVector2d()
    {
        return new Vector2d(myX, myY);
    }

    /**
     * Get this position as a 2-D vector, truncating the coordinates to
     * integers.
     *
     * @return The vector.
     */
    public Vector2i asVector2i()
    {
        return new Vector2i((int)myX, (int)myY);
    }

    @Override
    public Vector3d asVector3d()
    {
        return new Vector3d(myX, myY, 0.);
    }

    /**
     * Get the horizontal coordinate.
     *
     * @return The X value.
     */
    public double getX()
    {
        return myX;
    }

    /**
     * Get the vertical coordinate.
     *
     * @return The Y value.
     */
    public double getY()
    {
        return myY;
    }

    @Override
    public ScreenPosition interpolate(Position pos, double fraction)
    {
        Vector2d vec = asVector2d();
        Vector2d other = ((ScreenPosition)pos).asVector2d();
        vec = vec.interpolate(other, fraction);
        return new ScreenPosition(vec);
    }

    @Override
    public Vector3d subtract(Position pos)
    {
        return asVector3d().subtract(pos.asVector3d());
    }

    /**
     * Provide a simple string version of my data.
     *
     * @return A simple string.
     */
    public String toSimpleString()
    {
        StringBuilder sb = new StringBuilder(30);
        sb.append((int)getX()).append('/').append((int)getY());
        return sb.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(30);
        sb.append(getClass().getSimpleName()).append(" [").append(getX()).append('/').append(getY()).append(']');
        return sb.toString();
    }
}
