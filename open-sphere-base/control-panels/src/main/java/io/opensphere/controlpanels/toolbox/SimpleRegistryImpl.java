package io.opensphere.controlpanels.toolbox;

import java.util.Collection;
import java.util.Properties;

import io.opensphere.controlpanels.SimpleRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;

/**
 * A default implementation of the {@link DetailPanelProviderRegistry}, in which
 * providers are cached.
 * 
 * @param <T> the type of the provider.
 */
public class SimpleRegistryImpl<T> implements SimpleRegistry<T>
{
    /**
     * The collection of registered providers.
     */
    private Collection<T> myProviders;

    @Override
    public void registerProvider(T provider)
    {
        myProviders.add(provider);
    }

    @Override
    public Collection<T> getProviders()
    {
        return myProviders;
    }

    @Override
    public void initialize(Toolbox pToolbox, Properties pProperties)
    {
        myProviders = New.collection();
    }
}
