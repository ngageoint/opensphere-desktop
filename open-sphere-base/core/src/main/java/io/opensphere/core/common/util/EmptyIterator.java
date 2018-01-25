package io.opensphere.core.common.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An empty iterator, which always returns false for the {@link #hasNext()}
 * method.
 *
 * @param <ITERATOR_TYPE> the type for which the iterator is configured.
 */
public class EmptyIterator<ITERATOR_TYPE> implements Iterator<ITERATOR_TYPE>
{
    /**
     * A static instance of the empty iterator, to prevent the need for
     * re-instantiation.
     */
    public static final EmptyIterator<Object> INSTANCE = new EmptyIterator<>();

    /**
     * Creates an empty iterator.
     */
    EmptyIterator()
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public ITERATOR_TYPE next()
    {
        throw new NoSuchElementException("Hashtable Iterator");
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove()
    {
        throw new IllegalStateException("Hashtable Iterator");
    }
}
