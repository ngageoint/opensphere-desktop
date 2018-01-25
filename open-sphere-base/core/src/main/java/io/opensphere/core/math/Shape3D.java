package io.opensphere.core.math;

/** A 3-Dimensional {@link Shape}. */
public interface Shape3D extends Shape
{
    /**
     * Get the X axis of the shape.
     *
     * @return The X axis.
     */
    Vector3d getXAxis();

    /**
     * Get the Y axis of the shape.
     *
     * @return The Y axis.
     */
    Vector3d getYAxis();

    /**
     * Get the Z axis of the shape.
     *
     * @return The Z axis.
     */
    Vector3d getZAxis();
}
