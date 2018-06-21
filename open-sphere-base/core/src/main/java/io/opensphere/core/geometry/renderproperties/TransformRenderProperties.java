package io.opensphere.core.geometry.renderproperties;

import edu.umd.cs.findbugs.annotations.Nullable;

import io.opensphere.core.math.Matrix4d;

/**
 * Render properties for geometries that have their own transformation matrices.
 */
public interface TransformRenderProperties extends RenderProperties
{
    /**
     * Get a transformation matrix that can be multiplied with the standard
     * view's model-view matrix to get a model-view matrix for rendering the
     * associated geometries.
     *
     * @return The transform.
     */
    @Nullable
    Matrix4d getTransform();

    /**
     * Set a transformation matrix that can be multiplied with the standard
     * view's model-view matrix to get a model-view matrix for rendering the
     * associated geometries.
     *
     * @param transform The transform.
     */
    void setTransform(@Nullable Matrix4d transform);
}
