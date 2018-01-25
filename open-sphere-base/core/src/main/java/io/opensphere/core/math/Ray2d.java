package io.opensphere.core.math;

/**
 * Ray in 2 dimensions.
 */
public class Ray2d
{
    /** Direction of the ray. */
    private final Vector2d myDirection;

    /** position of the ray. */
    private final Vector2d myPosition;

    /**
     * Construct me.
     *
     * @param position position
     * @param direction direction
     */
    public Ray2d(Vector2d position, Vector2d direction)
    {
        myPosition = position;
        myDirection = direction.getNormalized();
    }

    /**
     * Get the direction.
     *
     * @return the direction
     */
    public Vector2d getDirection()
    {
        return myDirection;
    }

    /**
     * Get the distance from me to the line defined by the given points.
     *
     * @param ptA first point
     * @param ptB second point
     * @return distance
     */
    public double getDistance(Vector2d ptA, Vector2d ptB)
    {
        Vector2d ab = ptB.subtract(ptA);
        Vector2d norm = new Vector2d(ab.getY(), -ab.getX());
        Line2d line = new Line2d(ptA, norm);
        return line.getDistance(this);
    }

    /**
     * Get the intersection of me with the line segment defined by the points.
     *
     * @param ptA first point
     * @param ptB second point
     * @return intersection
     */
    public Vector2d getIntersection(Vector2d ptA, Vector2d ptB)
    {
        Line2d colinear = new Line2d(myPosition, myDirection.getPerpendicular());
        if (colinear.onSameSide(ptA, ptB))
        {
            return null;
        }
        Line2d actualLine = new Line2d(ptA, ptB.subtract(ptA).getPerpendicular());
        return actualLine.getIntersection(this, false);
    }

    /**
     * Get the position.
     *
     * @return the position
     */
    public Vector2d getPosition()
    {
        return myPosition;
    }

    /**
     * Tell whether there exists an intersection between me and the line segment
     * defined by the points.
     *
     * @param ptA first point
     * @param ptB second point
     * @return true when an intersection exists.
     */
    public boolean hasIntersection(Vector2d ptA, Vector2d ptB)
    {
        Line2d colinear = new Line2d(myPosition, myDirection.getPerpendicular());
        if (colinear.onSameSide(ptA, ptB))
        {
            return false;
        }
        Line2d actualLine = new Line2d(ptA, ptB.subtract(ptA).getPerpendicular());
        double dist = actualLine.getDistance(this);
        return !Double.isNaN(dist) && dist >= 0.;
    }

    /**
     * Check whether the point is in front of me within the given tolerance.
     *
     * @param position position
     * @param distance tolerance
     * @return true if the point is in front of me within the given tolerance.
     */
    public boolean isInFront(Vector2d position, double distance)
    {
        return position.subtract(myPosition).dot(myDirection) > -distance;
    }
}
