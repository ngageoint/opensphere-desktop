package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Iterator;

/**
 * Iterator for traversing all the values for the {@link Iterable}.
 */
public class LongIterableIterator implements Iterator<Long>
{
    /** The my list iterator. */
    private final Iterator<? extends Iterable<Long>> myListIterator;

    /** The my block iterator. */
    private Iterator<Long> myBlockIterator;

    /**
     * Instantiates a new block list iterator.
     *
     * @param blockList the block list
     */
    public LongIterableIterator(Collection<? extends Iterable<Long>> blockList)
    {
        myListIterator = blockList.iterator();
    }

    @Override
    public boolean hasNext()
    {
        if (myBlockIterator == null)
        {
            return myListIterator.hasNext();
        }
        else
        {
            return myBlockIterator.hasNext() || myListIterator.hasNext();
        }
    }

    @Override
    public Long next()
    {
        if (myBlockIterator == null)
        {
            myBlockIterator = myListIterator.next().iterator();
        }
        if (myBlockIterator.hasNext())
        {
            return myBlockIterator.next();
        }
        else
        {
            if (myListIterator.hasNext())
            {
                myBlockIterator = myListIterator.next().iterator();
            }
        }
        return myBlockIterator.next();
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
