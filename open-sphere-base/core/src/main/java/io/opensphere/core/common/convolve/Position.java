package io.opensphere.core.common.convolve;

/**
 * Simple container class for storing X, Y, and Z coordinate information.
 *
 */
public class Position
{
    private double x;

    private double y;

    private double z;

    /**
     * Default Constructor, sets position to (0,0,0).
     */
    public Position()
    {
        x = 0;
        y = 0;
        z = 0;
    }

    /**
     * Constructor that sets position to (x,y,0).
     *
     * @param x
     * @param y
     */
    public Position(double x, double y)
    {
        this.x = x;
        this.y = y;
        z = 0;
    }

    /**
     * Constructor that sets position to (x,y,z)
     *
     * @param x
     * @param y
     * @param z
     */
    public Position(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(Position pos)
    {
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    /**
     * Get the X coordinate.
     *
     * @return x
     */
    public double getX()
    {
        return x;
    }

    /**
     * Sets the X coordinate.
     *
     * @param x The new X coordinate
     */
    public void setX(double x)
    {
        this.x = x;
    }

    /**
     * Gets the Y coordinate.
     *
     * @return y
     */
    public double getY()
    {
        return y;
    }

    /**
     * Sets the Y coordinate.
     *
     * @param y The new Y coordinate
     */
    public void setY(double y)
    {
        this.y = y;
    }

    /**
     * Gets the Z coordinate.
     *
     * @return z
     */
    public double getZ()
    {
        return z;
    }

    /**
     * Sets the Z coordinate.
     *
     * @param z
     */
    public void setZ(double z)
    {
        this.z = z;
    }

    /**
     * Converts to string.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "X: " + x + "; Y: " + y + "; Z: ";
    }

}
