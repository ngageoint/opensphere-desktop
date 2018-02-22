package io.opensphere.tracktool.model.impl;

import java.util.Objects;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.tracktool.model.TrackNode;

/**
 * The default implementation of a track node.
 */
public class DefaultTrackNode implements TrackNode
{
    /** The geographic position of the node. */
    private final LatLonAlt myPosition;

    /** The Time. */
    private final TimeSpan myTime;
    
    private final Long myCacheID;

    /**
     * The callout offset.
     */
    private Vector2i myOffset;

    /**
     * Instantiates a new default track node.
     *
     * @param position the position
     */
    public DefaultTrackNode(LatLonAlt position)
    {
        this(position, TimeSpan.TIMELESS, Long.MIN_VALUE);
    }

    /**
     * Instantiates a new default track node.
     *
     * @param position the position.
     * @param span the span.
     * @param id the cacheID.
     */
    public DefaultTrackNode(LatLonAlt position, TimeSpan span, Long id)
    {
        myPosition = position;
        myTime = span;
        myCacheID = id;
    }

    /**
     * Instantiates a new default track node.
     *
     * @param other the other
     */
    public DefaultTrackNode(TrackNode other)
    {
        myPosition = other.getLocation();
        myTime = other.getTime();
        myCacheID = other.getCacheID();
    }

    @Override
    public LatLonAlt getLocation()
    {
        return myPosition;
    }

    @Override
    public Vector2i getOffset()
    {
        return myOffset;
    }

    @Override
    public TimeSpan getTime()
    {
        return myTime;
    }

    /**
     * Sets the callout offset for the node.
     *
     * @param offset The callout offset.
     */
    public void setOffset(Vector2i offset)
    {
        myOffset = offset;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myPosition);
        return result;
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
        DefaultTrackNode other = (DefaultTrackNode)obj;
        return Objects.equals(myPosition, other.myPosition);
    }

    @Override
    public Long getCacheID()
    {
      return myCacheID;
    }
}
