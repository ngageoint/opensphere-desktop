package io.opensphere.core.util;

/**
 * A service associated with a reference.
 *
 * @param <T> The type of the reference.
 */
public abstract class ReferenceService<T> implements Service
{
    /** The reference. */
    private final T myReference;

    /**
     * Constructor.
     *
     * @param reference The reference.
     */
    public ReferenceService(T reference)
    {
        myReference = reference;
    }

    /**
     * Get the reference.
     *
     * @return The reference.
     */
    public T getReference()
    {
        return myReference;
    }
}
