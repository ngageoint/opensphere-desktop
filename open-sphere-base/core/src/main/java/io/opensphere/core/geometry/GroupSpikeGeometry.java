package io.opensphere.core.geometry;

import java.util.Map.Entry;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.renderproperties.DefaultMeshScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.lang.Pair;

/**
 * A geometry of grouped points that display a spike for the points. The spike
 * height is variable to the intensity of other nearby points.
 */
public class GroupSpikeGeometry extends AbstractGroupHeightGeometry
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GroupSpikeGeometry.class);

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     */
    public GroupSpikeGeometry(AbstractGroupHeightGeometry.Builder builder, ZOrderRenderProperties renderProperties)
    {
        super(builder, renderProperties);
    }

    /**
     * Create the geometries for the locations.
     */
    @Override
    protected void createGeometries()
    {
        // Create spike geometries (frustum with four points and top radius of
        // zero).
        for (Entry<Pair<Integer, Integer>, GridCoordinateInfo> entry : getGridCoordinatesMap().entrySet())
        {
            if (!entry.getValue().getPositions().isEmpty())
            {
                ScalableMeshRenderProperties spikeProperty = new DefaultMeshScalableRenderProperties(ZOrderRenderProperties.TOP_Z,
                        true, false);
                spikeProperty.setColor(getRenderProperties().getColor());
                spikeProperty.setBaseAltitude(getRenderProperties().getBaseAltitude());
                spikeProperty.setBaseColor(getRenderProperties().getBaseColor());
                spikeProperty.setHidden(getRenderProperties().isHidden());
                spikeProperty.setHighlightColor(getRenderProperties().getHighlightColor());

                float adjustedHeight = (float)entry.getValue().getAdjustedHeight();
                spikeProperty.setHeight(getRenderProperties().getHeight() + adjustedHeight);

                FrustumGeometry.Builder<Position> spikeBuilder = new FrustumGeometry.Builder<Position>();
                spikeBuilder.setPosition(entry.getValue().getCenterLocation());
                spikeBuilder.setCircularPoints(4);
                spikeBuilder.setBaseRadius(getRenderProperties().getWidth());
                spikeBuilder.setTopRadius(0f);

                FrustumGeometry frustum = new FrustumGeometry(spikeBuilder, spikeProperty, null);

                getGeometries().add(frustum);
            }
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("The number of created geometries = " + getGeometries().size());
        }
    }
}
