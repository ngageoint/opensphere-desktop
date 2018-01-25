package io.opensphere.core.common.collection;

import java.util.Iterator;

/**
 * This class decorates a Java <code>Iterator</code>.
 */
public abstract class AbstractIteratorDecorator<E> implements Iterator<E>
{
    /**
     * The decorated <code>Iterator</code>.
     */
    private Iterator<E> iterator;

    /**
     * Creates a new instance that decorates the given <code>Iterator</code>.
     *
     * @param iterator the <code>Iterator</code> to be decorated.
     */
    public AbstractIteratorDecorator(Iterator<E> iterator)
    {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext()
    {
        return getIterator().hasNext();
    }

    @Override
    public E next()
    {
        return getIterator().next();
    }

    @Override
    public void remove()
    {
        getIterator().remove();
    }

    /**
     * Returns the decorated iterator.
     *
     * @return the decorated iterator.
     */
    protected Iterator<E> getIterator()
    {
        return iterator;
    }
}
