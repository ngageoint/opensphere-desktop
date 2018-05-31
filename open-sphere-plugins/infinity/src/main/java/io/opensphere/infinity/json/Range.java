package io.opensphere.infinity.json;

public class Range
{
    private Timefield myTimefield;

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
