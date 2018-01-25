package io.opensphere.server.toolbox.impl;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.migration.Migrator4to5;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.toolbox.ServerListChangeEvent;
import io.opensphere.server.toolbox.ServerListChangeEvent.ServerState;
import io.opensphere.server.toolbox.ServerListManager;

/**
 * The default implementation of {@link ServerListManager}.
 */
public class ServerListManagerImpl implements ServerListManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ServerListManagerImpl.class);

    /** My change listeners. */
    private final WeakChangeSupport<EventListener<ServerListChangeEvent>> myChangeSupport = WeakChangeSupport.create();

    /** Reference to the Mantle DataGroupController. */
    private final DataGroupController myGroupController;

    /** My active server configurations. */
    private final Map<ServerConnectionParams, DataGroupInfo> myGroupMap = New.map();

    /** The migrator. */
    private final Migrator4to5 myMigrator;

    /**
     * Instantiates an implementation of {@link ServerListManager}.
     *
     * @param toolbox the Core toolbox
     */
    public ServerListManagerImpl(Toolbox toolbox)
    {
        myGroupController = MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController();
        myMigrator = new Migrator4to5(toolbox);
    }

    @Override
    public boolean addServer(ServerConnectionParams server, DataGroupInfo layerGroup)
    {
        Utilities.checkNull(server, "server");
        Utilities.checkNull(layerGroup, "layerGroup");
        boolean wasAdded = false;

        if (!myGroupMap.containsKey(server))
        {
            myGroupMap.put(server, layerGroup);
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Added new server [" + server.getServerTitle() + "] to configuration.");
            }
            myGroupController.addRootDataGroupInfo(layerGroup, this);
            fireChanged(server, ServerState.ADDED);

            // Activate any groups from legacy config file
            myMigrator.migrate(layerGroup);

            wasAdded = true;
        }

        return wasAdded;
    }

    @Override
    public void addServerListChangeListener(EventListener<ServerListChangeEvent> listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public Collection<ServerConnectionParams> getActiveServers()
    {
        return myGroupMap.keySet();
    }

    @Override
    public DataGroupInfo getAllLayersForServer(ServerConnectionParams server)
    {
        return myGroupMap.get(server);
    }

    @Override
    public DataGroupInfo getAllLayersForServer(String serverName)
    {
        return getAllLayersForServer(getServerByName(serverName));
    }

    @Override
    public void removeServer(ServerConnectionParams server)
    {
        Utilities.checkNull(server, "server");

        if (myGroupMap.containsKey(server))
        {
            DataGroupInfo group = myGroupMap.remove(server);
            myGroupController.removeDataGroupInfo(group, this);
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Removed server [" + server.getServerTitle() + "] from configuration.");
            }
            fireChanged(server, ServerState.REMOVED);
        }
    }

    @Override
    public void removeServer(String serverName)
    {
        Utilities.checkNull(serverName, "serverName");
        ServerConnectionParams server = getServerByName(serverName);
        if (server != null)
        {
            removeServer(server);
        }
    }

    @Override
    public void removeServerListChangeListener(EventListener<ServerListChangeEvent> listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Gets the Server payload that contains a URL.
     *
     * @param serverName the server name/title
     * @return the payload for the server with the specified name
     */
    protected ServerConnectionParams getServerByName(String serverName)
    {
        if (StringUtils.isNotEmpty(serverName))
        {
            for (ServerConnectionParams config : myGroupMap.keySet())
            {
                if (serverName.equals(config.getServerTitle()))
                {
                    return config;
                }
            }
        }
        return null;
    }

    /**
     * Gets the Server payload that contains a URL.
     *
     * @param serverUrl the server URL
     * @return the Server payload that contains the specified URL
     */
    protected ServerConnectionParams getServerByUrl(String serverUrl)
    {
        if (serverUrl != null)
        {
            for (ServerConnectionParams config : myGroupMap.keySet())
            {
                if (serverUrl.equalsIgnoreCase(config.getWmsUrl()) || serverUrl.equalsIgnoreCase(config.getWfsUrl())
                        || serverUrl.equalsIgnoreCase(config.getWpsUrl()))
                {
                    return config;
                }
            }
        }
        return null;
    }

    /**
     * Fire server list change event.
     *
     * @param server the server that changed
     * @param state the change state of the specified server
     */
    private void fireChanged(final ServerConnectionParams server, final ServerState state)
    {
        ChangeSupport.Callback<EventListener<ServerListChangeEvent>> callback = new ChangeSupport.Callback<EventListener<ServerListChangeEvent>>()
        {
            @Override
            public void notify(EventListener<ServerListChangeEvent> listener)
            {
                listener.notify(new ServerListChangeEvent(server, state));
            }
        };
        myChangeSupport.notifyListeners(callback);
    }
}
