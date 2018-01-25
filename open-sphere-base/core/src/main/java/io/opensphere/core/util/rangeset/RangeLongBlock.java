package io.opensphere.core.util.rangeset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import io.opensphere.core.model.RangeRelationType;

/**
 * The block of contiguous long values that represent a range.
 *
 * Also provides static utility functions for quickly creating sets of
 * RageLongBlock to represent arrays or collections of long values.
 */
public class RangeLongBlock implements Comparable<RangeLongBlock>, Iterable<Long>
{
    /** The start id. */
    private long myStartValue;

    /** The end id. */
    private long myEndValue;

    /**
     * Creates the smallest number of RangeLongBlocks that can represent the
     * values found in the provided array.
     *
     * @param values - the {@link Collection} of {@link Long} to use to create
     *            blocks.
     * @return the {@link List} of {@link RangeLongBlock}
     */
    public static List<RangeLongBlock> createRangeLongBlocks(Collection<? extends Long> values)
    {
        List<RangeLongBlock> resultBlocks = new LinkedList<>();

        if (values != null && !values.isEmpty())
        {
            if (values.size() == 1)
            {
                long val = values.iterator().next().longValue();
                resultBlocks.add(new RangeLongBlock(val));
            }
            else
            {
                List<Long> valuesToUse = new ArrayList<>(values);
                Collections.sort(valuesToUse);
                Iterator<Long> valueItr = valuesToUse.iterator();
                long lastStart = valueItr.next().longValue();
                long lastVal = lastStart;
                long curVal = 0;
                while (valueItr.hasNext())
                {
                    curVal = valueItr.next().longValue();
                    if (curVal != lastVal && curVal != lastVal + 1)
                    {
                        resultBlocks.add(new RangeLongBlock(lastStart, lastVal));
                        lastStart = curVal;
                    }

                    if (!valueItr.hasNext())
                    {
                        resultBlocks.add(new RangeLongBlock(lastStart, curVal));
                    }

                    lastVal = curVal;
                }
            }
        }
        return resultBlocks;
    }

    /**
     * Creates the smallest number of RangeLongBlocks that can represent the
     * values found in the provided array.
     *
     * @param values - the value list to use to create blocks.
     * @return the {@link List} of {@link RangeLongBlock}
     */
    public static List<RangeLongBlock> createRangeLongBlocks(long[] values)
    {
        List<RangeLongBlock> resultBlocks = new LinkedList<>();

        if (values.length == 1)
        {
            resultBlocks.add(new RangeLongBlock(values[0]));
        }
        else
        {
            long[] valuesToUse = Arrays.copyOf(values, values.length);
            Arrays.sort(valuesToUse);
            long lastStart = valuesToUse[0];
            long lastVal = valuesToUse[0];
            for (int i = 1; i < valuesToUse.length; i++)
            {
                if (valuesToUse[i] != lastVal && valuesToUse[i] != lastVal + 1)
                {
                    resultBlocks.add(new RangeLongBlock(lastStart, lastVal));
                    lastStart = valuesToUse[i];
                }

                if (i == valuesToUse.length - 1)
                {
                    resultBlocks.add(new RangeLongBlock(lastStart, valuesToUse[i]));
                }

                lastVal = valuesToUse[i];
            }
        }
        return resultBlocks;
    }

    /**
     * Creates the smallest number of RangeLongBlocks that can represent the
     * values found in the provided array.
     *
     * @param values - the value list to use to create blocks.
     * @return the {@link List} of {@link RangeLongBlock}
     */
    public static List<RangeLongBlock> createRangeLongBlocks(Long[] values)
    {
        List<RangeLongBlock> resultBlocks = null;
        if (values != null && values.length > 0)
        {
            long[] valArray = new long[values.length];
            for (int i = 0; i < values.length; i++)
            {
                valArray[i] = values[i].longValue();
            }

            resultBlocks = createRangeLongBlocks(valArray);
        }
        if (resultBlocks == null)
        {
            resultBlocks = new ArrayList<>();
        }
        return resultBlocks;
    }

    /**
     * Creates a {@link List} of {@link RangeLongBlock} that represents the
     * resultant sets after the specified value is removed from the provided
     * set. If the value to remove is not contained within the set, the provided
     * block is the only block returned in the result set. If the value to
     * remove is either the beginning or end value of the provided block only
     * one new block will be returned representing the new block with either the
     * first or last value removed. If the value to remove is in the interior of
     * the block two blocks will be returned representing the sub-sections of
     * the original block.
     *
     * If the provided block contains only the value to remove the set will be
     * empty.
     *
     * @param block the block
     * @param valueToRemove the value to remove
     * @return the list
     */
    public static List<RangeLongBlock> createSubBlocksByRemovingValue(RangeLongBlock block, long valueToRemove)
    {
        List<RangeLongBlock> resultBlockSet = new LinkedList<>();
        if (block.containsValue(valueToRemove))
        {
            if (valueToRemove == block.myStartValue)
            {
                if (block.size() > 1)
                {
                    resultBlockSet.add(new RangeLongBlock(block.myStartValue + 1, block.myEndValue));
                }
            }
            else if (valueToRemove == block.myEndValue)
            {
                if (block.size() > 1)
                {
                    resultBlockSet.add(new RangeLongBlock(block.myStartValue, block.myEndValue - 1));
                }
            }
            else
            {
                resultBlockSet.add(new RangeLongBlock(block.myStartValue, valueToRemove - 1));
                resultBlockSet.add(new RangeLongBlock(valueToRemove + 1, block.myEndValue));
            }
        }
        else
        {
            resultBlockSet.add(block);
        }
        return resultBlockSet;
    }

    /**
     * Checks if a array of values contains contiguous values. Where each value
     * is one greater than the previous value.
     *
     * Note: array of values must be sorted in ascending order.
     *
     * @param values the values
     * @return true, if is contiguous
     */
    public static boolean isContiguous(long[] values)
    {
        if (values.length == 1)
        {
            return true;
        }

        boolean contiguous = true;
        for (int i = 1; i < values.length && contiguous; i++)
        {
            if (values[i - 1] != values[i] && values[i - 1] != values[i] - 1)
            {
                contiguous = false;
                break;
            }
        }
        return contiguous;
    }

    /**
     * Creates a new RangeLongBlock that represents the merged blocks provided
     * they are contiguous. Or null if they don't overlap.
     *
     * @param one the first block
     * @param two the second block
     * @return the merged block or null if the two blocks don't overlap.
     */
    public static RangeLongBlock merge(RangeLongBlock one, RangeLongBlock two)
    {
        RangeLongBlock merged = null;
        if (one.formsContiguousRange(two))
        {
            long start = one.myStartValue < two.myStartValue ? one.myStartValue : two.myStartValue;
            long end = one.myEndValue > two.myEndValue ? one.myEndValue : two.myEndValue;
            merged = new RangeLongBlock(start, end);
        }
        return merged;
    }

    /**
     * Single value block constructor.
     *
     * @param value - the value for the block
     */
    public RangeLongBlock(long value)
    {
        this(value, value);
    }

    /**
     * Instantiates a new RangeLongBlock.
     *
     * @param startValue the start value in the range
     * @param endValue the end value in the range
     */
    public RangeLongBlock(long startValue, long endValue)
    {
        if (startValue < endValue)
        {
            myStartValue = startValue;
            myEndValue = endValue;
        }
        else
        {
            myStartValue = endValue;
            myEndValue = startValue;
        }
    }

    /**
     * Copy constructor.
     *
     * @param other - other block to copy
     */
    public RangeLongBlock(RangeLongBlock other)
    {
        this(other.myStartValue, other.myEndValue);
    }

    /**
     * True if the value borders this block. i.e. is one less than the start of
     * the block or is one greater than the end.
     *
     * @param value the value to check
     * @return true, if successful
     */
    public boolean borders(long value)
    {
        return value == myStartValue - 1 || value == myEndValue + 1;
    }

    /**
     * True if the specified block borders this block such that the start of the
     * specified block is directly after the end of this block or the end of the
     * specified block is directly before the start of this block.
     *
     * @param block the block to test for bordering.
     * @return true, if borders
     */
    public boolean borders(RangeLongBlock block)
    {
        return block.myStartValue == myEndValue + 1 || block.myEndValue == myStartValue - 1;
    }

    @Override
    public int compareTo(RangeLongBlock o)
    {
        if (myStartValue == o.myStartValue && myEndValue == o.myEndValue)
        // Equality
        {
            return 0;
        }
        else if (myEndValue < o.myStartValue || o.myEndValue < myStartValue)
        // Does not overlap
        {
            return myEndValue < o.myStartValue ? -1 : 1;
        }
        else
        // Overlaps
        {
            return myStartValue < o.myStartValue ? -1 : 1;
        }
    }

    /**
     * Checks to see if this set contains the specified value.
     *
     * @param value the value to check for containment.
     * @return true, if contained in range (inclusive)
     */
    public boolean containsValue(long value)
    {
        return value >= myStartValue && value <= myEndValue;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        RangeLongBlock other = (RangeLongBlock)obj;
        return myEndValue == other.myEndValue && myStartValue == other.myStartValue;
    }

    /**
     * Returns true if the specified block can combine with this block and form
     * a contiguous range block.
     *
     * @param other the other to test against
     * @return true, if it forms a contiguous block.
     */
    public boolean formsContiguousRange(RangeLongBlock other)
    {
        RangeRelationType relation = getRelation(other);
        return relation != RangeRelationType.BEFORE && relation != RangeRelationType.AFTER;
    }

    /**
     * Gets the last value in the range.
     *
     * @return the last value
     */
    public long getEnd()
    {
        return myEndValue;
    }

    /**
     * Gets the relation.
     *
     * @param block the block
     * @return the relation
     */
    public RangeRelationType getRelation(RangeLongBlock block)
    {
        RangeRelationType result = null;
        if (block.myEndValue < myStartValue)
        {
            if (block.myEndValue == myStartValue - 1)
            {
                result = RangeRelationType.BORDERS_BEFORE;
            }
            else
            {
                result = RangeRelationType.BEFORE;
            }
        }
        else if (block.myStartValue > myEndValue)
        {
            if (block.myStartValue - 1 == myEndValue)
            {
                result = RangeRelationType.BORDERS_AFTER;
            }
            else
            {
                result = RangeRelationType.AFTER;
            }
        }
        else if (equals(block))
        {
            result = RangeRelationType.EQUAL;
        }
        else
        // Overlaps, is contained or contains.
        {
            if (block.myStartValue >= myStartValue && block.myEndValue <= myEndValue)
            {
                result = RangeRelationType.SUBSET;
            }
            else if (block.myStartValue <= myStartValue && block.myEndValue >= myEndValue)
            {
                result = RangeRelationType.SUPERSET;
            }
            else if (block.myStartValue < myStartValue && block.myEndValue <= myEndValue)
            {
                result = RangeRelationType.OVERLAPS_FRONT_EDGE;
            }
            else
            {
                result = RangeRelationType.OVERLAPS_BACK_EDGE;
            }
        }
        return result;
    }

    /**
     * Gets the first value in the range.
     *
     * @return the first value
     */
    public long getStart()
    {
        return myStartValue;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(myEndValue ^ myEndValue >>> 32);
        result = prime * result + (int)(myStartValue ^ myStartValue >>> 32);
        return result;
    }

    /**
     * Returns a {@link RangeLongBlock} that represents the intersection of the
     * test block with this block.
     *
     * @param other the other block to check for intersection.
     * @return the RangeLongBlock or null if no intersection.
     */
    public RangeLongBlock intersection(RangeLongBlock other)
    {
        RangeLongBlock intersection = null;
        RangeRelationType relation = getRelation(other);
        switch (relation)
        {
            case AFTER:
            case BEFORE:
            case BORDERS_AFTER:
            case BORDERS_BEFORE:
                // Do nothing there is no intersection.
                break;
            case EQUAL:
            case SUPERSET:
                intersection = new RangeLongBlock(myStartValue, myEndValue);
                break;
            case OVERLAPS_BACK_EDGE:
                intersection = new RangeLongBlock(other.myStartValue, myEndValue);
                break;
            case OVERLAPS_FRONT_EDGE:
                intersection = new RangeLongBlock(myStartValue, other.myEndValue);
                break;
            case SUBSET:
                intersection = new RangeLongBlock(other.myStartValue, other.myEndValue);
                break;
            default:
                break;
        }
        return intersection;
    }

    /**
     * Checks if this block is after the specified block i.e. {@code other.end
     * < this.start}
     *
     * @param other the other
     * @return true, if is after
     */
    public boolean isAfter(RangeLongBlock other)
    {
        return other.myEndValue < myStartValue;
    }

    /**
     * Checks if this block is before the specified block i.e. other.start &gt;
     * this.end this.start.
     *
     * @param other the other
     * @return true, if is before
     */
    public boolean isBefore(RangeLongBlock other)
    {
        return other.myStartValue > myEndValue;
    }

    @Override
    public Iterator<Long> iterator()
    {
        return new BlockLongIterator(this);
    }

    /**
     * Tests to see if another RangeLongBlock overlaps this one.
     *
     * @param other the other to test against this block
     * @return true, if overlaps.
     */
    public boolean overlaps(RangeLongBlock other)
    {
        return containsValue(other.myStartValue) || containsValue(other.myEndValue) || other.containsValue(myStartValue)
                || other.containsValue(myEndValue);
    }

    /**
     * Returns the total number of values in the set.
     *
     * @return the number in the set
     */
    public int size()
    {
        return (int)(myEndValue - myStartValue) + 1;
    }

    @Override
    public String toString()
    {
        return myStartValue == myEndValue ? "[" + myStartValue + "]" : "[" + myStartValue + " to " + myEndValue + "]";
    }

    /**
     * Expands this block to additionally include the specified value but only
     * if it is a bordering value. i.e. one less than start, or one greater than
     * end. Otherwise it will throw an exception. Expanding on an value that is
     * already contained does nothing.
     *
     * @param value the value to use to expand this block.
     * @throws IllegalArgumentException If the value does not neighbor this
     *             block.
     */
    protected void expand(long value)
    {
        if (value < myStartValue - 1 || value > myEndValue + 1)
        {
            throw new IllegalArgumentException("Expanding RangeLongBlock Can Only Be Done With Neighboring Values");
        }
        else
        {
            if (value == myStartValue - 1)
            {
                myStartValue = value;
            }
            else if (value == myEndValue + 1)
            {
                myEndValue = value;
            }
        }
    }

    /**
     * If the specified block overlaps, is contained with, is a super set of
     * this block, it will alter this block to encompass the entire value range
     * represented by both blocks.
     *
     * @param other the other block to merge with.
     * @return true if the block is changed as a result of the merge, false if
     *         unaltered
     * @throws IllegalArgumentException if the other block will not form a
     *             continuous range with this block.
     */
    protected boolean merge(RangeLongBlock other)
    {
        RangeRelationType relation = getRelation(other);
        if (relation == RangeRelationType.AFTER || relation == RangeRelationType.BEFORE)
        {
            throw new IllegalArgumentException("Cannot merge non contiguous blocks together");
        }
        else
        {
            long oldStart = myStartValue;
            long oldEnd = myEndValue;
            myStartValue = myStartValue < other.myStartValue ? myStartValue : other.myStartValue;
            myEndValue = myEndValue > other.myEndValue ? myEndValue : other.myEndValue;
            return myStartValue != oldStart || myEndValue != oldEnd;
        }
    }

    /**
     * Sets the range for the block. Only for use by the {@link RangedLongSet}.
     *
     * @param start the start value for the range.
     * @param end the end value for the range
     */
    protected void setRange(long start, long end)
    {
        if (start < end)
        {
            myStartValue = start;
            myEndValue = end;
        }
        else
        {
            myStartValue = end;
            myEndValue = start;
        }
    }

    /**
     * The Class BlockLongIterator.
     */
    public static class BlockLongIterator implements Iterator<Long>
    {
        /** The my started. */
        private boolean myStarted;

        /** The last value. */
        private final long myLastValue;

        /** The current value. */
        private long myCurrentValue;

        /**
         * Instantiates a new block long iterator.
         *
         * @param block the block
         */
        public BlockLongIterator(RangeLongBlock block)
        {
            myCurrentValue = block.myStartValue;
            myLastValue = block.myEndValue;
        }

        @Override
        public boolean hasNext()
        {
            return !myStarted || myCurrentValue < myLastValue;
        }

        @Override
        public Long next()
        {
            if (hasNext())
            {
                if (myStarted)
                {
                    myCurrentValue++;
                }
                else
                {
                    myStarted = true;
                }
                return Long.valueOf(myCurrentValue);
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
