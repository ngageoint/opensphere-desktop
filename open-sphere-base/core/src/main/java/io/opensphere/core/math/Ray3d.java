package io.opensphere.core.math;

/**
 * Ray in 3 dimensions.
 */
public class Ray3d
{
    /** Direction of the ray. */
    private final Vector3d myDirection;

    /** position of the ray. */
    private final Vector3d myPosition;

    /**
     * Construct me.
     *
     * @param position position
     * @param direction direction
     */
    public Ray3d(Vector3d position, Vector3d direction)
    {
        myPosition = position;
        myDirection = direction.getNormalized();
    }

    /**
     * Get the direction.
     *
     * @return the direction
     */
    public Vector3d getDirection()
    {
        return myDirection;
    }

    /**
     * Get the intersection of me with an ellipsoid.
     *
     * @param ell ellipsoid
     * @return intersection.
     */
    public Vector3d getIntersection(Ellipsoid ell)
    {
        return ell.getIntersection(this);
    }

    /**
     * Get the intersection of me with the given plane.
     *
     * @param plane plane
     * @return intersection
     */
    public Vector3d getIntersection(Plane plane)
    {
        return plane.getIntersection(this);
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
     * Check whether the point is in front of me within the given tolerance.
     *
     * @param position position
     * @param distance tolerance
     * @return true if the point is in front of me within the given tolerance.
     */
    public boolean isInFront(Vector3d position, double distance)
    {
        return position.subtract(myPosition).dot(myDirection) > -distance;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Position");
        sb.append(myPosition).append(", Direction").append(myDirection);
        return sb.toString();
    }
}
