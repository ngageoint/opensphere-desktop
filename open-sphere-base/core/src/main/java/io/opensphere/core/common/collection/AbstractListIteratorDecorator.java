package io.opensphere.core.common.collection;

import java.util.ListIterator;

/**
 * This class decorates a Java <code>ListIterator</code>.
 */
public abstract class AbstractListIteratorDecorator<E> extends AbstractIteratorDecorator<E> implements ListIterator<E>
{
    /**
     * Creates a new instance that decorates the given
     * <code>ListIterator</code>.
     *
     * @param iterator the <code>ListIterator</code> to be decorated.
     */
    public AbstractListIteratorDecorator(ListIterator<E> iterator)
    {
        super(iterator);
    }

    @Override
    public boolean hasPrevious()
    {
        return getIterator().hasPrevious();
    }

    @Override
    public E previous()
    {
        return getIterator().previous();
    }

    @Override
    public int nextIndex()
    {
        return getIterator().nextIndex();
    }

    @Override
    public int previousIndex()
    {
        return getIterator().previousIndex();
    }

    @Override
    public void set(E e)
    {
        getIterator().set(e);
    }

    @Override
    public void add(E e)
    {
        getIterator().add(e);
    }

    /**
     * Returns the decorated iterator.
     *
     * @return the decorated iterator.
     */
    @Override
    protected ListIterator<E> getIterator()
    {
        return (ListIterator<E>)super.getIterator();
    }
}
