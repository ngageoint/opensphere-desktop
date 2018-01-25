package io.opensphere.core.util.concurrent;

import java.util.concurrent.locks.Lock;

import io.opensphere.core.util.lang.QuietCloseable;

/**
 * Wrapper for a {@link Lock} with a {@link #lock()} method that returns an
 * {@link AutoCloseable} that unlocks the lock. This is suitable for use in a
 * try-with-resources block.
 */
public class LockWrapper
{
    /** The wrapped lock. */
    private final Lock myLock;

    /** The unlocker. */
    private final QuietCloseable myUnlocker;

    /**
     * Constructor.
     *
     * @param lock The lock to wrap
     */
    public LockWrapper(Lock lock)
    {
        myLock = lock;
        myUnlocker = () -> myLock.unlock();
    }

    /**
     * Acquires the lock.
     *
     * @return an {@link AutoCloseable} that unlocks the lock
     */
    public QuietCloseable lock()
    {
        myLock.lock();
        return myUnlocker;
    }
}
