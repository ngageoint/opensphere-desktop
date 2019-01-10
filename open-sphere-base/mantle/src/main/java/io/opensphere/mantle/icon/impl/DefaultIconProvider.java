package io.opensphere.mantle.icon.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import io.opensphere.core.util.io.IOUtilities;
import io.opensphere.mantle.icon.IconProvider;

/**
 * The Class DefaultIconProvider.
 */
public class DefaultIconProvider implements IconProvider
{
    /** The Collection name. */
    private final String myCollectionName;

    /** The Image URL. */
    private final URL myImageURL;

    /** The Source Key. */
    private final String mySourceKey;

    /** The flag used to track the favorite state of the icon. */
    private final boolean myFavorite;

    /**
     * Instantiates a new default icon provider.
     *
     * @param imageURL the image url
     * @param collectionName the collection name
     * @param sourceKey the source key
     */
    public DefaultIconProvider(URL imageURL, String collectionName, String sourceKey)
    {
        this(imageURL, collectionName, sourceKey, false);
    }

    /**
     * Instantiates a new default icon provider.
     *
     * @param imageURL the image url
     * @param collectionName the collection name
     * @param sourceKey the source key
     * @param favorite a flag used to mark the icon as a favorite.
     */
    public DefaultIconProvider(URL imageURL, String collectionName, String sourceKey, boolean favorite)
    {
        myImageURL = imageURL;
        myCollectionName = collectionName;
        mySourceKey = sourceKey;
        myFavorite = favorite;
    }

    @Override
    public String getCollectionName()
    {
        return myCollectionName;
    }

    @Override
    public URL getIconURL()
    {
        return myImageURL;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconProvider#getIconImageData()
     */
    @Override
    public InputStream getIconImageData() throws IOException
    {
        return IOUtilities.getInputStream(myImageURL);
    }

    @Override
    public String getSourceKey()
    {
        return mySourceKey;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getName()).append(" ColName:").append(myCollectionName).append(" URL:")
                .append(myImageURL == null ? "NULL" : myImageURL.toString()).append(" SourceKey:").append(mySourceKey);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconProvider#isFavorite()
     */
    @Override
    public boolean isFavorite()
    {
        return myFavorite;
    }
}
