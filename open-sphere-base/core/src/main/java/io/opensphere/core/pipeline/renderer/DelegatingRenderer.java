package io.opensphere.core.pipeline.renderer;

import io.opensphere.core.geometry.Geometry;

/**
 * Interface for a renderer that delegates the actual rendering to another
 * renderer.
 *
 * @param <T> The type of geometry handled by the delegate renderer.
 */
public interface DelegatingRenderer<T extends Geometry>
{
    /**
     * Get the delegate renderer.
     *
     * @return The renderer.
     */
    GeometryRenderer<T> getRenderer();

    /**
     * Tell the renderer that it is has geometries which have changed and
     * require re-rendering.
     */
    void setDirty();
}
