package io.opensphere.stkterrain.server;

import io.opensphere.mantle.datasources.IDataSource;

/**
 * Interface to an object that controls the deactivation of an STK Terrain
 * server.
 */
public interface STKServerController
{
    /**
     * Deactivates the specified server.
     *
     * @param source The server to deactivate.
     */
    void deactivateSource(IDataSource source);
}
