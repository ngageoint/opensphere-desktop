package io.opensphere.mantle.icon.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 *
 */
public class MemoryCachedIconProvider extends DefaultIconProvider
{
    /** The byte array in which the contents of the image are stored. */
    private byte[] myImageData;

    /**
     * @param imageURL
     * @param imageData
     * @param collectionName
     * @param subCategory
     * @param sourceKey
     */
    public MemoryCachedIconProvider(URL imageURL, byte[] imageData, String collectionName, String subCategory, String sourceKey)
    {
        super(imageURL, collectionName, subCategory, sourceKey);
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
