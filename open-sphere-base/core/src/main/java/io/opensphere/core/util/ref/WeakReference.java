package io.opensphere.core.util.ref;

/**
 * Wrapper for {@link java.lang.ref.WeakReference} that implements
 * {@link Reference}.
 *
 * @param <T> The type of the referent.
 */
public class WeakReference<T> extends AbstractReference<T>
{
    /**
     * The wrapped weak reference.
     */
    private final java.lang.ref.WeakReference<T> myReference;

    /**
     * Creates a new weak reference that refers to the given object. The new
     * reference is not registered with any queue.
     *
     * @param referent object the new weak reference will refer to
     */
    public WeakReference(T referent)
    {
        myReference = new java.lang.ref.WeakReference<>(referent);
    }

    /**
     * Creates a new weak reference that refers to the given object and is
     * registered with the given queue.
     *
     * @param referent object the new weak reference will refer to
     * @param q the queue with which the reference is to be registered, or
     *            <tt>null</tt> if registration is not required
     *
     */
    public WeakReference(T referent, ReferenceQueue<? super T> q)
    {
        myReference = new WeakReferenceExtension(referent, q);
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
     * Weak reference extension that allows linking from the java.lang reference
     * back to the OpenSphere reference.
     */
    private final class WeakReferenceExtension extends java.lang.ref.WeakReference<T> implements ReferenceProvider<T>
    {
        /**
         * Construct the reference extension.
         *
         * @param referent The referent.
         * @param q The queue.
         */
        public WeakReferenceExtension(T referent, ReferenceQueue<? super T> q)
        {
            super(referent, q.getReferenceQueue());
        }

        @Override
        public Reference<T> getReference()
        {
            return WeakReference.this;
        }
    }
}
