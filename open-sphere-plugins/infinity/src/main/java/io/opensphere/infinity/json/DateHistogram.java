package io.opensphere.infinity.json;

/** Elasticsearch date histogram JSON bean. Used with date binning. */
public class DateHistogram
{
    /** The date field being binned. */
    private String myField;

    /** The format of the date that should be returned. (e.g., yyyy-mm-dd hh:ss) */
    private String myFormat;

    /** The interval of the bin. (e.g., "minutes") */
    private String myInterval;

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
        myField = field;
        myFormat = format;
        myInterval = interval;
    }

    /**
     * Get the field.
     *
     * @return the field
     */
    public String getField()
    {
        return myField;
    }

    /**
     * Set the field.
     *
     * @param field the field to set
     */
    public void setField(String field)
    {
        myField = field;
    }

    /**
     * Get the format.
     *
     * @return the format
     */
    public String getFormat()
    {
        return myFormat;
    }

    /**
     * Set the format.
     *
     * @param format the format to set
     */
    public void setFormat(String format)
    {
        myFormat = format;
    }

    /**
     * Get the interval.
     *
     * @return the interval
     */
    public String getInterval()
    {
        return myInterval;
    }

    /**
     * Set the interval.
     *
     * @param interval the interval to set
     */
    public void setInterval(String interval)
    {
        myInterval = interval;
    }

    @Override
    public String toString()
    {
        return "DateHistogram [field=" + myField + ", format=" + myFormat + ", interval=" + myInterval + "]";
    }
}
