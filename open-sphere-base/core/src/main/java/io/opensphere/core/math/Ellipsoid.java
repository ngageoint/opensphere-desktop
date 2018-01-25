package io.opensphere.core.math;

import io.opensphere.core.util.SizeProvider;

/**
 * An shape whose cross-sections are ellipses.
 */
public interface Ellipsoid extends Shape3D, SizeProvider
{
    /**
     * Transform a direction to the ellipsoid's local coordinate system.
     *
     * @param direction The direction in standard coordinates.
     * @return The direction in the ellipsoid's local coordinate system.
     */
    Vector3d directionToLocal(Vector3d direction);

    /**
     * Transform a direction to the ellipsoid's local coordinate system.
     *
     * @param direction The direction in standard coordinates.
     * @return The direction in the ellipsoid's local coordinate system.
     */
    Vector3d directionToModel(Vector3d direction);

    /**
     * Get the center.
     *
     * @return the center
     */
    Vector3d getCenter();

    /**
     * Get the tangent point on the ellipsoid where the normal is equal to the
     * input vector.
     *
     * @param normal The normal vector.
     * @return The tangent point.
     */
    Vector3d getTangent(Vector3d normal);

    /**
     * Get if this ellipsoid is a sphere.
     *
     * @return If this is a sphere.
     */
    boolean isSphere();

    /**
     * Transform the position from the ellipsoid's local coordinate system to
     * standard coordinates.
     *
     * @param position The position in the ellipsoid's local coordinate system.
     * @return The position in standard coordinates.
     */
    Vector3d localToModel(Vector3d position);

    /**
     * Transform the position from standard coordinates to the ellipsoid's local
     * coordinate system.
     *
     * @param position The position in standard coordinates.
     * @return The position in the ellipsoid's local coordinate system.
     */
    Vector3d modelToLocal(Vector3d position);

    /**
     * Convert a normal vector in model coordinates to the equivalent normal
     * vector in the ellipsoid's local coordinate system.
     *
     * @param normal The normal in model coordinates.
     * @return The normal in the ellipsoid's local coordinate system.
     */
    Vector3d normalToLocal(Vector3d normal);
}
