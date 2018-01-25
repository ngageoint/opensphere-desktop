package io.opensphere.core.geometry;

import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;

/**
 * A {@link Geometry} that has color.
 *
 */
public interface ColorGeometry extends RenderableGeometry
{
    @Override
    ColorRenderProperties getRenderProperties();
}
