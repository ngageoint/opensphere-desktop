package io.opensphere.mantle.data.geom.impl;

import java.util.stream.Collectors;

import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.mantle.data.DataTypeInfo;

/** Converts a Mantle polygon to a Core polygon. */
public final class PolygonConverter
{
    /**
     * Converts a Mantle polygon to a Core polygon.
     *
     * @param mantleGeom the mantle polygon
     * @param dataType the data type
     * @return the core polygon
     */
    public static PolygonGeometry convert(DefaultMapPolygonGeometrySupport mantleGeom, DataTypeInfo dataType)
    {
        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<>();
        builder.setVertices(
                mantleGeom.getLocations().stream().map(lla -> new GeographicPosition(lla)).collect(Collectors.toList()));
        DefaultPolygonRenderProperties renderProperties = new DefaultPolygonRenderProperties(
                dataType.getMapVisualizationInfo().getZOrder(), true, true);
        renderProperties.setColor(mantleGeom.getColor());
        renderProperties.setWidth(3);
        return new PolygonGeometry(builder, renderProperties, null);
    }

    /** Private constructor. */
    private PolygonConverter()
    {
    }
}
