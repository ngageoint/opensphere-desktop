package io.opensphere.infinity.json;

public class Timefield
{
    private long myGte;

    private long myLt;

    private final String myFormat = "epoch_millis";

    public Timefield(long gte, long lt)
    {
        myGte = gte;
        myLt = lt;
    }

    /**
     * Gets the gte.
     *
     * @return the gte
     */
    public long getGte()
    {
        return myGte;
    }

    /**
     * Sets the gte.
     *
     * @param gte the gte
     */
    public void setGte(long gte)
    {
        myGte = gte;
    }

    /**
     * Gets the lt.
     *
     * @return the lt
     */
    public long getLt()
    {
        return myLt;
    }

    /**
     * Sets the lt.
     *
     * @param lt the lt
     */
    public void setLt(long lt)
    {
        myLt = lt;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat()
    {
        return myFormat;
    }
}
