package io.opensphere.core.util.collections.petrifyable;

import java.util.Collection;
import java.util.Random;

import gnu.trove.TCollections;
import gnu.trove.TShortCollection;
import gnu.trove.function.TShortFunction;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.procedure.TShortProcedure;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * A list of primitive shorts backed by a {@link TShortArrayList} that is also
 * {@link Petrifyable}.
 */
@SuppressWarnings({ "PMD.AvoidUsingShortType", "PMD.GodClass" })
public class PetrifyableTShortArrayList extends AbstractPetrifyable implements PetrifyableTShortList
{
    /** The wrapped list. */
    private TShortList myList;

    /**
     * Create a new empty collection with the default capacity.
     */
    public PetrifyableTShortArrayList()
    {
        myList = new TShortArrayList();
    }

    /**
     * Create a new empty collection with a certain capacity.
     *
     * @param capacity The number of shorts that can be held by the list.
     */
    public PetrifyableTShortArrayList(int capacity)
    {
        myList = new TShortArrayList(capacity);
    }

    /**
     * Create a new instance using an array of shorts for the initial values.
     * <p>
     * NOTE: This constructor copies the given array.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTShortArrayList(short[] values)
    {
        myList = new TShortArrayList(values);
    }

    /**
     * Create a new instance from another collection.
     * <p>
     * NOTE: This constructor iterates over the contents of the given
     * collection, so it may be slow for large arrays.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTShortArrayList(TShortCollection values)
    {
        myList = new TShortArrayList(values);
    }

    @Override
    public boolean add(short val)
    {
        return myList.add(val);
    }

    @Override
    public void add(short[] vals)
    {
        myList.add(vals);
    }

    @Override
    public void add(short[] vals, int offset, int length)
    {
        myList.add(vals, offset, length);
    }

    @Override
    public boolean addAll(Collection<? extends Short> collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public boolean addAll(short[] array)
    {
        return myList.addAll(array);
    }

    @Override
    public boolean addAll(TShortCollection collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public int binarySearch(short value)
    {
        return myList.binarySearch(value);
    }

    @Override
    public int binarySearch(short value, int fromIndex, int toIndex)
    {
        return myList.binarySearch(value, fromIndex, toIndex);
    }

    @Override
    public void clear()
    {
        myList.clear();
    }

    @Override
    public boolean contains(short value)
    {
        return myList.contains(value);
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public boolean containsAll(short[] array)
    {
        return myList.containsAll(array);
    }

    @Override
    public boolean containsAll(TShortCollection collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public void fill(int fromIndex, int toIndex, short val)
    {
        myList.fill(fromIndex, toIndex, val);
    }

    @Override
    public void fill(short val)
    {
        myList.fill(val);
    }

    @Override
    public boolean forEach(TShortProcedure procedure)
    {
        return myList.forEach(procedure);
    }

    @Override
    public boolean forEachDescending(TShortProcedure procedure)
    {
        return myList.forEachDescending(procedure);
    }

    @Override
    public short get(int offset)
    {
        return myList.get(offset);
    }

    @Override
    public short getNoEntryValue()
    {
        return myList.getNoEntryValue();
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.BOOLEAN_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES
                        + Constants.SHORT_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + size() * Constants.SHORT_SIZE_BYTES,
                        Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    @Override
    public PetrifyableTShortArrayList grep(TShortProcedure condition)
    {
        return new PetrifyableTShortArrayList(myList.grep(condition));
    }

    @Override
    public int indexOf(int offset, short value)
    {
        return myList.indexOf(offset, value);
    }

    @Override
    public int indexOf(short value)
    {
        return myList.indexOf(value);
    }

    @Override
    public void insert(int offset, short value)
    {
        myList.insert(offset, value);
    }

    @Override
    public void insert(int offset, short[] values)
    {
        myList.insert(offset, values);
    }

    @Override
    public void insert(int offset, short[] values, int valOffset, int len)
    {
        myList.insert(offset, values, valOffset, len);
    }

    @Override
    public PetrifyableTShortArrayList inverseGrep(TShortProcedure condition)
    {
        return new PetrifyableTShortArrayList(myList.inverseGrep(condition));
    }

    @Override
    public boolean isEmpty()
    {
        return myList.isEmpty();
    }

    @Override
    public TShortIterator iterator()
    {
        return myList.iterator();
    }

    @Override
    public int lastIndexOf(int offset, short value)
    {
        return myList.lastIndexOf(offset, value);
    }

    @Override
    public int lastIndexOf(short value)
    {
        return myList.lastIndexOf(value);
    }

    @Override
    public short max()
    {
        return myList.max();
    }

    @Override
    public short min()
    {
        return myList.min();
    }

    @Override
    public synchronized void petrify()
    {
        if (!isPetrified())
        {
            super.petrify();

            ((TShortArrayList)myList).trimToSize();
            myList = TCollections.unmodifiableList(myList);
        }
    }

    @Override
    public void remove(int offset, int length)
    {
        myList.remove(offset, length);
    }

    @Override
    public boolean remove(short value)
    {
        return myList.remove(value);
    }

    @Override
    public boolean removeAll(Collection<?> collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public boolean removeAll(short[] array)
    {
        return myList.removeAll(array);
    }

    @Override
    public boolean removeAll(TShortCollection collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public short removeAt(int offset)
    {
        return myList.removeAt(offset);
    }

    @Override
    public short replace(int offset, short val)
    {
        return myList.replace(offset, val);
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        return myList.retainAll(collection);
    }

    @Override
    public boolean retainAll(short[] array)
    {
        return myList.retainAll(array);
    }

    @Override
    public boolean retainAll(TShortCollection collection)
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
    public short set(int offset, short val)
    {
        return myList.set(offset, val);
    }

    @Override
    public void set(int offset, short[] values)
    {
        myList.set(offset, values);
    }

    @Override
    public void set(int offset, short[] values, int valOffset, int length)
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
    public PetrifyableTShortArrayList subList(int begin, int end)
    {
        return new PetrifyableTShortArrayList(myList.subList(begin, end));
    }

    @Override
    public short sum()
    {
        return myList.sum();
    }

    @Override
    public short[] toArray()
    {
        return myList.toArray();
    }

    @Override
    public short[] toArray(int offset, int len)
    {
        return myList.toArray(offset, len);
    }

    @Override
    public short[] toArray(short[] dest)
    {
        return myList.toArray(dest);
    }

    @Override
    public short[] toArray(short[] dest, int offset, int len)
    {
        return myList.toArray(dest, offset, len);
    }

    @Override
    public short[] toArray(short[] dest, int sourcePos, int destPos, int len)
    {
        return myList.toArray(dest, sourcePos, destPos, len);
    }

    @Override
    public void transformValues(TShortFunction function)
    {
        myList.transformValues(function);
    }
}
