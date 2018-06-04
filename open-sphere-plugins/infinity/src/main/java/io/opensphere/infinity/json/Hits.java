package io.opensphere.infinity.json;

/** Elasticsearch hits JSON bean. */
public class Hits
{
    /** The total. */
    private long myTotal;

    /**
     * Constructor.
     */
    public Hits()
    {
    }

    /**
     * Constructor.
     *
     * @param total the total
     */
    public Hits(long total)
    {
        myTotal = total;
    }

    /**
     * Gets the total.
     *
     * @return the total
     */
    public long getTotal()
    {
        return myTotal;
    }

    /**
     * Sets the total.
     *
     * @param total the total
     */
    public void setTotal(long total)
    {
        myTotal = total;
    }
}
