package io.opensphere.server.state.activate.serversource;

import java.util.List;

import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.services.ServerConfigEvent;

/**
 * Interface to an object that is interested in knowing when server are starting
 * to be activated.
 *
 */
public interface IActivationListener
{
    /**
     * Notifies that the specified servers are starting to be activated.
     *
     * @param servers The server being activated.
     */
    void activatingServers(List<IDataSource> servers);

    /**
     * Notified that the list of server.
     *
     * @param event Contains the server information whose activation has
     *            completed.
     */
    void activationComplete(ServerConfigEvent event);
}
