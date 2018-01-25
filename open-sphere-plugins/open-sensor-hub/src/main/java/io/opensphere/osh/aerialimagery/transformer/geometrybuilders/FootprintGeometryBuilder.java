package io.opensphere.osh.aerialimagery.transformer.geometrybuilders;

import java.util.List;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.MutableConstraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * This class builds a quadrialteral polygon representing the camera's footprint
 * on the earth.
 */
public class FootprintGeometryBuilder implements GeometryBuilder
{
    /**
     * The order manager registry.
     */
    private final OrderManagerRegistry myOrderManager;

    /**
     * Constructs a new foot print geometry builder.
     *
     * @param orderRegistry The {@link OrderManagerRegistry}.
     */
    public FootprintGeometryBuilder(OrderManagerRegistry orderRegistry)
    {
        myOrderManager = orderRegistry;
    }

    @Override
    public Pair<List<Geometry>, List<Geometry>> buildGeometries(PlatformMetadata model, DataTypeInfo uavDataType,
            DataTypeInfo videoLayer)
    {
        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<>();

        builder.setVertices(model.getFootprint().getVertices());

        OrderParticipantKey orderKey = uavDataType.getOrderKey();
        int zorder = myOrderManager.getOrderManager(orderKey).getOrder(orderKey);

        Constraints constraints = new MutableConstraints(
                TimeConstraint.getMostRecentTimeConstraint(uavDataType.getTypeKey(), model.getTime().getTime()));
        PolygonRenderProperties renderProperties = new DefaultPolygonRenderProperties(zorder, true, true);
        renderProperties.setColor(uavDataType.getBasicVisualizationInfo().getTypeColor());
        renderProperties.setHidden(!uavDataType.isVisible());
        setOpacity(renderProperties, uavDataType.getBasicVisualizationInfo().getTypeOpacity());

        return new Pair<>(New.list(new PolygonGeometry(builder, renderProperties, constraints)), New.list());
    }

    @Override
    public boolean cachePublishedGeometries()
    {
        return true;
    }

    /**
     * Set the opacity on a geometry.
     *
     * @param renderProperties The geometry render properties.
     * @param opacity The opacity.
     */
    private void setOpacity(ColorRenderProperties renderProperties, int opacity)
    {
        renderProperties.setColor(ColorUtilities.opacitizeColor(renderProperties.getColor(),
                opacity / (float)ColorUtilities.COLOR_COMPONENT_MAX_VALUE));
    }
}
