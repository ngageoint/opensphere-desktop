package io.opensphere.core.util.ref;

/**
 * Wrapper for {@link java.lang.ref.SoftReference} that implements
 * {@link Reference}.
 *
 * @param <T> The type of the referent.
 */
public class SoftReference<T> extends AbstractReference<T>
{
    /**
     * The wrapped soft reference.
     */
    private final java.lang.ref.SoftReference<T> myReference;

    /**
     * Creates a new soft reference that refers to the given object. The new
     * reference is not registered with any queue.
     *
     * @param referent object the new soft reference will refer to
     */
    public SoftReference(T referent)
    {
        myReference = new java.lang.ref.SoftReference<>(referent);
    }

    /**
     * Creates a new soft reference that refers to the given object and is
     * registered with the given queue.
     *
     * @param referent object the new soft reference will refer to
     * @param q the queue with which the reference is to be registered, or
     *            <tt>null</tt> if registration is not required
     *
     */
    public SoftReference(T referent, ReferenceQueue<? super T> q)
    {
        myReference = new SoftReferenceExtension(referent, q);
    }

    @Override
    public void clear()
    {
        myReference.clear();
    }

    @Override
    public T get()
    {
        return myReference.get();
    }

    /**
     * Soft reference extension that allows linking from the java.lang reference
     * back to the OpenSphere reference.
     */
    private final class SoftReferenceExtension extends java.lang.ref.SoftReference<T> implements ReferenceProvider<T>
    {
        /**
         * Construct the reference extension.
         *
         * @param referent The referent.
         * @param q The queue.
         */
        public SoftReferenceExtension(T referent, ReferenceQueue<? super T> q)
        {
            super(referent, q.getReferenceQueue());
        }

        @Override
        public Reference<T> getReference()
        {
            return SoftReference.this;
        }
    }
}
