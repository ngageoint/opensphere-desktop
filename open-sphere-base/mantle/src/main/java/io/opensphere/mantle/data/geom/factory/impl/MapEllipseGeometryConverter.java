package io.opensphere.mantle.data.geom.factory.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.EllipseGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.util.MantleConstants;

/** Factory class to create geometry from geometry support class. */
public final class MapEllipseGeometryConverter extends AbstractGeometryConverter
{
    /**
     * Constructor.
     *
     * @param toolbox The toolbox used for getting the latest projection.
     */
    public MapEllipseGeometryConverter(Toolbox toolbox)
    {
        super(toolbox);
    }

    /**
     * Convert.
     *
     * @param geomSupport the geom support
     * @param id the id
     * @param dti the {@link DataTypeInfo}
     * @param visState the vis state
     * @param renderPropertyPool the render property pool
     * @return the ellipse geometry
     */
    public EllipseGeometry convert(MapEllipseGeometrySupport geomSupport, long id, DataTypeInfo dti, VisualizationState visState,
            RenderPropertyPool renderPropertyPool)
    {
        MapVisualizationInfo mapVisInfo = dti.getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = dti.getBasicVisualizationInfo();

        EllipseGeometry.ProjectedBuilder ellipseBuilder = new EllipseGeometry.ProjectedBuilder();
        ellipseBuilder.setCenter(new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(geomSupport.getLocation().getLatD(), geomSupport.getLocation().getLonD(),
                        geomSupport.getLocation().getAltM() + visState.getAltitudeAdjust(), geomSupport.followTerrain()
                                ? Altitude.ReferenceLevel.TERRAIN : geomSupport.getLocation().getAltitudeReference())));
        ellipseBuilder.setProjection(getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot());
        ellipseBuilder.setAngle(geomSupport.getOrientation());
        ellipseBuilder.setSemiMajorAxis(geomSupport.getSemiMajorAxis());
        ellipseBuilder.setSemiMinorAxis(geomSupport.getSemiMinorAxis());
        ellipseBuilder.setDataModelId(id);
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = visState.isSelected() ? ZOrderRenderProperties.TOP_Z : mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();

        Constraints constraints = null;
        if (mapVisInfo != null && basicVisInfo.getLoadsTo().isTimelineEnabled() && !geomSupport.getTimeSpan().isTimeless())
        {
            constraints = createTimeConstraints(getToolbox(), dti, geomSupport.getTimeSpan());
        }

        PolygonRenderProperties props = createPolygonRenderProperties(geomSupport, visState, renderPropertyPool, pickable,
                zOrder);

        EllipseGeometry ellipseGeom = new EllipseGeometry(ellipseBuilder, props, constraints);

        return ellipseGeom;
    }

    @Override
    public AbstractRenderableGeometry createGeometry(MapGeometrySupport geomSupport, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        if (getConvertedClassType().isAssignableFrom(geomSupport.getClass()))
        {
            MapEllipseGeometrySupport megs = (MapEllipseGeometrySupport)geomSupport;
            return convert(megs, id, dti, visState, renderPropertyPool);
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
        return MapEllipseGeometrySupport.class;
    }

    /**
     * Creates the polygon render properties.
     *
     * @param geomSupport the geom support
     * @param visState the vis state
     * @param renderPropertyPool the render property pool
     * @param pickable the pickable
     * @param zOrder the z order
     * @return the polygon render properties
     */
    private PolygonRenderProperties createPolygonRenderProperties(MapEllipseGeometrySupport geomSupport,
            VisualizationState visState, RenderPropertyPool renderPropertyPool, boolean pickable, int zOrder)
    {
        PolygonRenderProperties props = new DefaultPolygonRenderProperties(zOrder, true, pickable);
        props.setColor(visState.isSelected() ? MantleConstants.SELECT_COLOR
                : visState.isDefaultColor() ? geomSupport.getColor() : visState.getColor());
        props.setWidth(1);
        props.setRenderingOrder(visState.isSelected() ? 1 : 0);
        props = renderPropertyPool.getPoolInstance(props);
        return props;
    }
}
