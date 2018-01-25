package io.opensphere.core.util.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;

/**
 * Pairing of a lock and a condition created using the lock.
 */
public class LockCondition
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LockCondition.class);

    /** The condition. */
    private final Condition myCondition;

    /** The lock. */
    private final Lock myLock;

    /**
     * Construct the lock condition.
     *
     * @param lock The lock.
     */
    public LockCondition(Lock lock)
    {
        myLock = Utilities.checkNull(lock, "lock");
        myCondition = lock.newCondition();
    }

    /**
     * Lock the lock and wait on the condition.
     */
    public void await()
    {
        myLock.lock();
        try
        {
            while (true)
            {
                try
                {
                    myCondition.await();
                    break;
                }
                catch (InterruptedException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(e, e);
                    }
                }
            }
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Get the condition.
     *
     * @return The condition.
     */
    public Condition getCondition()
    {
        return myCondition;
    }

    /**
     * Get the lock.
     *
     * @return The lock.
     */
    public Lock getLock()
    {
        return myLock;
    }

    /**
     * Lock the lock and signal all threads waiting on the condition.
     */
    public void signalAll()
    {
        myLock.lock();
        try
        {
            myCondition.signalAll();
        }
        finally
        {
            myLock.unlock();
        }
    }
}
