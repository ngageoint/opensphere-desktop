package io.opensphere.mantle.icon.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** An icon provider drawing from cached memory. */
public class MemoryCachedIconProvider extends DefaultIconProvider
{
    /** The byte array in which the contents of the image are stored. */
    private final byte[] myImageData;

    /**
     * Creates a new icon provider using the supplied information.
     *
     * @param imageURL the URL of the icon.
     * @param imageData the contents of the icon as a byte array.
     * @param collectionName the name of the collection to which the icon
     *            belongs.
     * @param sourceKey the source key applied to the icon.
     */
    public MemoryCachedIconProvider(final URL imageURL, final byte[] imageData, final String collectionName,
            final String sourceKey)
    {
        super(imageURL, collectionName, sourceKey);
        myImageData = imageData;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.impl.DefaultIconProvider#getIconImageData()
     */
    @Override
    public InputStream getIconImageData() throws IOException
    {
        return new ByteArrayInputStream(myImageData);
    }
}
