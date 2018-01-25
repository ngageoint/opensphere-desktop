package io.opensphere.server.state.activate.serversource;

import java.util.List;

import io.opensphere.mantle.datasources.IDataSource;

/**
 * Given a list of IDataSource, this class will put the servers in two other
 * lists. One will be a list of servers that need to be added and then
 * activated. The other list will be a list of servers already added but need to
 * be activated.
 */
public interface ServerSourceFilterer
{
    /**
     * Checks to see if servers are busy being activated.
     *
     * @param listener the listener
     * @param servers the servers
     */
    void findBusyServers(IActivationListener listener, List<IDataSource> servers);

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
    List<IDataSource> getNonActiveServers(List<IDataSource> servers);

    /**
     * Gets a list of servers that need to be added and activated in the system.
     *
     * @param servers The list of servers to determine if they are in the system
     *            already or not.
     * @return The list of servers that need to be added and activated.
     */
    List<IDataSource> getNonAddedServers(List<IDataSource> servers);
}
