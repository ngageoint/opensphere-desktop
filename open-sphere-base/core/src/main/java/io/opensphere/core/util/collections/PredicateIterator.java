package io.opensphere.core.util.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * An iterator that filters the results from a nested iterator so it only
 * returns elements that pass the predicate. {@link Iterator#remove()} is not
 * supported.
 *
 * @param <E> The type returned by the nested iterator.
 * @param <F> The type returned by this iterator.
 */
public abstract class PredicateIterator<E, F> implements Iterator<F>
{
    /** The wrapped iterator. */
    private final Iterator<? extends E> myIter;

    /**
     * Reference to the next item, or {@code null} if it hasn't been retrieved.
     */
    private F myNext;

    /** The predicate used to test each value returned by the iterator. */
    private final Predicate<E> myPredicate;

    /**
     * Constructor.
     *
     * @param iter The wrapped iterator.
     * @param predicate The predicate used to test each value.
     */
    public PredicateIterator(Iterator<? extends E> iter, Predicate<E> predicate)
    {
        myIter = iter;
        myPredicate = predicate;
    }

    @Override
    public boolean hasNext()
    {
        return getNext() != null;
    }

    @Override
    public synchronized F next()
    {
        F next = getNext();
        if (next == null)
        {
            throw new NoSuchElementException();
        }
        myNext = null;
        return next;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert from the type of the nested iterator to the type returned by me.
     *
     * @param obj The object from the nested iterator.
     * @return The converted object.
     */
    protected abstract F convert(E obj);

    /**
     * Iterate over the wrapped iterator until a suitable value is found.
     *
     * @return The next suitable value, or {@code null} if none could be found.
     */
    private synchronized F getNext()
    {
        if (myNext == null)
        {
            while (myIter.hasNext())
            {
                E obj = myIter.next();
                if (myPredicate.test(obj))
                {
                    myNext = convert(obj);
                    break;
                }
            }
        }
        return myNext;
    }
}
