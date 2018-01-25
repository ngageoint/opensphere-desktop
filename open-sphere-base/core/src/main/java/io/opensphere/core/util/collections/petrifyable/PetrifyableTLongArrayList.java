package io.opensphere.core.util.collections.petrifyable;

import java.util.Collection;
import java.util.Random;

import gnu.trove.TCollections;
import gnu.trove.TLongCollection;
import gnu.trove.function.TLongFunction;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.procedure.TLongProcedure;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * A list of primitive longs backed by a {@link TLongArrayList} that is also
 * {@link Petrifyable}.
 */
@SuppressWarnings("PMD.GodClass")
public class PetrifyableTLongArrayList extends AbstractPetrifyable implements PetrifyableTLongList
{
    /** The wrapped list. */
    private TLongList myList;

    /**
     * Create a new empty collection with the default capacity.
     */
    public PetrifyableTLongArrayList()
    {
        myList = new TLongArrayList();
    }

    /**
     * Create a new empty collection with a certain capacity.
     *
     * @param capacity The number of longs that can be held by the list.
     */
    public PetrifyableTLongArrayList(int capacity)
    {
        myList = new TLongArrayList(capacity);
    }

    /**
     * Create a new instance using an array of longs for the initial values.
     * <p>
     * NOTE: This constructor copies the given array.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTLongArrayList(long[] values)
    {
        myList = new TLongArrayList(values);
    }

    /**
     * Create a new instance from another collection.
     * <p>
     * NOTE: This constructor iterates over the contents of the given
     * collection, so it may be slow for large arrays.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTLongArrayList(TLongCollection values)
    {
        myList = new TLongArrayList(values);
    }

    @Override
    public boolean add(long val)
    {
        return myList.add(val);
    }

    @Override
    public void add(long[] vals)
    {
        myList.add(vals);
    }

    @Override
    public void add(long[] vals, int offset, int length)
    {
        myList.add(vals, offset, length);
    }

    @Override
    public boolean addAll(Collection<? extends Long> collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public boolean addAll(long[] array)
    {
        return myList.addAll(array);
    }

    @Override
    public boolean addAll(TLongCollection collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public int binarySearch(long value)
    {
        return myList.binarySearch(value);
    }

    @Override
    public int binarySearch(long value, int fromIndex, int toIndex)
    {
        return myList.binarySearch(value, fromIndex, toIndex);
    }

    @Override
    public void clear()
    {
        myList.clear();
    }

    @Override
    public boolean contains(long value)
    {
        return myList.contains(value);
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public boolean containsAll(long[] array)
    {
        return myList.containsAll(array);
    }

    @Override
    public boolean containsAll(TLongCollection collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public void fill(int fromIndex, int toIndex, long val)
    {
        myList.fill(fromIndex, toIndex, val);
    }

    @Override
    public void fill(long val)
    {
        myList.fill(val);
    }

    @Override
    public boolean forEach(TLongProcedure procedure)
    {
        return myList.forEach(procedure);
    }

    @Override
    public boolean forEachDescending(TLongProcedure procedure)
    {
        return myList.forEachDescending(procedure);
    }

    @Override
    public long get(int offset)
    {
        return myList.get(offset);
    }

    @Override
    public long getNoEntryValue()
    {
        return myList.getNoEntryValue();
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.BOOLEAN_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES
                        + Constants.LONG_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + size() * Constants.LONG_SIZE_BYTES,
                        Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    @Override
    public TLongList grep(TLongProcedure condition)
    {
        return myList.grep(condition);
    }

    @Override
    public int indexOf(int offset, long value)
    {
        return myList.indexOf(offset, value);
    }

    @Override
    public int indexOf(long value)
    {
        return myList.indexOf(value);
    }

    @Override
    public void insert(int offset, long value)
    {
        myList.insert(offset, value);
    }

    @Override
    public void insert(int offset, long[] values)
    {
        myList.insert(offset, values);
    }

    @Override
    public void insert(int offset, long[] values, int valOffset, int len)
    {
        myList.insert(offset, values, valOffset, len);
    }

    @Override
    public TLongList inverseGrep(TLongProcedure condition)
    {
        return myList.inverseGrep(condition);
    }

    @Override
    public boolean isEmpty()
    {
        return myList.isEmpty();
    }

    @Override
    public TLongIterator iterator()
    {
        return myList.iterator();
    }

    @Override
    public int lastIndexOf(int offset, long value)
    {
        return myList.lastIndexOf(offset, value);
    }

    @Override
    public int lastIndexOf(long value)
    {
        return myList.lastIndexOf(value);
    }

    @Override
    public long max()
    {
        return myList.max();
    }

    @Override
    public long min()
    {
        return myList.min();
    }

    @Override
    public synchronized void petrify()
    {
        if (!isPetrified())
        {
            super.petrify();

            ((TLongArrayList)myList).trimToSize();
            myList = TCollections.unmodifiableList(myList);
        }
    }

    @Override
    public void remove(int offset, int length)
    {
        myList.remove(offset, length);
    }

    @Override
    public boolean remove(long value)
    {
        return myList.remove(value);
    }

    @Override
    public boolean removeAll(Collection<?> collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public boolean removeAll(long[] array)
    {
        return myList.removeAll(array);
    }

    @Override
    public boolean removeAll(TLongCollection collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public long removeAt(int offset)
    {
        return myList.removeAt(offset);
    }

    @Override
    public long replace(int offset, long val)
    {
        return myList.replace(offset, val);
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        return myList.retainAll(collection);
    }

    @Override
    public boolean retainAll(long[] array)
    {
        return myList.retainAll(array);
    }

    @Override
    public boolean retainAll(TLongCollection collection)
    {
        return myList.retainAll(collection);
    }

    @Override
    public void reverse()
    {
        myList.reverse();
    }

    @Override
    public void reverse(int from, int to)
    {
        myList.reverse(from, to);
    }

    @Override
    public long set(int offset, long val)
    {
        return myList.set(offset, val);
    }

    @Override
    public void set(int offset, long[] values)
    {
        myList.set(offset, values);
    }

    @Override
    public void set(int offset, long[] values, int valOffset, int length)
    {
        myList.set(offset, values, valOffset, length);
    }

    @Override
    public void shuffle(Random rand)
    {
        myList.shuffle(rand);
    }

    @Override
    public int size()
    {
        return myList.size();
    }

    @Override
    public void sort()
    {
        myList.sort();
    }

    @Override
    public void sort(int fromIndex, int toIndex)
    {
        myList.sort(fromIndex, toIndex);
    }

    @Override
    public PetrifyableTLongArrayList subList(int begin, int end)
    {
        return new PetrifyableTLongArrayList(myList.subList(begin, end));
    }

    @Override
    public long sum()
    {
        return myList.sum();
    }

    @Override
    public long[] toArray()
    {
        return myList.toArray();
    }

    @Override
    public long[] toArray(int offset, int len)
    {
        return myList.toArray(offset, len);
    }

    @Override
    public long[] toArray(long[] dest)
    {
        return myList.toArray(dest);
    }

    @Override
    public long[] toArray(long[] dest, int offset, int len)
    {
        return myList.toArray(dest, offset, len);
    }

    @Override
    public long[] toArray(long[] dest, int sourcePos, int destPos, int len)
    {
        return myList.toArray(dest, sourcePos, destPos, len);
    }

    @Override
    public void transformValues(TLongFunction function)
    {
        myList.transformValues(function);
    }
}
