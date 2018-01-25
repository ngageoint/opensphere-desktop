package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;

import io.opensphere.core.geometry.renderproperties.BaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultBaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.PointRenderPropertyFactory;

/** Helper for point render properties. */
public class PointRenderPropertiesHelper
{
    /** The render property pool. */
    private final RenderPropertyPool myRenderPropertyPool;

    /**
     * Constructor.
     *
     * @param renderPropertyPool The render property pool.
     */
    public PointRenderPropertiesHelper(RenderPropertyPool renderPropertyPool)
    {
        myRenderPropertyPool = renderPropertyPool;
    }

    /**
     * Gets the point size render properties if available.
     *
     * @param mapVisInfo the map vis info
     * @param basicVisInfo the basic vis info
     * @param size the size
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param color the c
     * @param rpf the rpf
     * @return the point size render properties if available
     */
    public PointRenderProperties getPointSizeRenderPropertiesIfAvailable(MapVisualizationInfo mapVisInfo,
            BasicVisualizationInfo basicVisInfo, float size, FeatureIndividualGeometryBuilderData bd, Color color,
            PointRenderPropertyFactory rpf)
    {
        PointSizeRenderProperty pointSizeRP = getPointSizeRenderPropertiesIfAvailable(myRenderPropertyPool, size);
        BaseAltitudeRenderProperties brp = getBaseRenderPropertiesIfAvailable(myRenderPropertyPool, bd, basicVisInfo, mapVisInfo,
                bd.getVS().isSelected(), color);
        PointRenderProperties props = rpf == null ? new DefaultPointRenderProperties(brp, pointSizeRP)
                : rpf.createPointRenderProperties(brp, pointSizeRP);
        props.setColor(color);
        props = myRenderPropertyPool.getPoolInstance(props);
        return props;
    }

    /**
     * Get the pooled base render properties or create one to put in the pool.
     *
     * @param renderPropertyPool the render property pool
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param basicVisInfo Basic information for the data type.
     * @param mapVisInfo Data type level info relevant for rendering.
     * @param isSelected true when the geometry is selected
     * @param color the geometry color
     * @return The base render properties.
     */
    private BaseAltitudeRenderProperties getBaseRenderPropertiesIfAvailable(RenderPropertyPool renderPropertyPool,
            FeatureIndividualGeometryBuilderData bd, BasicVisualizationInfo basicVisInfo, MapVisualizationInfo mapVisInfo,
            boolean isSelected, Color color)
    {
        int zOrder = bd.getVS().isSelected() ? DefaultOrderCategory.FEATURE_CATEGORY.getOrderRange().getMaximumInteger()
                : mapVisInfo == null ? DefaultOrderCategory.FEATURE_CATEGORY.getOrderRange().getMinimumInteger()
                        : mapVisInfo.getZOrder();
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
     * @param renderPropertyPool the render property pool
     * @param size the default point size
     * @return the point size render properties if available
     */
    private PointSizeRenderProperty getPointSizeRenderPropertiesIfAvailable(RenderPropertyPool renderPropertyPool, float size)
    {
        PointSizeRenderProperty pointSizeRP = new DefaultPointSizeRenderProperty();
        pointSizeRP.setSize(size);
        pointSizeRP.setHighlightSize(size);
        pointSizeRP = renderPropertyPool.getPoolInstance(pointSizeRP);
        return pointSizeRP;
    }
}
