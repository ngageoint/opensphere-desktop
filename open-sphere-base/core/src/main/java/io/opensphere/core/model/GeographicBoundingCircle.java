package io.opensphere.core.model;

/**
 * A circle with geographic center and radius in degrees.
 */
public class GeographicBoundingCircle
{
    /** Center of the circle. */
    private final GeographicPosition myCenter;

    /** Radius in decimal degrees. */
    private final double myRadiusD;

    /**
     * Constructor.
     *
     * @param center center of the circle.
     * @param radius radius in degrees
     */
    public GeographicBoundingCircle(GeographicPosition center, double radius)
    {
        myCenter = center;
        myRadiusD = radius;
    }

    /**
     * Determine whether the position is within the circle.
     *
     * @param position Position which may be in the circle.
     * @param tolerance The tolerance for error.
     * @return true when the position is contained.
     */
    public boolean contains(GeographicPosition position, double tolerance)
    {
        return myCenter.getLatLonAlt().asVec2d().distance(position.getLatLonAlt().asVec2d()) - myRadiusD > -tolerance;
    }

    /**
     * Get the center.
     *
     * @return the center
     */
    public GeographicPosition getCenter()
    {
        return myCenter;
    }

    /**
     * Get the radius in degrees.
     *
     * @return the radius in degrees
     */
    public double getRadiusD()
    {
        return myRadiusD;
    }

    /**
     * Determine whether the circles overlap.
     *
     * @param otherCircle The other circle which may overlap this one.
     * @return true when the circles overlap.
     */
    public boolean overlaps(GeographicBoundingCircle otherCircle)
    {
        return myCenter.getLatLonAlt().asVec2d().distance(otherCircle.getCenter().getLatLonAlt().asVec2d()) < myRadiusD
                + otherCircle.getRadiusD();
    }
}
