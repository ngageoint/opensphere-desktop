package io.opensphere.wms.layer;

import java.util.Objects;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * A key object used to request images for geographic tiles.
 */
public class TileImageKey
{
    /** The bounding box. */
    private final GeographicBoundingBox myBoundingBox;

    /** The time span. */
    private final TimeSpan myTimeSpan;

    /**
     * Construct the key.
     *
     * @param boundingBox The bounding box that the image should cover.
     * @param timeSpan The time span that the image should cover. This may be
     *            null for timeless layers.
     */
    public TileImageKey(GeographicBoundingBox boundingBox, TimeSpan timeSpan)
    {
        myBoundingBox = Utilities.checkNull(boundingBox, "boundingBox");
        myTimeSpan = timeSpan;
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
        TileImageKey other = (TileImageKey)obj;
        //@formatter:off
        return Objects.equals(myBoundingBox, other.myBoundingBox)
                && Objects.equals(myTimeSpan, other.myTimeSpan);
        //@formatter:on
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

    /**
     * Accessor for the timeSpan. This may be <code>null</code>.
     *
     * @return The timeSpan.
     */
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myBoundingBox);
        result = prime * result + HashCodeHelper.getHashCode(myTimeSpan);
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append(getClass().getSimpleName()).append(" [").append(myBoundingBox).append(' ');
        sb.append(myTimeSpan == null ? TimeSpan.TIMELESS : myTimeSpan).append(']');

        return sb.toString();
    }
}
