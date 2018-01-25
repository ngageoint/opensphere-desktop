package io.opensphere.server.state;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.state.activate.ServerActivator;
import io.opensphere.server.state.activate.serversource.IActivationListener;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerStateController;
import io.opensphere.server.util.ServerConstants;

/**
 * Adds and/or activates any servers contained in a state node not already added
 * or activated in the system.
 */
@SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
public final class ServerStateControllerImpl implements IActivationListener, ServerStateController
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(ServerStateControllerImpl.class);

    /**
     * The list of current activating servers.
     */
    private final List<IDataSource> myCurrentActivatingServers = New.list();

    /**
     * The semaphore used to block.
     */
    private Semaphore mySemaphore;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The manages all ServerSourceControllers.
     */
    private final ServerSourceControllerManager myServerManager;

    /**
     * Constructs a new controller.
     *
     * @param toolbox The system toolbox.
     * @param serverManager Manages all server source controllers.
     */
    public ServerStateControllerImpl(Toolbox toolbox, ServerSourceControllerManager serverManager)
    {
        myToolbox = toolbox;
        myServerManager = serverManager;
    }

    /**
     * Adds and/or activates any servers contained in the specified node that
     * are not already added or activated. This method returns once all servers
     * have completely loaded.
     *
     * @param node The state node containing server information.
     * @throws InterruptedException If the thread is interrupted.
     */
    @Override
    public void activateServers(Node node) throws InterruptedException
    {
        activateServersInternal(node);
    }

    @Override
    public void activateServers(StateType state) throws InterruptedException
    {
        activateServersInternal(state);
    }

    @Override
    public void activatingServers(List<IDataSource> servers)
    {
        myCurrentActivatingServers.addAll(servers);
    }

    @Override
    public void activationComplete(ServerConfigEvent event)
    {
        String serverTitle = event.getServer().getServerTitle();

        boolean isEmpty = false;
        synchronized (myCurrentActivatingServers)
        {
            int removeIndex = -1;
            int index = 0;
            for (IDataSource dataSource : myCurrentActivatingServers)
            {
                if (dataSource.getName().equals(serverTitle))
                {
                    removeIndex = index;
                    break;
                }

                index++;
            }

            if (removeIndex >= 0)
            {
                myCurrentActivatingServers.remove(removeIndex);
            }

            isEmpty = myCurrentActivatingServers.isEmpty();
        }

        if (isEmpty)
        {
            mySemaphore.release();
        }
    }

    /**
     * Activates servers.
     *
     * @param object the object to activate
     * @throws InterruptedException if the thread was interrupted
     */
    private synchronized void activateServersInternal(Object object) throws InterruptedException
    {
        myCurrentActivatingServers.clear();
        mySemaphore = new Semaphore(0);

        ServerActivator activator = new ServerActivator(myServerManager, myToolbox.getEventManager(), this);
        activator.activateServers(object);

        if (!myCurrentActivatingServers.isEmpty())
        {
            boolean successful = mySemaphore.tryAcquire(
                    ServerConstants.getDefaultServerActivateTimeoutFromPrefs(myToolbox.getPreferencesRegistry()),
                    TimeUnit.MILLISECONDS);
            if (!successful)
            {
                LOGGER.warn("State servers did not activate within time limit");
            }
        }

        myCurrentActivatingServers.clear();
        activator.close();
    }
}
