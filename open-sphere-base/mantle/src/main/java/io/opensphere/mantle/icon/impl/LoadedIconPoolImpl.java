package io.opensphere.mantle.icon.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.image.processor.ImageProcessor;
import io.opensphere.core.util.SharedObjectPool;
import io.opensphere.mantle.icon.IconImageProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.LoadedIconPool;

/**
 * The Class LoadedIconPoolImpl.
 */
public class LoadedIconPoolImpl implements LoadedIconPool
{
    /** The Provider pool. */
    private final SharedObjectPool<IconImageProvider> myProviderPool;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new loaded icon pool impl.
     *
     * @param toolbox The system toolbox.
     */
    public LoadedIconPoolImpl(Toolbox toolbox)
    {
        myProviderPool = new SharedObjectPool<>();
        myToolbox = toolbox;
    }

    @Override
    public IconImageProvider getIconImageProvider(IconRecord record)
    {
        return getIconImageProvider(record, (ImageProcessor)null);
    }

    @Override
    public IconImageProvider getIconImageProvider(IconRecord record, ImageProcessor imageProcessor)
    {
        IconImageProvider provider = new IconImageProvider(record.getImageURL(), imageProcessor, myToolbox);
        provider = myProviderPool.get(provider);
        return provider;
    }
}
