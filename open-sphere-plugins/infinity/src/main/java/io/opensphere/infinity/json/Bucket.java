package io.opensphere.infinity.json;

public class Bucket
{
    private String myKey;

    private long myDocCount;

    public Bucket()
    {
    }

    public Bucket(String key, long docCount)
    {
        myKey = key;
        myDocCount = docCount;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey()
    {
        return myKey;
    }

    /**
     * Sets the key.
     *
     * @param key the key
     */
    public void setKey(String key)
    {
        myKey = key;
    }

    /**
     * Gets the docCount.
     *
     * @return the docCount
     */
    public long getDoc_count()
    {
        return myDocCount;
    }

    /**
     * Sets the docCount.
     *
     * @param docCount the docCount
     */
    public void setDoc_count(long docCount)
    {
        myDocCount = docCount;
    }
}
