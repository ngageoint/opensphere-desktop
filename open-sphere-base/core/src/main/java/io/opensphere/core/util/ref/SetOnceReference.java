package io.opensphere.core.util.ref;

/**
 * A reference that can only be set once.
 *
 * @param <T> The type of the object.
 */
public class SetOnceReference<T> extends AbstractReference<T>
{
    /** The object. */
    private volatile T myObject;

    /**
     * Construct the reference with no value.
     */
    public SetOnceReference()
    {
    }

    /**
     * Construct the reference with an object.
     *
     * @param obj The object being referenced.
     */
    public SetOnceReference(T obj)
    {
        myObject = obj;
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("Cannot clear a set-once reference.");
    }

    /**
     * Accessor for the object. This returns <code>null</code> until an object
     * is set.
     *
     * @return The object.
     */
    @Override
    public T get()
    {
        return myObject;
    }

    /**
     * Set the referenced object. If successful, this will notify all threads
     * waiting on this object's monitor.
     *
     * @param ref The object.
     * @throws IllegalArgumentException If the argument is <code>null</code>.
     * @throws IllegalStateException If the object has already been set.
     */
    public void set(T ref) throws IllegalArgumentException, IllegalStateException
    {
        if (ref == null)
        {
            throw new IllegalArgumentException("Cannot set a " + SetOnceReference.class.getSimpleName() + " to null.");
        }
        synchronized (this)
        {
            if (myObject == null)
            {
                myObject = ref;
                notifyAll();
            }
            else
            {
                throw new IllegalStateException("Cannot set a " + SetOnceReference.class.getSimpleName() + " more than once.");
            }
        }
    }
}
