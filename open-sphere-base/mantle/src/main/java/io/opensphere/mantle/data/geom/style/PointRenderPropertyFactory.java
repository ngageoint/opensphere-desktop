package io.opensphere.mantle.data.geom.style;

import io.opensphere.core.geometry.renderproperties.BaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;

/**
 * A factory for creating PointRenderProperty objects.
 */
@FunctionalInterface
public interface PointRenderPropertyFactory
{
    /**
     * Creates a new PointRenderProperty object.
     *
     * @param baseRp the base rp
     * @param pointSizeRP the point size rp
     * @return the point render properties
     */
    PointRenderProperties createPointRenderProperties(BaseAltitudeRenderProperties baseRp, PointSizeRenderProperty pointSizeRP);
}
