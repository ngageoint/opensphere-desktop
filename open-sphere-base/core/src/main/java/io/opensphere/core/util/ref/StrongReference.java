package io.opensphere.core.util.ref;

/**
 * A strong reference (one that will never be automatically cleared).
 *
 * @param <T> The type of the referent.
 */
public class StrongReference<T> extends AbstractReference<T>
{
    /**
     * The referent.
     */
    private final T myReference;

    /**
     * Creates a new strong reference that refers to the given object.
     *
     * @param referent object the new strong reference will refer to
     */
    public StrongReference(T referent)
    {
        myReference = referent;
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("A strong reference cannot be cleared.");
    }

    @Override
    public T get()
    {
        return myReference;
    }
}
