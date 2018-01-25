package io.opensphere.server.state.activate.serversource;

import java.util.List;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.mantle.datasources.IDataSource;

/**
 * An interface to an object that will inspect a state node and return a list of
 * OGCServerSource that are contained in the state node.
 *
 */
public interface ServerSourceProvider
{
    /**
     * Gets the list of server sources contained within the specified state
     * node.
     *
     * @param node The state node to inspect.
     * @return The list of server sources or empty if none.
     */
    List<IDataSource> getServersInNode(Node node);

    /**
     * Gets the list of server sources contained within the specified state
     * object.
     *
     * @param state The state object to inspect.
     * @return The list of server sources or empty if none.
     */
    List<IDataSource> getServersInNode(StateType state);
}
