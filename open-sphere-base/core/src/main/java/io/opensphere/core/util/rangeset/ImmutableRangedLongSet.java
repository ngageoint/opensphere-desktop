package io.opensphere.core.util.rangeset;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper around a RangeLongSet that makes the set immutable. All functions
 * that would change the value of the set throw
 * {@link UnsupportedOperationException}.
 */
public class ImmutableRangedLongSet implements RangedLongSet
{
    /** The Constant THIS_RANGED_LONG_SET_IS_IMMUTABLE. */
    private static final String THIS_RANGED_LONG_SET_IS_IMMUTABLE = "This RangedLongSet is Immutable";

    /** The wrapped set. */
    private final RangedLongSet myWrappedRangedLongSet;

    /**
     * Instantiates a new immutable ranged long set.
     *
     * @param wrappedSet the wrapped set that backs this set
     */
    public ImmutableRangedLongSet(RangedLongSet wrappedSet)
    {
        myWrappedRangedLongSet = wrappedSet;
    }

    @Override
    public boolean add(long value)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean add(Long e)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean add(RangedLongSet otherSet)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean addAll(Collection<? extends Long> values)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean addAll(long[] values)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean addAll(Long[] values)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean addBlock(RangeLongBlock insertBlock)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean addBlocks(Collection<RangeLongBlock> blocksToAdd)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public int blockCount()
    {
        return myWrappedRangedLongSet.blockCount();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean contains(Long value)
    {
        return myWrappedRangedLongSet.contains(value);
    }

    @Override
    public boolean contains(Object o)
    {
        return myWrappedRangedLongSet.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return myWrappedRangedLongSet.containsAll(c);
    }

    @Override
    public List<RangeLongBlock> getBlocks()
    {
        return myWrappedRangedLongSet.getBlocks();
    }

    @Override
    public RangedLongSet getDifference(long[] values)
    {
        return myWrappedRangedLongSet.getDifference(values);
    }

    @Override
    public RangedLongSet getDifference(RangedLongSet other)
    {
        return myWrappedRangedLongSet.getDifference(other);
    }

    @Override
    public RangedLongSet getIntersection(Collection<Long> values)
    {
        return myWrappedRangedLongSet.getIntersection(values);
    }

    @Override
    public RangedLongSet getIntersection(long[] values)
    {
        return myWrappedRangedLongSet.getIntersection(values);
    }

    @Override
    public RangedLongSet getIntersection(Long[] values)
    {
        return myWrappedRangedLongSet.getIntersection(values);
    }

    @Override
    public RangedLongSet getIntersection(RangedLongSet otherSet)
    {
        return myWrappedRangedLongSet.getIntersection(otherSet);
    }

    @Override
    public RangedLongSet getIntersection(RangeLongBlock blockToIntersect)
    {
        return myWrappedRangedLongSet.getIntersection(blockToIntersect);
    }

    @Override
    public Long getMaximum()
    {
        return myWrappedRangedLongSet.getMaximum();
    }

    @Override
    public Long getMinimum()
    {
        return myWrappedRangedLongSet.getMinimum();
    }

    @Override
    public RangedLongSet getUnion(RangedLongSet other)
    {
        return myWrappedRangedLongSet.getUnion(other);
    }

    @Override
    public long[] getValues() throws TooManyValuesToConstructArrayException
    {
        return myWrappedRangedLongSet.getValues();
    }

    @Override
    public boolean hasValue(long value)
    {
        return myWrappedRangedLongSet.hasValue(value);
    }

    @Override
    public boolean intersects(RangeLongBlock block)
    {
        return myWrappedRangedLongSet.intersects(block);
    }

    @Override
    public boolean isEmpty()
    {
        return myWrappedRangedLongSet.isEmpty();
    }

    @Override
    public Iterator<Long> iterator()
    {
        return myWrappedRangedLongSet.iterator();
    }

    @Override
    public boolean remove(Collection<? extends Long> valueList)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean remove(long value)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean remove(Long value)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean remove(long[] values)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean remove(Long[] values)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean remove(RangedLongSet otherSet)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean removeBlock(RangeLongBlock blockToRemove)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean removeBlocks(Collection<RangeLongBlock> blocksToRemove)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException(THIS_RANGED_LONG_SET_IS_IMMUTABLE);
    }

    @Override
    public int size()
    {
        return myWrappedRangedLongSet.size();
    }

    @Override
    public Object[] toArray()
    {
        return myWrappedRangedLongSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return myWrappedRangedLongSet.toArray(a);
    }

    @Override
    public String toString()
    {
        return myWrappedRangedLongSet.toString();
    }

    @Override
    public long valueCount()
    {
        return myWrappedRangedLongSet.valueCount();
    }
}
