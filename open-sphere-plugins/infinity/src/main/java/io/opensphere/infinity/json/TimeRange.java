package io.opensphere.infinity.json;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

import io.opensphere.core.model.time.TimeSpan;

/** Elasticsearch time range JSON bean. */
@JsonPropertyOrder({ "gte", "lt", "format" })
public class TimeRange
{
    /** The gte. */
    private Long myGte;

    /** The lt. */
    private Long myLt;

    /** The format. */
    private static final String myFormat = "epoch_millis";

    /**
     * Constructor.
     *
     * @param timeSpan the time span
     */
    public TimeRange(TimeSpan timeSpan)
    {
        myGte = Long.valueOf(timeSpan.getStart());
        myLt = Long.valueOf(timeSpan.getEnd());
    }

    /**
     * Constructor.
     *
     * @param gte the gte
     * @param lt the lt
     */
    public TimeRange(Long gte, Long lt)
    {
        myGte = gte;
        myLt = lt;
    }

    /**
     * Gets the gte.
     *
     * @return the gte
     */
    public Long getGte()
    {
        return myGte;
    }

    /**
     * Sets the gte.
     *
     * @param gte the gte
     */
    public void setGte(Long gte)
    {
        myGte = gte;
    }

    /**
     * Gets the lt.
     *
     * @return the lt
     */
    public Long getLt()
    {
        return myLt;
    }

    /**
     * Sets the lt.
     *
     * @param lt the lt
     */
    public void setLt(Long lt)
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
