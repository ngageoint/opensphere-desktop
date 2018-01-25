package io.opensphere.core.util.ref;

/**
 * A reference to an object that can be changed.
 *
 * @param <T> The type of the object.
 */
public class VolatileReference<T> extends AbstractReference<T>
{
    /** The object. */
    private volatile T myObject;

    /**
     * Construct the reference with a <code>null</code> value.
     */
    public VolatileReference()
    {
    }

    /**
     * Construct the reference.
     *
     * @param obj The object being referenced.
     */
    public VolatileReference(T obj)
    {
        myObject = obj;
    }

    @Override
    public void clear()
    {
        set(null);
    }

    @Override
    public T get()
    {
        return myObject;
    }

    /**
     * Get a read-only view of this reference.
     *
     * @return A read-only view.
     */
    public VolatileReference<T> getReadOnly()
    {
        return new ReadonlyVolatileReference<T>(this);
    }

    /**
     * Set the referenced object.
     *
     * @param ref The object.
     */
    public void set(T ref)
    {
        myObject = ref;
    }

    /**
     * Read-only view of the reference.
     *
     * @param <T> The type of the object.
     */
    public static class ReadonlyVolatileReference<T> extends VolatileReference<T>
    {
        /** The protected reference. */
        private final VolatileReference<T> myRef;

        /**
         * Construct the read-only reference.
         *
         * @param ref The protected reference.
         */
        ReadonlyVolatileReference(VolatileReference<T> ref)
        {
            myRef = ref;
        }

        @Override
        public T get()
        {
            return myRef.get();
        }

        @Override
        public void set(Object ref)
        {
            throw new UnsupportedOperationException("Reference is read-only.");
        }
    }
}
