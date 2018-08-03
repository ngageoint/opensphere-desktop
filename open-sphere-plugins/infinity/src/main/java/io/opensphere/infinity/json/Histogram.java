package io.opensphere.infinity.json;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

import io.opensphere.mantle.infinity.InfinityUtilities;

/** Elasticsearch histogram JSON bean. Used with numeric binning. */
@JsonPropertyOrder({ "field", "interval", "min_doc_count", "missing", "offset" })
public class Histogram
{
    /** The field */
    private String myField;

    /** The bin interval */
    private double myInterval;

    /** The min_doc_count */
    private int myMinDocCount;

    /** The missing */
    private long myMissing;

    /** The bin offset */
    private double myOffset;

    /**
     * Constructor
     */
    public Histogram()
    {
        // Intentionally left blank
    }

    /**
     * Constructor
     *
     * @param field the field
     * @param interval the interval
     * @param missing the missing
     * @param offset the offset
     */
    public Histogram(String field, double interval, long missing, double offset)
    {
        myField = field;
        myInterval = interval;
        myMissing = missing;
        myOffset = offset;
        myMinDocCount = InfinityUtilities.DEFAULT_MIN_DOC_COUNT;
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField()
    {
        return myField;
    }

    /**
     * Sets the field.
     *
     * @param field the field to set
     */
    public void setField(String field)
    {
        myField = field;
    }

    /**
     * Gets the interval.
     *
     * @return the interval
     */
    public double getInterval()
    {
        return myInterval;
    }

    /**
     * Sets the interval.
     *
     * @param interval the interval to set
     */
    public void setInterval(float interval)
    {
        myInterval = interval;
    }

    /**
     * Gets the min_doc_count.
     *
     * @return the minDocCount
     */
    //Note: method name requires the underscores
    public int getMin_doc_count()
    {
        return myMinDocCount;
    }

    /**
     * Sets the min_doc_count.
     *
     * @param minDocCount the minDocCount to set
     */
    //Note: Method name requires the underscores
    public void setMin_doc_count(int minDocCount)
    {
        myMinDocCount = minDocCount;
    }

    /**
     * Gets the missing.
     *
     * @return the missing
     */
    public long getMissing()
    {
        return myMissing;
    }

    /**
     * Sets the missing.
     *
     * @param missing the missing to set
     */
    public void setMissing(long missing)
    {
        myMissing = missing;
    }

    /**
     * Gets the offset.
     *
     * @return the offset
     */
    public double getOffset()
    {
        return myOffset;
    }

    /**
     * Sets the offset.
     *
     * @param offset the offset to set
     */
    public void setOffset(int offset)
    {
        myOffset = offset;
    }

    @Override
    /**
     * Creates a string from the Histogram class
     */
    public String toString()
    {
        return "Histogram [myField=" + myField + ", myInterval=" + myInterval + ", myMinDocCount=" + myMinDocCount
                + ", myMissing=" + myMissing + ", myOffset=" + myOffset + "]";
    }

}
