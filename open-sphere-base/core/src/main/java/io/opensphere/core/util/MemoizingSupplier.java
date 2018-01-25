package io.opensphere.core.util;

import java.util.function.Supplier;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Memoizing supplier.
 *
 * @param <T> the type of results supplied by this supplier
 */
@ThreadSafe
public class MemoizingSupplier<T> implements Supplier<T>
{
    /** The delegate supplier. */
    private final Supplier<T> myDelegate;

    /** The memoized value. */
    @GuardedBy("this")
    private T myValue;

    /** Whether the memoized value is valid. */
    @GuardedBy("this")
    private boolean myIsValid;

    /**
     * Constructor.
     *
     * @param delegate the delegate supplier
     */
    public MemoizingSupplier(Supplier<T> delegate)
    {
        myDelegate = delegate;
    }

    @Override
    public synchronized T get()
    {
        if (!myIsValid)
        {
            myValue = myDelegate.get();
            myIsValid = true;
        }
        return myValue;
    }

    /**
     * Invalidates the memoized value.
     */
    public synchronized void invalidate()
    {
        myIsValid = false;
    }
}
