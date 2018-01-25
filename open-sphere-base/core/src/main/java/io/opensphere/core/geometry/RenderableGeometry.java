package io.opensphere.core.geometry;

import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;

/**
 * Interface for geometries that can be rendered.
 */
public interface RenderableGeometry extends Geometry
{
    @Override
    BaseRenderProperties getRenderProperties();
}
