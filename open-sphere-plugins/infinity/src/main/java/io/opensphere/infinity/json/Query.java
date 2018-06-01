package io.opensphere.infinity.json;

/** Elasticsearch query JSON bean. */
public class Query
{
    /** The bool. */
    private Bool myBool = new Bool();

    /**
     * Gets the bool.
     *
     * @return the bool
     */
    public Bool getBool()
    {
        return myBool;
    }

    /**
     * Sets the bool.
     *
     * @param bool the bool
     */
    public void setBool(Bool bool)
    {
        myBool = bool;
    }
}
