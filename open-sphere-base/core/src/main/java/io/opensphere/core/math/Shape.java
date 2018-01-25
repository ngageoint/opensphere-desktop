package io.opensphere.core.math;

/**
 * Base interface for a double-precision shape.
 */
public interface Shape
{
    /**
     * Get the radius of the sphere which bounds the shape.
     *
     * @return the radius of the sphere which bounds the shape.
     */
    double getBoundingRadius();

    /**
     * Get the heading of the direction vector from the location.
     *
     * @param location The location from which the heading is calculated.
     * @param direction The direction from the location for which the heading is
     *            desired.
     * @return The heading.
     */
    double getHeading(Vector3d location, Vector3d direction);

    /**
     * Determine the location where the ray intersects this shape.
     *
     * @param ray The ray to intersect.
     * @return The intersection point or {@code null}.
     */
    Vector3d getIntersection(Ray3d ray);

    /**
     * Get relative direction of north from the given direction and orientation.
     *
     * @param direction The unit vector from the location for which the heading
     *            is desired.
     * @param orientation The local up unit vector at the location.
     * @return The heading.
     */
    double getMapOrientationHeading(Vector3d direction, Vector3d orientation);

    /**
     * Get the name of the shape.
     *
     * @return The name of the shape.
     */
    String getName();
}
