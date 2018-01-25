package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * A set implementation that proxies set calls through to the set provided by
 * the subclass.
 *
 * @param <E> The type of the elements in the set.
 */
public abstract class AbstractProxySet<E> implements Set<E>
{
    @Override
    public boolean add(E e)
    {
        return getSet().add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        return getSet().addAll(c);
    }

    @Override
    public void clear()
    {
        getSet().clear();
    }

    @Override
    public boolean contains(Object o)
    {
        return getSet().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return getSet().containsAll(c);
    }

    @Override
    public boolean isEmpty()
    {
        return getSet().isEmpty();
    }

    @Override
    public Iterator<E> iterator()
    {
        return getSet().iterator();
    }

    @Override
    public boolean remove(Object o)
    {
        return getSet().remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return getSet().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return getSet().retainAll(c);
    }

    @Override
    public int size()
    {
        return getSet().size();
    }

    @Override
    public Object[] toArray()
    {
        return getSet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return getSet().toArray(a);
    }

    /**
     * Accessor for the set.
     *
     * @return The set.
     */
    protected abstract Set<E> getSet();
}
