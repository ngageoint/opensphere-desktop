package io.opensphere.mantle.data.geom.factory.impl;

import java.awt.Color;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultBaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPointGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.util.MantleConstants;

/** Factory class to create geometry from geometry support class. */
public final class MapPointGeometryConverter extends AbstractGeometryConverter
{
    /** The Constant DEFAULT_POINT_SIZE. */
    private static final float DEFAULT_POINT_SIZE = 4.0f;

    /**
     * Convert.
     *
     * @param tb the {@link Toolbox}
     * @param geomSupport the geom support
     * @param id the id
     * @param dti the {@link DataTypeInfo}
     * @param visState - the {@link VisualizationState}
     * @param renderPropertyPool the render property pool
     * @return the point geometry
     */
    public static PointGeometry convert(Toolbox tb, MapPointGeometrySupport geomSupport, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        MapVisualizationInfo mapVisInfo = dti.getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = dti.getBasicVisualizationInfo();

        PointGeometry.Builder<GeographicPosition> pointBuilder = new PointGeometry.Builder<>();
        float size = DEFAULT_POINT_SIZE;
        if (geomSupport.getScale() != 1f)
        {
            size *= geomSupport.getScale();
        }

        PointRenderProperties props = getPointSizeRenderPropertiesIfAvailable(mapVisInfo, basicVisInfo, renderPropertyPool,
                visState.isSelected(), size, visState.isSelected() ? MantleConstants.SELECT_COLOR
                        : visState.isDefaultColor() ? geomSupport.getColor() : visState.getColor());

        pointBuilder.setPosition(new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(geomSupport.getLocation().getLatD(), geomSupport.getLocation().getLonD(),
                        geomSupport.getLocation().getAltM() + visState.getAltitudeAdjust(), geomSupport.followTerrain()
                                ? Altitude.ReferenceLevel.TERRAIN : geomSupport.getLocation().getAltitudeReference())));

        pointBuilder.setDataModelId(id);

        // Add a time constraint if in time line mode.
        Constraints constraints = null;
        if (mapVisInfo != null && basicVisInfo.getLoadsTo().isTimelineEnabled() && !geomSupport.getTimeSpan().isTimeless())
        {
            constraints = createTimeConstraints(tb, dti, geomSupport.getTimeSpan());
        }

        PointGeometry pointGeom = new PointGeometry(pointBuilder, props, constraints);
        return pointGeom;
    }

    /**
     * Get the pooled base render properties or create one to put in the pool.
     *
     * @param renderPropertyPool the render property pool
     * @param basicVisInfo Basic information for the data type.
     * @param mapVisInfo Data type level info relevant for rendering.
     * @param isSelected true when the geometry is selected
     * @param color the geometry color
     * @return The base render properties.
     */
    private static BaseAltitudeRenderProperties getBaseRenderPropertiesIfAvailable(RenderPropertyPool renderPropertyPool,
            BasicVisualizationInfo basicVisInfo, MapVisualizationInfo mapVisInfo, boolean isSelected, Color color)
    {
        int zOrder = isSelected ? ZOrderRenderProperties.TOP_Z : mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        DefaultBaseAltitudeRenderProperties baseProps = new DefaultBaseAltitudeRenderProperties(zOrder, true, pickable, false);
        baseProps.setColor(color);
        baseProps.setRenderingOrder(isSelected ? 1 : 0);
        return renderPropertyPool.getPoolInstance(baseProps);
    }

    /**
     * Gets the point size render properties if available, if not creates a new
     * one and adds it to the share with the provided size.
     *
     * @param mapVisInfo Data type level info relevant for rendering.
     * @param basicVisInfo Basic information for the data type.
     * @param renderPropertyPool the render property pool
     * @param isSelected true when the geometry is selected
     * @param size the default point size
     * @param color the geometry color
     * @return the point size render properties if available
     */
    private static PointRenderProperties getPointSizeRenderPropertiesIfAvailable(MapVisualizationInfo mapVisInfo,
            BasicVisualizationInfo basicVisInfo, RenderPropertyPool renderPropertyPool, boolean isSelected, float size,
            Color color)
    {
        PointSizeRenderProperty psRP = getPointSizeRenderPropertiesIfAvailable(renderPropertyPool, size);
        BaseAltitudeRenderProperties baseProps = getBaseRenderPropertiesIfAvailable(renderPropertyPool, basicVisInfo, mapVisInfo,
                isSelected, color);

        PointRenderProperties properties = new DefaultPointRenderProperties(baseProps, psRP);
        return renderPropertyPool.getPoolInstance(properties);
    }

    /**
     * Gets the point size render properties if available, if not creates a new
     * one and adds it to the share with the provided size.
     *
     * @param renderPropertyPool the render property pool
     * @param size the default point size
     * @return the point size render properties if available
     */
    private static PointSizeRenderProperty getPointSizeRenderPropertiesIfAvailable(RenderPropertyPool renderPropertyPool,
            float size)
    {
        PointSizeRenderProperty pointSizeRP = new DefaultPointSizeRenderProperty();
        pointSizeRP.setSize(size);
        pointSizeRP = renderPropertyPool.getPoolInstance(pointSizeRP);
        return pointSizeRP;
    }

    /**
     * Instantiates a new map point geometry factory.
     *
     * @param tb the {@link Toolbox}
     */
    public MapPointGeometryConverter(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public AbstractRenderableGeometry createGeometry(MapGeometrySupport geomSupport, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        if (getConvertedClassType().isAssignableFrom(geomSupport.getClass()))
        {
            MapPointGeometrySupport localSupport = (MapPointGeometrySupport)geomSupport;
            return convert(getToolbox(), localSupport, id, dti, visState, renderPropertyPool);
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
        return MapPointGeometrySupport.class;
    }
}
