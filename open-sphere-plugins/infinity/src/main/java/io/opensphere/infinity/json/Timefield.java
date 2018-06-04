package io.opensphere.infinity.json;

/** Elasticsearch timefield JSON bean. */
public class Timefield
{
    /** The gte. */
    private long myGte;

    /** The lt. */
    private long myLt;

    /** The format. */
    private final String myFormat = "epoch_millis";

    /**
     * Constructor.
     *
     * @param gte the gte
     * @param lt the lt
     */
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
