package io.opensphere.wfs.envoy;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.server.services.ServerConnectionParams;

/**
 * A factory definition in which the contract to create a new WFS envoy is
 * defined.
 */
public interface WFSEnvoyFactory
{
    /**
     * Creates a new envoy using the supplied parameters.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param preferences the set of preferences with which the envoy will be
     *            configured.
     * @param connection the connection with which the envoy will be configured.
     * @param tools the set of WFS utilities to provide to the generated envoy.
     * @return an envoy created and configured with the supplied parameters.
     */
    AbstractWFSEnvoy createEnvoy(Toolbox toolbox, Preferences preferences, ServerConnectionParams connection, WFSTools tools);
}
