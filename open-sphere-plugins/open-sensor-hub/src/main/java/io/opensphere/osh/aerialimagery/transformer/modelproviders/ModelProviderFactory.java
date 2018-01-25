package io.opensphere.osh.aerialimagery.transformer.modelproviders;

import java.util.List;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.osh.util.OSHImageQuerier;

/**
 * Creates a list of {@link ModelProvider} that are able to provide models of
 * the data of a specified layer.
 */
public class ModelProviderFactory
{
    /**
     * The data registry.
     */
    private final DataRegistry myDataRegistry;

    /**
     * Used to query video images.
     */
    private final OSHImageQuerier myQuerier;

    /**
     * Constructs a model provider factory.
     *
     * @param dataRegistry The data registry.
     * @param querier Used to query video images.
     */
    public ModelProviderFactory(DataRegistry dataRegistry, OSHImageQuerier querier)
    {
        myDataRegistry = dataRegistry;
        myQuerier = querier;
    }

    /**
     * Creates the providers.
     *
     * @return The available providers.
     */
    public List<ModelProvider> createProviders()
    {
        return New.list(new PlatformMetadataProvider(myDataRegistry), new AerialImageProvider(myQuerier));
    }
}
