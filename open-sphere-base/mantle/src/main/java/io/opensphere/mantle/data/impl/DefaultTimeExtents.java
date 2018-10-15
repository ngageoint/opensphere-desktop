package io.opensphere.mantle.data.impl;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.TimeExtents;

/**
 * Stores a list of {@link TimeSpan}.
 */
public class DefaultTimeExtents implements TimeExtents
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The bounding boxes. */
    private final List<TimeSpan> myTimeSpans;

    /**
     * CTOR.
     */
    public DefaultTimeExtents()
    {
        myTimeSpans = new ArrayList<>();
    }

    /**
     * CTOR with single {@link TimeSpan}.
     *
     * @param ts - the time extent
     */
    public DefaultTimeExtents(TimeSpan ts)
    {
        myTimeSpans = new ArrayList<>();
        myTimeSpans.add(ts);
    }

    /**
     * Adds the time span to the extent.
     *
     * @param ts the {@link TimeSpan}
     */
    public synchronized void addTimeSpan(TimeSpan ts)
    {
        myTimeSpans.add(ts);
    }

    @Override
    public TimeSpan getExtent()
    {
        ExtentAccumulator extentAccumumator = new ExtentAccumulator();
        synchronized (this)
        {
            myTimeSpans.stream().filter(s -> !s.isTimeless()).forEach(extentAccumumator::add);
        }
        return extentAccumumator.getExtent();
    }

    /**
     * Gets the list of {@link TimeSpan}.
     *
     * @return the TimeSpan list
     */
    @Override
    public synchronized List<TimeSpan> getTimespans()
    {
        return New.list(myTimeSpans);
    }

    /**
     * Sets the time extent, clearing all other spans.
     *
     * @param ts the new time extent
     */
    public synchronized void setTimeExtent(TimeSpan ts)
    {
        myTimeSpans.clear();
        myTimeSpans.add(ts);
    }

    @Override
    public synchronized String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(": NumSpans: ").append(myTimeSpans == null ? 0 : myTimeSpans.size())
                .append('\n');
        if (myTimeSpans != null)
        {
            for (TimeSpan ts : myTimeSpans)
            {
                sb.append("  ").append(ts.toString()).append('\n');
            }
        }
        return sb.toString();
    }
}
