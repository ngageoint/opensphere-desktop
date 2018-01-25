package io.opensphere.controlpanels.toolbox;

import java.util.Collection;
import java.util.Properties;

import io.opensphere.controlpanels.DetailPanelProvider;
import io.opensphere.controlpanels.DetailPanelProviderRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;

/**
 * A default implementation of the {@link DetailPanelProviderRegistry}, in which
 * providers are cached.
 */
public class DetailPanelProviderRegistryImpl implements DetailPanelProviderRegistry
{
    /**
     * The collection of registered providers.
     */
    private Collection<DetailPanelProvider> myProviders;

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

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.DetailPanelProviderRegistry#registerProvider(io.opensphere.controlpanels.DetailPanelProvider)
     */
    @Override
    public void registerProvider(DetailPanelProvider pProvider)
    {
        myProviders.add(pProvider);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.DetailPanelProviderRegistry#getProviders()
     */
    @Override
    public Collection<DetailPanelProvider> getProviders()
    {
        return myProviders;
    }
}
