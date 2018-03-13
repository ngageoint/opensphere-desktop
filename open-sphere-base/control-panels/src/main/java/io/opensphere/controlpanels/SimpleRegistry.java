package io.opensphere.controlpanels;

import java.util.Collection;
import java.util.Properties;

import io.opensphere.core.Toolbox;

/**
 * A registry of detail panel providers. Plugins may register a provider with
 * the registry to add additional behavior.
 * 
 * @param <T> the type of the provider.
 */
public interface SimpleRegistry<T>
{
    /**
     * Registers the supplied provider with the registry.
     *
     * @param pProvider the provider to add to the registry.
     */
    void registerProvider(T provider);

    /**
     * Gets the {@link Collection} of registered provider implementations from
     * the registry.
     *
     * @return the {@link Collection} of available providers.
     */
    Collection<T> getProviders();

    /**
     * Initializes the registry with properties from configuration.
     *
     * @param pToolbox the toolbox through which system interactions occur.
     * @param pProperties the properties with which to initialize the registry.
     */
    void initialize(Toolbox pToolbox, Properties pProperties);
}
