package io.opensphere.core.quantify;

import io.opensphere.core.PluginToolbox;
import io.opensphere.core.quantify.settings.QuantifySettingsModel;

/**
 * An interface for a toolbox used by the quantify plugin.
 */
public interface QuantifyToolbox extends PluginToolbox
{
    /**
     * Gets the service through which metrics are collected and sent.
     *
     * @return the service through which metrics are collected and sent.
     */
    QuantifyService getQuantifyService();

    /**
     * Gets the model in which settings are persisted.
     *
     * @return the model in which settings are persisted.
     */
    QuantifySettingsModel getSettingsModel();
}
