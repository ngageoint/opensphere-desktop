package io.opensphere.wfs.placenames;

import java.util.Objects;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.lang.HashCodeHelper;

/** A key which uniquely identifies a group of placenames. */
public class PlaceNameKey
{
    /** Bounding box over which the key is valid. */
    private final GeographicBoundingBox myBounds;

    /**
     * Constructor.
     *
     * @param bounds Bounding box over which the key is valid.
     */
    public PlaceNameKey(GeographicBoundingBox bounds)
    {
        myBounds = bounds;
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
        PlaceNameKey other = (PlaceNameKey)obj;
        return Objects.equals(myBounds, other.myBounds);
    }

    /**
     * Get the bounds.
     *
     * @return the bounds
     */
    public GeographicBoundingBox getBounds()
    {
        return myBounds;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myBounds);
        return result;
    }

    @Override
    public String toString()
    {
        return "PlaceNameKey " + myBounds;
    }
}
