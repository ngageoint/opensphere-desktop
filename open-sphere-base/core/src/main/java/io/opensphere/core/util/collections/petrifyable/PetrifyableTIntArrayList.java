package io.opensphere.core.util.collections.petrifyable;

import java.util.Collection;
import java.util.Random;

import gnu.trove.TCollections;
import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * A list of primitive ints backed by a {@link TIntArrayList} that is also
 * {@link Petrifyable}.
 */
@SuppressWarnings("PMD.GodClass")
public class PetrifyableTIntArrayList extends AbstractPetrifyable implements PetrifyableTIntList
{
    /** The wrapped list. */
    private TIntList myList;

    /**
     * Create a new empty collection with the default capacity.
     */
    public PetrifyableTIntArrayList()
    {
        myList = new TIntArrayList();
    }

    /**
     * Create a new empty collection with a certain capacity.
     *
     * @param capacity The number of ints that can be held by the list.
     */
    public PetrifyableTIntArrayList(int capacity)
    {
        myList = new TIntArrayList(capacity);
    }

    /**
     * Create a new instance using an array of ints for the initial values.
     * <p>
     * NOTE: This constructor copies the given array.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTIntArrayList(int[] values)
    {
        myList = new TIntArrayList(values);
    }

    /**
     * Create a new instance from another collection.
     * <p>
     * NOTE: This constructor iterates over the contents of the given
     * collection, so it may be slow for large arrays.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTIntArrayList(TIntCollection values)
    {
        myList = new TIntArrayList(values);
    }

    @Override
    public boolean add(int val)
    {
        return myList.add(val);
    }

    @Override
    public void add(int[] vals)
    {
        myList.add(vals);
    }

    @Override
    public void add(int[] vals, int offset, int length)
    {
        myList.add(vals, offset, length);
    }

    @Override
    public boolean addAll(Collection<? extends Integer> collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public boolean addAll(int[] array)
    {
        return myList.addAll(array);
    }

    @Override
    public boolean addAll(TIntCollection collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public int binarySearch(int value)
    {
        return myList.binarySearch(value);
    }

    @Override
    public int binarySearch(int value, int fromIndex, int toIndex)
    {
        return myList.binarySearch(value, fromIndex, toIndex);
    }

    @Override
    public void clear()
    {
        myList.clear();
    }

    @Override
    public boolean contains(int value)
    {
        return myList.contains(value);
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public boolean containsAll(int[] array)
    {
        return myList.containsAll(array);
    }

    @Override
    public boolean containsAll(TIntCollection collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public void fill(int val)
    {
        myList.fill(val);
    }

    @Override
    public void fill(int fromIndex, int toIndex, int val)
    {
        myList.fill(fromIndex, toIndex, val);
    }

    @Override
    public boolean forEach(TIntProcedure procedure)
    {
        return myList.forEach(procedure);
    }

    @Override
    public boolean forEachDescending(TIntProcedure procedure)
    {
        return myList.forEachDescending(procedure);
    }

    @Override
    public int get(int offset)
    {
        return myList.get(offset);
    }

    @Override
    public int getNoEntryValue()
    {
        return myList.getNoEntryValue();
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.BOOLEAN_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES
                        + Constants.INT_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + size() * Constants.INT_SIZE_BYTES,
                        Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    @Override
    public PetrifyableTIntArrayList grep(TIntProcedure condition)
    {
        return new PetrifyableTIntArrayList(myList.grep(condition));
    }

    @Override
    public int indexOf(int value)
    {
        return myList.indexOf(value);
    }

    @Override
    public int indexOf(int offset, int value)
    {
        return myList.indexOf(offset, value);
    }

    @Override
    public void insert(int offset, int value)
    {
        myList.insert(offset, value);
    }

    @Override
    public void insert(int offset, int[] values)
    {
        myList.insert(offset, values);
    }

    @Override
    public void insert(int offset, int[] values, int valOffset, int len)
    {
        myList.insert(offset, values, valOffset, len);
    }

    @Override
    public PetrifyableTIntArrayList inverseGrep(TIntProcedure condition)
    {
        return new PetrifyableTIntArrayList(myList.inverseGrep(condition));
    }

    @Override
    public boolean isEmpty()
    {
        return myList.isEmpty();
    }

    @Override
    public TIntIterator iterator()
    {
        return myList.iterator();
    }

    @Override
    public int lastIndexOf(int value)
    {
        return myList.lastIndexOf(value);
    }

    @Override
    public int lastIndexOf(int offset, int value)
    {
        return myList.lastIndexOf(offset, value);
    }

    @Override
    public int max()
    {
        return myList.max();
    }

    @Override
    public int min()
    {
        return myList.min();
    }

    @Override
    public synchronized void petrify()
    {
        if (!isPetrified())
        {
            super.petrify();

            ((TIntArrayList)myList).trimToSize();
            myList = TCollections.unmodifiableList(myList);
        }
    }

    @Override
    public boolean remove(int value)
    {
        return myList.remove(value);
    }

    @Override
    public void remove(int offset, int length)
    {
        myList.remove(offset, length);
    }

    @Override
    public boolean removeAll(Collection<?> collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public boolean removeAll(int[] array)
    {
        return myList.removeAll(array);
    }

    @Override
    public boolean removeAll(TIntCollection collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public int removeAt(int offset)
    {
        return myList.removeAt(offset);
    }

    @Override
    public int replace(int offset, int val)
    {
        return myList.replace(offset, val);
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        return myList.retainAll(collection);
    }

    @Override
    public boolean retainAll(int[] array)
    {
        return myList.retainAll(array);
    }

    @Override
    public boolean retainAll(TIntCollection collection)
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
    public int set(int offset, int val)
    {
        return myList.set(offset, val);
    }

    @Override
    public void set(int offset, int[] values)
    {
        myList.set(offset, values);
    }

    @Override
    public void set(int offset, int[] values, int valOffset, int length)
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
    public PetrifyableTIntArrayList subList(int begin, int end)
    {
        return new PetrifyableTIntArrayList(myList.subList(begin, end));
    }

    @Override
    public int sum()
    {
        return myList.sum();
    }

    @Override
    public int[] toArray()
    {
        return myList.toArray();
    }

    @Override
    public int[] toArray(int offset, int len)
    {
        return myList.toArray(offset, len);
    }

    @Override
    public int[] toArray(int[] dest)
    {
        return myList.toArray(dest);
    }

    @Override
    public int[] toArray(int[] dest, int offset, int len)
    {
        return myList.toArray(dest, offset, len);
    }

    @Override
    public int[] toArray(int[] dest, int sourcePos, int destPos, int len)
    {
        return myList.toArray(dest, sourcePos, destPos, len);
    }

    @Override
    public void transformValues(TIntFunction function)
    {
        myList.transformValues(function);
    }
}
