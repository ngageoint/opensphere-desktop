package io.opensphere.wms.config.v1;

/**
 * An aggregation of the configuration for a layer, the configuration for the
 * server which provides the layer and any configuration which can be inherited
 * by the layer.
 */
public class WMSLayerConfigurationSet implements Cloneable
{
    /** The inherited configuration values for this layer. */
    private WMSInheritedLayerConfig myInheritedLayerConfig;

    /** The configuration for this layer. */
    private WMSLayerConfig myLayerConfig;

    /** The configuration for the server which servers this layer. */
    private WMSServerConfig myServerConfig;

    /**
     * Constructor.
     *
     * @param serverConfig The configuration for the server which servers this
     *            layer.
     * @param layerConfig he configuration for this layer.
     * @param inheritedConfig The inherited configuration values for this layer.
     */
    public WMSLayerConfigurationSet(WMSServerConfig serverConfig, WMSLayerConfig layerConfig,
            WMSInheritedLayerConfig inheritedConfig)
    {
        myServerConfig = serverConfig;
        myLayerConfig = layerConfig;
        myInheritedLayerConfig = inheritedConfig;
    }

    @Override
    public WMSLayerConfigurationSet clone() throws CloneNotSupportedException
    {
        WMSLayerConfigurationSet clone = (WMSLayerConfigurationSet)super.clone();
        clone.myServerConfig = myServerConfig.clone();
        clone.myLayerConfig = myLayerConfig.clone();
        clone.myInheritedLayerConfig = myInheritedLayerConfig.clone();

        return clone;
    }

    /**
     * Get the inheritedLayerConfig.
     *
     * @return the inheritedLayerConfig
     */
    public WMSInheritedLayerConfig getInheritedLayerConfig()
    {
        return myInheritedLayerConfig;
    }

    /**
     * Get the layerConfig.
     *
     * @return the layerConfig
     */
    public WMSLayerConfig getLayerConfig()
    {
        return myLayerConfig;
    }

    /**
     * Get the serverConfig.
     *
     * @return the serverConfig
     */
    public WMSServerConfig getServerConfig()
    {
        return myServerConfig;
    }
}
