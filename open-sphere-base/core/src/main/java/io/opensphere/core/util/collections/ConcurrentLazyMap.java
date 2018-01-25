package io.opensphere.core.util.collections;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.ReferenceQueue;
import io.opensphere.core.util.ref.WeakReference;

/**
 * A {@link LazyMap} that maintains locks per key such that two requests for two
 * different keys will not block each other, but two requests for the same key
 * will still get the same value. The wrapped map must handle its own
 * synchronization.
 * <p>
 * Null values are treated the same as non-existent values.
 * <p>
 * If a value is inserted into the map externally at the same time a get
 * operation is performed on the lazy map, it is undefined which value the get
 * operation will return.
 *
 * @param <K> The type of the keys in the map.
 * @param <V> The type of the values in the map.
 */
public class ConcurrentLazyMap<K, V> extends LazyMap<K, V>
{
    /**
     * The reference queue populated with locks that are no longer referenced.
     */
    private final ReferenceQueue<Lock> myLockReferenceQueue = new ReferenceQueue<>();

    /** Map of keys to locks. */
    private final Map<K, Reference<Lock>> myLocks;

    /**
     * Static construction method.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The wrapped map.
     * @param keyType The type of the keys in the map.
     * @return The new map.
     */
    public static <K, V> ConcurrentLazyMap<K, V> create(Map<K, V> map, Class<? extends K> keyType)
    {
        return new ConcurrentLazyMap<K, V>(map, keyType);
    }

    /**
     * Static construction method.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The wrapped map.
     * @param keyType The type of the keys in the map.
     * @param factory The factory to provide new values in the map.
     * @return The new map.
     */
    public static <K, V> ConcurrentLazyMap<K, V> create(Map<K, V> map, Class<? extends K> keyType,
            Factory<? super K, ? extends V> factory)
    {
        return new ConcurrentLazyMap<K, V>(map, keyType, factory);
    }

    /**
     * Constructor.
     *
     * @param map The wrapped map, which is responsible for its own
     *            synchronization.
     * @param keyType The type of the keys in the map.
     */
    public ConcurrentLazyMap(Map<K, V> map, Class<? extends K> keyType)
    {
        this(map, keyType, null);
    }

    /**
     * Constructor.
     *
     * @param map The wrapped map, which is responsible for its own
     *            synchronization.
     * @param keyType The type of the keys in the map.
     * @param factory The factory to provide new values in the map.
     */
    public ConcurrentLazyMap(Map<K, V> map, Class<? extends K> keyType, Factory<? super K, ? extends V> factory)
    {
        super(map, keyType, factory);
        myLocks = New.weakMap();
    }

    @Override
    public V get(Object key, Factory<? super K, ? extends V> factory)
    {
        Utilities.checkNull(factory, "factory");
        if (key != null && !getKeyType().isInstance(key))
        {
            return getIfExists(key);
        }
        V value = getIfExists(key);
        if (value != null)
        {
            return value;
        }

        @SuppressWarnings("unchecked")
        K k = (K)key;

        Lock lock = getLock(k);
        lock.lock();
        try
        {
            // Check again.
            value = getIfExists(key);
            if (value == null)
            {
                value = factory.create(k);
                getMap().put(k, value);
            }
            return value;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Get the lock for a particular key. This lock must be locked if values are
     * set in the map to prevent the lazy behavior from overwriting the set
     * values.
     *
     * @param key The key.
     * @return The lock.
     */
    public Lock getLock(K key)
    {
        synchronized (getLocks())
        {
            Reference<Lock> ref = getLocks().get(key);
            Lock lock = ref == null ? null : ref.get();
            if (lock == null)
            {
                lock = new ReentrantLock();
                ref = new LockReference<>(key, lock, getLockReferenceQueue());
                getLocks().put(key, ref);
            }
            purgeStaleLockEntries();
            return lock;
        }
    }

    @Override
    public V put(K key, V value)
    {
        Lock lock = getLock(key);
        lock.lock();
        try
        {
            return getMap().put(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry<? extends K, ? extends V> e = i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Accessor for the lock reference queue.
     *
     * @return The queue.
     */
    protected ReferenceQueue<Lock> getLockReferenceQueue()
    {
        return myLockReferenceQueue;
    }

    /**
     * Accessor for the locks.
     *
     * @return The locks.
     */
    protected final Map<K, Reference<Lock>> getLocks()
    {
        return myLocks;
    }

    /**
     * Remove any locks that are no longer referenced.
     */
    protected void purgeStaleLockEntries()
    {
        LockReference<?> ref;
        while ((ref = (LockReference<?>)getLockReferenceQueue().poll()) != null)
        {
            Reference<?> keyRef = ref.getKey();
            Object key = keyRef.get();
            if (key != null)
            {
                getLocks().remove(key);
            }
        }
    }

    /**
     * Weak reference extension used for the key locks.
     *
     * @param <T> The type of the referenced key.
     */
    private static class LockReference<T> extends WeakReference<Lock>
    {
        /** The key that this lock is for. */
        private final Reference<T> myKey;

        /**
         * Constructor.
         *
         * @param key The key.
         * @param lock The lock.
         * @param queue The reference queue.
         */
        public LockReference(T key, Lock lock, ReferenceQueue<? super Lock> queue)
        {
            super(lock, queue);
            myKey = new WeakReference<>(key);
        }

        /**
         * Get the key associated with this lock.
         *
         * @return The key.
         */
        public Reference<T> getKey()
        {
            return myKey;
        }
    }
}
