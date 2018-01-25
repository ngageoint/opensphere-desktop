package io.opensphere.wms.layer;

import java.util.List;

import io.opensphere.core.math.Vector2d;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.wms.config.v1.WMSLayerConfig;

/**
 * Provides certain values from a WMSLayer to any calling object.
 *
 */
public class WMSLayerValueProviderImpl implements WMSLayerValueProvider
{
    /**
     * The layer to get values for.
     */
    private final WMSLayer myLayer;

    /**
     * Constructs a new WMS value provider.
     *
     * @param wmsLayer The layer to get values for.
     */
    public WMSLayerValueProviderImpl(WMSLayer wmsLayer)
    {
        myLayer = wmsLayer;
    }

    @Override
    public WMSLayerConfig getConfiguration()
    {
        return myLayer.getConfiguration();
    }

    @Override
    public WMSLayer getLayer()
    {
        return myLayer;
    }

    @Override
    public List<Vector2d> getLayerCellDimensions()
    {
        return myLayer.getLayerCellDimensions();
    }

    @Override
    public int getMaximumDisplaySize()
    {
        return myLayer.getMaximumDisplaySize();
    }

    @Override
    public int getMinimumDisplaySize()
    {
        return myLayer.getMinimumDisplaySize();
    }

    @Override
    public DataTypeInfo getTypeInfo()
    {
        return myLayer.getTypeInfo();
    }
}
