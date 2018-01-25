package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.opensphere.core.util.Utilities;

/**
 * The Class FixedSizeBufferMap.
 *
 * A Map that can contain only so many entries. If the map size reaches the
 * buffer size any further additions to the map will cause the oldest entry to
 * be removed.
 *
 * Replacing an existing entry will not cause any dropping of oldest entries,
 * and the updated entry will be marked as the newest in the map.
 *
 * Note: Removes from the map will not be terribly efficient. And also that
 * using the putAll function will only act as a macro for calling put so that
 * the last N values ( where N is the buffer size ) will remain in the buffer.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class FixedSizeBufferMap<K, V> implements Map<K, V>
{
    /** The my buffer map. */
    private final Map<K, V> myBufferMap;

    /** The my key list. */
    private final Deque<K> myKeyList;

    /** The my buffer size. */
    private final int myBufferSize;

    /**
     * Instantiates a new fixed size buffer map.
     *
     * @param bufferSize the buffer size
     */
    public FixedSizeBufferMap(int bufferSize)
    {
        if (bufferSize < 1)
        {
            throw new IllegalArgumentException("Buffer Size " + bufferSize + " is invalid, must be one or greater.");
        }
        myBufferSize = bufferSize;
        myKeyList = new LinkedList<>();
        myBufferMap = new ConcurrentHashMap<>();
    }

    @Override
    public void clear()
    {
        synchronized (myKeyList)
        {
            myKeyList.clear();
        }
        myBufferMap.clear();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return myBufferMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return myBufferMap.containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet()
    {
        return myBufferMap.entrySet();
    }

    @Override
    public V get(Object key)
    {
        return myBufferMap.get(key);
    }

    /**
     * Gets the buffer size.
     *
     * @return the buffer size
     */
    public int getBufferSize()
    {
        return myBufferSize;
    }

    @Override
    public boolean isEmpty()
    {
        return myBufferMap.isEmpty();
    }

    @Override
    public Set<K> keySet()
    {
        return myBufferMap.keySet();
    }

    @Override
    public V put(K key, V value)
    {
        V previousValue = myBufferMap.get(key);

        // If re-inserting the same object then stop the put as it is already
        // up to date.
        if (previousValue != null && Utilities.sameInstance(value, previousValue))
        {
            return value;
        }

        synchronized (myKeyList)
        {
            if (previousValue != null)
            {
                // Remove the old key, note this may not be
                // terribly efficient as this is a linked list.
                myKeyList.remove(key);
            }

            int size = myKeyList.size();
            if (size == myBufferSize)
            {
                K oldest = myKeyList.removeFirst();
                myBufferMap.remove(oldest);
            }

            myKeyList.add(key);
        }
        if (value != null)
        {
            myBufferMap.put(key, value);
        }
        else
        {
            myBufferMap.remove(key);
        }
        return previousValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        Utilities.checkNull(m, "m");
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key)
    {
        V removed = myBufferMap.remove(key);
        if (removed != null)
        {
            synchronized (myKeyList)
            {
                myKeyList.remove(key);
            }
        }
        return removed;
    }

    @Override
    public int size()
    {
        return myBufferMap.size();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        synchronized (myKeyList)
        {
            Iterator<K> keyIter = myKeyList.iterator();
            K key = null;
            V value = null;
            while (keyIter.hasNext())
            {
                key = keyIter.next();
                value = myBufferMap.get(key);
                sb.append(key).append('=').append(value == null ? "null" : value.toString());
                if (keyIter.hasNext())
                {
                    sb.append(',');
                }
            }
        }
        sb.append('}');

        return sb.toString();
    }

    @Override
    public Collection<V> values()
    {
        return myBufferMap.values();
    }
}
