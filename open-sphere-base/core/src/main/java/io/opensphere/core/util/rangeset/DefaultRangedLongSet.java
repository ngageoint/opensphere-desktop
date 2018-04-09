package io.opensphere.core.util.rangeset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.model.RangeRelationType;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.LongIterableIterator;

/**
 * Default implementation of a {@link RangedLongSet}.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultRangedLongSet implements RangedLongSet
{
    /**
     * Indicates that the value count is in a stale state and needs to be
     * recomputed.
     */
    private boolean myValueCountStale;

    /** The value count. */
    private long myValueCount;

    /** The block list lock. */
    private final ReentrantLock myBlockListLock;

    /** The block list. */
    private List<RangeLongBlock> myBlockList;

    /**
     * Instantiates a new RangedLongSet.
     */
    public DefaultRangedLongSet()
    {
        myBlockListLock = new ReentrantLock();
        myValueCountStale = true;
        myBlockList = new LinkedList<>();
    }

    /**
     * Instantiates a new RangedLongSet.
     *
     * @param values the values
     */
    public DefaultRangedLongSet(Collection<Long> values)
    {
        this();
        myBlockList.addAll(RangeLongBlock.createRangeLongBlocks(values));
    }

    /**
     * Copy constructor ( deep copy ).
     *
     * @param other the other set to copy
     */
    public DefaultRangedLongSet(DefaultRangedLongSet other)
    {
        this();
        myBlockList = other.cloneBlockList();
        myValueCount = other.myValueCount;
        myValueCountStale = other.myValueCountStale;
    }

    /**
     * Instantiates a new RangedLongSet with a single value.
     *
     * @param singularValue the single value
     */
    public DefaultRangedLongSet(long singularValue)
    {
        this();
        myBlockList.add(new RangeLongBlock(singularValue));
    }

    /**
     * Instantiates a new RangedLongSet.
     *
     * @param values the values
     */
    public DefaultRangedLongSet(long[] values)
    {
        this();
        Utilities.checkNull(values, "values");
        if (values.length > 0)
        {
            myBlockList.addAll(RangeLongBlock.createRangeLongBlocks(values));
        }
    }

    /**
     * Instantiates a new RangedLongSet.
     *
     * @param values the values
     */
    public DefaultRangedLongSet(Long[] values)
    {
        this();
        myBlockList.addAll(RangeLongBlock.createRangeLongBlocks(values));
    }

    /**
     * Instantiates a new RangedLongSet with a specified block.
     *
     * @param block the block
     */
    public DefaultRangedLongSet(RangeLongBlock block)
    {
        this();
        myBlockList.add(block);
    }

    @Override
    public boolean add(long value)
    {
        return addBlock(new RangeLongBlock(value));
    }

    /**
     * Adds the value to the set.
     *
     * @param value the value to add
     * @return true if this set did not already contain the specified element
     */
    @Override
    public boolean add(Long value)
    {
        return addBlock(new RangeLongBlock(value.longValue()));
    }

    @Override
    public boolean add(RangedLongSet otherSet)
    {
        return addBlocks(otherSet.getBlocks());
    }

    /**
     * Adds the values to the set.
     *
     * @param values the values to add to the set
     * @return true if the set was changed as a result of this call
     */
    @Override
    public boolean addAll(Collection<? extends Long> values)
    {
        boolean wasChanged = false;
        if (values != null && !values.isEmpty())
        {
            wasChanged = add(RangeLongBlock.createRangeLongBlocks(values));
        }
        return wasChanged;
    }

    @Override
    public boolean addAll(long[] values)
    {
        boolean wasChanged = false;
        if (values != null && values.length > 0)
        {
            wasChanged = add(RangeLongBlock.createRangeLongBlocks(values));
        }
        return wasChanged;
    }

    @Override
    public boolean addAll(Long[] values)
    {
        boolean wasChanged = false;
        if (values != null && values.length > 0)
        {
            wasChanged = add(RangeLongBlock.createRangeLongBlocks(values));
        }
        return wasChanged;
    }

    @Override
    public boolean addBlock(RangeLongBlock insertBlock)
    {
        boolean changedSomething = false;
        myBlockListLock.lock();
        try
        {
            int size = myBlockList.size();
            if (size == 0)
            {
                myBlockList.add(insertBlock);
                changedSomething = true;
            }
            else
            {
                if (size == 1)
                {
                    // Do quick match for the one block case.
                    if (addBlockWhenListHasOnlyOneBlock(insertBlock))
                    {
                        changedSomething = true;
                    }
                }
                else
                {
                    changedSomething = binarySearchInsert(insertBlock);
                }
            }

            if (changedSomething)
            {
                myValueCountStale = true;
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return changedSomething;
    }

    @Override
    public boolean addBlocks(Collection<RangeLongBlock> blocksToAdd)
    {
        boolean wasChanged = false;
        if (blocksToAdd != null && !blocksToAdd.isEmpty())
        {
            for (RangeLongBlock block : blocksToAdd)
            {
                if (addBlock(block))
                {
                    wasChanged = true;
                }
            }
        }
        return wasChanged;
    }

    @Override
    public int blockCount()
    {
        int count = 0;
        myBlockListLock.lock();
        try
        {
            count = myBlockList.size();
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return count;
    }

    @Override
    public void clear()
    {
        myBlockListLock.lock();
        try
        {
            myBlockList.clear();
        }
        finally
        {
            myBlockListLock.unlock();
        }
    }

    @Override
    public boolean contains(Long value)
    {
        return hasValue(value.longValue());
    }

    @Override
    public boolean contains(Object value)
    {
        Long lValue = getAsLong(value);
        if (lValue != null)
        {
            return contains(lValue);
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        boolean containsAll = false;
        Collection<Long> stuff = extractUsableElements(c);

        // If we lost anything in translation we can't contain
        // all of whatever was in the provided collection.
        if (stuff.size() == c.size())
        {
            myBlockListLock.lock();
            try
            {
                DefaultRangedLongSet newSet = new DefaultRangedLongSet(stuff);
                RangedLongSet intersection = this.getIntersection(newSet);
                containsAll = newSet.equals(intersection);
            }
            finally
            {
                myBlockListLock.unlock();
            }
        }
        return containsAll;
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
        DefaultRangedLongSet other = (DefaultRangedLongSet)obj;
        return Objects.equals(myBlockList, other.myBlockList);
    }

    @Override
    public List<RangeLongBlock> getBlocks()
    {
        List<RangeLongBlock> resultList = null;
        myBlockListLock.lock();
        try
        {
            resultList = cloneBlockList();
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return Collections.unmodifiableList(resultList);
    }

    @Override
    public RangedLongSet getDifference(long[] values)
    {
        return getDifference(new DefaultRangedLongSet(values));
    }

    @Override
    public RangedLongSet getDifference(RangedLongSet other)
    {
        DefaultRangedLongSet result = new DefaultRangedLongSet(this);
        result.remove(other);
        return result;
    }

    @Override
    public RangedLongSet getIntersection(Collection<Long> values)
    {
        return getIntersection(new DefaultRangedLongSet(values));
    }

    @Override
    public RangedLongSet getIntersection(long[] values)
    {
        return getIntersection(new DefaultRangedLongSet(values));
    }

    @Override
    public RangedLongSet getIntersection(Long[] values)
    {
        return getIntersection(new DefaultRangedLongSet(values));
    }

    @Override
    public RangedLongSet getIntersection(RangedLongSet otherSet)
    {
        DefaultRangedLongSet result = new DefaultRangedLongSet();
        if (otherSet != null)
        {
            myBlockListLock.lock();
            try
            {
                if (!otherSet.isEmpty())
                {
                    List<RangeLongBlock> overallIntersectList = new LinkedList<>();
                    for (RangeLongBlock block : otherSet.getBlocks())
                    {
                        List<RangeLongBlock> intersect = getIntersectionList(block);
                        if (intersect != null && !intersect.isEmpty())
                        {
                            overallIntersectList.addAll(intersect);
                        }
                    }
                    if (!overallIntersectList.isEmpty())
                    {
                        result.myBlockList.addAll(overallIntersectList);
                    }
                }
            }
            finally
            {
                myBlockListLock.unlock();
            }
        }
        return result;
    }

    @Override
    public RangedLongSet getIntersection(RangeLongBlock blockToIntersect)
    {
        DefaultRangedLongSet result = new DefaultRangedLongSet();
        if (blockToIntersect != null)
        {
            List<RangeLongBlock> intersect = getIntersectionList(blockToIntersect);
            if (intersect != null && !intersect.isEmpty())
            {
                result.myBlockList.addAll(intersect);
            }
        }
        return result;
    }

    @Override
    public Long getMaximum()
    {
        Long result = null;
        myBlockListLock.lock();
        try
        {
            if (!myBlockList.isEmpty())
            {
                result = Long.valueOf(myBlockList.get(myBlockList.size() - 1).getEnd());
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return result;
    }

    @Override
    public Long getMinimum()
    {
        Long result = null;
        myBlockListLock.lock();
        try
        {
            if (!myBlockList.isEmpty())
            {
                result = Long.valueOf(myBlockList.get(0).getStart());
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return result;
    }

    @Override
    public RangedLongSet getUnion(RangedLongSet other)
    {
        RangedLongSet unionSet = new DefaultRangedLongSet(this);
        unionSet.add(other);
        return unionSet;
    }

    @Override
    public long[] getValues() throws TooManyValuesToConstructArrayException
    {
        long[] idArray = null;
        int curIdx = 0;
        Iterator<Long> itr = null;

        myBlockListLock.lock();
        try
        {
            long count = valueCount();
            if (count < Integer.MAX_VALUE)
            {
                try
                {
                    idArray = new long[(int)count];
                }
                catch (OutOfMemoryError e)
                {
                    throw new TooManyValuesToConstructArrayException(
                            "Could not produce array, number of values \"" + count + "\" is too high.", e);
                }
                itr = iterator();

                while (itr.hasNext())
                {
                    idArray[curIdx] = itr.next().longValue();
                    curIdx++;
                }
            }
            else
            {
                throw new TooManyValuesToConstructArrayException(
                        "Could not produce array, number of values \"" + count + "\" is too high.");
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return idArray;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myBlockList == null ? 0 : myBlockList.hashCode());
        return result;
    }

    @Override
    public boolean hasValue(long value)
    {
        boolean found = false;
        myBlockListLock.lock();
        try
        {
            int size = myBlockList.size();

            if (size != 0)
            {
                if (size == 1)
                {
                    // Do quick match for the one block case.
                    found = myBlockList.get(0).containsValue(value);
                }
                else if (size == 2)
                {
                    // Do quick match for the two block case.
                    found = myBlockList.get(0).containsValue(value);
                    if (!found)
                    {
                        found = myBlockList.get(1).containsValue(value);
                    }
                }
                else
                {
                    // Use a binary search to locate the index in which this
                    // value
                    // would be inserted in the context of the blocks that are
                    // already
                    // in the list.
                    RangeLongBlock searchVal = new RangeLongBlock(value);
                    int idx = Collections.binarySearch(myBlockList, searchVal);

                    if (idx >= 0)
                    {
                        // Exact match case
                        found = true;
                    }
                    else
                    {
                        int insertIndex = -1 * (idx + 1);

                        // Check boundary conditions first.
                        if (insertIndex == 0)
                        {
                            // Check if we are before the first block if we
                            // overlap
                            // with the first block.
                            found = myBlockList.get(insertIndex).containsValue(value);
                        }
                        else if (insertIndex == size)
                        {
                            // Check if we are after the end block for overlap
                            // on
                            // the last block.
                            found = myBlockList.get(size - 1).containsValue(value);
                        }
                        else
                        {
                            // We fall between two block, so check each of the
                            // blocks we
                            // fall between.
                            found = myBlockList.get(insertIndex - 1).containsValue(value);
                            if (!found)
                            {
                                found = myBlockList.get(insertIndex).containsValue(value);
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return found;
    }

    @Override
    public boolean intersects(RangeLongBlock block)
    {
        boolean intersects = false;
        if (block != null)
        {
            myBlockListLock.lock();
            try
            {
                // First make sure that the block we are being asked to
                // intersect is
                // not before the beginning of our list
                // or after the end.
                if (!myBlockList.isEmpty() && !myBlockList.get(0).isAfter(block)
                        && !myBlockList.get(myBlockList.size() - 1).isBefore(block))
                {
                    int idx = Collections.binarySearch(myBlockList, block);

                    if (idx < 0)
                    {
                        int locIndex = -1 * (idx + 1);
                        if (locIndex > 0)
                        {
                            RangeLongBlock lowNeighbor = myBlockList.get(locIndex - 1);
                            RangeRelationType relation = lowNeighbor.getRelation(block);
                            if (relation == RangeRelationType.OVERLAPS_BACK_EDGE || relation == RangeRelationType.SUBSET)
                            {
                                intersects = true;
                            }
                        }

                        boolean forwardCheck = locIndex != myBlockList.size();

                        if (!intersects && forwardCheck)
                        {
                            ListIterator<RangeLongBlock> blockIter = myBlockList.listIterator(locIndex);
                            RangeLongBlock currentBlock = null;
                            while (!intersects && forwardCheck && blockIter.hasNext())
                            {
                                currentBlock = blockIter.next();
                                RangeRelationType relation = currentBlock.getRelation(block);
                                switch (relation)
                                {
                                    case OVERLAPS_BACK_EDGE:
                                    case SUPERSET:
                                        intersects = true;
                                        break;
                                    case OVERLAPS_FRONT_EDGE:
                                    case SUBSET:
                                    case EQUAL:
                                        intersects = true;
                                        break;
                                    default:
                                        forwardCheck = false;
                                        break;
                                }
                            }
                        }
                    }
                    else
                    {
                        intersects = true;
                    }
                }
            }
            finally
            {
                myBlockListLock.unlock();
            }
        }

        return intersects;
    }

    @Override
    public boolean isEmpty()
    {
        boolean empty = false;
        myBlockListLock.lock();
        try
        {
            empty = myBlockList.isEmpty();
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return empty;
    }

    @Override
    public Iterator<Long> iterator()
    {
        return new LongIterableIterator(getBlocks());
    }

    @Override
    public boolean remove(Collection<? extends Long> valueList)
    {
        boolean setWasChanged = false;
        if (valueList != null && !valueList.isEmpty())
        {
            setWasChanged = removeBlocks(RangeLongBlock.createRangeLongBlocks(valueList));
        }
        return setWasChanged;
    }

    @Override
    public boolean remove(long value)
    {
        return removeBlock(new RangeLongBlock(value));
    }

    @Override
    public boolean remove(Long value)
    {
        return remove(value.longValue());
    }

    @Override
    public boolean remove(long[] values)
    {
        boolean setWasChanged = false;
        if (values != null && values.length > 0)
        {
            setWasChanged = removeBlocks(RangeLongBlock.createRangeLongBlocks(values));
        }
        return setWasChanged;
    }

    @Override
    public boolean remove(Long[] values)
    {
        boolean setWasChanged = false;
        if (values != null && values.length > 0)
        {
            setWasChanged = removeBlocks(RangeLongBlock.createRangeLongBlocks(values));
        }
        return setWasChanged;
    }

    @Override
    public boolean remove(Object o)
    {
        boolean changedSet = false;
        Long val = getAsLong(o);
        if (val != null)
        {
            changedSet = remove(val);
        }
        return changedSet;
    }

    @Override
    public boolean remove(RangedLongSet otherSet)
    {
        return removeBlocks(otherSet.getBlocks());
    }

    @Override
    public boolean removeAll(Collection<?> stuff)
    {
        boolean setWasChanged = false;
        if (stuff != null && !stuff.isEmpty())
        {
            Collection<Long> stuffICanUse = extractUsableElements(stuff);
            // Remove the stuff in the decimated list.
            if (!stuffICanUse.isEmpty())
            {
                setWasChanged = remove(stuffICanUse);
            }
        }
        return setWasChanged;
    }

    @Override
    public boolean removeBlock(RangeLongBlock blockToRemove)
    {
        boolean setWasChanged = false;
        // Some assumptions about the set that this method requires.
        // 1. The set is already in ascending order ( low ids blocks have lower
        // indices )
        // 2. No block in the set has overlapping ids with any other block.
        // 3. Every block in the set is separated from its neighbors by at least
        // one id. Should that condition exist those contiguous blocks should
        // have
        // been merged into one block in the addition process.
        // i.e. no two blocks form a contiguous range. This is not allowed:
        // {x,x+n}{x+n+1,x+n2}

        myBlockListLock.lock();
        try
        {
            // First make sure that the block we are being asked to remove is
            // not before the beginning of our list
            // or after the end.
            if (!myBlockList.isEmpty() && !myBlockList.get(0).isAfter(blockToRemove)
                    && !myBlockList.get(myBlockList.size() - 1).isBefore(blockToRemove))
            {
                int idx = Collections.binarySearch(myBlockList, blockToRemove);

                if (idx >= 0)
                {
                    // Exact match case
                    myBlockList.remove(idx);
                    myValueCountStale = true;
                    setWasChanged = true;
                }
                else
                {
                    // Modify binary search index to position index.
                    int locIndex = -1 * (idx + 1);

                    // Do forward decimation unless the loc index is the back
                    // edge of the
                    // list.
                    boolean doForwardDecimation = locIndex != myBlockList.size();

                    // Only if we're at greater than zero
                    // do we need to check intersection with
                    // our lower index neighbor.
                    if (locIndex > 0 && decimateLowNeighbor(blockToRemove, locIndex))
                    {
                        setWasChanged = true;
                    }

                    if (doForwardDecimation && locIndex != myBlockList.size() && forwardDecimate(blockToRemove, locIndex))
                    {
                        setWasChanged = true;
                    }
                    myValueCountStale = true;
                }
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return setWasChanged;
    }

    @Override
    public boolean removeBlocks(Collection<RangeLongBlock> blocksToRemove)
    {
        boolean setWasChanged = false;
        if (blocksToRemove != null && !blocksToRemove.isEmpty())
        {
            myBlockListLock.lock();
            try
            {
                for (RangeLongBlock block : blocksToRemove)
                {
                    if (removeBlock(block))
                    {
                        setWasChanged = true;
                    }
                }
            }
            finally
            {
                myBlockListLock.unlock();
            }
        }
        return setWasChanged;
    }

    @Override
    public boolean retainAll(Collection<?> stuff)
    {
        boolean setWasChanged = false;
        if (stuff != null && !stuff.isEmpty())
        {
            // Go through the collection we've been handed
            // and filter out all the stuff that is either a Long or
            // that extends long.
            Collection<Long> stuffICanUse = extractUsableElements(stuff);
            // Remove the stuff in the decimated list.
            if (!stuffICanUse.isEmpty())
            {
                DefaultRangedLongSet setToRemove = new DefaultRangedLongSet(stuffICanUse);
                myBlockListLock.lock();
                try
                {
                    setWasChanged = !equals(setToRemove);
                    RangedLongSet intersection = this.getIntersection(setToRemove);
                    myBlockList = intersection.getBlocks();
                    myValueCountStale = true;
                }
                finally
                {
                    myBlockListLock.unlock();
                }
            }
        }
        return setWasChanged;
    }

    /**
     * Note that this could trunkate the size if there are over
     * Integer.MAX_VALUE values in the set, which is possible with the range.
     *
     * Prefer using valueCount().
     *
     * @return the size ( truncated to Integer.MAX_VALUE if the list is larger
     *         than an int can hold ).
     */
    @Override
    public int size()
    {
        if (valueCount() > Integer.MAX_VALUE)
        {
            return Integer.MAX_VALUE;
        }
        else
        {
            return (int)valueCount();
        }
    }

    @Override
    public Object[] toArray()
    {
        return toArray(new Object[1]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] arg0)
    {
        // This will get the length or throw a NullPointerException
        // which will help us conform to the Set interface.
        int passedSize = arg0.length;

        Object[] result = null;

        myBlockListLock.lock();
        try
        {
            long count = valueCount();
            if (count < Integer.MAX_VALUE)
            {
                if (passedSize >= count)
                {
                    result = arg0;
                }
                else
                {
                    if (arg0 instanceof Long[])
                    {
                        result = new Long[(int)count];
                    }
                    else
                    {
                        result = new Object[(int)count];
                    }
                }

                int index = 0;
                Iterator<Long> itr = iterator();
                while (itr.hasNext() && index < result.length && index < Integer.MAX_VALUE)
                {
                    result[index] = itr.next();
                    index++;
                }

                // If there is still empty space in the array
                // add the null value to conform to the interface.
                if (index < result.length - 1)
                {
                    result[index] = null;
                }
            }
            else
            {
                throw new TooManyValuesToConstructArrayException(
                        "Could not produce array, number of values \"" + count + "\" is too high.");
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return (T[])result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        myBlockListLock.lock();
        try
        {
            sb.append("RangedLongSet{");
            boolean isFirst = true;
            for (RangeLongBlock block : myBlockList)
            {
                if (isFirst)
                {
                    isFirst = !isFirst;
                }
                else
                {
                    sb.append(',');
                }
                sb.append(block.toString());
            }
            sb.append('}');
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return sb.toString();
    }

    @Override
    public long valueCount()
    {
        if (myValueCountStale)
        {
            int count = 0;
            myBlockListLock.lock();
            try
            {
                for (RangeLongBlock block : myBlockList)
                {
                    count += block.size();
                }
            }
            finally
            {
                myBlockListLock.unlock();
            }

            myValueCount = count;
        }
        return myValueCount;
    }

    /**
     * Adds the list of {@link RangeLongBlock} to this list. Assumes that list
     * is correctly ordered and deduped as produced by one of the
     * createRageLongBlocks calls.
     *
     * @param list the list
     * @return true if the set was changed as a result of this call
     */
    private boolean add(List<RangeLongBlock> list)
    {
        boolean wasChanged = false;

        myBlockListLock.lock();
        try
        {
            if (myBlockList.isEmpty())
            {
                wasChanged = myBlockList.addAll(list);
                myValueCountStale = true;
            }
            else
            {
                for (RangeLongBlock block : list)
                {
                    if (addBlock(block))
                    {
                        wasChanged = true;
                    }
                }
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return wasChanged;
    }

    /**
     * Quick add algorithm to add the insert block when list has only one entry.
     *
     * @param insertBlock the insert block
     * @return true if the set was changed by this call.
     */
    private boolean addBlockWhenListHasOnlyOneBlock(RangeLongBlock insertBlock)
    {
        if (myBlockList.size() != 1)
        {
            throw new IllegalStateException();
        }

        boolean changedSomething = false;
        RangeRelationType relation = myBlockList.get(0).getRelation(insertBlock);
        if (relation == RangeRelationType.BEFORE)
        {
            myBlockList.add(0, insertBlock);
            changedSomething = true;
        }
        else if (relation == RangeRelationType.AFTER)
        {
            myBlockList.add(insertBlock);
            changedSomething = true;
        }
        else
        {
            // If not before or after must form contiguous range.
            if (myBlockList.get(0).merge(insertBlock))
            {
                changedSomething = true;
            }
        }
        if (changedSomething)
        {
            myValueCountStale = true;
        }
        return changedSomething;
    }

    /**
     * Use a binary search to add a block to the set.
     *
     * @param insertBlock the insert block
     * @return true if set was changed as a result of the addition.
     */
    private boolean binarySearchInsert(RangeLongBlock insertBlock)
    {
        boolean changedSomething = false;
        int size = myBlockList.size();

        // Use a binary search to locate the location in which this value would
        // be inserted in the context of the blocks that are already in the
        // list.

        int idx = Collections.binarySearch(myBlockList, insertBlock);
        if (idx < 0) // No exact match but might be contained.
        {
            // Represents where compression should start, unless -1, then no
            // compression
            int compressIndex = -1;
            // Convert binary search result to an insert index
            int insertIndex = -1 * (idx + 1);

            // Check boundary conditions first.
            if (insertIndex == 0)
            {
                // Check if we are before the first block if we overlap
                // with the first block.
                RangeLongBlock block = myBlockList.get(insertIndex);
                RangeRelationType relation = block.getRelation(insertBlock);
                if (relation == RangeRelationType.BEFORE)
                {
                    myBlockList.add(insertIndex, insertBlock);
                    changedSomething = true;
                }
                else
                {
                    if (block.merge(insertBlock))
                    {
                        changedSomething = true;
                    }
                    compressIndex = 0;
                }
            }
            else if (insertIndex == size)
            {
                // Check if we are after the end block for overlap on
                // the last block.
                RangeLongBlock block = myBlockList.get(size - 1);
                RangeRelationType relation = block.getRelation(insertBlock);
                if (relation == RangeRelationType.AFTER)
                {
                    myBlockList.add(insertBlock);
                    changedSomething = true;
                }
                else
                {
                    if (block.merge(insertBlock))
                    {
                        changedSomething = true;
                    }
                    // No compress needed because we are adding at
                    // the end.
                }
            }
            else
            {
                // We fall between two blocks, so check each of the
                // blocks we fall between.
                RangeLongBlock beforeBlock = myBlockList.get(insertIndex - 1);
                RangeLongBlock afterBlock = myBlockList.get(insertIndex);
                compressIndex = insertIndex;
                if (beforeBlock.formsContiguousRange(insertBlock))
                {
                    if (beforeBlock.merge(insertBlock))
                    {
                        changedSomething = true;
                    }
                    compressIndex = insertIndex - 1;
                }
                else if (afterBlock.formsContiguousRange(insertBlock))
                {
                    if (afterBlock.merge(insertBlock))
                    {
                        changedSomething = true;
                    }
                }
                else
                // Does not overlap with either
                {
                    myBlockList.add(insertIndex, insertBlock);
                    changedSomething = true;
                }
            }

            // Need to run compression to make sure neighboring
            // blocks in the list are actually separate ranges
            // and are not contiguous.
            if (compressIndex != -1)
            {
                compressOverlappingWithBlockAtIndex(compressIndex);
            }
        }
        return changedSomething;
    }

    /**
     * Deep copies the backing block list for this ranged long set.
     *
     * @return the copy of the block list.
     */
    private List<RangeLongBlock> cloneBlockList()
    {
        LinkedList<RangeLongBlock> copy = null;
        myBlockListLock.lock();
        try
        {
            copy = new LinkedList<>();
            Iterator<RangeLongBlock> blockItr = myBlockList.iterator();
            while (blockItr.hasNext())
            {
                copy.add(new RangeLongBlock(blockItr.next()));
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return copy;
    }

    /**
     * Compress list from starting from an index working first forward in the
     * list and then backward merging overlapping ranges. Using the block at the
     * specified index as a seed. Terminates compression when the specified
     * block has no further interaction with surrounding blocks.
     *
     * @param index the compress index
     */
    private void compressOverlappingWithBlockAtIndex(int index)
    {
        int compressIndex = index;
        RangeLongBlock seedBlock = myBlockList.get(compressIndex);
        RangeLongBlock currentBlock = null;

        // First merge forward
        if (compressIndex < myBlockList.size() - 1)
        {
            boolean forwardCompressComplete = false;
            while (!forwardCompressComplete && compressIndex + 1 < myBlockList.size())
            {
                currentBlock = myBlockList.get(compressIndex + 1);
                if (seedBlock.formsContiguousRange(currentBlock))
                {
                    seedBlock.merge(currentBlock);
                    myBlockList.remove(compressIndex + 1);
                    myValueCountStale = true;
                }
                else
                {
                    forwardCompressComplete = true;
                }
            }
        }

        // Now backward merge.
        if (compressIndex > 0)
        {
            boolean reverseCompressComplete = false;
            while (!reverseCompressComplete && compressIndex > 0)
            {
                currentBlock = myBlockList.get(compressIndex - 1);
                if (currentBlock.formsContiguousRange(seedBlock))
                {
                    currentBlock.merge(seedBlock);
                    myBlockList.remove(compressIndex);
                    seedBlock = currentBlock;
                    myValueCountStale = true;
                    compressIndex--;
                }
                else
                {
                    reverseCompressComplete = true;
                }
            }
        }
    }

    /**
     * Assist function for removing blocks from the set, where this function
     * checks the neighboring block from the insert position of the block to
     * remove to see if the low neighbor needs to be decimated.
     *
     * @param blockToRemove - the block to decimate with
     * @param locIndex - the location index to start the decimation with.
     * @return true if the set was changed by this decimation.
     */
    private boolean decimateLowNeighbor(RangeLongBlock blockToRemove, int locIndex)
    {
        boolean setWasChanged = false;
        RangeLongBlock lowNeighbor = myBlockList.get(locIndex - 1);
        RangeRelationType relation = lowNeighbor.getRelation(blockToRemove);
        if (relation == RangeRelationType.OVERLAPS_BACK_EDGE)
        {
            lowNeighbor.setRange(lowNeighbor.getStart(), blockToRemove.getStart() - 1);
            setWasChanged = true;
        }
        else if (relation == RangeRelationType.SUBSET)
        {
            // Check to see if it spans to the end of the low
            // neighbor block
            // if so we can clip, otherwise we have to split and
            // clip.
            if (blockToRemove.getEnd() == lowNeighbor.getEnd())
            {
                lowNeighbor.setRange(lowNeighbor.getStart(), blockToRemove.getStart() - 1);
            }
            else
            {
                // Remove is in the interior, so clip the low
                // neighbor and create
                // a new block to represent the remainder.
                long oldStart = lowNeighbor.getStart();
                long oldEnd = lowNeighbor.getEnd();
                lowNeighbor.setRange(oldStart, blockToRemove.getStart() - 1);
                RangeLongBlock anteriorPart = new RangeLongBlock(blockToRemove.getEnd() + 1, oldEnd);
                myBlockList.add(locIndex, anteriorPart);
            }
            setWasChanged = true;
        }
        return setWasChanged;
    }

    /**
     * Extracts integer type values from the provided collection of objects.
     *
     * @param stuff collection of objects
     * @return the distilled collection containing integer values as longs.
     */
    private Collection<Long> extractUsableElements(Collection<?> stuff)
    {
        Collection<Long> stuffICanUse = new LinkedList<>();
        if (stuff != null && !stuff.isEmpty())
        {
            // Go through the collection we've been handed
            // and filter out all the stuff that is either a Long or
            // that extends long, or is an integer type value
            Iterator<?> itr = stuff.iterator();
            Long longVal = null;
            while (itr.hasNext())
            {
                longVal = getAsLong(itr.next());
                if (longVal != null)
                {
                    stuffICanUse.add(longVal);
                }
            }
        }
        return stuffICanUse;
    }

    /**
     * The assist function that does forward decimation starting at a location
     * in a list using a block of ids to remove. Will search forward from the
     * specified index determining if and how the blocks in the list need to be
     * modified, removed, or subsected to honor the remove request. Stops
     * automatically when there are no more blocks to process ( list end) or
     * when it detects that conditions indicate that no more blocks will require
     * decimation. See assumptions in comments at the top of the
     * removeBlock(RangeLongBlock blockToRemove) method.
     *
     * @param blockToRemove - the block of ids to remove.
     * @param locIndex - the location to use to begin forward decimation.
     * @return true if set was changed as a result of this call
     */
    private boolean forwardDecimate(RangeLongBlock blockToRemove, int locIndex)
    {
        boolean setWasChanged = false;
        ListIterator<RangeLongBlock> listItr = myBlockList.listIterator(locIndex);
        boolean forwardDecimationComplete = false;
        int count = 0;
        while (!forwardDecimationComplete && listItr.hasNext())
        {
            RangeLongBlock currBlock = listItr.next();
            RangeRelationType relation = currBlock.getRelation(blockToRemove);
            switch (relation)
            {
                case BEFORE:
                case BORDERS_BEFORE:
                    forwardDecimationComplete = true;
                    break;
                case OVERLAPS_FRONT_EDGE:
                    currBlock.setRange(blockToRemove.getEnd() + 1, currBlock.getEnd());
                    forwardDecimationComplete = true;
                    setWasChanged = true;
                    break;
                case OVERLAPS_BACK_EDGE:
                    currBlock.setRange(currBlock.getStart(), blockToRemove.getStart() - 1);
                    setWasChanged = true;
                    break;
                case SUBSET:
                    if (blockToRemove.getEnd() == currBlock.getEnd())
                    {
                        currBlock.setRange(currBlock.getStart(), blockToRemove.getStart() - 1);
                    }
                    else if (blockToRemove.getStart() == currBlock.getStart())
                    {
                        currBlock.setRange(blockToRemove.getEnd() + 1, currBlock.getEnd());
                    }
                    else
                    {
                        // Remove is in the interior, so clip the low neighbor
                        // and create a new block to represent the remainder.
                        long oldStart = currBlock.getStart();
                        long oldEnd = currBlock.getEnd();
                        currBlock.setRange(oldStart, blockToRemove.getStart() - 1);
                        RangeLongBlock anteriorPart = new RangeLongBlock(blockToRemove.getEnd() + 1, oldEnd);
                        myBlockList.add(locIndex + count + 1, anteriorPart);
                    }
                    forwardDecimationComplete = true;
                    setWasChanged = true;
                    break;
                case SUPERSET:
                    listItr.remove();
                    setWasChanged = true;
                    break;
                default:
                    break;
            }
            count++;
        }
        return setWasChanged;
    }

    /**
     * If the object represents an integer value (Long, Integer, Short, or Byte)
     * return it as a Long.
     *
     * @param o the object to get as a long.
     * @return the as long or null if not an integer type value.
     */
    private Long getAsLong(Object o)
    {
        if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte)
        {
            return Long.valueOf(((Number)o).longValue());
        }
        return null;
    }

    /**
     * Gets the list of {@link RangeLongBlock} that represent the intersection
     * of the specified block with this set.
     *
     * @param blockToIntersect the block to intersect
     * @return the {@link List} of {@link RangeLongBlock} that represent the
     *         intersection or null if no intersection.
     */
    private List<RangeLongBlock> getIntersectionList(RangeLongBlock blockToIntersect)
    {
        List<RangeLongBlock> resultList = null;
        myBlockListLock.lock();
        try
        {
            // First make sure that the block we are being asked to intersect is
            // not before the beginning of our list
            // or after the end.
            if (!myBlockList.isEmpty() && !myBlockList.get(0).isAfter(blockToIntersect)
                    && !myBlockList.get(myBlockList.size() - 1).isBefore(blockToIntersect))
            {
                resultList = new ArrayList<>();
                int idx = Collections.binarySearch(myBlockList, blockToIntersect);

                if (idx >= 0)
                {
                    // Exact match case
                    resultList.add(new RangeLongBlock(blockToIntersect));
                }
                else
                {
                    // Modify binary search index to position index.
                    int locIndex = -1 * (idx + 1);

                    // Do forward decimation unless the loc index is the back
                    // edge of the list.
                    boolean doForwardCheck = locIndex != myBlockList.size();
                    RangeRelationType relation = null;
                    // Only if we're at greater than zero
                    // do we need to check intersection with
                    // our lower index neighbor.

                    if (locIndex > 0)
                    {
                        RangeLongBlock lowNeighbor = myBlockList.get(locIndex - 1);
                        relation = lowNeighbor.getRelation(blockToIntersect);
                        if (relation == RangeRelationType.OVERLAPS_BACK_EDGE || relation == RangeRelationType.SUBSET)
                        {
                            resultList.add(lowNeighbor.intersection(blockToIntersect));
                            doForwardCheck = relation != RangeRelationType.SUBSET;
                        }
                    }

                    if (doForwardCheck)
                    {
                        ListIterator<RangeLongBlock> blockIter = myBlockList.listIterator(locIndex);
                        RangeLongBlock currentBlock = null;
                        while (doForwardCheck && blockIter.hasNext())
                        {
                            currentBlock = blockIter.next();
                            relation = currentBlock.getRelation(blockToIntersect);
                            switch (relation)
                            {
                                case OVERLAPS_BACK_EDGE:
                                case SUPERSET:
                                    resultList.add(currentBlock.intersection(blockToIntersect));
                                    break;
                                case OVERLAPS_FRONT_EDGE:
                                case SUBSET:
                                case EQUAL:
                                    resultList.add(currentBlock.intersection(blockToIntersect));
                                    doForwardCheck = false;
                                    break;
                                default:
                                    doForwardCheck = false;
                                    break;
                            }
                        }
                    }
                    myValueCountStale = true;
                }
                if (resultList.isEmpty())
                {
                    resultList = null;
                }
            }
        }
        finally
        {
            myBlockListLock.unlock();
        }
        return resultList;
    }
}
