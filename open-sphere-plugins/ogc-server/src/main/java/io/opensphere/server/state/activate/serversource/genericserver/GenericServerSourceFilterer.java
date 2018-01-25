package io.opensphere.server.state.activate.serversource.genericserver;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.state.activate.serversource.IActivationListener;
import io.opensphere.server.state.activate.serversource.ServerSourceFilterer;
import io.opensphere.server.toolbox.ServerSourceController;

/**
 * Given a list of OGCServerSources, this class will put the servers in two
 * other lists. One will be a list of servers that need to be added and then
 * activated. The other list will be a list of servers already added but need to
 * be activated.
 */
public class GenericServerSourceFilterer implements ServerSourceFilterer
{
    /**
     * The server source manager, used to get the ServerSourceControllers which
     * in turn gives information on system registered servers.
     */
    private final ServerSourceController myServerSourceController;

    /**
     * Constructs a new filterer.
     *
     * @param serverSourceController The server source manager, used to get the
     *            ServerSourceControllers which in turn gives information on
     *            system registered servers.
     */
    public GenericServerSourceFilterer(ServerSourceController serverSourceController)
    {
        myServerSourceController = serverSourceController;
    }

    @Override
    public void findBusyServers(IActivationListener listener, List<IDataSource> servers)
    {
        Map<String, OGCServerSource> registeredServers = mapRegisteredServers();
        for (IDataSource dataSource : servers)
        {
            OGCServerSource server = (OGCServerSource)dataSource;
            OGCServerSource registeredServer = registeredServers.get(server.getWFSServerURL());
            if (registeredServer != null && registeredServer.isBusy())
            {
                listener.activatingServers(New.list((IDataSource)registeredServer));
            }
        }
    }

    /**
     * Gets the list of servers that are not activated but are registered in the
     * system.
     *
     * @param servers The list of servers to determine if they are in the system
     *            but not active.
     * @return The list of servers already added to the system. This list of
     *         data sources are the registered copies of the servers and are not
     *         servers contained in the servers list.
     */
    @Override
    public List<IDataSource> getNonActiveServers(List<IDataSource> servers)
    {
        List<IDataSource> nonActiveServers = New.list();

        Map<String, OGCServerSource> registeredServers = mapRegisteredServers();

        for (IDataSource dataSource : servers)
        {
            OGCServerSource server = (OGCServerSource)dataSource;
            OGCServerSource registeredServer = registeredServers.get(server.getWFSServerURL());
            if (registeredServer != null)
            {
                if (!registeredServer.isActive())
                {
                    nonActiveServers.add(registeredServer);
                }
            }
            else
            {
                registeredServer = registeredServers.get(server.getWMSServerURL());
                if (registeredServer != null && !registeredServer.isActive())
                {
                    nonActiveServers.add(registeredServer);
                }
            }
        }

        return nonActiveServers;
    }

    /**
     * Gets a list of servers that need to be added and activated in the system.
     *
     * @param servers The list of servers to determine if they are in the system
     *            already or not.
     * @return The list of servers that need to be added and activated.
     */
    @Override
    public List<IDataSource> getNonAddedServers(List<IDataSource> servers)
    {
        List<IDataSource> nonAddedServers = New.list();

        Map<String, OGCServerSource> registeredServers = mapRegisteredServers();

        for (IDataSource dataSource : servers)
        {
            if (dataSource instanceof OGCServerSource)
            {
                OGCServerSource server = (OGCServerSource)dataSource;

                if (!registeredServers.containsKey(server.getWFSServerURL())
                        && !registeredServers.containsKey(server.getWMSServerURL()))
                {
                    nonAddedServers.add(server);
                }
            }
        }

        return nonAddedServers;
    }

    /**
     * Maps the wfs and wms urls to their registered server object.
     *
     * @return Map of urls to their registered server objects.
     */
    private Map<String, OGCServerSource> mapRegisteredServers()
    {
        Map<String, OGCServerSource> urlsToServers = New.map();

        List<IDataSource> sources = myServerSourceController.getSourceList();
        for (IDataSource source : sources)
        {
            if (source instanceof OGCServerSource)
            {
                OGCServerSource ogcSource = (OGCServerSource)source;

                if (StringUtils.isNotEmpty(ogcSource.getWFSServerURL()))
                {
                    urlsToServers.put(ogcSource.getWFSServerURL(), ogcSource);
                }

                if (StringUtils.isNotEmpty(ogcSource.getWMSServerURL()))
                {
                    urlsToServers.put(ogcSource.getWMSServerURL(), ogcSource);
                }
            }
        }

        return urlsToServers;
    }
}
