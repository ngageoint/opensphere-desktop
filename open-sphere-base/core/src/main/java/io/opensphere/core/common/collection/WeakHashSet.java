package io.opensphere.core.common.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The Class WeakHashSet, basically a {@link WeakHashMap} behind a {@link Set}
 * interface.
 *
 * @param <E> the element type of set
 */
public class WeakHashSet<E> implements Set<E>
{
    /** The underlying map. */
    private WeakHashMap<E, Object> myUnderlyingMap;

    /**
     * Instantiates a new weak hash set.
     */
    public WeakHashSet()
    {
        myUnderlyingMap = new WeakHashMap<E, Object>();
    }

    @Override
    public boolean add(E e)
    {
        return myUnderlyingMap.put(e, null) == null;
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        if (c == null || c.isEmpty())
            return false;

        boolean changed = false;
        for (E thing : c)
        {
            if (add(thing))
            {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear()
    {
        myUnderlyingMap.clear();
    }

    @Override
    public boolean contains(Object o)
    {
        return myUnderlyingMap.containsKey(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        boolean hasAll = true;
        for (Object o : c)
        {
            if (!contains(o))
            {
                hasAll = false;
                break;
            }
        }
        return hasAll;
    }

    @Override
    public boolean isEmpty()
    {
        return myUnderlyingMap.isEmpty();
    }

    @Override
    public Iterator<E> iterator()
    {
        return myUnderlyingMap.keySet().iterator();
    }

    @Override
    public boolean remove(Object o)
    {
        return myUnderlyingMap.remove(o) != null;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean oneWasRemoved = false;
        for (Object o : c)
        {
            if (remove(o))
            {
                oneWasRemoved = true;
            }
        }
        return oneWasRemoved;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        HashSet<E> local = new HashSet<E>(myUnderlyingMap.keySet());
        boolean changed = local.retainAll(c);
        if (changed)
        {
            myUnderlyingMap.clear();
            addAll(local);
        }
        return changed;
    }

    @Override
    public int size()
    {
        return myUnderlyingMap.size();
    }

    @Override
    public Object[] toArray()
    {
        return myUnderlyingMap.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return myUnderlyingMap.keySet().toArray(a);
    }
}
