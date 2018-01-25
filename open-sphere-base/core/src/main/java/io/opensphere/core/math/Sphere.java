package io.opensphere.core.math;

/**
 * Interface for a double-precision sphere.
 */
public interface Sphere extends Ellipsoid
{
    /**
     * Tell whether the position is contained within this sphere. In this
     * context, a point will also be considered contained when it lies on the
     * boundary.
     *
     * @param position The position to test for containment.
     * @return true when the position is contained within this sphere.
     */
    boolean contains(Vector3d position);

    /**
     * Get the radius of the sphere.
     *
     * @return The radius of the sphere.
     */
    double getRadius();

    /**
     * Determine whether the spheres overlap. In this context, spheres will also
     * be considered overlapping when they touch on the boundaries.
     *
     * @param sphere The other sphere which may overlap this one.
     * @return true when the spheres overlap.
     */
    boolean overlaps(Sphere sphere);
}
