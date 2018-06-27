package io.opensphere.infinity.json;

/** Elasticsearch bins JSON bean. */
public class Bins
{
    /** The terms. */
    private Terms myTerms;

    /** The histogram. */
    private Histogram myHistogram;

    /** The buckets. */
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
     * @param interval the bin width
     * @param missing the value to use if field is missing in results
     * @param offset the offset from zero
     * @param minDocCount the min_doc_count (minimum number of hits for which to return a result)
     */
    public Bins(String field, double interval, long missing, double offset, int minDocCount)
    {
        myHistogram = new Histogram(field, interval, missing, offset, minDocCount);
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
