package io.opensphere.server.toolbox;

import io.opensphere.server.state.activate.serversource.ServerSourceFilterer;
import io.opensphere.server.state.activate.serversource.ServerSourceProvider;

/**
 * A server source controller that provides objects to help restore its server
 * from a saved state.
 */
public interface StateServerSourceController
{
    /**
     * Gets the server source filterer that will group ServerSources into server
     * that are registered but not activated and servers that are not registered
     * at all.
     *
     * @return The ServerSourceFilterer.
     */
    ServerSourceFilterer getServerSourceFilterer();

    /**
     * Gets the ServerSourceProvider that will generate its servers from a state
     * file.
     *
     * @return The ServerSourceProvider.
     */
    ServerSourceProvider getStateServerProvider();
}
