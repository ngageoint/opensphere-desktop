package io.opensphere.infinity.json;

/** Elasticsearch aggregations JSON bean. */
public class Aggregations
{
    /** The bins. */
    private Bins myBins = new Bins();

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
