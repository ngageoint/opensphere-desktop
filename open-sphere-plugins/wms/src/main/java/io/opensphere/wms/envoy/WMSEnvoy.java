package io.opensphere.wms.envoy;

import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.util.WMSQueryTracker;

/**
 * Interface to a WMS server envoy.
 *
 */
public interface WMSEnvoy
{
    /**
     * Gets the active layer change listener, to activate or deactivate the
     * state layers.
     *
     * @return The active change listener.
     */
    ActivationListener getActiveLayerChangeListener();

    /**
     * Gets the query tracker for this layer.
     *
     * @return The query tracker.
     */
    WMSQueryTracker getQueryTracker();

    /**
     * Gets the server's connection configuration.
     *
     * @return The connection configurtion.
     */
    ServerConnectionParams getServerConnectionConfig();

    /**
     * Get the WMS version used in requests made by this envoy.
     *
     * @return the WMS Version (either "1.1.1" or "1.3.0").
     */
    String getWMSVersion();
}
