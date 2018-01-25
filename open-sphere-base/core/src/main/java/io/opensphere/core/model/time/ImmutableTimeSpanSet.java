package io.opensphere.core.model.time;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.opensphere.core.util.Utilities;

/**
 * Immutable wrapper for a TimeSpanSet. No method that alters the set will
 * operate but will throw {@link UnsupportedOperationException} instead.
 */
public class ImmutableTimeSpanSet implements TimeSpanSet
{
    /** The Constant THIS_TIMESPAN_SET_IS_IMMUTABLE. */
    private static final String THIS_TIMESPAN_SET_IS_IMMUTABLE = "This TimeSpanSet is Immutable";

    /** The my wrapped set. */
    private final TimeSpanSet myWrappedTimeSpanSet;

    /**
     * Instantiates a new immutable TimeSpanSet that wraps another TimeSpanSet.
     *
     * @param other the other
     */
    public ImmutableTimeSpanSet(TimeSpanSet other)
    {
        if (other == null)
        {
            throw new IllegalArgumentException("The TimeSpanSet to make Immutable cannot be null!");
        }
        myWrappedTimeSpanSet = other;
    }

    @Override
    public boolean add(TimeSpan e)
    {
        throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean add(TimeSpanSet other)
    {
        throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean addAll(Collection<? extends TimeSpan> c)
    {
        throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean contains(Date aDate)
    {
        return myWrappedTimeSpanSet.contains(aDate);
    }

    @Override
    public boolean contains(long aTime)
    {
        return myWrappedTimeSpanSet.contains(aTime);
    }

    @Override
    public boolean contains(Object o)
    {
        return myWrappedTimeSpanSet.contains(o);
    }

    @Override
    public boolean contains(TimeSpan ts)
    {
        return myWrappedTimeSpanSet.contains(ts);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return myWrappedTimeSpanSet.containsAll(c);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (Utilities.sameInstance(this, obj))
        {
            return true;
        }
        return myWrappedTimeSpanSet.equals(obj);
    }

    @Override
    public List<TimeSpan> getTimeSpans()
    {
        return myWrappedTimeSpanSet.getTimeSpans();
    }

    @Override
    public int hashCode()
    {
        return myWrappedTimeSpanSet.hashCode();
    }

    @Override
    public TimeSpanSet intersection(TimeSpan ts)
    {
        return myWrappedTimeSpanSet.intersection(ts);
    }

    @Override
    public TimeSpanSet intersection(TimeSpanSet other)
    {
        return myWrappedTimeSpanSet.intersection(other);
    }

    @Override
    public boolean intersects(TimeSpan ts)
    {
        return myWrappedTimeSpanSet.intersects(ts);
    }

    @Override
    public boolean intersects(TimeSpanProvider ts)
    {
        return myWrappedTimeSpanSet.intersects(ts);
    }

    @Override
    public boolean intersects(TimeSpanSet other)
    {
        return myWrappedTimeSpanSet.intersects(other);
    }

    @Override
    public boolean isEmpty()
    {
        return myWrappedTimeSpanSet.isEmpty();
    }

    @Override
    public Iterator<TimeSpan> iterator()
    {
        return new ImmutableIterator(myWrappedTimeSpanSet.iterator());
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean remove(TimeSpan ts)
    {
        throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean remove(TimeSpanSet other)
    {
        throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
    }

    @Override
    public int size()
    {
        return myWrappedTimeSpanSet.size();
    }

    @Override
    public Object[] toArray()
    {
        return myWrappedTimeSpanSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return myWrappedTimeSpanSet.toArray(a);
    }

    @Override
    public String toString()
    {
        return myWrappedTimeSpanSet.toString();
    }

    @Override
    public TimeSpanSet union(TimeSpanSet other)
    {
        return myWrappedTimeSpanSet.union(other);
    }

    /**
     * The Class ImmutableIterator.
     */
    private static class ImmutableIterator implements Iterator<TimeSpan>
    {
        /** The my wrapped iterator. */
        private final Iterator<TimeSpan> myWrappedIterator;

        /**
         * Instantiates a new immutable iterator.
         *
         * @param toWrap the to wrap
         */
        public ImmutableIterator(Iterator<TimeSpan> toWrap)
        {
            myWrappedIterator = toWrap;
        }

        @Override
        public boolean hasNext()
        {
            return myWrappedIterator.hasNext();
        }

        @Override
        public TimeSpan next()
        {
            return myWrappedIterator.next();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException(THIS_TIMESPAN_SET_IS_IMMUTABLE);
        }
    }
}
