package io.opensphere.wms.state.model;

import io.opensphere.wms.envoy.WMSEnvoy;
import io.opensphere.wms.layer.WMSDataType;

/**
 * A model class that contains both a WMSLayer object and its populated state
 * object.
 */
public class WMSEnvoyAndState
{
    /**
     * The server envoy associated with the state.
     */
    private final WMSEnvoy myEnvoy;

    /**
     * The layer state.
     */
    private final WMSLayerState myState;

    /**
     * The data type info pertaining to the state.
     */
    private final WMSDataType myDataType;

    /** The layer name (which layer the layer state this object refers to). */
    private final String myLayerName;

    /**
     * Constructor coupling the envoy value provider with the state.
     *
     * @param envoy The envoy value provider.
     * @param dataType The data type info pertaining to the state.
     * @param state The state associated with the provider.
     * @param layerName The layer name (which layer the layer state this object
     *            refers to)
     */
    public WMSEnvoyAndState(WMSEnvoy envoy, WMSDataType dataType, WMSLayerState state, String layerName)
    {
        myEnvoy = envoy;
        myState = state;
        myDataType = dataType;
        myLayerName = layerName;
    }

    /**
     * Gets the server envoy.
     *
     * @return The server envoy.
     */
    public WMSEnvoy getEnvoy()
    {
        return myEnvoy;
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

    /**
     * Gets the data type info pertaining to the state.
     *
     * @return The data type info.
     */
    public WMSDataType getTypeInfo()
    {
        return myDataType;
    }

    /**
     * Gets the layer name.
     *
     * @return The layer name (which layer the layer state this object refers
     *         to)
     */
    public String getLayerName()
    {
        return myLayerName;
    }
}
