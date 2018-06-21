package io.opensphere.core.util.collections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.opensphere.core.util.collections.LimitedFertilityBlockingQueue.Factory;
import net.jcip.annotations.ThreadSafe;

/**
 * A pool that can create objects according to a key as necessary up to a limit
 * and allows returning objects to the pool for use by other clients.
 *
 * @param <K> The type of the key that determines what kind of value can be
 *            returned.
 * @param <V> The type of the value.
 */
@ThreadSafe
public class MappedObjectPool<K, V>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MappedObjectPool.class);

    /**
     * Atomic integer that keeps track of how many threads are waiting for a
     * value.
     */
    private static final AtomicInteger ourWaitingCount = new AtomicInteger();

    /** Runnable used to clear the pool. */
    private final Runnable myCleanupRunnable;

    /** Optional executor to use for clearing the pool. */
    @Nullable
    private final Executor myExecutor;

    /** Lazy map of keys to fertile queues of values. */
    private final Map<K, LimitedFertilityBlockingQueue<V>> myMap;

    /**
     * Constructor.
     *
     * @param keyType The type of the key in the pool.
     * @param factory The factory for values.
     * @param coreSize The number of values that will be maintained per key in
     *            the pool.
     * @param maxSize The limit on the number of values that can be created per
     *            key.
     * @param cleanupExecutor Optional executor to use for clearing the pool. If
     *            no executor is provided, the pool will not be cleared
     *            automatically. A {@code ProcrastinatingExecutor} is
     *            recommended to avoid clearing the pool too often.
     */
    public MappedObjectPool(Class<K> keyType, final LazyMap.Factory<? super K, ? extends V> factory, final int coreSize,
            final int maxSize, @Nullable Executor cleanupExecutor)
    {
        myMap = LazyMap.create(Collections.synchronizedMap(New.<K, LimitedFertilityBlockingQueue<V>>map()), keyType,
                new LazyMap.Factory<K, LimitedFertilityBlockingQueue<V>>()
                {
                    @Override
                    public LimitedFertilityBlockingQueue<V> create(final K key)
                    {
                        return new LimitedFertilityBlockingQueue<V>(coreSize, maxSize, adaptFactory(factory, key));
                    }
                });
        myExecutor = cleanupExecutor;
        myCleanupRunnable = myExecutor == null ? null : new Runnable()
        {
            @Override
            public void run()
            {
                if (!clearIfNoWaiting())
                {
                    myExecutor.execute(this);
                }
            }
        };
    }

    /**
     * If no threads are waiting for a value, clear the pool.
     *
     * @return {@code true} if the pool was cleared.
     */
    public boolean clearIfNoWaiting()
    {
        if (ourWaitingCount.get() == 0)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Clearing pool because no threads are waiting.");
                synchronized (myMap)
                {
                    for (Entry<K, LimitedFertilityBlockingQueue<V>> entry : myMap.entrySet())
                    {
                        LOGGER.trace("Clearing key: " + entry.getKey() + " pool size: " + entry.getValue().size());
                    }
                }
            }
            myMap.clear();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Take an object from the pool if one is available. If one is not
     * available, return {@code null}.
     *
     * @param key The key.
     * @return The value or {@code null}.
     */
    public V poll(K key)
    {
        ourWaitingCount.incrementAndGet();
        try
        {
            return getQueue(key).poll();
        }
        finally
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(key + " queue size after poll: " + getQueue(key).size());
            }
            ourWaitingCount.decrementAndGet();
            if (myExecutor != null)
            {
                myExecutor.execute(myCleanupRunnable);
            }
        }
    }

    /**
     * Take an object from the pool if one is available. If one is not
     * available, return {@code null}.
     *
     * @param key The key.
     * @param factory The factory to use to generate an object.
     * @return The value or {@code null}.
     */
    public V poll(K key, LazyMap.Factory<? super K, ? extends V> factory)
    {
        return poll(key, factory, (Consumer<V>)null);
    }

    /**
     * Take an object from the pool if one is available. If one is not
     * available, return {@code null}.
     *
     * @param key The key.
     * @param factory The factory to use to generate an object.
     * @param adapter Optional adapter to be applied to objects returned from
     *            the queue that are not newly created.
     * @return The value or {@code null}.
     */
    public V poll(K key, LazyMap.Factory<? super K, ? extends V> factory, Consumer<? super V> adapter)
    {
        ourWaitingCount.incrementAndGet();
        try
        {
            return getQueue(key).poll(adaptFactory(factory, key), adapter);
        }
        finally
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(key + " queue size after poll: " + getQueue(key).size());
            }
            ourWaitingCount.decrementAndGet();
            if (myExecutor != null)
            {
                myExecutor.execute(myCleanupRunnable);
            }
        }
    }

    /**
     * Surrender a value back to the pool.
     *
     * @param key The key.
     * @param value The value.
     */
    public void surrender(K key, V value)
    {
        if (getQueue(key).offer(value))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(key + " queue size after surrender: " + getQueue(key).size());
            }
        }
        else if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Discarding surrendered value because the queue is full: key[" + key + "] value[" + value + "]");
        }
    }

    /**
     * Take an object from the pool if one is available. If one is not
     * available, block until one becomes available or the thread is
     * interrupted.
     *
     * @param key The key.
     * @return The value.
     * @throws InterruptedException If interrupted while waiting.
     */
    public V take(K key) throws InterruptedException
    {
        ourWaitingCount.incrementAndGet();
        try
        {
            return getQueue(key).take();
        }
        finally
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(key + " queue size after take: " + getQueue(key).size());
            }
            ourWaitingCount.decrementAndGet();
            if (myExecutor != null)
            {
                myExecutor.execute(myCleanupRunnable);
            }
        }
    }

    /**
     * Take an object from the pool if one is available. If one is not
     * available, block until one becomes available or the thread is
     * interrupted.
     *
     * @param key The key.
     * @param factory The factory to use to generate an object.
     * @return The value.
     * @throws InterruptedException If interrupted while waiting.
     */
    public V take(K key, LazyMap.Factory<? super K, ? extends V> factory) throws InterruptedException
    {
        return take(key, factory, (Consumer<V>)null);
    }

    /**
     * Take an object from the pool if one is available. If one is not
     * available, block until one becomes available or the thread is
     * interrupted.
     *
     * @param key The key.
     * @param factory The factory to use to generate an object.
     * @param adapter Optional adapter to be applied to objects returned from
     *            the queue that are not newly created.
     * @return The value.
     * @throws InterruptedException If interrupted while waiting.
     */
    public V take(K key, LazyMap.Factory<? super K, ? extends V> factory, Consumer<? super V> adapter) throws InterruptedException
    {
        ourWaitingCount.incrementAndGet();
        try
        {
            return getQueue(key).take(adaptFactory(factory, key), adapter);
        }
        finally
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(key + " queue size after take: " + getQueue(key).size());
            }
            ourWaitingCount.decrementAndGet();
            if (myExecutor != null)
            {
                myExecutor.execute(myCleanupRunnable);
            }
        }
    }

    /**
     * Convert a {@link LazyMap.Factory} to a
     * {@link LimitedFertilityBlockingQueue.Factory}.
     *
     * @param factory The map factory.
     * @param key The key.
     * @return The queue factory.
     */
    protected Factory<V> adaptFactory(final LazyMap.Factory<? super K, ? extends V> factory, final K key)
    {
        return new LimitedFertilityBlockingQueue.Factory<V>()
        {
            /**
             * Serial version UID.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public V create()
            {
                return factory.create(key);
            }

            /**
             * Read the object from a stream.
             *
             * @param in The input stream.
             * @throws IOException If there's a problem reading the object from
             *             the stream.
             * @throws ClassNotFoundException If the object class cannot be
             *             loaded.
             */
            private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
            {
                throw new UnsupportedOperationException();
            }

            /**
             * Write the object to a stream.
             *
             * @param out The output stream.
             * @throws IOException If there's a problem writing the object.
             */
            private void writeObject(ObjectOutputStream out) throws IOException
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Get the queue for a key.
     *
     * @param key The key.
     * @return The queue.
     */
    @NonNull
    protected LimitedFertilityBlockingQueue<V> getQueue(K key)
    {
        return myMap.get(key);
    }
}
