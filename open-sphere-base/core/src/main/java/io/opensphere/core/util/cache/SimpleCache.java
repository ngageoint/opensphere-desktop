package io.opensphere.core.util.cache;

import java.util.Map;
import java.util.function.Function;

/**
 * A simple generic cache.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class SimpleCache<K, V> implements Function<K, V>
{
    /** The cache map. */
    private final Map<K, V> myCacheMap;

    /** The look up function. */
    private final Function<K, V> myLookupFunction;

//    /** The instrumentation. */
//    private final CacheInstrumentation myInstrumentation = new CacheInstrumentation();

    /**
     * Constructor.
     *
     * @param cacheMap the cache map
     * @param lookupFunction the loop up function
     */
    public SimpleCache(Map<K, V> cacheMap, Function<K, V> lookupFunction)
    {
        myCacheMap = cacheMap;
        myLookupFunction = lookupFunction;
    }

    @Override
    public V apply(K key)
    {
//        myInstrumentation.start();

        V value = myCacheMap.get(key);
        if (value == null)
        {
            value = myLookupFunction.apply(key);
            myCacheMap.put(key, value);

//            myInstrumentation.miss();
        }
//        else
//        {
//            myInstrumentation.hit();
//        }

        return value;
    }

    /**
     * Clears the cache.
     */
    public void clear()
    {
        myCacheMap.clear();
    }

    /**
     * Invalidates the given key.
     *
     * @param key the key
     */
    public void invalidate(K key)
    {
        myCacheMap.remove(key);
    }
}
