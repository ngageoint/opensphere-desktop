package io.opensphere.core.util.ref;

/**
 * A {@link WeakReference} that passes {@link Object#hashCode()} and
 * {@link Object#equals(Object)} through to its referent. This allows it to be
 * used as a key in hashes and sets.
 *
 * @param <T> The type of the referent.
 */
public class TransparentEqualsWeakReference<T> extends WeakReference<T>
{
    /**
     * Remember the hashCode of the referent so it is still available after the
     * referent has been cleared.
     */
    private final int myHashCode;

    /**
     * Creates a new weak reference that refers to the given object. The new
     * reference is not registered with any queue.
     *
     * @param <T> The type of the object.
     * @param referent object the new weak reference will refer to
     * @return The reference.
     */
    public static <T> TransparentEqualsWeakReference<T> create(T referent)
    {
        return new TransparentEqualsWeakReference<>(referent);
    }

    /**
     * Creates a new weak reference that refers to the given object. The new
     * reference is not registered with any queue.
     *
     * @param <T> The type of the object.
     * @param referent object the new weak reference will refer to
     * @param q the queue with which the reference is to be registered, or
     *            <tt>null</tt> if registration is not required
     * @return The reference.
     */
    public static <T> TransparentEqualsWeakReference<T> create(T referent, ReferenceQueue<? super T> q)
    {
        return new TransparentEqualsWeakReference<>(referent, q);
    }

    /**
     * Creates a new weak reference that refers to the given object. The new
     * reference is not registered with any queue.
     *
     * @param referent object the new weak reference will refer to
     */
    public TransparentEqualsWeakReference(T referent)
    {
        super(referent);
        myHashCode = referent.hashCode();
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
    public TransparentEqualsWeakReference(T referent, ReferenceQueue<? super T> q)
    {
        super(referent, q);
        myHashCode = referent.hashCode();
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        TransparentEqualsWeakReference<?> other = (TransparentEqualsWeakReference<?>)obj;
        if (myHashCode != other.myHashCode)
        {
            return false;
        }
        T referent = get();
        Object otherReferent = other.get();
        return referent == otherReferent || referent != null && referent.equals(otherReferent);
    }

    @Override
    public int hashCode()
    {
        return myHashCode;
    }
}
