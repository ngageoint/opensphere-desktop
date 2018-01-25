package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.List;

import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Creates the geometries to properly visualize features below the earths
 * surface.
 */
public class SubSurfaceGeometryCreator
{
    /**
     * Creates the subsurface geometries such as a dashed line connecting the
     * feature to its position on the surface.
     *
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param renderPropertyPool the {@link RenderPropertyPool}
     * @param stylePointSize the style point size
     * @param factory Used to create points.
     * @param constraints the {@link Constraints}
     * @param color The default color for the geometries.
     * @param position The subsurface position.
     * @return the abstract renderable geometry
     * @throws IllegalArgumentException the illegal argument exception
     */
    public List<Geometry> createSubsurfaceGeometry(FeatureIndividualGeometryBuilderData bd, RenderPropertyPool renderPropertyPool,
            float stylePointSize, PointGeometryFactory factory, Constraints constraints, Color color, GeographicPosition position)
    {
        List<Geometry> geometries = New.list();

        GeographicPosition surfacePosition = new GeographicPosition(
                LatLonAlt.createFromDegrees(position.getLatLonAlt().getLatD(), position.getLatLonAlt().getLonD()));
        AbstractRenderableGeometry surfacePoint = factory.createPointGeometry(bd, surfacePosition, stylePointSize, color,
                constraints);
        geometries.add(surfacePoint);

        PolylineGeometry.Builder<GeographicPosition> builder = new PolylineGeometry.Builder<>();
        List<GeographicPosition> vertices = New.list(2);

        vertices.add(position);
        vertices.add(surfacePosition);
        builder.setVertices(vertices);
        builder.setDataModelId(bd.getGeomId());
        MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null : bd.getDataType().getBasicVisualizationInfo();
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();

        DefaultPolylineRenderProperties props = new DefaultPolylineRenderProperties(zOrder, true, pickable);
        props.setColor(bd.getVS().isSelected() ? MantleConstants.SELECT_COLOR
                : bd.getVS().isDefaultColor() ? color : bd.getVS().getColor());
        props = renderPropertyPool.getPoolInstance(props);
        props.setWidth(1);
        props.setStipple(StippleModelConfig.DASHED);
        props.setHidden(!bd.getDataType().isVisible());
        geometries.add(new PolylineGeometry(builder, props, constraints));

        return geometries;
    }
}
