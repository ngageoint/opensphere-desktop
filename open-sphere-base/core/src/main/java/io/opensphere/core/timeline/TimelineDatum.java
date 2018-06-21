package io.opensphere.core.timeline;

import net.jcip.annotations.Immutable;

import io.opensphere.core.model.time.TimeSpan;

/** One point in the timeline data. */
@Immutable
public class TimelineDatum
{
    /** The unique ID within the layer. */
    private final long myId;

    /** The time span. */
    private final TimeSpan myTimeSpan;

    /**
     * Constructor.
     *
     * @param id The unique ID within the layer
     * @param timeSpan The time span
     */
    public TimelineDatum(long id, TimeSpan timeSpan)
    {
        myId = id;
        myTimeSpan = timeSpan;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId()
    {
        return myId;
    }

    /**
     * Gets the time span.
     *
     * @return the time span
     */
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }
}
