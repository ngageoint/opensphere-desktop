package io.opensphere.mantle.data.geom.factory.impl;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import javax.tools.Tool;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.util.MantleConstants;

/** Factory class to create geometry from geometry support class. */
public final class MapPolygonGeometryConverter extends AbstractGeometryConverter
{
    /** The Constant DEFAULT_FILL_OPACITY. */
    private static final float DEFAULT_FILL_OPACITY = 0.3f;

    /**
     * Create PolygonGeometry from MapPolygonGeometrySupport.
     *
     * @param tb the {@link Toolbox}
     * @param geomSupport The geometry support to use.
     * @param id for the geometry
     * @param dti the {@link DataTypeInfo}
     * @param visState - the {@link VisualizationState}
     * @param renderPropertyPool the render property pool
     * @return The point geometry.
     */
    public static PolygonGeometry convert(Toolbox tb, MapPolygonGeometrySupport geomSupport, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        MapVisualizationInfo mapVisInfo = dti.getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = dti.getBasicVisualizationInfo();
        PolygonGeometry.Builder<GeographicPosition> polygonBuilder = new PolygonGeometry.Builder<>();
        PolygonRenderProperties props;
        Color color = visState.isSelected() ? MantleConstants.SELECT_COLOR
                : visState.isDefaultColor() ? geomSupport.getColor() : visState.getColor();

        props = createPropertiesAndDetermineFillColor(mapVisInfo, basicVisInfo, geomSupport, visState, color);
        props.setColor(color);
        determineLineWidth(geomSupport, visState, props, color);
        props = renderPropertyPool.getPoolInstance(props);
        polygonBuilder.setDataModelId(id);
        polygonBuilder.setLineType(geomSupport.getLineType() == null ? LineType.STRAIGHT_LINE : geomSupport.getLineType());

        // Add a time constraint if in time line mode.
        Constraints constraints = null;
        if (basicVisInfo != null && basicVisInfo.getLoadsTo().isTimelineEnabled() && !geomSupport.getTimeSpan().isTimeless())
        {
            constraints = createTimeConstraints(tb, dti, geomSupport.getTimeSpan());
        }

        // Convert list of LatLonAlt to list of GeographicPositions
        polygonBuilder.setVertices(geomSupport.getLocations().stream()
                .map(lla -> createGeographicPosition(lla, mapVisInfo, visState, geomSupport)).collect(Collectors.toList()));

        for (List<? extends LatLonAlt> hole : geomSupport.getHoles())
        {
            polygonBuilder.addHole(hole.stream().map(lla -> createGeographicPosition(lla, mapVisInfo, visState, geomSupport))
                    .collect(Collectors.toList()));
        }

        PolygonGeometry polygonGeom = new PolygonGeometry(polygonBuilder, props, constraints);

        return polygonGeom;
    }

    /**
     * Creates the geographic position.
     *
     * @param lla the lla
     * @param mapVisInfo the map vis info
     * @param visState the vis state
     * @param geomSupport the geom support
     * @return the geographic position
     */
    public static GeographicPosition createGeographicPosition(LatLonAlt lla, MapVisualizationInfo mapVisInfo,
            VisualizationState visState, MapPolygonGeometrySupport geomSupport)
    {
        return new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(lla.getLatD(), lla.getLonD(), lla.getAltM() + visState.getAltitudeAdjust(),
                        geomSupport.followTerrain() ? Altitude.ReferenceLevel.TERRAIN : lla.getAltitudeReference()));
    }

    /**
     * Crate properties and determine fill color.
     *
     * @param mapVisInfo Data type level info relevant for rendering.
     * @param basicVisInfo Basic information for the data type.
     * @param geomSupport the geom support
     * @param visState the vis state
     * @param lineColor the line color
     * @return the polygon render properties
     */
    private static PolygonRenderProperties createPropertiesAndDetermineFillColor(MapVisualizationInfo mapVisInfo,
            BasicVisualizationInfo basicVisInfo, MapPolygonGeometrySupport geomSupport, VisualizationState visState,
            Color lineColor)
    {
        int zOrder = visState.isSelected() ? ZOrderRenderProperties.TOP_Z : mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        PolygonRenderProperties props;
        if (geomSupport.isFilled())
        {
            Color opacitizedLineColor = ColorUtilities.opacitizeColor(lineColor, DEFAULT_FILL_OPACITY);
            Color fillColor = visState.isSelected() ? opacitizedLineColor
                    : geomSupport.getFillColor() == null ? opacitizedLineColor : geomSupport.getFillColor();

            ColorRenderProperties fillColorProps = new DefaultColorRenderProperties(zOrder, true, pickable, false);
            fillColorProps.setColor(fillColor);
            props = new DefaultPolygonRenderProperties(zOrder, true, pickable, fillColorProps);
        }
        else
        {
            props = new DefaultPolygonRenderProperties(zOrder, true, pickable);
        }
        props.setRenderingOrder(visState.isSelected() ? 1 : 0);
        return props;
    }

    /**
     * Determine line width.
     *
     * @param geomSupport the geom support
     * @param visState the vis state
     * @param props the props
     * @param color the color
     */
    private static void determineLineWidth(MapPolygonGeometrySupport geomSupport, VisualizationState visState,
            PolygonRenderProperties props, Color color)
    {
        int lineWidth = geomSupport.isLineDrawn() ? visState.isSelected()
                ? geomSupport.getLineWidth() + MantleConstants.SELECT_WIDTH_ADDITION : geomSupport.getLineWidth() : 0;
        if (lineWidth == 0)
        {
            if (geomSupport.isFilled())
            {
                Color opacitizedLineColor = ColorUtilities.opacitizeColor(color, DEFAULT_FILL_OPACITY);
                Color fillColor = visState.isSelected() ? opacitizedLineColor
                        : geomSupport.getFillColor() == null ? opacitizedLineColor : geomSupport.getFillColor();
                props.setColor(fillColor);
            }
            else
            {
                props.setColor(new Color(0, 0, 0, 0));
            }
        }
        props.setWidth(lineWidth);
    }

    /**
     * Instantiates a new map polygon geometry factory.
     *
     * @param tb the {@link Tool}
     */
    public MapPolygonGeometryConverter(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public AbstractRenderableGeometry createGeometry(MapGeometrySupport geomSupport, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        if (getConvertedClassType().isAssignableFrom(geomSupport.getClass()))
        {
            MapPolygonGeometrySupport localSupport = (MapPolygonGeometrySupport)geomSupport;
            return MapPolygonGeometryConverter.convert(getToolbox(), localSupport, id, dti, visState, renderPropertyPool);
        }
        throw new IllegalArgumentException("MapGeometrySupport \"" + geomSupport.getClass().getName()
                + "\" is not an instance of \"" + getConvertedClassType().getName() + "\"");
    }

    @Override
    public Class<?> getConvertedClassType()
    {
        return MapPolygonGeometrySupport.class;
    }
}
