package io.opensphere.core.util.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper for a {@link Map} that provides a {@link Set} view of the map's
 * keys. Unlike {@link Map#keySet()}, the returned set passes all set operations
 * through to the map.
 *
 * @param <K> The type of the keys in the wrapped map.
 * @param <V> The type of the values in the wrapped map.
 */
public class WrappedMapSet<K, V> extends AbstractSet<K>
{
    /** The wrapped map. */
    private final Map<K, V> myMap;

    /** The value to be used for add operations. */
    private final V myValue;

    /**
     * Constructor.
     *
     * @param map The wrapped map.
     * @param value The value to be used for any add operations. This may be
     *            {@code null} if the map supports {@code null} values.
     */
    public WrappedMapSet(Map<K, V> map, V value)
    {
        myMap = map;
        myValue = value;
    }

    @Override
    public boolean add(K e)
    {
        if (myMap.containsKey(e))
        {
            return false;
        }
        else
        {
            myMap.put(e, myValue);
            return true;
        }
    }

    @Override
    public void clear()
    {
        myMap.clear();
    }

    @Override
    public boolean contains(Object o)
    {
        return myMap.containsKey(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return myMap.keySet().containsAll(c);
    }

    @Override
    public boolean isEmpty()
    {
        return myMap.isEmpty();
    }

    @Override
    public Iterator<K> iterator()
    {
        return myMap.keySet().iterator();
    }

    @Override
    public boolean remove(Object o)
    {
        return myMap.keySet().remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return myMap.keySet().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return myMap.keySet().retainAll(c);
    }

    @Override
    public int size()
    {
        return myMap.size();
    }
}
