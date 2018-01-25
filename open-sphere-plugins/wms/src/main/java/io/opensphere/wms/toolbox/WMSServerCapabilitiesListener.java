package io.opensphere.wms.toolbox;

import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.capabilities.WMSServerCapabilities;

/**
 * Interface to an object wanting notification when new
 * {@link WMSServerCapabilities} are received from a WMS server.
 */
public interface WMSServerCapabilitiesListener
{
    /**
     * Called when new {@link WMSServerCapabilities} are received from a WMS
     * server.
     *
     * @param serverConfig Inidicates the type of WMS server.
     * @param capabilities The new capabilities.
     */
    void received(ServerConnectionParams serverConfig, WMSServerCapabilities capabilities);
}
