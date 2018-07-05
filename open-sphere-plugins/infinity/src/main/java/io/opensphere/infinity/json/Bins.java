package io.opensphere.infinity.json;

import org.codehaus.jackson.annotate.JsonProperty;

/** Elasticsearch bins JSON bean. */
public class Bins
{
    /** The terms. */
    private Terms myTerms;

    /** The histogram. */
    private Histogram myHistogram;

    /** The date histogram */
    @JsonProperty("date_histogram")
    private DateHistogram myDateHistogram;

    /** The buckets. */
    @SuppressWarnings("rawtypes")
    private Bucket[] myBuckets;

    /**
     * Constructor.
     */
    public Bins()
    {
    }

    /**
     * Constructor.
     *
     * @param field the field
     * @param size the size
     * @param missing the missing
     */
    public Bins(String field, int size, long missing)
    {
        myTerms = new Terms(field, size, missing);
    }

    /**
     * Constructor.
     *
     * @param field the field
     * @param size the size
     * @param dayOfWeek whether dayOfWeek or hourOfDay
     */
    public Bins(String field, int size, boolean dayOfWeek)
    {
        myTerms = new Terms(field, size, dayOfWeek);
    }

    /**
     * Constructor.
     *
     * @param field the field
     * @param interval the bin width
     * @param missing the value to use if field is missing in results
     * @param offset the offset from zero
     */
    public Bins(String field, double interval, long missing, double offset)
    {
        myHistogram = new Histogram(field, interval, missing, offset);
    }

    /**
     * Constructor.
     *
     * @param field the field
     * @param format the date format
     * @param interval the date bin interval
     */
    public Bins(String field, String format, String interval)
    {
        myDateHistogram = new DateHistogram(field, format, interval);
    }

    /**
     * Gets the terms.
     *
     * @return the terms
     */
    public Terms getTerms()
    {
        return myTerms;
    }

    /**
     * Sets the terms.
     *
     * @param terms the terms
     */
    public void setTerms(Terms terms)
    {
        myTerms = terms;
    }

    /**
     * Get the histogram.
     *
     * @return the histogram
     */
    public Histogram getHistogram()
    {
        return myHistogram;
    }

    /**
     * Set the histogram.
     *
     * @param histogram the histogram to set
     */
    public void setHistogram(Histogram histogram)
    {
        myHistogram = histogram;
    }

    /**
     * Get the date_histogram.
     *
     * @return the dateHistogram
     */
    //NOTE: Underscores in method name are required
    public DateHistogram getDate_histogram()
    {
        return myDateHistogram;
    }

    /**
     * Set the date_histogram.
     *
     * @param dateHistogram the dateHistogram to set
     */
    //NOTE: Underscores in method name are required
    public void setDate_histogram(DateHistogram dateHistogram)
    {
        myDateHistogram = dateHistogram;
    }

    /**
     * Gets the buckets.
     *
     * @return the buckets
     */
    @SuppressWarnings("rawtypes")
    public Bucket[] getBuckets()
    {
        return myBuckets;
    }

    /**
     * Sets the buckets.
     *
     * @param buckets the buckets
     */
    @SuppressWarnings("rawtypes")
    public void setBuckets(Bucket[] buckets)
    {
        myBuckets = buckets;
    }
}
