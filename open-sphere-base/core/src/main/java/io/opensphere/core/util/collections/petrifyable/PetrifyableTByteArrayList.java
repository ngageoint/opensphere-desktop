package io.opensphere.core.util.collections.petrifyable;

import java.util.Collection;
import java.util.Random;

import gnu.trove.TByteCollection;
import gnu.trove.TCollections;
import gnu.trove.function.TByteFunction;
import gnu.trove.iterator.TByteIterator;
import gnu.trove.list.TByteList;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.procedure.TByteProcedure;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * A list of primitive bytes backed by a {@link TByteArrayList} that is also
 * {@link Petrifyable}.
 */
@SuppressWarnings("PMD.GodClass")
public class PetrifyableTByteArrayList extends AbstractPetrifyable implements PetrifyableTByteList
{
    /** The wrapped list. */
    private TByteList myList;

    /**
     * Create a new empty collection with the default capacity.
     */
    public PetrifyableTByteArrayList()
    {
        myList = new TByteArrayList();
    }

    /**
     * Create a new instance using an array of bytes for the initial values.
     * <p>
     * NOTE: This constructor copies the given array.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTByteArrayList(byte[] values)
    {
        myList = new TByteArrayList(values);
    }

    /**
     * Create a new empty collection with a certain capacity.
     *
     * @param capacity The number of bytes that can be held by the list.
     */
    public PetrifyableTByteArrayList(int capacity)
    {
        myList = new TByteArrayList(capacity);
    }

    /**
     * Create a new instance from another collection.
     * <p>
     * NOTE: This constructor iterates over the contents of the given
     * collection, so it may be slow for large arrays.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTByteArrayList(TByteCollection values)
    {
        myList = new TByteArrayList(values);
    }

    @Override
    public boolean add(byte val)
    {
        return myList.add(val);
    }

    @Override
    public void add(byte[] vals)
    {
        myList.add(vals);
    }

    @Override
    public void add(byte[] vals, int offset, int length)
    {
        myList.add(vals, offset, length);
    }

    @Override
    public boolean addAll(byte[] array)
    {
        return myList.addAll(array);
    }

    @Override
    public boolean addAll(Collection<? extends Byte> collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public boolean addAll(TByteCollection collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public int binarySearch(byte value)
    {
        return myList.binarySearch(value);
    }

    @Override
    public int binarySearch(byte value, int fromIndex, int toIndex)
    {
        return myList.binarySearch(value, fromIndex, toIndex);
    }

    @Override
    public void clear()
    {
        myList.clear();
    }

    @Override
    public boolean contains(byte value)
    {
        return myList.contains(value);
    }

    @Override
    public boolean containsAll(byte[] array)
    {
        return myList.containsAll(array);
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public boolean containsAll(TByteCollection collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public void fill(byte val)
    {
        myList.fill(val);
    }

    @Override
    public void fill(int fromIndex, int toIndex, byte val)
    {
        myList.fill(fromIndex, toIndex, val);
    }

    @Override
    public boolean forEach(TByteProcedure procedure)
    {
        return myList.forEach(procedure);
    }

    @Override
    public boolean forEachDescending(TByteProcedure procedure)
    {
        return myList.forEachDescending(procedure);
    }

    @Override
    public byte get(int offset)
    {
        return myList.get(offset);
    }

    @Override
    public byte getNoEntryValue()
    {
        return myList.getNoEntryValue();
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.BOOLEAN_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES + 1L,
                        Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + size(), Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    @Override
    public PetrifyableTByteArrayList grep(TByteProcedure condition)
    {
        return new PetrifyableTByteArrayList(myList.grep(condition));
    }

    @Override
    public int indexOf(byte value)
    {
        return myList.indexOf(value);
    }

    @Override
    public int indexOf(int offset, byte value)
    {
        return myList.indexOf(offset, value);
    }

    @Override
    public void insert(int offset, byte value)
    {
        myList.insert(offset, value);
    }

    @Override
    public void insert(int offset, byte[] values)
    {
        myList.insert(offset, values);
    }

    @Override
    public void insert(int offset, byte[] values, int valOffset, int len)
    {
        myList.insert(offset, values, valOffset, len);
    }

    @Override
    public PetrifyableTByteArrayList inverseGrep(TByteProcedure condition)
    {
        return new PetrifyableTByteArrayList(myList.inverseGrep(condition));
    }

    @Override
    public boolean isEmpty()
    {
        return myList.isEmpty();
    }

    @Override
    public TByteIterator iterator()
    {
        return myList.iterator();
    }

    @Override
    public int lastIndexOf(byte value)
    {
        return myList.lastIndexOf(value);
    }

    @Override
    public int lastIndexOf(int offset, byte value)
    {
        return myList.lastIndexOf(offset, value);
    }

    @Override
    public byte max()
    {
        return myList.max();
    }

    @Override
    public byte min()
    {
        return myList.min();
    }

    @Override
    public synchronized void petrify()
    {
        if (!isPetrified())
        {
            super.petrify();

            ((TByteArrayList)myList).trimToSize();
            myList = TCollections.unmodifiableList(myList);
        }
    }

    @Override
    public boolean remove(byte value)
    {
        return myList.remove(value);
    }

    @Override
    public void remove(int offset, int length)
    {
        myList.remove(offset, length);
    }

    @Override
    public boolean removeAll(byte[] array)
    {
        return myList.removeAll(array);
    }

    @Override
    public boolean removeAll(Collection<?> collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public boolean removeAll(TByteCollection collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public byte removeAt(int offset)
    {
        return myList.removeAt(offset);
    }

    @Override
    public byte replace(int offset, byte val)
    {
        return myList.replace(offset, val);
    }

    @Override
    public boolean retainAll(byte[] array)
    {
        return myList.retainAll(array);
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        return myList.retainAll(collection);
    }

    @Override
    public boolean retainAll(TByteCollection collection)
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
    public byte set(int offset, byte val)
    {
        return myList.set(offset, val);
    }

    @Override
    public void set(int offset, byte[] values)
    {
        myList.set(offset, values);
    }

    @Override
    public void set(int offset, byte[] values, int valOffset, int length)
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
    public PetrifyableTByteArrayList subList(int begin, int end)
    {
        return new PetrifyableTByteArrayList(myList.subList(begin, end));
    }

    @Override
    public byte sum()
    {
        return myList.sum();
    }

    @Override
    public byte[] toArray()
    {
        return myList.toArray();
    }

    @Override
    public byte[] toArray(byte[] dest)
    {
        return myList.toArray(dest);
    }

    @Override
    public byte[] toArray(byte[] dest, int offset, int len)
    {
        return myList.toArray(dest, offset, len);
    }

    @Override
    public byte[] toArray(byte[] dest, int sourcePos, int destPos, int len)
    {
        return myList.toArray(dest, sourcePos, destPos, len);
    }

    @Override
    public byte[] toArray(int offset, int len)
    {
        return myList.toArray(offset, len);
    }

    @Override
    public void transformValues(TByteFunction function)
    {
        myList.transformValues(function);
    }
}
