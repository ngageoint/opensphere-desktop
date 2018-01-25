package io.opensphere.core.math;

/**
 * Circle shape.
 *
 * @param <T> The type of the center vector.
 */
public class Circle<T extends AbstractVector>
{
    /** The center. */
    private final T myCenter;

    /** The radius. */
    private final double myRadius;

    /**
     * Construct me.
     *
     * @param center center
     * @param radius radius
     */
    public Circle(T center, double radius)
    {
        super();
        myCenter = center;
        myRadius = radius;
    }

    /**
     * Determine whether the position is within the circle.
     *
     * @param position Position which may be in the circle.
     * @param tolerance The tolerance for error.
     * @return true when the position is contained.
     */
    public boolean contains(Vector2d position, double tolerance)
    {
        return myCenter.distance(position) - myRadius > -tolerance;
    }

    /**
     * Get the center.
     *
     * @return the center
     */
    public T getCenter()
    {
        return myCenter;
    }

    /**
     * Get the radius.
     *
     * @return the radius
     */
    public double getRadius()
    {
        return myRadius;
    }

    /**
     * Determine whether the circles overlap.
     *
     * @param otherCircle The other circle which may overlap this one.
     * @return true when the circles overlap.
     */
    public boolean overlaps(Circle<? extends T> otherCircle)
    {
        return myCenter.distance(otherCircle.getCenter()) < myRadius + otherCircle.getRadius();
    }
}
