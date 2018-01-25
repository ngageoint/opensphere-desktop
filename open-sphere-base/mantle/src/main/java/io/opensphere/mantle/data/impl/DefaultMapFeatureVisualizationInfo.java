package io.opensphere.mantle.data.impl;

import java.util.concurrent.atomic.AtomicInteger;

import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.TileLevelController;
import io.opensphere.mantle.data.event.DataTypeInfoZOrderChangeEvent;

/**
 * A support class for describing, at a type level, information relevant for
 * rendering the layer to assist the transformers in building the geometries.
 */
public class DefaultMapFeatureVisualizationInfo extends AbstractMapVisualizationInfo
{
    /** Starting point for feature order. */
    private static final int LOWEST_FEATURE_ORDER = 1000;

    /** Counter used to assign Z order to types. */
    private static AtomicInteger ourNextOrder = new AtomicInteger(LOWEST_FEATURE_ORDER);

    /** The Z order for feature geometries associated with this type. */
    private int myFeatureZOrder = ourNextOrder.incrementAndGet();

    /**
     * CTOR with default type color.
     *
     * @param visType - the visualization type
     */
    public DefaultMapFeatureVisualizationInfo(MapVisualizationType visType)
    {
        this(visType, true);
    }

    /**
     * CTOR with default type color.
     *
     * @param visType - the visualization type
     * @param usesVisualizationStyles the true if this type uses the
     *            visualization styles, false if native styles
     */
    public DefaultMapFeatureVisualizationInfo(MapVisualizationType visType, boolean usesVisualizationStyles)
    {
        super(visType, usesVisualizationStyles);
    }

    @Override
    public TileLevelController getTileLevelController()
    {
        return null;
    }

    @Override
    public TileRenderProperties getTileRenderProperties()
    {
        return null;
    }

    @Override
    public int getZOrder()
    {
        return myFeatureZOrder;
    }

    @Override
    public void setZOrder(int order, Object source)
    {
        if (order != myFeatureZOrder)
        {
            myFeatureZOrder = order;
            if (getDataTypeInfo() != null)
            {
                getDataTypeInfo().fireChangeEvent(new DataTypeInfoZOrderChangeEvent(getDataTypeInfo(), order, source));
            }
        }
    }
}
