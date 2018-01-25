package io.opensphere.analysis.util.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

import io.opensphere.core.util.lang.ToStringHelper;

/** A LongBlock that stores a contiguous range. */
public class RangeLongBlock implements LongBlock
{
    /** The min. */
    private long myMin = Long.MAX_VALUE;

    /** The max. */
    private long myMax = Long.MIN_VALUE;

    /** The size. */
    private int mySize;

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
    public boolean contains(long value)
    {
        return mySize > 0 && myMin <= value && value <= myMax;
    }

    @Override
    public boolean add(long value)
    {
        if (!canAdd(value))
        {
            throw new IllegalArgumentException("Can't add " + value + " to " + toString());
        }
        if (mySize == 0)
        {
            myMin = value;
        }
        myMax = value;
        ++mySize;
        return true;
    }

    @Override
    public boolean remove(long value)
    {
        if (!canRemove(value))
        {
            throw new IllegalArgumentException("Can't remove " + value + " from " + toString());
        }
        if (value == myMin)
        {
            ++myMin;
        }
        else
        {
            --myMax;
        }
        --mySize;
        return true;
    }

    @Override
    public void clear()
    {
        mySize = 0;
        myMin = Long.MAX_VALUE;
        myMax = Long.MIN_VALUE;
    }

    @Override
    public long get(int index)
    {
        if (mySize == 0 || index < 0 || index >= mySize)
        {
            throw new IndexOutOfBoundsException();
        }
        return myMin + index;
    }

    @Override
    public long remove(int index)
    {
        long value = get(index);
        remove(value);
        return value;
    }

    @Override
    public int indexOf(long value)
    {
        return contains(value) ? (int)(value - myMin) : -1;
    }

    @Override
    public boolean canAdd(long value)
    {
        return mySize == 0 || value == myMax + 1;
    }

    @Override
    public boolean canRemove(long value)
    {
        return mySize > 0 && (value == myMin || value == myMax);
    }

    @Override
    public Iterator<Long> iterator()
    {
        return new Iterator<Long>()
        {
            /** The next value. */
            private long myNextValue = myMin;

            @Override
            public boolean hasNext()
            {
                return mySize > 0 && myNextValue <= myMax;
            }

            @Override
            public Long next() throws NoSuchElementException
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }
                long next = myNextValue;
                ++myNextValue;
                return Long.valueOf(next);
            }
        };
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("size", mySize);
        if (mySize > 0)
        {
            helper.add("min", myMin);
            helper.add("max", myMax);
        }
        return helper.toString();
    }

    /**
     * Splits the block into two blocks at the value.
     *
     * @param value the value on which to split
     * @return the two split blocks
     */
    RangeLongBlock[] split(long value)
    {
        RangeLongBlock[] blocks = new RangeLongBlock[2];
        blocks[0] = new RangeLongBlock();
        blocks[0].myMin = myMin;
        blocks[0].myMax = value - 1;
        blocks[0].mySize = (int)(value - myMin);
        blocks[1] = new RangeLongBlock();
        blocks[1].myMin = value + 1;
        blocks[1].myMax = myMax;
        blocks[1].mySize = (int)(myMax - value);
        return blocks;
    }
}
