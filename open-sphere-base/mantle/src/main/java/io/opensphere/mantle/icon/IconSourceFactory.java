package io.opensphere.mantle.icon;

import java.util.Collection;
import java.util.List;

import io.opensphere.mantle.icon.impl.CompressedCollectionIconSource;
import io.opensphere.mantle.icon.impl.DirectoryCollectionIconSource;
import io.opensphere.mantle.icon.impl.SingleFileIconSource;

/**
 *
 */
public class IconSourceFactory
{
    /** Singleton reference. */
    private static final IconSourceFactory ourInstance = new IconSourceFactory();

    private final Collection<IconSource> iconSources;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private IconSourceFactory()
    {
        iconSources = List.of(new SingleFileIconSource(), new CompressedCollectionIconSource(),
                new DirectoryCollectionIconSource());
    }

    /**
     * Get a reference to the IconSourceFactory.
     *
     * @return the singleton instance
     */
    public static IconSourceFactory getInstance()
    {
        return ourInstance;
    }

    public Collection<IconSource> getIconSources()
    {
        return iconSources;
    }
}
