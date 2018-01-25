package io.opensphere.core.util.collections.petrifyable;

import java.util.Collection;
import java.util.Random;

import gnu.trove.TCollections;
import gnu.trove.TDoubleCollection;
import gnu.trove.function.TDoubleFunction;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.procedure.TDoubleProcedure;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * A list of primitive doubles backed by a {@link TDoubleArrayList} that is also
 * {@link Petrifyable}.
 */
@SuppressWarnings("PMD.GodClass")
public class PetrifyableTDoubleArrayList extends AbstractPetrifyable implements PetrifyableTDoubleList
{
    /** The wrapped list. */
    private TDoubleList myList;

    /**
     * Create a new empty collection with the default capacity.
     */
    public PetrifyableTDoubleArrayList()
    {
        myList = new TDoubleArrayList();
    }

    /**
     * Create a new instance using an array of doubles for the initial values.
     * <p>
     * NOTE: This constructor copies the given array.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTDoubleArrayList(double[] values)
    {
        myList = new TDoubleArrayList(values);
    }

    /**
     * Create a new empty collection with a certain capacity.
     *
     * @param capacity The number of doubles that can be held by the list.
     */
    public PetrifyableTDoubleArrayList(int capacity)
    {
        myList = new TDoubleArrayList(capacity);
    }

    /**
     * Create a new instance from another collection.
     * <p>
     * NOTE: This constructor iterates over the contents of the given
     * collection, so it may be slow for large arrays.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTDoubleArrayList(TDoubleCollection values)
    {
        myList = new TDoubleArrayList(values);
    }

    @Override
    public boolean add(double val)
    {
        return myList.add(val);
    }

    @Override
    public void add(double[] vals)
    {
        myList.add(vals);
    }

    @Override
    public void add(double[] vals, int offset, int length)
    {
        myList.add(vals, offset, length);
    }

    @Override
    public boolean addAll(Collection<? extends Double> collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public boolean addAll(double[] array)
    {
        return myList.addAll(array);
    }

    @Override
    public boolean addAll(TDoubleCollection collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public int binarySearch(double value)
    {
        return myList.binarySearch(value);
    }

    @Override
    public int binarySearch(double value, int fromIndex, int toIndex)
    {
        return myList.binarySearch(value, fromIndex, toIndex);
    }

    @Override
    public void clear()
    {
        myList.clear();
    }

    @Override
    public boolean contains(double value)
    {
        return myList.contains(value);
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public boolean containsAll(double[] array)
    {
        return myList.containsAll(array);
    }

    @Override
    public boolean containsAll(TDoubleCollection collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public void fill(double val)
    {
        myList.fill(val);
    }

    @Override
    public void fill(int fromIndex, int toIndex, double val)
    {
        myList.fill(fromIndex, toIndex, val);
    }

    @Override
    public boolean forEach(TDoubleProcedure procedure)
    {
        return myList.forEach(procedure);
    }

    @Override
    public boolean forEachDescending(TDoubleProcedure procedure)
    {
        return myList.forEachDescending(procedure);
    }

    @Override
    public double get(int offset)
    {
        return myList.get(offset);
    }

    @Override
    public double getNoEntryValue()
    {
        return myList.getNoEntryValue();
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.BOOLEAN_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES
                        + Constants.DOUBLE_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + size() * Constants.DOUBLE_SIZE_BYTES,
                        Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    @Override
    public PetrifyableTDoubleArrayList grep(TDoubleProcedure condition)
    {
        return new PetrifyableTDoubleArrayList(myList.grep(condition));
    }

    @Override
    public int indexOf(double value)
    {
        return myList.indexOf(value);
    }

    @Override
    public int indexOf(int offset, double value)
    {
        return myList.indexOf(offset, value);
    }

    @Override
    public void insert(int offset, double value)
    {
        myList.insert(offset, value);
    }

    @Override
    public void insert(int offset, double[] values)
    {
        myList.insert(offset, values);
    }

    @Override
    public void insert(int offset, double[] values, int valOffset, int len)
    {
        myList.insert(offset, values, valOffset, len);
    }

    @Override
    public PetrifyableTDoubleArrayList inverseGrep(TDoubleProcedure condition)
    {
        return new PetrifyableTDoubleArrayList(myList.inverseGrep(condition));
    }

    @Override
    public boolean isEmpty()
    {
        return myList.isEmpty();
    }

    @Override
    public TDoubleIterator iterator()
    {
        return myList.iterator();
    }

    @Override
    public int lastIndexOf(double value)
    {
        return myList.lastIndexOf(value);
    }

    @Override
    public int lastIndexOf(int offset, double value)
    {
        return myList.lastIndexOf(offset, value);
    }

    @Override
    public double max()
    {
        return myList.max();
    }

    @Override
    public double min()
    {
        return myList.min();
    }

    @Override
    public synchronized void petrify()
    {
        if (!isPetrified())
        {
            super.petrify();

            ((TDoubleArrayList)myList).trimToSize();
            myList = TCollections.unmodifiableList(myList);
        }
    }

    @Override
    public boolean remove(double value)
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
    public boolean removeAll(double[] array)
    {
        return myList.removeAll(array);
    }

    @Override
    public boolean removeAll(TDoubleCollection collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public double removeAt(int offset)
    {
        return myList.removeAt(offset);
    }

    @Override
    public double replace(int offset, double val)
    {
        return myList.replace(offset, val);
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        return myList.retainAll(collection);
    }

    @Override
    public boolean retainAll(double[] array)
    {
        return myList.retainAll(array);
    }

    @Override
    public boolean retainAll(TDoubleCollection collection)
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
    public double set(int offset, double val)
    {
        return myList.set(offset, val);
    }

    @Override
    public void set(int offset, double[] values)
    {
        myList.set(offset, values);
    }

    @Override
    public void set(int offset, double[] values, int valOffset, int length)
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
    public PetrifyableTDoubleArrayList subList(int begin, int end)
    {
        return new PetrifyableTDoubleArrayList(myList.subList(begin, end));
    }

    @Override
    public double sum()
    {
        return myList.sum();
    }

    @Override
    public double[] toArray()
    {
        return myList.toArray();
    }

    @Override
    public double[] toArray(double[] dest)
    {
        return myList.toArray(dest);
    }

    @Override
    public double[] toArray(double[] dest, int offset, int len)
    {
        return myList.toArray(dest, offset, len);
    }

    @Override
    public double[] toArray(double[] dest, int sourcePos, int destPos, int len)
    {
        return myList.toArray(dest, sourcePos, destPos, len);
    }

    @Override
    public double[] toArray(int offset, int len)
    {
        return myList.toArray(offset, len);
    }

    @Override
    public void transformValues(TDoubleFunction function)
    {
        myList.transformValues(function);
    }
}
