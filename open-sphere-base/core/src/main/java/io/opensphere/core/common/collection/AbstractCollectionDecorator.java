package io.opensphere.core.common.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class decorates a Java <code>Collection</code>.
 */
@SuppressWarnings("serial")
public abstract class AbstractCollectionDecorator<E> implements Collection<E>, Serializable
{
    /**
     * The decorated <code>Collection</code>.
     */
    private Collection<E> collection;

    /**
     * Creates a new instance that decorates the given <code>Collection</code>.
     *
     * @param collection the <code>Collection</code> to be decorated.
     */
    public AbstractCollectionDecorator(Collection<E> collection)
    {
        this.collection = collection;
    }

    @Override
    public int size()
    {
        return getCollection().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getCollection().isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return getCollection().contains(o);
    }

    @Override
    public Iterator<E> iterator()
    {
        return getCollection().iterator();
    }

    @Override
    public Object[] toArray()
    {
        return getCollection().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return getCollection().toArray(a);
    }

    @Override
    public boolean add(E e)
    {
        return getCollection().add(e);
    }

    @Override
    public boolean remove(Object o)
    {
        return getCollection().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return getCollection().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        return getCollection().addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return getCollection().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return getCollection().retainAll(c);
    }

    @Override
    public void clear()
    {
        getCollection().clear();
    }

    /**
     * Returns the decorated collection.
     *
     * @return the decorated collection.
     */
    protected Collection<E> getCollection()
    {
        return collection;
    }
}
