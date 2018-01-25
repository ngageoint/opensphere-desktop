package io.opensphere.wms.state.model;

import io.opensphere.wms.layer.WMSLayerValueProvider;

/**
 * A model class that contains both a WMSLayer object and its populated state
 * object.
 */
public class WMSLayerAndState
{
    /**
     * The layer value provider associated with the state.
     */
    private final WMSLayerValueProvider myLayer;

    /**
     * The layer state.
     */
    private final WMSLayerState myState;

    /**
     * Constructer coupling the layer value provider with the state.
     *
     * @param layer The layer value provider.
     * @param state The state associated with the provider.
     */
    public WMSLayerAndState(WMSLayerValueProvider layer, WMSLayerState state)
    {
        myLayer = layer;
        myState = state;
    }

    /**
     * Gets the layer value provider.
     *
     * @return The layer value provider.
     */
    public WMSLayerValueProvider getLayer()
    {
        return myLayer;
    }

    /**
     * Gets the layer's state values.
     *
     * @return The layers state values.
     */
    public WMSLayerState getState()
    {
        return myState;
    }
}
