package io.opensphere.core.util.collections;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An iterator over an enumeration.
 *
 * @param <E> The element type.
 */
public class EnumerationIterator<E> implements Iterator<E>
{
    /** The enumeration. */
    private final Enumeration<? extends E> myEnumeration;

    /**
     * Constructor.
     *
     * @param enumeration The enumeration.
     */
    public EnumerationIterator(Enumeration<? extends E> enumeration)
    {
        myEnumeration = enumeration;
    }

    @Override
    public boolean hasNext()
    {
        return myEnumeration.hasMoreElements();
    }

    @Override
    public E next()
    {
        return myEnumeration.nextElement();
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
