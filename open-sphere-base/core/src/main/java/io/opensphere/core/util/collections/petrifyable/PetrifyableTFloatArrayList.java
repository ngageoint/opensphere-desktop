package io.opensphere.core.util.collections.petrifyable;

import java.util.Collection;
import java.util.Random;

import gnu.trove.TCollections;
import gnu.trove.TFloatCollection;
import gnu.trove.function.TFloatFunction;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.procedure.TFloatProcedure;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * A list of primitive floats backed by a {@link TFloatArrayList} that is also
 * {@link Petrifyable}.
 */
@SuppressWarnings("PMD.GodClass")
public class PetrifyableTFloatArrayList extends AbstractPetrifyable implements PetrifyableTFloatList
{
    /** The wrapped list. */
    private TFloatList myList;

    /**
     * Create a new empty collection with the default capacity.
     */
    public PetrifyableTFloatArrayList()
    {
        myList = new TFloatArrayList();
    }

    /**
     * Create a new instance using an array of floats for the initial values.
     * <p>
     * NOTE: This constructor copies the given array.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTFloatArrayList(float[] values)
    {
        myList = new TFloatArrayList(values);
    }

    /**
     * Create a new empty collection with a certain capacity.
     *
     * @param capacity The number of floats that can be held by the list.
     */
    public PetrifyableTFloatArrayList(int capacity)
    {
        myList = new TFloatArrayList(capacity);
    }

    /**
     * Create a new instance from another collection.
     * <p>
     * NOTE: This constructor iterates over the contents of the given
     * collection, so it may be slow for large arrays.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTFloatArrayList(TFloatCollection values)
    {
        myList = new TFloatArrayList(values);
    }

    @Override
    public boolean add(float val)
    {
        return myList.add(val);
    }

    @Override
    public void add(float[] vals)
    {
        myList.add(vals);
    }

    @Override
    public void add(float[] vals, int offset, int length)
    {
        myList.add(vals, offset, length);
    }

    @Override
    public boolean addAll(Collection<? extends Float> collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public boolean addAll(float[] array)
    {
        return myList.addAll(array);
    }

    @Override
    public boolean addAll(TFloatCollection collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public int binarySearch(float value)
    {
        return myList.binarySearch(value);
    }

    @Override
    public int binarySearch(float value, int fromIndex, int toIndex)
    {
        return myList.binarySearch(value, fromIndex, toIndex);
    }

    @Override
    public void clear()
    {
        myList.clear();
    }

    @Override
    public boolean contains(float value)
    {
        return myList.contains(value);
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public boolean containsAll(float[] array)
    {
        return myList.containsAll(array);
    }

    @Override
    public boolean containsAll(TFloatCollection collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public void fill(float val)
    {
        myList.fill(val);
    }

    @Override
    public void fill(int fromIndex, int toIndex, float val)
    {
        myList.fill(fromIndex, toIndex, val);
    }

    @Override
    public boolean forEach(TFloatProcedure procedure)
    {
        return myList.forEach(procedure);
    }

    @Override
    public boolean forEachDescending(TFloatProcedure procedure)
    {
        return myList.forEachDescending(procedure);
    }

    @Override
    public float get(int offset)
    {
        return myList.get(offset);
    }

    @Override
    public float getNoEntryValue()
    {
        return myList.getNoEntryValue();
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.BOOLEAN_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES
                        + Constants.FLOAT_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + size() * Constants.FLOAT_SIZE_BYTES,
                        Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    @Override
    public PetrifyableTFloatArrayList grep(TFloatProcedure condition)
    {
        return new PetrifyableTFloatArrayList(myList.grep(condition));
    }

    @Override
    public int indexOf(float value)
    {
        return myList.indexOf(value);
    }

    @Override
    public int indexOf(int offset, float value)
    {
        return myList.indexOf(offset, value);
    }

    @Override
    public void insert(int offset, float value)
    {
        myList.insert(offset, value);
    }

    @Override
    public void insert(int offset, float[] values)
    {
        myList.insert(offset, values);
    }

    @Override
    public void insert(int offset, float[] values, int valOffset, int len)
    {
        myList.insert(offset, values, valOffset, len);
    }

    @Override
    public PetrifyableTFloatArrayList inverseGrep(TFloatProcedure condition)
    {
        return new PetrifyableTFloatArrayList(myList.inverseGrep(condition));
    }

    @Override
    public boolean isEmpty()
    {
        return myList.isEmpty();
    }

    @Override
    public TFloatIterator iterator()
    {
        return myList.iterator();
    }

    @Override
    public int lastIndexOf(float value)
    {
        return myList.lastIndexOf(value);
    }

    @Override
    public int lastIndexOf(int offset, float value)
    {
        return myList.lastIndexOf(offset, value);
    }

    @Override
    public float max()
    {
        return myList.max();
    }

    @Override
    public float min()
    {
        return myList.min();
    }

    @Override
    public synchronized void petrify()
    {
        if (!isPetrified())
        {
            super.petrify();

            ((TFloatArrayList)myList).trimToSize();
            myList = TCollections.unmodifiableList(myList);
        }
    }

    @Override
    public boolean remove(float value)
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
    public boolean removeAll(float[] array)
    {
        return myList.removeAll(array);
    }

    @Override
    public boolean removeAll(TFloatCollection collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public float removeAt(int offset)
    {
        return myList.removeAt(offset);
    }

    @Override
    public float replace(int offset, float val)
    {
        return myList.replace(offset, val);
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        return myList.retainAll(collection);
    }

    @Override
    public boolean retainAll(float[] array)
    {
        return myList.retainAll(array);
    }

    @Override
    public boolean retainAll(TFloatCollection collection)
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
    public float set(int offset, float val)
    {
        return myList.set(offset, val);
    }

    @Override
    public void set(int offset, float[] values)
    {
        myList.set(offset, values);
    }

    @Override
    public void set(int offset, float[] values, int valOffset, int length)
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
    public PetrifyableTFloatArrayList subList(int begin, int end)
    {
        return new PetrifyableTFloatArrayList(myList.subList(begin, end));
    }

    @Override
    public float sum()
    {
        return myList.sum();
    }

    @Override
    public float[] toArray()
    {
        return myList.toArray();
    }

    @Override
    public float[] toArray(float[] dest)
    {
        return myList.toArray(dest);
    }

    @Override
    public float[] toArray(float[] dest, int offset, int len)
    {
        return myList.toArray(dest, offset, len);
    }

    @Override
    public float[] toArray(float[] dest, int sourcePos, int destPos, int len)
    {
        return myList.toArray(dest, sourcePos, destPos, len);
    }

    @Override
    public float[] toArray(int offset, int len)
    {
        return myList.toArray(offset, len);
    }

    @Override
    public void transformValues(TFloatFunction function)
    {
        myList.transformValues(function);
    }
}
