package io.opensphere.server.state.activate;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConfigEvent.ServerEventAction;
import io.opensphere.server.state.activate.serversource.IActivationListener;
import io.opensphere.server.state.activate.serversource.ServerSourceFilterer;
import io.opensphere.server.state.activate.serversource.ServerSourceProvider;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.StateServerSourceController;

/**
 * Inspects a state node and adds and activates any servers contained in the
 * state node, not already added or activated in the system.
 */
public class ServerActivator implements EventListener<ServerConfigEvent>
{
    /**
     * The activation listener.
     */
    private final IActivationListener myActivationListener;

    /**
     * The manager of ServerSourceControllers used to activate servers.
     */
    private final ServerSourceControllerManager myServerSourceManager;

    /**
     * Used to register for server events.
     */
    private final EventManager myEventManager;

    /**
     * Constructs a new server activator.
     *
     * @param serverSourceManager The manager of ServerSourceControllers used to
     *            activate servers.
     * @param eventManager The event manager used to listen for server load
     *            events.
     * @param activationListener The listener interested when servers are being
     *            activated.
     */
    public ServerActivator(ServerSourceControllerManager serverSourceManager, EventManager eventManager,
            IActivationListener activationListener)
    {
        myActivationListener = activationListener;
        myServerSourceManager = serverSourceManager;
        myEventManager = eventManager;
        myEventManager.subscribe(ServerConfigEvent.class, this);
    }

    /**
     * Adds and/or activates servers contained within the specified object, that
     * are not already added or active.
     *
     * @param object The state object to inspect.
     */
    public void activateServers(Object object)
    {
        for (ServerSourceController controller : myServerSourceManager.getControllers())
        {
            if (controller instanceof StateServerSourceController)
            {
                StateServerSourceController stateController = (StateServerSourceController)controller;
                ServerSourceProvider serverSource = stateController.getStateServerProvider();
                List<IDataSource> servers = Collections.emptyList();
                if (object instanceof Node)
                {
                    servers = serverSource.getServersInNode((Node)object);
                }
                else if (object instanceof StateType)
                {
                    servers = serverSource.getServersInNode((StateType)object);
                }

                if (!servers.isEmpty())
                {
                    ServerSourceFilterer filterer = stateController.getServerSourceFilterer();

                    filterer.findBusyServers(myActivationListener, servers);

                    List<IDataSource> nonActiveServers = filterer.getNonActiveServers(servers);

                    List<IDataSource> nonAddedServers = filterer.getNonAddedServers(servers);

                    addThenActivate(controller, nonAddedServers, nonActiveServers);
                }
            }
        }
    }

    /**
     * Removes itself as a listener to server changes.
     */
    public void close()
    {
        myEventManager.unsubscribe(ServerConfigEvent.class, this);
    }

    @Override
    public void notify(ServerConfigEvent event)
    {
        if (event.getEventAction() == ServerEventAction.LOADCOMPLETE && myActivationListener != null)
        {
            myActivationListener.activationComplete(event);
        }
    }

    /**
     * Activate the specified servers.
     *
     * @param controller Used to activate the servers.
     * @param servers The servers to activate.
     */
    private void activateServers(ServerSourceController controller, List<IDataSource> servers)
    {
        if (myActivationListener != null)
        {
            myActivationListener.activatingServers(servers);
        }

        for (IDataSource server : servers)
        {
            controller.activateSource(server);
        }
    }

    /**
     * Adds the server that need to be added then activates both the added
     * servers and the servers that were already registered with the system but
     * not active.
     *
     * @param controller Used to add the servers and activate them.
     * @param nonAdded The servers that need to be added to the system
     * @param nonActive The servers that are already added to the system, but
     *            need to be activated.
     */
    private void addThenActivate(ServerSourceController controller, List<IDataSource> nonAdded, List<IDataSource> nonActive)
    {
        List<IDataSource> serversToActivate = New.list(nonActive);

        for (IDataSource server : nonAdded)
        {
            controller.addSource(server);
            serversToActivate.add(server);
        }

        activateServers(controller, serversToActivate);
    }
}
