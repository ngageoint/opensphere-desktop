package io.opensphere.core.model;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;

/**
 * A position in the view (model coordinates).
 */
public class ModelPosition implements Position
{
    /** The coordinates. */
    private final Vector3d myPosition;

    /**
     * Create the position in model coordinates.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     */
    public ModelPosition(double x, double y, double z)
    {
        myPosition = new Vector3d(x, y, z);
    }

    /**
     * Create the position in model coordinates.
     *
     * @param pos position is copied.
     */
    public ModelPosition(Vector3d pos)
    {
        myPosition = new Vector3d(pos);
    }

    @Override
    public Position add(Position pos)
    {
        if (pos instanceof ModelPosition)
        {
            ModelPosition mdlPos = (ModelPosition)pos;
            return new ModelPosition(myPosition.add(mdlPos.getPosition()));
        }
        return null;
    }

    @Override
    public Position add(Vector3d vec)
    {
        return new ModelPosition(myPosition.add(vec));
    }

    @Override
    public Vector3d asFlatVector3d()
    {
        return new Vector3d(myPosition.getX(), myPosition.getY(), 0.);
    }

    @Override
    public Vector2d asVector2d()
    {
        return new Vector2d(myPosition.getX(), myPosition.getY());
    }

    @Override
    public Vector3d asVector3d()
    {
        return myPosition;
    }

    /**
     * Get the position.
     *
     * @return the position
     */
    public Vector3d getPosition()
    {
        return myPosition;
    }

    /**
     * Get the x coordinate.
     *
     * @return the x coordinate.
     */
    public double getX()
    {
        return myPosition.getX();
    }

    /**
     * Get the y coordinate.
     *
     * @return the y coordinate.
     */
    public double getY()
    {
        return myPosition.getY();
    }

    /**
     * Get the z coordinate.
     *
     * @return the z coordinate.
     */
    public double getZ()
    {
        return myPosition.getZ();
    }

    @Override
    public Position interpolate(Position pos, double fraction)
    {
        return new ModelPosition(myPosition.interpolate(((ModelPosition)pos).getPosition(), fraction));
    }

    @Override
    public Vector3d subtract(Position pos)
    {
        return myPosition.subtract(((ModelPosition)pos).getPosition());
    }

    /**
     * Provide a simple string version of my data.
     *
     * @return A simple string.
     */
    public String toSimpleString()
    {
        StringBuilder sb = new StringBuilder(30);
        sb.append(myPosition.getX()).append('/').append(myPosition.getY()).append('/').append(myPosition.getZ());
        return sb.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(30);
        sb.append(getClass().getSimpleName()).append(" [").append(myPosition.getX());
        sb.append('/').append(myPosition.getY()).append('/').append(myPosition.getZ()).append(']');
        return sb.toString();
    }
}
