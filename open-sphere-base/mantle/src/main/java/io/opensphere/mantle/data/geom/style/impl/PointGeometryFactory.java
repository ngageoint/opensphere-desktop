package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;

import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.util.MantleConstants;

/** Factory for point geometries. */
public class PointGeometryFactory
{
    /** The render property pool. */
    private final RenderPropertyPool myRenderPropertyPool;

    /**
     * Constructor.
     *
     * @param renderPropertyPool The render property pool.
     */
    public PointGeometryFactory(RenderPropertyPool renderPropertyPool)
    {
        myRenderPropertyPool = renderPropertyPool;
    }

    /**
     * Creates the point geometry.
     *
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param position The position.
     * @param stylePointSize the style point size
     * @param color The color.
     * @param constraints the {@link Constraints}
     * @return the abstract renderable geometry
     */
    public AbstractRenderableGeometry createPointGeometry(FeatureIndividualGeometryBuilderData bd, GeographicPosition position,
            float stylePointSize, Color color, Constraints constraints)
    {
        MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null : bd.getDataType().getBasicVisualizationInfo();

        PointRenderProperties props = new PointRenderPropertiesHelper(myRenderPropertyPool)
                .getPointSizeRenderPropertiesIfAvailable(mapVisInfo, basicVisInfo, stylePointSize, bd, getColor(bd, color), null);

        PointGeometry.Builder<GeographicPosition> pointBuilder = createPointBuilder(bd.getGeomId(), position);

        return new PointGeometry(pointBuilder, props, constraints);
    }

    /**
     * Creates the point builder.
     *
     * @param elementId the element id
     * @param position The position.
     * @return the point geometry. builder
     */
    private PointGeometry.Builder<GeographicPosition> createPointBuilder(long elementId, GeographicPosition position)
    {
        PointGeometry.Builder<GeographicPosition> pointBuilder = new PointGeometry.Builder<>();
        pointBuilder.setPosition(position);
        pointBuilder.setDataModelId(elementId);
        return pointBuilder;
    }

    /**
     * Determine the correct color for the features based on style settings and
     * selection state.
     *
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param defaultColor The default color.
     * @return The color of the features.
     */
    private Color getColor(FeatureIndividualGeometryBuilderData bd, Color defaultColor)
    {
        if (bd.getVS().isSelected())
        {
            return MantleConstants.SELECT_COLOR;
        }
        else
        {
            return bd.getVS().isDefaultColor() ? defaultColor : bd.getVS().getColor();
        }
    }
}
