package io.opensphere.core.common.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class decorates another <code>List</code> that prevents the addition or
 * removal of members. This class is different from an immutable
 * <code>List</code> in that it allows changes to existing members.
 */
@SuppressWarnings("serial")
public class FixedSizeList<E> extends AbstractListDecorator<E>
{
    public FixedSizeList(List<E> list)
    {
        super(list);
    }

    /**
     * Throws an exception.
     *
     * @see io.opensphere.core.common.collection.AbstractListDecorator#addAll(int,
     *      java.util.Collection)
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c)
    {
        throw new UnsupportedOperationException("The List is fixed size");
    }

    /**
     * Throws an exception.
     *
     * @see io.opensphere.core.common.collection.AbstractListDecorator#add(int,
     *      java.lang.Object)
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    public void add(int index, E element)
    {
        throw new UnsupportedOperationException("The List is fixed size");
    }

    /**
     * Throws an exception.
     *
     * @see io.opensphere.core.common.collection.AbstractListDecorator#remove(int)
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    public E remove(int index)
    {
        throw new UnsupportedOperationException("The List is fixed size");
    }

    /**
     * Returns an iterator that throws an exception is any method that modifies
     * the underlying list.
     *
     * @see io.opensphere.core.common.collection.AbstractListDecorator#listIterator()
     */
    @Override
    public ListIterator<E> listIterator()
    {
        return new AbstractListIteratorDecorator<E>(super.listIterator())
        {
            @Override
            public void add(E e)
            {
                throw new UnsupportedOperationException("The List is fixed size");
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("The List is fixed size");
            }
        };
    }

    /**
     * Returns an iterator that throws an exception is any method that modifies
     * the underlying list.
     *
     * @see io.opensphere.core.common.collection.AbstractListDecorator#listIterator(int)
     */
    @Override
    public ListIterator<E> listIterator(int index)
    {
        return new AbstractListIteratorDecorator<E>(super.listIterator(index))
        {
            @Override
            public void add(E e)
            {
                throw new UnsupportedOperationException("The List is fixed size");
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("The List is fixed size");
            }
        };
    }

    /**
     *
     * @see io.opensphere.core.common.collection.AbstractListDecorator#subList(int,
     *      int)
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex)
    {
        return new FixedSizeList<>(super.subList(fromIndex, toIndex));
    }

    /**
     * Returns an iterator that throws an exception if the <code>remove</code>
     * method is invoked.
     *
     * @see io.opensphere.core.common.collection.AbstractCollectionDecorator#iterator()
     */
    @Override
    public Iterator<E> iterator()
    {
        return new AbstractIteratorDecorator<E>(super.iterator())
        {
            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("The List is fixed size");
            }
        };
    }

    /**
     * Throws an exception.
     *
     * @see io.opensphere.core.common.collection.AbstractCollectionDecorator#add(java.lang.Object)
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    public boolean add(E e)
    {
        throw new UnsupportedOperationException("The List is fixed size");
    }

    /**
     * Throws an exception.
     *
     * @see io.opensphere.core.common.collection.AbstractCollectionDecorator#remove(java.lang.Object)
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException("The List is fixed size");
    }

    /**
     * Throws an exception.
     *
     * @see io.opensphere.core.common.collection.AbstractCollectionDecorator#addAll(java.util.Collection)
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        throw new UnsupportedOperationException("The List is fixed size");
    }

    /**
     * Throws an exception.
     *
     * @see io.opensphere.core.common.collection.AbstractCollectionDecorator#removeAll(java.util.Collection)
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("The List is fixed size");
    }

    /**
     * Throws an exception.
     *
     * @see io.opensphere.core.common.collection.AbstractCollectionDecorator#retainAll(java.util.Collection)
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("The List is fixed size");
    }

    /**
     * Throws an exception.
     *
     * @see io.opensphere.core.common.collection.AbstractCollectionDecorator#clear()
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("The List is fixed size");
    }
}
