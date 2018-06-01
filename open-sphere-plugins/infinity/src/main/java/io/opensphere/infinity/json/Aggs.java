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
