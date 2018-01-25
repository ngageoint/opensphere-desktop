package io.opensphere.core.geometry;

import io.opensphere.core.geometry.constraint.Constraints;

/**
 * Interface for geometries that can have constraints.
 */
public interface ConstrainableGeometry extends Geometry
{
    /**
     * Get the constraints on the visibility for this geometry.
     *
     * @return The visibility constraints.
     */
    Constraints getConstraints();
}
