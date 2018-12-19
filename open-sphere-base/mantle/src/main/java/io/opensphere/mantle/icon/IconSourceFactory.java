package io.opensphere.mantle.icon;

import java.util.Collection;
import java.util.List;

import io.opensphere.mantle.icon.impl.CompressedCollectionIconSource;
import io.opensphere.mantle.icon.impl.DirectoryCollectionIconSource;
import io.opensphere.mantle.icon.impl.SingleFileIconSource;

/** A factory used to create new Icon sources. */
public class IconSourceFactory
{
    /** Singleton reference. */
    private static final IconSourceFactory INSTANCE = new IconSourceFactory();

    /** The icon sources managed by the factory. */
    private final Collection<IconSource<? extends IconSourceModel>> myIconSources;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private IconSourceFactory()
    {
        myIconSources = List.of(new SingleFileIconSource(), new CompressedCollectionIconSource(),
                new DirectoryCollectionIconSource());
    }

    /**
     * Get a reference to the IconSourceFactory.
     *
     * @return the singleton instance
     */
    public static IconSourceFactory getInstance()
    {
        return INSTANCE;
    }

    /**
     * Gets the value of the {@link #myIconSources} field.
     *
     * @return the value of the myIconSources field.
     */
    public Collection<IconSource<? extends IconSourceModel>> getIconSources()
    {
        return myIconSources;
    }
}
