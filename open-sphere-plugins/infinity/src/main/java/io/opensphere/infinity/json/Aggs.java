package io.opensphere.infinity.json;

public class Aggs
{
    private Bins myBins;

    public Aggs()
    {
    }

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
