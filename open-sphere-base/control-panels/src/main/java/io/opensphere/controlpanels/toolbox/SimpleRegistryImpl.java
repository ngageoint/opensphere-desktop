package io.opensphere.controlpanels.toolbox;

import java.util.Collection;
import java.util.Properties;

import io.opensphere.controlpanels.SimpleRegistry;
import io.opensphere.controlpanels.GenericThingProvider;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;

/**
 * A default implementation of the {@link DetailPanelProviderRegistry}, in which
 * providers are cached.
 * 
 * @param <T>
 */
public class SimpleRegistryImpl<T> implements SimpleRegistry<T>
{
    /**
     * The collection of registered providers.
     */
    private Collection<GenericThingProvider> myProviders;

    @Override
    public void registerProvider(T provider)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<T> getProviders()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void initialize(Toolbox pToolbox, Properties pProperties)
    {
        // TODO Auto-generated method stub
        
    }



}
