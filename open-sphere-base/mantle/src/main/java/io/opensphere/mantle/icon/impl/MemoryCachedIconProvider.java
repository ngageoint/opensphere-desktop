package io.opensphere.mantle.icon.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** An icon provider drawing from cached memory. */
public class MemoryCachedIconProvider extends DefaultIconProvider
{
    /** The byte array in which the contents of the image are stored. */
    private byte[] myImageData;

    /**
     * Creates a new icon provider using the supplied information.
     * 
     * @param imageURL the URL of the icon.
     * @param imageData the contents of the icon as a byte array.
     * @param collectionName the name of the collection to which the icon
     *            belongs.
     * @param subCategory the subcategory to which the icon belongs.
     * @param sourceKey the source key applied to the icon.
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
