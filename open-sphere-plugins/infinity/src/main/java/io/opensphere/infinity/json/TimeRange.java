package io.opensphere.infinity.json;

import io.opensphere.core.model.time.TimeSpan;

/** Elasticsearch time range JSON bean. */
public class TimeRange
{
    /** The gte. */
    private long myGte;

    /** The lt. */
    private long myLt;

    /** The format. */
    private static final String myFormat = "epoch_millis";

    /**
     * Constructor.
     *
     * @param timeSpan the time span
     */
    public TimeRange(TimeSpan timeSpan)
    {
        myGte = timeSpan.getStart();
        myLt = timeSpan.getEnd();
    }

    /**
     * Gets the gte.
     *
     * @return the gte
     */
    public long getGte()
    {
        return myGte;
    }

    /**
     * Sets the gte.
     *
     * @param gte the gte
     */
    public void setGte(long gte)
    {
        myGte = gte;
    }

    /**
     * Gets the lt.
     *
     * @return the lt
     */
    public long getLt()
    {
        return myLt;
    }

    /**
     * Sets the lt.
     *
     * @param lt the lt
     */
    public void setLt(long lt)
    {
        myLt = lt;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat()
    {
        return myFormat;
    }
}
