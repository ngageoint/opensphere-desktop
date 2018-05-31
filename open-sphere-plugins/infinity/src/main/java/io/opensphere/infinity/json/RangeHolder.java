package io.opensphere.infinity.json;

public class RangeHolder
{
    private Range myRange;

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
