package io.opensphere.infinity.json;

/** Elasticsearch date histogram JSON bean. Used with date binning. */
public class DateHistogram
{
    /** The date field being binned. */
    private String field;

    /** The format of the date that should be returned. (e.g., yyyy-mm-dd hh:ss)*/
    private String format;

    /** The interval of the bin.  (e.g., "minutes") */
    private String interval;

    /**
     * Constructor.
     */
    public DateHistogram()
    {
        // Intentionally left blank
    }

    /**
     * Constructor.
     *
     * @param field the field
     * @param format the date format
     * @param interval the bin interval
     */
    public DateHistogram(String field, String format, String interval)
    {
        this.field = field;
        this.format = format;
        this.interval = interval;
    }

    /**
     * Get the field.
     *
     * @return the field
     */
    public String getField()
    {
        return field;
    }

    /**
     * Set the field.
     *
     * @param field the field to set
     */
    public void setField(String field)
    {
        this.field = field;
    }

    /**
     * Get the format.
     *
     * @return the format
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * Set the format.
     *
     * @param format the format to set
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * Get the interval.
     *
     * @return the interval
     */
    public String getInterval()
    {
        return interval;
    }

    /**
     * Set the interval.
     *
     * @param interval the interval to set
     */
    public void setInterval(String interval)
    {
        this.interval = interval;
    }

    @Override
    public String toString()
    {
        return "DateHistogram [field=" + field + ", format=" + format + ", interval=" + interval + "]";
    }
}
