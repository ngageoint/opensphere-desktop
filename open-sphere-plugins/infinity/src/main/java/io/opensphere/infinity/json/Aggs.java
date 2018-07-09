package io.opensphere.infinity.json;

/** Elasticsearch aggs JSON bean. */
public class Aggs
{
    /** The bins. */
    private Bins myBins;

    /**
     * Constructor.
     */
    public Aggs()
    {
    }

    /**
     * Constructor.
     *
     * @param field the field
     * @param size the size
     * @param missing the missing
     */
    public Aggs(String field, int size, long missing)
    {
        myBins = new Bins(field, size, missing);
    }

    /**
     * Constructor.
     *
     * @param field the field
     * @param size the size
     * @param dayOfWeek whether dayOfWeek or hourOfDay
     */
    public Aggs(String field, int size, boolean dayOfWeek)
    {
        myBins = new Bins(field, size, dayOfWeek);
    }

    /**
     * Constructor for numeric binning.
     *
     * @param field the field
     * @param interval the bin width
     * @param missing value to use if field is missing
     * @param offset the offset from zero
     */
    public Aggs(String field, double interval, long missing, double offset)
    {
        myBins = new Bins(field, interval, missing, offset);
    }

    /**
     * Constructor for date binning.
     *
     * @param field the field
     * @param format the date format
     * @param interval the bin interval
     */
    public Aggs(String field, String format, String interval)
    {
        myBins = new Bins(field, format, interval);
    }

    /**
     * Gets the bins.
     *
     * @return the bins
     */
    public Bins getBins()
    {
        return myBins;
    }

    /**
     * Sets the bins.
     *
     * @param bins the bins
     */
    public void setBins(Bins bins)
    {
        myBins = bins;
    }
}
