package io.opensphere.mantle.data.geom.factory.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.LineOfBearingGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultLOBRenderProperties;
import io.opensphere.core.geometry.renderproperties.LOBRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.util.MantleConstants;

/** Factory class to create geometry from geometry support class. */
public final class MapLineOfBearingGeometryConverter extends AbstractGeometryConverter
{
    /**
     * Create PointGeometry from MapPointGeometrySupport.
     *
     * @param tb the tb
     * @param geomSupport The geometry support to use.
     * @param id for the geometry
     * @param dti the {@link DataTypeInfo}
     * @param visState - the {@link VisualizationState}
     * @param renderPropertyPool the render property pool
     * @return The point geometry.
     */
    public static LineOfBearingGeometry convert(Toolbox tb, MapLineOfBearingGeometrySupport geomSupport, long id,
            DataTypeInfo dti, VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        LineOfBearingGeometry.Builder lobBuilder = new LineOfBearingGeometry.Builder();

        MapVisualizationInfo mapVisInfo = dti.getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = dti.getBasicVisualizationInfo();
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = visState.isSelected() ? ZOrderRenderProperties.TOP_Z : mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
        LOBRenderProperties props = createLOBRenderProperties(geomSupport, visState, renderPropertyPool, pickable, zOrder);
        lobBuilder.setPosition(new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(geomSupport.getLocation().getLatD(), geomSupport.getLocation().getLonD(),
                        geomSupport.getLocation().getAltM() + visState.getAltitudeAdjust(), geomSupport.followTerrain()
                                ? Altitude.ReferenceLevel.TERRAIN : geomSupport.getLocation().getAltitudeReference())));
        lobBuilder.setLineOrientation(geomSupport.getOrientation());
        lobBuilder.setDataModelId(id);
        Constraints constraints = setupPickableZOrderAndTime(tb, lobBuilder, geomSupport, dti, visState);

        LineOfBearingGeometry lobGeom = new LineOfBearingGeometry(lobBuilder, props, constraints);

        return lobGeom;
    }

    /**
     * Creates the lob render properties.
     *
     * @param geomSupport the geom support
     * @param visState the vis state
     * @param renderPropertyPool the render property pool
     * @param pickable the pickable
     * @param zOrder the z order
     * @return the lOB render properties
     */
    private static LOBRenderProperties createLOBRenderProperties(MapLineOfBearingGeometrySupport geomSupport,
            VisualizationState visState, RenderPropertyPool renderPropertyPool, boolean pickable, int zOrder)
    {
        LOBRenderProperties props = new DefaultLOBRenderProperties(zOrder, true, pickable);
        props.setRenderingOrder(visState.isSelected() ? 1 : 0);
        props.setColor(visState.isSelected() ? MantleConstants.SELECT_COLOR
                : visState.isDefaultColor() ? geomSupport.getColor() : visState.getColor());
        props.setWidth(visState.isSelected() ? 1 + MantleConstants.SELECT_WIDTH_ADDITION : 1);
        props.setLineLength(visState.isLobVisible() ? geomSupport.getLength() : 0.0f);
        props.setBaseAltitude(visState.getAltitudeAdjust());
        props = renderPropertyPool.getPoolInstance(props);
        return props;
    }

    /**
     * Setup pickable z order and time.
     *
     * @param tb the Toolbox
     * @param lobBuilder the lob builder
     * @param geomSupport the geom support
     * @param dti the dti
     * @param visState the vis state
     * @return The time constraints.
     */
    private static Constraints setupPickableZOrderAndTime(Toolbox tb, LineOfBearingGeometry.Builder lobBuilder,
            MapLineOfBearingGeometrySupport geomSupport, DataTypeInfo dti, VisualizationState visState)
    {
        MapVisualizationInfo mapVisInfo = dti.getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = dti.getBasicVisualizationInfo();

        // Add a time constraint if in time line mode.
        Constraints constraints = null;
        if (mapVisInfo != null && basicVisInfo.getLoadsTo().isTimelineEnabled() && !geomSupport.getTimeSpan().isTimeless())
        {
            constraints = createTimeConstraints(tb, dti, geomSupport.getTimeSpan());
        }
        return constraints;
    }

    /**
     * Instantiates a new map line of bearing geometry factory.
     *
     * @param tb the {@link Toolbox}
     */
    public MapLineOfBearingGeometryConverter(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public AbstractRenderableGeometry createGeometry(MapGeometrySupport geomSupport, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        if (getConvertedClassType().isAssignableFrom(geomSupport.getClass()))
        {
            MapLineOfBearingGeometrySupport localSupport = (MapLineOfBearingGeometrySupport)geomSupport;
            return MapLineOfBearingGeometryConverter.convert(getToolbox(), localSupport, id, dti, visState, renderPropertyPool);
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
        return MapLineOfBearingGeometrySupport.class;
    }
}
