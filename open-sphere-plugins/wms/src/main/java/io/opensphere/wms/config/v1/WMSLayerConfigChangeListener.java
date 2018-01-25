package io.opensphere.wms.config.v1;

/** Interface for listeners for WMS layer configuration changes. */
public interface WMSLayerConfigChangeListener
{
    /**
     * This method should be called when configuration has been changed.
     *
     * @param event The configuration change event.
     */
    void configurationChanged(WMSLayerConfigChangeEvent event);

    /**
     * Event containing the information related to the layer configuration
     * change.
     */
    class WMSLayerConfigChangeEvent
    {
        /** The updated layer configuration. */
        private final WMSLayerConfigurationSet myLayerConfig;

        /**
         * Constructor.
         *
         * @param config The updated layer configuration.
         */
        public WMSLayerConfigChangeEvent(WMSLayerConfigurationSet config)
        {
            myLayerConfig = config;
        }

        /**
         * Get the layerConfig.
         *
         * @return the layerConfig
         */
        public WMSLayerConfigurationSet getLayerConfig()
        {
            return myLayerConfig;
        }
    }
}
