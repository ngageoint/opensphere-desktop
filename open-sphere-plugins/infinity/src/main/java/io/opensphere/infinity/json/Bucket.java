package io.opensphere.infinity.json;

import org.codehaus.jackson.annotate.JsonProperty;

/** Elasticsearch bucket JSON bean.
 *
 * @param <T> Type contained in buckets
 */
public class Bucket<T>
{
    /** The key. */
    private T myKey;

    /** The doc count. */
    private long myDocCount;

    /** The key as a string.  Used for Dates. */
    @JsonProperty("key_as_string")
    private String myKeyAsString;

    /**
     * Constructor.
     */
    public Bucket()
    {
        //Intentionally left blank
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
     * Constructor.
     *
     * @param key the key
     * @param docCount the doc count
     * @param keyAsString the key as a String
     */
    public Bucket(T key, long docCount, String keyAsString)
    {
        myKey = key;
        myDocCount = docCount;
        myKeyAsString = keyAsString;
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

    /**
     * Get the key as a string.
     *
     * @return the keyAsString
     */
    public String getKey_As_String()
    {
        return myKeyAsString;
    }

    /**
     * Set the key as a string;
     *
     * @param keyAsString the keyAsString to set
     */
    public void setKey_As_String(String keyAsString)
    {
        myKeyAsString = keyAsString;
    }

    @Override
    public String toString()
    {
        return "Bucket [key=" + myKey.toString() + ", docCount=" + myDocCount + ", keyAsString=" + myKeyAsString + "]";
    }
}
