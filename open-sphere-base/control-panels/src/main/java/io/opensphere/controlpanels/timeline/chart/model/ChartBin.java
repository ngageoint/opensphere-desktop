package io.opensphere.controlpanels.timeline.chart.model;

import io.opensphere.core.model.time.TimeSpan;

/**
 * A chart bin. It's final only as an optimization.
 */
public final class ChartBin
{
    /** The count. */
    private int myCount;

    /** The time span. */
    private final TimeSpan mySpan;

    /**
     * Constructor.
     *
     * @param span the span
     */
    public ChartBin(TimeSpan span)
    {
        mySpan = span;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount()
    {
        return myCount;
    }

    /**
     * Gets the span.
     *
     * @return the span
     */
    public TimeSpan getSpan()
    {
        return mySpan;
    }

    /**
     * Increments the count.
     */
    public void increment()
    {
        ++myCount;
    }

    /**
     * Sets the count.
     *
     * @param count the count
     */
    public void setCount(int count)
    {
        myCount = count;
    }
}
