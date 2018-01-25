package io.opensphere.wms.state.activate.controllers;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.layer.WMSDataType;
import io.opensphere.wms.layer.WMSDataTypeInfo;

/**
 * The WMS data type factory, responsible for create new WMS data types.
 */
public class WMSDataTypeFactoryImpl implements WMSDataTypeFactory
{
    /**
     * The instance of this class.
     */
    private static WMSDataTypeFactoryImpl ourInstance = new WMSDataTypeFactoryImpl();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static WMSDataTypeFactoryImpl getInstance()
    {
        return ourInstance;
    }

    @Override
    public WMSDataType createDataType(Toolbox toolbox, Preferences preferences, String serverId,
            WMSLayerConfigurationSet wmsConfig, String newLayerKey, String displayName, String url)
    {
        WMSDataTypeInfo dataType = new WMSDataTypeInfo(toolbox, preferences, serverId, wmsConfig, newLayerKey, displayName);

        dataType.setUrl(url);

        return dataType;
    }
}
