package io.opensphere.core.control.action.context;

import io.opensphere.core.model.time.TimeSpan;

/** Context key for actions related to a timespan. */
public class TimespanContextKey
{
    /** The time span. */
    private final TimeSpan myTimeSpan;

    /**
     * Constructor.
     *
     * @param timeSpan The time span.
     */
    public TimespanContextKey(TimeSpan timeSpan)
    {
        myTimeSpan = timeSpan;
    }

    /**
     * Get the time span.
     *
     * @return The time span.
     */
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }
}
