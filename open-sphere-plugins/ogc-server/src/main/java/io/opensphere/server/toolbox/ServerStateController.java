package io.opensphere.server.toolbox;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

/**
 * Interface to an object who is responsible for restoring server connections
 * within a State document node.
 */
public interface ServerStateController
{
    /**
     * Restores and activates server connections within the specified state
     * node.
     *
     * @param node The state node potentially containing server connections.
     * @throws InterruptedException If the thread is interrupted.
     */
    void activateServers(Node node) throws InterruptedException;

    /**
     * Restores and activates server connections within the specified state
     * object.
     *
     * @param state The state object potentially containing server connections.
     * @throws InterruptedException If the thread is interrupted.
     */
    void activateServers(StateType state) throws InterruptedException;
}
