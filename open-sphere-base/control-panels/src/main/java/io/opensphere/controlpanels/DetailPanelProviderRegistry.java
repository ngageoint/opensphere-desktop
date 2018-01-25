package io.opensphere.controlpanels;

import java.util.Collection;
import java.util.Properties;

import io.opensphere.core.Toolbox;

/**
 * A registry of detail panel providers. Plugins may register a provider with
 * the registry to add additional behavior.
 */
public interface DetailPanelProviderRegistry
{
    /**
     * Registers the supplied provider with the registry.
     *
     * @param pProvider the provider to add to the registry.
     */
    void registerProvider(DetailPanelProvider pProvider);

    /**
     * Gets the {@link Collection} of registered {@link DetailPanelProvider}
     * implementations from the registry.
     *
     * @return the {@link Collection} of available providers.
     */
    Collection<DetailPanelProvider> getProviders();

    /**
     * Initializes the registry with properties from configuration.
     *
     * @param pToolbox the toolbox through which system interactions occur.
     * @param pProperties the properties with which to initialize the registry.
     */
    void initialize(Toolbox pToolbox, Properties pProperties);
}
