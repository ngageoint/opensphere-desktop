package io.opensphere.core.util.cache;

import java.util.Map;
import java.util.function.IntFunction;

/**
 * A simple generic cache with an integer key.
 *
 * @param <V> the value type
 */
public class SimpleIntCache<V> implements IntFunction<V>
{
    /** The cache map. */
    private final Map<Integer, V> myCacheMap;

    /** The look up function. */
    private final IntFunction<V> myLookupFunction;

//    /** The instrumentation. */
//    private final CacheInstrumentation myInstrumentation = new CacheInstrumentation();

    /**
     * Constructor.
     *
     * @param cacheMap the cache map
     * @param lookupFunction the loop up function
     */
    public SimpleIntCache(Map<Integer, V> cacheMap, IntFunction<V> lookupFunction)
    {
        myCacheMap = cacheMap;
        myLookupFunction = lookupFunction;
    }

    @Override
    public V apply(int key)
    {
//        myInstrumentation.start();

        Integer wrapperKey = Integer.valueOf(key);
        V value = myCacheMap.get(wrapperKey);
        if (value == null)
        {
            value = myLookupFunction.apply(key);
            myCacheMap.put(wrapperKey, value);

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
    public void invalidate(int key)
    {
        myCacheMap.remove(Integer.valueOf(key));
    }

    /**
     * Re-maps the value at the given key to the value at newValueKey.
     *
     * @param key the key
     * @param newValueKey the key who's value to use
     */
    public void remap(int key, int newValueKey)
    {
        myCacheMap.put(Integer.valueOf(key), myCacheMap.get(Integer.valueOf(newValueKey)));
    }
}
