package io.opensphere.infinity.json;

/** Elasticsearch bucket JSON bean. */
public class Bucket<T>
{
    /** The key. */
    private T myKey;

    /** The doc count. */
    private long myDocCount;

    /**
     * Constructor.
     */
    public Bucket()
    {
    }

    /**
     * Constructor.
     *
     * @param key the key
     * @param docCount the doc count
     */
    public Bucket(T key, long docCount)
    {
        myKey = key;
        myDocCount = docCount;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public T getKey()
    {
        return myKey;
    }

    /**
     * Sets the key.
     *
     * @param key the key
     */
    public void setKey(T key)
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

    @Override
    public String toString()
    {
        return "Bucket [key=" + myKey + ", docCount=" + myDocCount + "]";
    }
}
