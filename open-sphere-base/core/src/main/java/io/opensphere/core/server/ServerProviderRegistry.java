package io.opensphere.core.server;

/**
 * Contains a collection of providers that can provide objects that communicate
 * to specified servers.
 *
 */
public interface ServerProviderRegistry
{
    /**
     * Registers the provider under the specified server class.
     *
     * @param serverClass The type of the server object.
     * @param provider The provider to register.
     * @param <T> The type of the server object that is used to communicate with
     *            the server.
     */
    <T> void registerProvider(Class<T> serverClass, ServerProvider<T> provider);

    /**
     * Gets the provider for the specified server class.
     *
     * @param serverClass The type of the server object.
     * @param <T> The type of the server object that is used to communicate with
     *            the server.
     * @return The server provider.
     */
    <T> ServerProvider<T> getProvider(Class<T> serverClass);
}
