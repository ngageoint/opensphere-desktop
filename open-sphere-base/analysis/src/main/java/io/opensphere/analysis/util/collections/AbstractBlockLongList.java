package io.opensphere.analysis.util.collections;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import io.opensphere.core.util.collections.LongIterableIterator;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ToStringHelper;

/**
 * Abstract class for implementing a List of blocked longs.
 */
public abstract class AbstractBlockLongList extends AbstractList<Long>
{
    /** The size. */
    private int mySize;

    /** The blocks. */
    private final List<LongBlock> myBlocks;

    /** The min. */
    private long myMin = Long.MAX_VALUE;

    /** The max. */
    private long myMax = Long.MIN_VALUE;

    /**
     * Constructor.
     */
    public AbstractBlockLongList()
    {
        myBlocks = New.list();
    }

    @Override
    public int size()
    {
        return mySize;
    }

    @Override
    public boolean isEmpty()
    {
        return mySize == 0;
    }

    @Override
    public boolean contains(Object o)
    {
        return o instanceof Long && contains(((Long)o).longValue());
    }

    @Override
    public Iterator<Long> iterator()
    {
        return new LongIterableIterator(myBlocks);
    }

    @Override
    public boolean add(Long e)
    {
        return add(e.longValue());
    }

    @Override
    public boolean remove(Object o)
    {
        return o instanceof Long && removeValue(((Long)o).longValue());
    }

    @Override
    public void clear()
    {
        mySize = 0;
        myBlocks.clear();
        myMin = Long.MAX_VALUE;
        myMax = Long.MIN_VALUE;
    }

    @Override
    public Long get(int index)
    {
        if (index >= mySize || index < 0)
        {
            throw new IndexOutOfBoundsException();
        }
        if (myBlocks.size() == 1)
        {
            return Long.valueOf(myBlocks.get(0).get(index));
        }
        else
        {
            int curSize = 0;
            int nextSize = 0;
            for (LongBlock block : myBlocks)
            {
                nextSize += block.size();
                if (index < nextSize)
                {
                    return Long.valueOf(block.get(index - curSize));
                }
                curSize = nextSize;
            }
        }
        return null;
    }

    @Override
    public Long remove(int index)
    {
        Long value = get(index);
        removeValue(value.longValue());
        return value;
    }

    @Override
    public int indexOf(Object o)
    {
        return o instanceof Long ? indexOf(((Long)o).longValue()) : -1;
    }

    /**
     * Adds a value to the list.
     *
     * @param value the value
     * @return whether the value was added
     */
    public boolean add(long value)
    {
        LongBlock lastBlock = !myBlocks.isEmpty() ? myBlocks.get(myBlocks.size() - 1) : null;
        if (lastBlock != null && lastBlock.canAdd(value))
        {
            lastBlock.add(value);
        }
        else
        {
            LongBlock block = newBlock();
            block.add(value);
            myBlocks.add(block);
        }
        ++mySize;
        if (value < myMin)
        {
            myMin = value;
        }
        if (value > myMax)
        {
            myMax = value;
        }
        return true;
    }

    /**
     * Removes the value from the list.
     *
     * @param value the value
     * @return whether the value was removed
     */
    public boolean removeValue(long value)
    {
        boolean removed = false;
        for (int blockIndex = 0; blockIndex < myBlocks.size(); ++blockIndex)
        {
            LongBlock block = myBlocks.get(blockIndex);
            if (block.contains(value))
            {
                if (block.canRemove(value))
                {
                    block.remove(value);
                    if (block.isEmpty())
                    {
                        myBlocks.remove(blockIndex);
                    }
                }
                else
                {
                    handleCantRemove(value, blockIndex);
                }
                --mySize;
                if (value == myMin)
                {
                    myMin = stream().min(Long::compareTo).orElse(Long.valueOf(Long.MAX_VALUE)).longValue();
                }
                if (value == myMax)
                {
                    myMax = stream().max(Long::compareTo).orElse(Long.valueOf(Long.MIN_VALUE)).longValue();
                }
                removed = true;
                break;
            }
        }
        return removed;
    }

    /**
     * Gets the value at the index.
     *
     * @param index the index
     * @return the value
     */
    public long getValue(int index)
    {
        return get(index).longValue();
    }

    /**
     * Determines if the list contains the value.
     *
     * @param value the value
     * @return whether the list contains the value
     */
    public boolean contains(long value)
    {
        boolean contains = false;
        boolean mightContain = mySize > 0 && myMin <= value && value <= myMax;
        if (mightContain)
        {
            for (LongBlock block : myBlocks)
            {
                if (block.contains(value))
                {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    /**
     * Gets the index of the value, or -1.
     *
     * @param value the value
     * @return the index of the value, or -1
     */
    public int indexOf(long value)
    {
        int index = -1;
        int size = 0;
        for (LongBlock block : myBlocks)
        {
            index = block.indexOf(value);
            if (index != -1)
            {
                index += size;
                break;
            }
            size += block.size();
        }
        return index;
    }

    /**
     * Gets the min.
     *
     * @return the min
     */
    public long min()
    {
        if (mySize == 0)
        {
            throw new IllegalStateException("cannot find minimum of an empty list");
        }
        return myMin;
    }

    /**
     * Gets the max.
     *
     * @return the max
     */
    public long max()
    {
        if (mySize == 0)
        {
            throw new IllegalStateException("cannot find maximum of an empty list");
        }
        return myMax;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        int i = 0;
        helper.add("size", mySize);
        helper.add("min", myMin);
        helper.add("max", myMax);
        float ratio = mySize != 0 ? (float)getBlockSize() / mySize : 0f;
        helper.add("ratio", Float.valueOf(ratio));
        for (LongBlock block : myBlocks)
        {
            helper.add(Integer.toString(i), block);
            ++i;
        }
        return helper.toStringMultiLine();
    }

    /**
     * Creates a new block.
     *
     * @return the new block
     */
    protected abstract LongBlock newBlock();

    /**
     * Handles when the value can't be removed from the block index.
     *
     * @param value the value to remove
     * @param blockIndex the index of the block containing the value
     */
    protected abstract void handleCantRemove(long value, int blockIndex);

    /**
     * Gets the blocks.
     *
     * @return the blocks
     */
    protected List<LongBlock> getBlocks()
    {
        return myBlocks;
    }

    /**
     * Gets the number of blocks.
     *
     * @return the block size
     */
    int getBlockSize()
    {
        return myBlocks.size();
    }
}
