package io.opensphere.core.util.callout;

/**
 * The callout key that contains the callout key and an index of the callout.
 *
 * @param <E> The type of the key.
 */
public class CalloutKey<E>
{
    /**
     * The key object.
     */
    private final E myKey;

    /**
     * The index of the callout.
     */
    private final int myIndex;

    /**
     * Constructs a new callout key.
     *
     * @param key The key.
     * @param index The index of the callout.
     */
    public CalloutKey(E key, int index)
    {
        myKey = key;
        myIndex = index;
    }

    /**
     * The index of the callout.
     *
     * @return The index of the callout.
     */
    public int getIndex()
    {
        return myIndex;
    }

    /**
     * The callout key.
     *
     * @return The callout key.
     */
    public E getKey()
    {
        return myKey;
    }
}
