package io.opensphere.wms.layer;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;

/**
 * Interface to a WMS data type.
 *
 */
public interface WMSDataType extends DataTypeInfo
{
    @Override
    String getDisplayName();

    @Override
    DataGroupInfo getParent();

    /**
     * Gets the preferences.
     *
     * @return The preferences.
     */
    Preferences getPrefs();

    @Override
    String getSourcePrefix();

    /**
     * Gets the toolbox.
     *
     * @return The toolbox.
     */
    Toolbox getToolbox();

    @Override
    String getTypeKey();

    /**
     * Gets the WMS config for the data type.
     *
     * @return The wms config.
     */
    WMSLayerConfigurationSet getWmsConfig();
}
