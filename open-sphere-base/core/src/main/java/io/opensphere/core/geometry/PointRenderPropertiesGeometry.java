package io.opensphere.core.geometry;

import io.opensphere.core.geometry.renderproperties.PointRenderProperties;

/**
 * Interface for geometries that use {@link PointRenderProperties}.
 */
@FunctionalInterface
public interface PointRenderPropertiesGeometry
{
    /**
     * Get the render properties.
     *
     * @return The render properties.
     */
    PointRenderProperties getRenderProperties();
}
