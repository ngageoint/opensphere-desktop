package io.opensphere.core.quantify;

import io.opensphere.core.PluginToolbox;

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
}
