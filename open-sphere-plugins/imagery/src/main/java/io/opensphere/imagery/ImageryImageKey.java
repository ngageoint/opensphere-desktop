package io.opensphere.imagery;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.Utilities;

/**
 * A key object used to request images from the {@link ImageryLayer} image
 * provider.
 */
public class ImageryImageKey
{
    /** The bounding box. */
    private final GeographicBoundingBox myBoundingBox;

    /**
     * Construct the key.
     *
     * @param boundingBox The bounding box that the image should cover.
     */
    public ImageryImageKey(GeographicBoundingBox boundingBox)
    {
        Utilities.checkNull(boundingBox, "boundingBox");
        myBoundingBox = boundingBox;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ImageryImageKey other = (ImageryImageKey)obj;
        return myBoundingBox.equals(other.myBoundingBox);
    }

    /**
     * Accessor for the boundingBox.
     *
     * @return The boundingBox.
     */
    public GeographicBoundingBox getBoundingBox()
    {
        return myBoundingBox;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myBoundingBox.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("ImageryImageKey [").append(myBoundingBox).append(' ');
        return sb.toString();
    }
}
