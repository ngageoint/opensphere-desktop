package io.opensphere.mantle.data.geom.factory.impl;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.util.MantleConstants;

/** Factory class to create geometry from geometry support class. */
public final class MapPolylineGeometryConverter extends AbstractGeometryConverter
{
    /**
     * Create PolylineGeometry from MapPolylineGeometrySupport.
     *
     * @param tb the Toolbox
     * @param geomSupport The geometry support to use.
     * @param id for the geometry
     * @param dti the {@link DataTypeInfo}
     * @param visState - the {@link VisualizationState}
     * @param renderPropertyPool the render property pool
     * @return The point geometry.
     */
    public static PolylineGeometry convert(Toolbox tb, MapPolylineGeometrySupport geomSupport, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        MapVisualizationInfo mapVisInfo = dti.getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = dti.getBasicVisualizationInfo();
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = visState.isSelected() ? ZOrderRenderProperties.TOP_Z : mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
        PolylineGeometry.Builder<GeographicPosition> polylineBuilder = new PolylineGeometry.Builder<GeographicPosition>();
        PolylineRenderProperties props = createPolylineRenderProperties(geomSupport, visState, renderPropertyPool, pickable,
                zOrder);
        polylineBuilder.setDataModelId(id);
        polylineBuilder.setLineType(geomSupport.getLineType() == null ? LineType.STRAIGHT_LINE : geomSupport.getLineType());

        // Convert list of LatLonAlt to list of GeographicPositions
        List<GeographicPosition> geoPos = new ArrayList<>();
        for (LatLonAlt lla : geomSupport.getLocations())
        {
            geoPos.add(createGeographicPosition(lla, mapVisInfo, visState, geomSupport));
        }
        polylineBuilder.setVertices(geoPos);

        // Add a time constraint if in time line mode.
        Constraints constraints = null;
        if (basicVisInfo != null && basicVisInfo.getLoadsTo().isTimelineEnabled() && !geomSupport.getTimeSpan().isTimeless())
        {
            constraints = createTimeConstraints(tb, dti, geomSupport.getTimeSpan());
        }

        PolylineGeometry polylineGeom = new PolylineGeometry(polylineBuilder, props, constraints);

        return polylineGeom;
    }

    /**
     * Creates the geographic position.
     *
     * @param lla the lla
     * @param visInfo the map vis info
     * @param visState the vis state
     * @param geom the geom support
     * @return the geographic position
     */
    public static GeographicPosition createGeographicPosition(LatLonAlt lla, MapVisualizationInfo visInfo,
            VisualizationState visState, MapPolylineGeometrySupport geom)
    {
        return new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(lla.getLatD(), lla.getLonD(), lla.getAltM() + visState.getAltitudeAdjust(),
                        geom.followTerrain() ? Altitude.ReferenceLevel.TERRAIN : lla.getAltitudeReference()));
    }

    /**
     * Creates the polyline render properties.
     *
     * @param geomSupport the geom support
     * @param visState the vis state
     * @param renderPropertyPool the render property pool
     * @param pickable the pickable
     * @param zOrder the z order
     * @return the polyline render properties
     */
    private static PolylineRenderProperties createPolylineRenderProperties(MapPolylineGeometrySupport geomSupport,
            VisualizationState visState, RenderPropertyPool renderPropertyPool, boolean pickable, int zOrder)
    {
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(zOrder, true, pickable);
        props.setColor(visState.isSelected() ? MantleConstants.SELECT_COLOR
                : visState.isDefaultColor() ? geomSupport.getColor() : visState.getColor());
        props.setWidth(visState.isSelected() ? geomSupport.getLineWidth() + MantleConstants.SELECT_WIDTH_ADDITION
                : geomSupport.getLineWidth());
        props = renderPropertyPool.getPoolInstance(props);
        props.setRenderingOrder(visState.isSelected() ? 1 : 0);
        return props;
    }

    /**
     * Instantiates a new map polyline geometry factory.
     *
     * @param tb the {@link Toolbox}
     */
    public MapPolylineGeometryConverter(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public AbstractRenderableGeometry createGeometry(MapGeometrySupport geomSupport, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        if (getConvertedClassType().isAssignableFrom(geomSupport.getClass()))
        {
            MapPolylineGeometrySupport megs = (MapPolylineGeometrySupport)geomSupport;
            return MapPolylineGeometryConverter.convert(getToolbox(), megs, id, dti, visState, renderPropertyPool);
        }
        else
        {
            throw new IllegalArgumentException("MapGeometrySupport \"" + geomSupport.getClass().getName()
                    + "\" is not an instance of \"" + getConvertedClassType().getName() + "\"");
        }
    }

    @Override
    public Class<?> getConvertedClassType()
    {
        return MapPolylineGeometrySupport.class;
    }
}
