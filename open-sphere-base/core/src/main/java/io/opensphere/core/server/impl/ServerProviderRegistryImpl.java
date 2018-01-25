package io.opensphere.core.server.impl;

import java.util.Map;

import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.New;

/**
 * The implementation of the ServerProviderRegistry.
 *
 */
public class ServerProviderRegistryImpl implements ServerProviderRegistry
{
    /**
     * The map of server providers.
     */
    private final Map<Class<?>, ServerProvider<?>> myProviders = New.map();

    @Override
    public synchronized <T> void registerProvider(Class<T> serverClass, ServerProvider<T> provider)
    {
        myProviders.put(serverClass, provider);
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> ServerProvider<T> getProvider(Class<T> serverClass)
    {
        return (ServerProvider<T>)myProviders.get(serverClass);
    }
}
