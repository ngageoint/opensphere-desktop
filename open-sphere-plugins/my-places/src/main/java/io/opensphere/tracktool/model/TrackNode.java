package io.opensphere.tracktool.model;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;

/**
 * The Interface TrackNode.
 */
public interface TrackNode
{
    /**
     * Gets the location.
     *
     * @return the location
     */
    LatLonAlt getLocation();

    /**
     * Gets the callout offset for this segment.
     *
     * @return The callout offset.
     */
    Vector2i getOffset();

    /**
     * Gets the time.
     *
     * @return the time
     */
    TimeSpan getTime();
}
