package io.opensphere.core.util;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.opensphere.core.util.ref.WeakReference;

/**
 * A pool of objects, used to reduce the memory footprint of a large number of
 * duplicate objects. The objects in the pool must be immutable since they are
 * shared, and they must implement {@link Object#equals(Object)} and
 * {@link Object#hashCode()} correctly.
 *
 * @param <T> The type of object in the pool.
 */
@net.jcip.annotations.ThreadSafe
public class SharedObjectPool<T>
{
    /** Map of objects. */
    private final Map<T, WeakReference<T>> myPool = new WeakHashMap<>();

    /** Read lock. */
    private final Lock myReadLock;

    /** Write lock. */
    private final Lock myWriteLock;

    {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        myReadLock = lock.readLock();
        myWriteLock = lock.writeLock();
    }

    /**
     * If an object equal to the input object (according to the equals method of
     * the type) exists in this pool, return the object from the pool.
     * Otherwise, add the input object to the pool and return it.
     *
     * @param input The input object.
     * @return The object from the pool, or the input object if the object does
     *         not exist in the pool.
     */
    public T get(T input)
    {
        myReadLock.lock();
        try
        {
            WeakReference<T> ref = myPool.get(input);
            T poolObject = ref == null ? null : ref.get();
            if (poolObject != null)
            {
                return poolObject;
            }
        }
        finally
        {
            myReadLock.unlock();
        }

        myWriteLock.lock();
        try
        {
            WeakReference<T> ref = myPool.get(input);
            T poolObject = ref == null ? null : ref.get();
            if (poolObject == null)
            {
                myPool.put(input, new WeakReference<>(input));
                poolObject = input;
            }

            return poolObject;
        }
        finally
        {
            myWriteLock.unlock();
        }
    }

    /**
     * Remove an object from the pool.
     *
     * @param obj The object.
     */
    public void remove(T obj)
    {
        myWriteLock.lock();
        try
        {
            myPool.remove(obj);
        }
        finally
        {
            myWriteLock.unlock();
        }
    }

    /**
     * Runs a {@link PoolObjectProcedure} on the non-garbage collected instances
     * in the pool. Will stop executing if procedure returns false for any
     * execution on any individual object in the pool.
     *
     * @param procedure the procedure
     */
    public void runProcedure(PoolObjectProcedure<T> procedure)
    {
        myWriteLock.lock();
        try
        {
            for (Map.Entry<T, WeakReference<T>> entry : myPool.entrySet())
            {
                T poolObj = entry.getValue().get();
                if (poolObj != null && !procedure.procedure(poolObj))
                {
                    break;
                }
            }
        }
        finally
        {
            myWriteLock.unlock();
        }
    }

    /**
     * Get the current size of the pool.
     *
     * @return The number of objects in the pool.
     */
    public int size()
    {
        // Because WeakHashMap.size() can change the map, we need a write lock
        // here.
        myWriteLock.lock();
        try
        {
            return myPool.size();
        }
        finally
        {
            myWriteLock.unlock();
        }
    }

    @Override
    public String toString()
    {
        myReadLock.lock();
        try
        {
            return new StringBuilder(128).append(SharedObjectPool.class.getSimpleName()).append(myPool.toString()).toString();
        }
        finally
        {
            myReadLock.unlock();
        }
    }

    /**
     * A procedure that can be run on all non-garbage collected objects in the
     * pool.
     *
     * @param <T> the pool object type.
     */
    @FunctionalInterface
    public interface PoolObjectProcedure<T>
    {
        /**
         * Procedure to invoke on all non-garbage collected pool objects.
         *
         * @param poolObject the pool object
         * @return true to keep invoking the procedure on remaining pool
         *         objects, false to stop executing the procedure.
         */
        boolean procedure(T poolObject);
    }
}
