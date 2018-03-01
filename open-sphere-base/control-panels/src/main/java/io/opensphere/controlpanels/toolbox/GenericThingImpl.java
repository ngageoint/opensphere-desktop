package io.opensphere.controlpanels.toolbox;

import java.util.Collection;
import java.util.Properties;

import io.opensphere.controlpanels.DetailPanelProviderRegistry;
import io.opensphere.controlpanels.GenericThing;
import io.opensphere.controlpanels.GenericThingProvider;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;

/**
 * A default implementation of the {@link DetailPanelProviderRegistry}, in which
 * providers are cached.
 * 
 * @param <T>
 */
public class GenericThingImpl<T> implements GenericThing<T>
{
    /**
     * The collection of registered providers.
     */
    private Collection<GenericThingProvider> myProviders;

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.DetailPanelProviderRegistry#initialize(Toolbox,
     *      java.util.Properties)
     */
    @Override
    public void initialize(Toolbox pToolbox, Properties pProperties)
    {
        myProviders = New.collection();
    }

    @Override
    public void registerProvider(GenericThingProvider provider)
    {
        myProviders.add(provider);

    }

    @Override
    public Collection<GenericThingProvider> getProviders()
    {
        return myProviders;
    }

}
