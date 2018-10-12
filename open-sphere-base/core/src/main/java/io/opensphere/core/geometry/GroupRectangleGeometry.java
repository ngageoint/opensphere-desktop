package io.opensphere.core.geometry;

import java.util.Map.Entry;

import io.opensphere.core.geometry.renderproperties.DefaultMeshScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.lang.Pair;

/**
 * A geometry of grouped points that display a column. The surface height is
 * variable to the intensity of other nearby points.
 */
public class GroupRectangleGeometry extends AbstractGroupHeightGeometry
{
    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     */
    public GroupRectangleGeometry(AbstractGroupHeightGeometry.Builder builder, ZOrderRenderProperties renderProperties)
    {
        super(builder, renderProperties);
    }

    /**
     * Create the geometries for the locations.
     */
    @Override
    protected void createGeometries()
    {
        // Create "square column" geometries
        for (Entry<Pair<Integer, Integer>, GridCoordinateInfo> entry : getGridCoordinatesMap().entrySet())
        {
            if (!entry.getValue().getPositions().isEmpty())
            {
                ScalableMeshRenderProperties spikeProperty = new DefaultMeshScalableRenderProperties(ZOrderRenderProperties.TOP_Z,
                        true, false);
                spikeProperty.setColor(getRenderProperties().getColor());
                spikeProperty.setBaseAltitude(getRenderProperties().getBaseAltitude());
                spikeProperty.setBaseColor(getRenderProperties().getBaseColor());

                float adjustedHeight = (float)entry.getValue().getAdjustedHeight();
                spikeProperty.setHeight(getRenderProperties().getHeight() + adjustedHeight);

                FrustumGeometry.Builder<Position> spikeBuilder = new FrustumGeometry.Builder<>();
                spikeBuilder.setPosition(entry.getValue().getCenterLocation());
                spikeBuilder.setCircularPoints(4);
                spikeBuilder.setBaseRadius(getRenderProperties().getWidth());
                spikeBuilder.setTopRadius(getRenderProperties().getWidth());

                FrustumGeometry frustum = new FrustumGeometry(spikeBuilder, spikeProperty, null);

                getGeometries().add(frustum);
            }
        }
    }
}
