package io.opensphere.wms.toolbox;

import io.opensphere.core.PluginToolbox;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.capabilities.WMSServerCapabilities;
import io.opensphere.wms.sld.SldRegistry;

/**
 * The Interface WMSToolbox.
 */
public interface WMSToolbox extends PluginToolbox
{
    /**
     * Gets the sld registry.
     *
     * @return the sld registry
     */
    SldRegistry getSldRegistry();

    /**
     * Adds a new {@link WMSServerCapabilitiesListener}.
     *
     * @param listener The object wanting notification of new
     *            {@link WMSServerCapabilities}.
     */
    void addServerCapabiltiesListener(WMSServerCapabilitiesListener listener);

    /**
     * Removes a previously added {@link WMSServerCapabilitiesListener}.
     *
     * @param listener The listener to remove.
     */
    void removeServerCapabilitiesListener(WMSServerCapabilitiesListener listener);

    /**
     * Notifies any {@link WMSServerCapabilitiesListener} of the new
     * {@link WMSServerCapabilities}.
     *
     * @param serverConfig Indicates the type of WMS server.
     * @param capabilities The new capabilities.
     */
    void notifyServerCapabilitiesListener(ServerConnectionParams serverConfig, WMSServerCapabilities capabilities);
}
