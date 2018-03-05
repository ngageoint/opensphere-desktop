package io.opensphere.analysis.heatmap;

import java.util.List;
import java.util.stream.Collectors;

import io.opensphere.core.geometry.EllipseGeometry;
import io.opensphere.core.geometry.LineOfBearingGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLOBRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.MapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.MapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractEllipseFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractLOBFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.CircleGeometryFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.DynamicEllipseFeatureVisualization;
import io.opensphere.mantle.data.geom.style.impl.DynamicLOBFeatureVisualization;
import io.opensphere.mantle.data.geom.style.impl.StyleUtils;

/** Geometry utilities. */
public final class HeatmapGeometryUtilities
{
    /**
     * Lets the caller know if the style needs conversion (for performance).
     *
     * @param style the style
     * @return whether it needs conversion
     */
    public static boolean needsConversion(FeatureVisualizationStyle style)
    {
        return style instanceof AbstractEllipseFeatureVisualizationStyle || style instanceof AbstractLOBFeatureVisualizationStyle;
    }

    /**
     * Converts the mantle geometry to the appropriate mantle geometry based on
     * the style.
     *
     * @param geomInfo the geometry info
     * @param style the style
     * @param dataType the data type
     * @param projection the projection
     * @return the converted geometry
     */
    public static MapGeometrySupport convertGeometry(GeometryInfo geomInfo, FeatureVisualizationStyle style,
            DataTypeInfo dataType, Projection projection)
    {
        MapLocationGeometrySupport geom = (MapLocationGeometrySupport)geomInfo.getGeometry();

        MapGeometrySupport convertedGeom = geom;
        if (style instanceof CircleGeometryFeatureVisualizationStyle)
        {
            CircleGeometryFeatureVisualizationStyle circleStyle = (CircleGeometryFeatureVisualizationStyle)style;
            if (geom instanceof MapCircleGeometrySupport)
            {
                MapCircleGeometrySupport circle = (MapCircleGeometrySupport)geom;
                convertedGeom = buildEllipse(circle, circleStyle, projection, 0, circle.getRadius(), circle.getRadius());
            }
        }
        else if (style instanceof AbstractEllipseFeatureVisualizationStyle)
        {
            AbstractEllipseFeatureVisualizationStyle ellipseStyle = (AbstractEllipseFeatureVisualizationStyle)style;
            if (geom instanceof MapEllipseGeometrySupport)
            {
                MapEllipseGeometrySupport ellipse = (MapEllipseGeometrySupport)geom;
                convertedGeom = buildEllipse(ellipse, ellipseStyle, projection, ellipse.getOrientation(),
                        ellipse.getSemiMajorAxis(), ellipse.getSemiMinorAxis());
            }
            else if (style instanceof DynamicEllipseFeatureVisualization)
            {
                DynamicEllipseFeatureVisualization dynEllipseStyle = (DynamicEllipseFeatureVisualization)style;
                MetaDataProvider provider = new MDILinkedMetaDataProvider(dataType.getMetaDataInfo(), geomInfo.getMetaData());
                double[] smaSmiOrn = dynEllipseStyle.getSmaSmiOrnFromMetaData(geom.getLocation(), 0, provider);
                if (smaSmiOrn != null)
                {
                    convertedGeom = buildEllipse(geom, dynEllipseStyle, projection, smaSmiOrn[2], smaSmiOrn[0], smaSmiOrn[1]);
                }
            }
        }
        else if (style instanceof AbstractLOBFeatureVisualizationStyle)
        {
            AbstractLOBFeatureVisualizationStyle lobStyle = (AbstractLOBFeatureVisualizationStyle)style;
            MetaDataProvider provider = new MDILinkedMetaDataProvider(dataType.getMetaDataInfo(), geomInfo.getMetaData());
            if (geom instanceof MapLineOfBearingGeometrySupport)
            {
                float orientation = ((MapLineOfBearingGeometrySupport)geom).getOrientation();
                convertedGeom = buildLine(geom, lobStyle, orientation, provider);
            }
            else if (style instanceof DynamicLOBFeatureVisualization)
            {
                float orientation = NumberUtilities.toFloat(lobStyle.getLobOrientation(0, geom, provider), 0f);
                convertedGeom = buildLine(geom, lobStyle, orientation, provider);
            }
        }
        return convertedGeom;
    }

    /**
     * Builds a polygon geometry from the point geometry.
     *
     * @param geom the mantle geometry
     * @param style the style
     * @param projection the projection
     * @param orientation the orientation
     * @param semiMajor the semi-major axis
     * @param semiMinor the semi-minor axis
     * @return the polygon geometry
     */
    private static MapPolygonGeometrySupport buildEllipse(MapLocationGeometrySupport geom,
            AbstractEllipseFeatureVisualizationStyle style, Projection projection, double orientation, double semiMajor,
            double semiMinor)
    {
        EllipseGeometry.ProjectedBuilder builder = new EllipseGeometry.ProjectedBuilder();
        builder.setProjection(projection);
        builder.setCenter(new GeographicPosition(geom.getLocation()));
        builder.setAngle(orientation);
        builder.setSemiMajorAxis(StyleUtils.getValueInMeters(semiMajor, style.getAxisUnit()));
        builder.setSemiMinorAxis(StyleUtils.getValueInMeters(semiMinor, style.getAxisUnit()));

        PolygonRenderProperties props = new DefaultPolygonRenderProperties(0, true, false);
        EllipseGeometry coreEllipse = new EllipseGeometry(builder, props, null);
        List<LatLonAlt> locations = coreEllipse.getVertices().stream().map(v -> ((GeographicPosition)v).getLatLonAlt())
                .collect(Collectors.toList());
        return new SimpleMapPolygonGeometrySupport(locations, null);
    }

    /**
     * Builds a line geometry from the point geometry.
     *
     * @param geom the mantle geometry
     * @param style the style
     * @param orientation the orientation
     * @param provider the meta data provider
     * @return the polyline geometry
     */
    private static MapPolylineGeometrySupport buildLine(MapLocationGeometrySupport geom,
            AbstractLOBFeatureVisualizationStyle style, float orientation, MetaDataProvider provider)
    {
        LineOfBearingGeometry.Builder builder = new LineOfBearingGeometry.Builder();
        builder.setPosition(new GeographicPosition(geom.getLocation()));
        builder.setLineOrientation(orientation);

        DefaultLOBRenderProperties props = new DefaultLOBRenderProperties(0, true, false);
        props.setLineLength((float)style.getLobLength(provider).inMeters());
        LineOfBearingGeometry coreLOB = new LineOfBearingGeometry(builder, props, null);
        List<LatLonAlt> locations = coreLOB.getVertices().stream().map(v -> ((GeographicPosition)v).getLatLonAlt())
                .collect(Collectors.toList());
        return new SimpleMapPolylineGeometrySupport(locations);
    }

    /** Disallow instantiation. */
    private HeatmapGeometryUtilities()
    {
    }
}
