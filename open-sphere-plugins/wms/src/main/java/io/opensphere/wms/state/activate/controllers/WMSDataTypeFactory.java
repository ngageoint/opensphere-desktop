package io.opensphere.wms.state.activate.controllers;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.layer.WMSDataType;

/**
 * Interface to the WMSDataType factory responsible for creating new WMS data
 * types.
 *
 */
@FunctionalInterface
public interface WMSDataTypeFactory
{
    /**
     * Creates a new WMS data type.
     *
     * @param toolbox The system toolbox.
     * @param preferences The WMS preferences.
     * @param serverId The server id.
     * @param wmsConfig The WMS config.
     * @param newLayerKey The new layer key.
     * @param displayName The display name.
     * @param url The url of the WMS data type.
     * @return A newly created WMS data type.
     */
    WMSDataType createDataType(Toolbox toolbox, Preferences preferences, String serverId, WMSLayerConfigurationSet wmsConfig,
            String newLayerKey, String displayName, String url);
}
