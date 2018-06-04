package io.opensphere.infinity.json;

/** Elasticsearch range JSON bean. */
public class Range
{
    /** The timefield. */
    private Timefield myTimefield;

    /**
     * Constructor.
     *
     * @param gte the gte
     * @param lt the lt
     */
    public Range(long gte, long lt)
    {
        myTimefield = new Timefield(gte, lt);
    }

    /**
     * Gets the timefield.
     *
     * @return the timefield
     */
    public Timefield getTimefield()
    {
        return myTimefield;
    }

    /**
     * Sets the timefield.
     *
     * @param timefield the timefield
     */
    public void setTimefield(Timefield timefield)
    {
        myTimefield = timefield;
    }
}
