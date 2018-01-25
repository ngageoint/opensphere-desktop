package io.opensphere.core.util.collections;

import java.util.HashMap;
import java.util.Map;

/**
 * A map that tries to minimize its memory footprint by replacing its map
 * implementation based on the number of mappings it contains.
 *
 * @param <K> The type of the keys in the map.
 * @param <V> The type of the values in the map.
 */
public class CompactMap<K, V> extends AbstractProxyMap<K, V>
{
    /** The wrapped map. */
    private Map<K, V> myMap;

    /**
     * Default constructor.
     */
    public CompactMap()
    {
    }

    /**
     * Constructor to be used when the number of items in the map can be
     * estimated.
     *
     * @param expectedSize The expected size of the map.
     */
    public CompactMap(int expectedSize)
    {
        if (expectedSize > 1)
        {
            myMap = getMapProvider().get(expectedSize);
        }
    }

    @Override
    public synchronized void clear()
    {
        myMap = getMapProvider().getEmpty();
    }

    @Override
    public synchronized V put(K key, V value)
    {
        V oldValue;
        if ((myMap == null || myMap.isEmpty()) && !(myMap instanceof HashMap))
        {
            myMap = getMapProvider().getSingleton(key, value);
            oldValue = null;
        }
        else if (myMap.size() == 1 && !(myMap instanceof HashMap))
        {
            Map<K, V> map = getMapProvider().get(2);
            map.putAll(myMap);
            oldValue = map.put(key, value);
            if (map.size() > 1)
            {
                myMap = map;
            }
            else
            {
                myMap = getMapProvider().getSingleton(key, value);
            }
        }
        else
        {
            oldValue = myMap.put(key, value);
        }
        return oldValue;
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> m)
    {
        if (!m.isEmpty())
        {
            if (myMap == null || myMap.isEmpty())
            {
                if (m.size() == 1)
                {
                    Entry<? extends K, ? extends V> entry = m.entrySet().iterator().next();
                    myMap = getMapProvider().getSingleton(entry.getKey(), entry.getValue());
                }
                else
                {
                    myMap = getMapProvider().get(m);
                }
            }
            else if (myMap.size() == 1)
            {
                Map<K, V> map = getMapProvider().get(m.size() + 1);
                map.putAll(myMap);
                map.putAll(m);

                if (map.size() > 1)
                {
                    myMap = map;
                }
                else
                {
                    Map.Entry<K, V> entry = map.entrySet().iterator().next();
                    myMap = getMapProvider().getSingleton(entry.getKey(), entry.getValue());
                }
            }
            else
            {
                myMap.putAll(m);
            }
        }
    }

    @Override
    public synchronized V remove(Object key)
    {
        V value;
        if (myMap == null || myMap.isEmpty())
        {
            value = null;
        }
        else if (myMap.size() == 1)
        {
            value = myMap.get(key);
            if (myMap.containsKey(key))
            {
                clear();
            }
        }
        else
        {
            value = myMap.remove(key);
            if (myMap.size() == 1)
            {
                Map.Entry<K, V> entry = myMap.entrySet().iterator().next();
                myMap = getMapProvider().getSingleton(entry.getKey(), entry.getValue());
            }
        }
        return value;
    }

    @Override
    protected synchronized Map<K, V> getMap()
    {
        if (myMap == null)
        {
            myMap = getMapProvider().getEmpty();
        }
        return myMap;
    }

    /**
     * Get the map provider to be used to create my wrapped map. This may be
     * overridden by subclasses to use a different map implementation.
     *
     * @return The map provider.
     */
    protected MapProvider<K, V> getMapProvider()
    {
        return New.mapFactory();
    }
}
