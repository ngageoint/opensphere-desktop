package io.opensphere.infinity.json;

public class Hits
{
    private long myTotal;

    public Hits()
    {
    }

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
