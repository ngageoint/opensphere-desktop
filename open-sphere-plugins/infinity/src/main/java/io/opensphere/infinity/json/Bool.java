package io.opensphere.infinity.json;

/** Elasticsearch bool JSON bean. */
public class Bool
{
    /** The must. */
    private Object[] myMust;

    /**
     * Gets the must.
     *
     * @return the must
     */
    public Object[] getMust()
    {
        return myMust;
    }

    /**
     * Sets the must.
     *
     * @param must the must
     */
    public void setMust(Object[] must)
    {
        myMust = must;
    }
}
