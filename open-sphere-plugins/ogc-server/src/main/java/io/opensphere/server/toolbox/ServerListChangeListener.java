package io.opensphere.server.toolbox;

/**
 * The listener interface for receiving serverListChange events. The class that
 * is interested in processing a serverSourceChange event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's <code>addServerListChangeListener</code>
 * method. When the serverSourceChange event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ServerListChangeEvent
 */
@FunctionalInterface
public interface ServerListChangeListener
{
    /**
     * Handle server list change event.
     *
     * @param event the event
     */
    void handle(ServerListChangeEvent event);
}
