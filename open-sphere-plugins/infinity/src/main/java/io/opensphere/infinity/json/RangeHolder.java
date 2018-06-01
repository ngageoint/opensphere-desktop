package io.opensphere.infinity.json;

/** Elasticsearch range holder JSON bean. */
public class RangeHolder
{
    /** The range. */
    private Range myRange;

    /**
     * Constructor.
     *
     * @param gte the gte
     * @param lt the lt
     */
    public RangeHolder(long gte, long lt)
    {
        myRange = new Range(gte, lt);
    }

    /**
     * Gets the range.
     *
     * @return the range
     */
    public Range getRange()
    {
        return myRange;
    }

    /**
     * Sets the range.
     *
     * @param range the range
     */
    public void setRange(Range range)
    {
        myRange = range;
    }
}
