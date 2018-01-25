package io.opensphere.core.model;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;

/**
 * Interface for classes that provide position information.
 */
public interface Position
{
    /**
     * Add a position to this position and return the result.
     *
     * @param pos The vector to add.
     * @return The result position.
     */
    Position add(Position pos);

    /**
     * Add a vector to this position and return the result.
     *
     * @param vec The vector to add.
     * @return The result position.
     */
    Position add(Vector3d vec);

    /**
     * Get a representation of this position as a 3-D vector which has been
     * flattened onto the x-y plane.
     *
     * @return The vector.
     */
    Vector3d asFlatVector3d();

    /**
     * Get a representation of this position as a 2-D vector which has been
     * flattened onto the x-y plane.
     *
     * @return The vector.
     */
    Vector2d asVector2d();

    /**
     * Get a representation of this position as a 3-D vector.
     *
     * @return The vector.
     */
    Vector3d asVector3d();

    /**
     * Interpolate between this point and a given position.
     *
     * @param pos The other position.
     * @param fraction The fraction of the distance to the other position.
     * @return The interpolated position.
     */
    Position interpolate(Position pos, double fraction);

    /**
     * Subtract another position from this position and return a vector.
     *
     * @param pos The position to subtract.
     * @return The result vector.
     */
    Vector3d subtract(Position pos);
}
