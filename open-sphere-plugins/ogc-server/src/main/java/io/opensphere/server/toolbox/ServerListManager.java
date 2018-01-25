package io.opensphere.server.toolbox;

import java.util.Collection;

import io.opensphere.core.event.EventListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.server.services.ServerConnectionParams;

/**
 * The ServerListManager provides access to the .
 */
public interface ServerListManager
{
    /**
     * Adds a server and sets the layers in activeLayerNames active.
     *
     * @param server the server configuration to add
     * @param layerGroup Group with all server layers
     * @return true, if server was successfully added
     */
    boolean addServer(ServerConnectionParams server, DataGroupInfo layerGroup);

    /**
     * Adds a server list change listener.
     *
     * @param listener the listener to add
     */
    void addServerListChangeListener(EventListener<ServerListChangeEvent> listener);

    /**
     * Gets the active server configurations.
     *
     * @return the active server configurations
     */
    Collection<ServerConnectionParams> getActiveServers();

    /**
     * Gets all the layers (active or not) on a given server.
     *
     * @param server the Connection payload for the server
     * @return all the layers on the specified server
     */
    DataGroupInfo getAllLayersForServer(ServerConnectionParams server);

    /**
     * Gets all the layers (active or not) on a given server.
     *
     * @param serverName the name/title of the server
     * @return all the layers on the specified server
     */
    DataGroupInfo getAllLayersForServer(String serverName);

    /**
     * Removes a server from the active server list.
     *
     * @param server the server configuration to remove
     */
    void removeServer(ServerConnectionParams server);

    /**
     * Removes a server from the active server list.
     *
     * @param serverName the name/title of the server to remove
     */
    void removeServer(String serverName);

    /**
     * Removes a server list change listener.
     *
     * @param listener the listener to remove
     */
    void removeServerListChangeListener(EventListener<ServerListChangeEvent> listener);
}
