package io.opensphere.core.util.collections.petrifyable;

import java.util.Collection;
import java.util.Random;

import gnu.trove.TCharCollection;
import gnu.trove.TCollections;
import gnu.trove.function.TCharFunction;
import gnu.trove.iterator.TCharIterator;
import gnu.trove.list.TCharList;
import gnu.trove.list.array.TCharArrayList;
import gnu.trove.procedure.TCharProcedure;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * A list of primitive chars backed by a {@link TCharArrayList} that is also
 * {@link Petrifyable}.
 */
@SuppressWarnings("PMD.GodClass")
public class PetrifyableTCharArrayList extends AbstractPetrifyable implements PetrifyableTCharList
{
    /** The wrapped list. */
    private TCharList myList;

    /**
     * Create a new empty collection with the default capacity.
     */
    public PetrifyableTCharArrayList()
    {
        myList = new TCharArrayList();
    }

    /**
     * Create a new instance using an array of chars for the initial values.
     * <p>
     * NOTE: This constructor copies the given array.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTCharArrayList(char[] values)
    {
        myList = new TCharArrayList(values);
    }

    /**
     * Create a new empty collection with a certain capacity.
     *
     * @param capacity The number of chars that can be held by the list.
     */
    public PetrifyableTCharArrayList(int capacity)
    {
        myList = new TCharArrayList(capacity);
    }

    /**
     * Create a new instance from another collection.
     * <p>
     * NOTE: This constructor iterates over the contents of the given
     * collection, so it may be slow for large arrays.
     *
     * @param values The initial values for the list.
     */
    public PetrifyableTCharArrayList(TCharCollection values)
    {
        myList = new TCharArrayList(values);
    }

    @Override
    public boolean add(char val)
    {
        return myList.add(val);
    }

    @Override
    public void add(char[] vals)
    {
        myList.add(vals);
    }

    @Override
    public void add(char[] vals, int offset, int length)
    {
        myList.add(vals, offset, length);
    }

    @Override
    public boolean addAll(char[] array)
    {
        return myList.addAll(array);
    }

    @Override
    public boolean addAll(Collection<? extends Character> collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public boolean addAll(TCharCollection collection)
    {
        return myList.addAll(collection);
    }

    @Override
    public int binarySearch(char value)
    {
        return myList.binarySearch(value);
    }

    @Override
    public int binarySearch(char value, int fromIndex, int toIndex)
    {
        return myList.binarySearch(value, fromIndex, toIndex);
    }

    @Override
    public void clear()
    {
        myList.clear();
    }

    @Override
    public boolean contains(char value)
    {
        return myList.contains(value);
    }

    @Override
    public boolean containsAll(char[] array)
    {
        return myList.containsAll(array);
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public boolean containsAll(TCharCollection collection)
    {
        return myList.containsAll(collection);
    }

    @Override
    public void fill(char val)
    {
        myList.fill(val);
    }

    @Override
    public void fill(int fromIndex, int toIndex, char val)
    {
        myList.fill(fromIndex, toIndex, val);
    }

    @Override
    public boolean forEach(TCharProcedure procedure)
    {
        return myList.forEach(procedure);
    }

    @Override
    public boolean forEachDescending(TCharProcedure procedure)
    {
        return myList.forEachDescending(procedure);
    }

    @Override
    public char get(int offset)
    {
        return myList.get(offset);
    }

    @Override
    public char getNoEntryValue()
    {
        return myList.getNoEntryValue();
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.BOOLEAN_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES
                        + Constants.CHAR_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + size() * Constants.CHAR_SIZE_BYTES,
                        Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    @Override
    public PetrifyableTCharArrayList grep(TCharProcedure condition)
    {
        return new PetrifyableTCharArrayList(myList.grep(condition));
    }

    @Override
    public int indexOf(char value)
    {
        return myList.indexOf(value);
    }

    @Override
    public int indexOf(int offset, char value)
    {
        return myList.indexOf(offset, value);
    }

    @Override
    public void insert(int offset, char value)
    {
        myList.insert(offset, value);
    }

    @Override
    public void insert(int offset, char[] values)
    {
        myList.insert(offset, values);
    }

    @Override
    public void insert(int offset, char[] values, int valOffset, int len)
    {
        myList.insert(offset, values, valOffset, len);
    }

    @Override
    public PetrifyableTCharArrayList inverseGrep(TCharProcedure condition)
    {
        return new PetrifyableTCharArrayList(myList.inverseGrep(condition));
    }

    @Override
    public boolean isEmpty()
    {
        return myList.isEmpty();
    }

    @Override
    public TCharIterator iterator()
    {
        return myList.iterator();
    }

    @Override
    public int lastIndexOf(char value)
    {
        return myList.lastIndexOf(value);
    }

    @Override
    public int lastIndexOf(int offset, char value)
    {
        return myList.lastIndexOf(offset, value);
    }

    @Override
    public char max()
    {
        return myList.max();
    }

    @Override
    public char min()
    {
        return myList.min();
    }

    @Override
    public synchronized void petrify()
    {
        if (!isPetrified())
        {
            super.petrify();

            ((TCharArrayList)myList).trimToSize();
            myList = TCollections.unmodifiableList(myList);
        }
    }

    @Override
    public boolean remove(char value)
    {
        return myList.remove(value);
    }

    @Override
    public void remove(int offset, int length)
    {
        myList.remove(offset, length);
    }

    @Override
    public boolean removeAll(char[] array)
    {
        return myList.removeAll(array);
    }

    @Override
    public boolean removeAll(Collection<?> collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public boolean removeAll(TCharCollection collection)
    {
        return myList.removeAll(collection);
    }

    @Override
    public char removeAt(int offset)
    {
        return myList.removeAt(offset);
    }

    @Override
    public char replace(int offset, char val)
    {
        return myList.replace(offset, val);
    }

    @Override
    public boolean retainAll(char[] array)
    {
        return myList.retainAll(array);
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        return myList.retainAll(collection);
    }

    @Override
    public boolean retainAll(TCharCollection collection)
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
    public char set(int offset, char val)
    {
        return myList.set(offset, val);
    }

    @Override
    public void set(int offset, char[] values)
    {
        myList.set(offset, values);
    }

    @Override
    public void set(int offset, char[] values, int valOffset, int length)
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
    public PetrifyableTCharArrayList subList(int begin, int end)
    {
        return new PetrifyableTCharArrayList(myList.subList(begin, end));
    }

    @Override
    public char sum()
    {
        return myList.sum();
    }

    @Override
    public char[] toArray()
    {
        return myList.toArray();
    }

    @Override
    public char[] toArray(char[] dest)
    {
        return myList.toArray(dest);
    }

    @Override
    public char[] toArray(char[] dest, int offset, int len)
    {
        return myList.toArray(dest, offset, len);
    }

    @Override
    public char[] toArray(char[] dest, int sourcePos, int destPos, int len)
    {
        return myList.toArray(dest, sourcePos, destPos, len);
    }

    @Override
    public char[] toArray(int offset, int len)
    {
        return myList.toArray(offset, len);
    }

    @Override
    public void transformValues(TCharFunction function)
    {
        myList.transformValues(function);
    }
}
