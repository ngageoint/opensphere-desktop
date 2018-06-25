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
     * @param interval the bin width
     * @param missing value to use if field is missing
     * @param offset the offset from zero
     * @param minDocCount the min_doc_count (minimum number of hits for which to return a result)
     */
    public Aggs(String field, double interval, long missing, double offset, int minDocCount)
    {
        myBins = new Bins(field, interval, missing, offset, minDocCount);
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
