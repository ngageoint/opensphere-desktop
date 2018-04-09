package io.opensphere.core.model.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.model.RangeRelationType;
import io.opensphere.core.util.rangeset.RangeLongBlock;

/**
 * Default implementation of a {@link TimeSpanSet}.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultTimeSpanSet implements TimeSpanSet
{
    /** Add timeless error message. */
    private static final String CANNOT_ADD_UNBOUNDED_TIME_SPAN_TO_SET = "Cannot add unbounded TimeSpan to set";

    /** The element list lock. */
    private final ReentrantLock myElementListLock;

    /** The element list. */
    private final List<TimeSpan> myElementList;

    /**
     * Instantiates a new DefaultTimeSpanSet.
     */
    public DefaultTimeSpanSet()
    {
        myElementList = new LinkedList<>();
        myElementListLock = new ReentrantLock();
    }

    /**
     * Instantiates a new DefaultTimeSpanSet.
     *
     * @param ts the initial {@link TimeSpan}
     * @throws IllegalArgumentException if ts  is unbounded
     */
    public DefaultTimeSpanSet(TimeSpan ts)
    {
        this();
        if (!ts.isBounded())
        {
            throw new IllegalArgumentException(CANNOT_ADD_UNBOUNDED_TIME_SPAN_TO_SET);
        }
        myElementList.add(ts);
    }

    /**
     * Copy constructor.
     *
     * @param other the other {@link TimeSpanSet} to add all elements from.
     */
    public DefaultTimeSpanSet(TimeSpanSet other)
    {
        myElementListLock = new ReentrantLock();
        myElementList = new LinkedList<>(other);
    }

    @Override
    public boolean add(TimeSpan e)
    {
        if (e == null || e.isTimeless())
        {
            return false;
        }

        boolean changedSomething = false;
        myElementListLock.lock();
        try
        {
            int size = myElementList.size();
            if (size == 0)
            {
                myElementList.add(e);
                changedSomething = true;
            }
            else if (size == 1)
            {
                // Do quick match for the one span case.
                if (addSpanWhenListHasOnlyOneBlock(e))
                {
                    changedSomething = true;
                }
            }
            else
            {
                changedSomething = binarySearchInsert(e);
            }
        }
        finally
        {
            myElementListLock.unlock();
        }
        return changedSomething;
    }

    @Override
    public final boolean add(TimeSpanSet other)
    {
        boolean wasChanged = false;
        for (TimeSpan ts : other)
        {
            if (add(ts) && !wasChanged)
            {
                wasChanged = true;
            }
        }
        return wasChanged;
    }

    @Override
    public boolean addAll(Collection<? extends TimeSpan> c)
    {
        boolean wasChanged = false;
        for (TimeSpan ts : c)
        {
            if (add(ts) && !wasChanged)
            {
                wasChanged = true;
            }
        }
        return wasChanged;
    }

    @Override
    public void clear()
    {
        myElementListLock.lock();
        try
        {
            myElementList.clear();
        }
        finally
        {
            myElementListLock.unlock();
        }
    }

    @Override
    public boolean contains(Date aDate)
    {
        return contains(aDate.getTime());
    }

    @Override
    public boolean contains(long aTime)
    {
        return aTime > 0 && contains(TimeSpan.get(aTime, aTime));
    }

    @Override
    public boolean contains(Object o)
    {
        TimeSpan value = getAsTimespan(o);
        return contains(value);
    }

    @Override
    public boolean contains(TimeSpan value)
    {
        return test(TimeSpan::contains, value, false);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        for (Object o : c)
        {
            TimeSpan ts = getAsTimespan(o);
            if (ts == null || !contains(ts))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Generic test method to perform timespan membership operations (such as
     * contains and overlaps).
     *
     * @param test the test method to execute (such as
     *            <code>TimeSpan::contains</code> or
     *            <code>TimeSpan::overlaps</code>, etc.)
     * @param value The value for which to execute the test (e.g.:
     *            <code>myElementList.get(x).contains(value);</code>).
     * @param timelessValue the return value to return if the supplied
     *            <code>value</code> is timeless.
     * @return the result of the test.
     * @see MembershipTest
     */
    protected boolean test(MembershipTest test, TimeSpan value, boolean timelessValue)
    {
        boolean result = false;
        if (value != null)
        {
            if (value.isTimeless())
            {
                return timelessValue;
            }

            myElementListLock.lock();
            try
            {
                int size = myElementList.size();

                if (size != 0)
                {
                    if (size == 1)
                    {
                        // Do quick match for the one span case.
                        result = test.execute(myElementList.get(0), value);
                    }
                    else if (size == 2)
                    {
                        // Do quick match for the two span case.
                        result = test.execute(myElementList.get(0), value);
                        if (!result)
                        {
                            result = test.execute(myElementList.get(1), value);
                        }
                    }
                    else
                    {
                        int idx = Collections.binarySearch(myElementList, value);
                        if (idx >= 0)
                        {
                            result = true;
                        }
                        else
                        {
                            int insertIndex = -1 * (idx + 1);
                            // Check boundary conditions first.
                            if (insertIndex == 0)
                            {
                                // Check if we are before the first span if we
                                // overlap with the first span.
                                result = test.execute(myElementList.get(insertIndex), value);
                            }
                            else if (insertIndex == size)
                            {
                                // Check if we are after the end span for
                                // overlap on the last span.
                                result = test.execute(myElementList.get(size - 1), value);
                            }
                            else
                            {
                                // We fall between two span, so check each of
                                // the spans we fall between.
                                result = test.execute(myElementList.get(insertIndex - 1), value);
                                if (!result)
                                {
                                    result = test.execute(myElementList.get(insertIndex), value);
                                }
                            }
                        }
                    }
                }
            }
            finally
            {
                myElementListLock.unlock();
            }
        }
        return result;
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
        DefaultTimeSpanSet other = (DefaultTimeSpanSet)obj;
        return Objects.equals(myElementList, other.myElementList);
    }

    @Override
    public List<TimeSpan> getTimeSpans()
    {
        List<TimeSpan> resultList = null;
        myElementListLock.lock();
        try
        {
            resultList = new ArrayList<>(myElementList);
        }
        finally
        {
            myElementListLock.unlock();
        }
        return Collections.unmodifiableList(resultList);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        myElementListLock.lock();
        try
        {
            result = prime * result + (myElementList == null ? 0 : myElementList.hashCode());
        }
        finally
        {
            myElementListLock.unlock();
        }
        return result;
    }

    @Override
    public TimeSpanSet intersection(TimeSpan ts)
    {
        DefaultTimeSpanSet result = new DefaultTimeSpanSet();
        if (ts != null && !ts.isTimeless())
        {
            List<TimeSpan> intersect = getIntersectionList(ts);
            if (intersect != null && !intersect.isEmpty())
            {
                result.myElementList.addAll(intersect);
            }
        }
        return result;
    }

    @Override
    public TimeSpanSet intersection(TimeSpanSet otherSet)
    {
        DefaultTimeSpanSet result = new DefaultTimeSpanSet();
        if (otherSet != null)
        {
            myElementListLock.lock();
            try
            {
                if (!otherSet.isEmpty())
                {
                    List<TimeSpan> overallIntersectList = new LinkedList<>();
                    for (TimeSpan ts : otherSet)
                    {
                        List<TimeSpan> intersect = getIntersectionList(ts);
                        if (intersect != null && !intersect.isEmpty())
                        {
                            overallIntersectList.addAll(intersect);
                        }
                    }
                    if (!overallIntersectList.isEmpty())
                    {
                        result.myElementList.addAll(overallIntersectList);
                    }
                }
            }
            finally
            {
                myElementListLock.unlock();
            }
        }
        return result;
    }

    @Override
    public boolean intersects(TimeSpan value)
    {
        return test(TimeSpan::overlaps, value, true);
    }

    @Override
    public boolean intersects(TimeSpanProvider ts)
    {
        return intersects(ts.getTimeSpan());
    }

    @Override
    public boolean intersects(TimeSpanSet other)
    {
        for (TimeSpan ts : other)
        {
            if (intersects(ts))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        boolean empty = false;
        myElementListLock.lock();
        try
        {
            empty = myElementList.isEmpty();
        }
        finally
        {
            myElementListLock.unlock();
        }
        return empty;
    }

    @Override
    public Iterator<TimeSpan> iterator()
    {
        return myElementList.iterator();
    }

    @Override
    public boolean remove(Object o)
    {
        TimeSpan ts = getAsTimespan(o);
        if (ts != null)
        {
            return remove(ts);
        }
        return false;
    }

    @Override
    public boolean remove(TimeSpan spanToRemove)
    {
        boolean setWasChanged = false;
        // Some assumptions about the set that this method requires.
        // 1. The set is already in ascending order ( low value spans have lower
        // indices )
        // 2. No span in the set has overlapping span with any other span.
        // 3. Every span in the set is separated from its neighbors by at least
        // one id. Should that condition exist those
        // contiguous spans should have been merged into one span in the
        // addition process. i.e. no two spans form a contiguous
        // range. This is not allowed: {x,x+n}{x+n+1,x+n2}

        myElementListLock.lock();
        try
        {
            // First make sure that the span we are being asked to remove is not
            // before the beginning of our list or after the
            // end.
            if (!myElementList.isEmpty() && !myElementList.get(0).isAfter(spanToRemove)
                    && !myElementList.get(myElementList.size() - 1).isBefore(spanToRemove))
            {
                int idx = Collections.binarySearch(myElementList, spanToRemove);

                if (idx >= 0)
                {
                    // Exact match case
                    myElementList.remove(idx);
                    setWasChanged = true;
                }
                else
                {
                    // Modify binary search index to position index.
                    int locIndex = -1 * (idx + 1);

                    // Do forward decimation unless the loc index is the back
                    // edge of the list.
                    boolean doForwardDecimation = locIndex != myElementList.size();

                    // Only if we're at greater than zero do we need to check
                    // intersection with our lower index neighbor.
                    if (locIndex > 0 && decimateLowNeighbor(spanToRemove, locIndex))
                    {
                        setWasChanged = true;
                    }

                    if (doForwardDecimation && locIndex != myElementList.size() && forwardDecimate(spanToRemove, locIndex))
                    {
                        setWasChanged = true;
                    }
                }
            }
        }
        finally
        {
            myElementListLock.unlock();
        }
        return setWasChanged;
    }

    @Override
    public boolean remove(TimeSpanSet other)
    {
        boolean wasChanged = false;
        for (TimeSpan ts : other)
        {
            if (remove(ts) && !wasChanged)
            {
                wasChanged = true;
            }
        }
        return wasChanged;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean wasChanged = false;
        for (Object o : c)
        {
            TimeSpan ts = getAsTimespan(o);
            if (remove(ts) && !wasChanged)
            {
                wasChanged = true;
            }
        }
        return wasChanged;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean setWasChanged = false;
        if (c != null && !c.isEmpty())
        {
            // Go through the collection we've been handed and filter out all
            // the stuff that is either a Long or that extends
            // long.
            Collection<TimeSpan> stuffICanUse = extractUsableElements(c);
            // Remove the stuff in the decimated list.
            if (!stuffICanUse.isEmpty())
            {
                DefaultTimeSpanSet setToRemove = new DefaultTimeSpanSet();
                setToRemove.addAll(stuffICanUse);
                myElementListLock.lock();
                try
                {
                    setWasChanged = !equals(setToRemove);
                    TimeSpanSet intersection = this.intersection(setToRemove);
                    myElementList.clear();
                    myElementList.addAll(intersection);
                }
                finally
                {
                    myElementListLock.unlock();
                }
            }
        }
        return setWasChanged;
    }

    @Override
    public int size()
    {
        int size = -1;
        myElementListLock.lock();
        try
        {
            size = myElementList.size();
        }
        finally
        {
            myElementListLock.unlock();
        }
        return size;
    }

    @Override
    public Object[] toArray()
    {
        Object[] result = null;
        myElementListLock.lock();
        try
        {
            result = myElementList.toArray();
        }
        finally
        {
            myElementListLock.unlock();
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        T[] result = null;
        myElementListLock.lock();
        try
        {
            result = myElementList.toArray(a);
        }
        finally
        {
            myElementListLock.unlock();
        }
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        myElementListLock.lock();
        try
        {
            sb.append("TimeSpanSet{");
            boolean isFirst = true;
            for (TimeSpan block : myElementList)
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
            myElementListLock.unlock();
        }
        return sb.toString();
    }

    @Override
    public TimeSpanSet union(TimeSpanSet other)
    {
        DefaultTimeSpanSet newSet = new DefaultTimeSpanSet(this);
        newSet.add(other);
        return newSet;
    }

    /**
     * Quick add algorithm to add the insert span when list has only one entry.
     *
     * @param insertSpan the insert span
     * @return true if the set was changed by this call.
     */
    protected boolean addSpanWhenListHasOnlyOneBlock(TimeSpan insertSpan)
    {
        if (myElementList.size() != 1)
        {
            throw new IllegalStateException();
        }

        boolean changedSomething = false;
        RangeRelationType relation = myElementList.get(0).getRelation(insertSpan);
        if (relation == RangeRelationType.AFTER)
        {
            myElementList.add(0, insertSpan);
            changedSomething = true;
        }
        else if (relation == RangeRelationType.BEFORE)
        {
            myElementList.add(insertSpan);
            changedSomething = true;
        }
        else
        {
            // If not before or after must form contiguous range.
            changedSomething = replaceElementWithMergedSpan(0, insertSpan);
        }
        return changedSomething;
    }

    /**
     * Use a binary search to add a TimeSpan to the set.
     *
     * @param insertSpan the insert TimeSpan
     * @return true if set was changed as a result of the addition.
     */
    protected boolean binarySearchInsert(TimeSpan insertSpan)
    {
        boolean changedSomething = false;
        int size = myElementList.size();

        // Use a binary search to locate the location in which this value would
        // be inserted in the context of the spans that are
        // already in the list.

        int idx = Collections.binarySearch(myElementList, insertSpan);
        if (idx < 0)
        {
            // No exact match but might be contained.

            // Represents where compression should start, unless -1, then no
            // compression
            int compressIndex = -1;

            // Convert binary search result to an insert index
            int insertIndex = -1 * (idx + 1);

            // Check boundary conditions first.
            if (insertIndex == 0)
            {
                // Check if we are before the first span if we overlap with the
                // first span.
                TimeSpan span = myElementList.get(insertIndex);
                RangeRelationType relation = span.getRelation(insertSpan);
                if (relation == RangeRelationType.AFTER)
                {
                    myElementList.add(insertIndex, insertSpan);
                    changedSomething = true;
                }
                else
                {
                    if (replaceElementWithMergedSpan(insertIndex, insertSpan))
                    {
                        changedSomething = true;
                    }
                    compressIndex = 0;
                }
            }
            else if (insertIndex == size)
            {
                // Check if we are after the end span for overlap on the last
                // span.
                TimeSpan span = myElementList.get(size - 1);
                RangeRelationType relation = span.getRelation(insertSpan);
                if (relation == RangeRelationType.BEFORE)
                {
                    myElementList.add(insertSpan);
                    changedSomething = true;
                }
                else if (replaceElementWithMergedSpan(size - 1, insertSpan))
                {
                    changedSomething = true;
                }
                // No compress needed because we are adding at the end.
            }
            else
            {
                // We fall between two spans, so check each of the spans we fall
                // between.
                TimeSpan beforeBlock = myElementList.get(insertIndex - 1);
                TimeSpan afterBlock = myElementList.get(insertIndex);
                compressIndex = insertIndex;
                if (beforeBlock.formsContiguousRange(insertSpan))
                {
                    if (replaceElementWithMergedSpan(insertIndex - 1, insertSpan))
                    {
                        changedSomething = true;
                    }
                    compressIndex = insertIndex - 1;
                }
                else if (afterBlock.formsContiguousRange(insertSpan))
                {
                    if (replaceElementWithMergedSpan(insertIndex, insertSpan))
                    {
                        changedSomething = true;
                    }
                }
                else
                {
                    // Does not overlap with either
                    myElementList.add(insertIndex, insertSpan);
                    changedSomething = true;
                }
            }

            // Need to run compression to make sure neighboring spans in the
            // list are actually separate ranges and are not
            // contiguous.
            if (compressIndex != -1)
            {
                compressOverlappingWithSpanAtIndex(compressIndex);
            }
        }
        return changedSomething;
    }

    /**
     * Compress list from starting from an index working first forward in the
     * list and then backward merging overlapping ranges. Using the span at the
     * specified index as a seed. Terminates compression when the specified span
     * has no further interaction with surrounding spans.
     *
     * @param index the compress index
     */
    protected void compressOverlappingWithSpanAtIndex(int index)
    {
        int compressIndex = index;
        TimeSpan seedBlock = myElementList.get(compressIndex);
        TimeSpan currentBlock = null;

        // First merge forward
        if (compressIndex < myElementList.size() - 1)
        {
            boolean forwardCompressComplete = false;
            while (!forwardCompressComplete && compressIndex + 1 < myElementList.size())
            {
                currentBlock = myElementList.get(compressIndex + 1);
                if (seedBlock.formsContiguousRange(currentBlock))
                {
                    myElementList.remove(compressIndex + 1);
                    replaceElementWithMergedSpan(compressIndex, currentBlock);
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
                currentBlock = myElementList.get(compressIndex - 1);
                if (currentBlock.formsContiguousRange(seedBlock))
                {
                    myElementList.remove(compressIndex);
                    replaceElementWithMergedSpan(compressIndex - 1, seedBlock);
                    seedBlock = currentBlock;
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
     * Assist function for removing spans from the set, where this function
     * checks the neighboring span from the insert position of the span to
     * remove to see if the low neighbor needs to be decimated.
     *
     * @param spanToRemove - the span to decimate with
     * @param locIndex - the location index to start the decimation with.
     * @return true if the set was changed by this decimation.
     */
    protected boolean decimateLowNeighbor(TimeSpan spanToRemove, int locIndex)
    {
        boolean setWasChanged = false;
        TimeSpan lowNeighbor = myElementList.get(locIndex - 1);
        RangeRelationType relation = lowNeighbor.getRelation(spanToRemove);
        if (relation == RangeRelationType.OVERLAPS_FRONT_EDGE)
        {
            replaceElementWithNewSpan(locIndex - 1, TimeSpan.get(lowNeighbor.getStart(), spanToRemove.getStart()));
            setWasChanged = true;
        }
        else if (relation == RangeRelationType.SUPERSET)
        {
            // Check to see if it spans to the end of the low neighbor span if
            // so we can clip, otherwise we have to split and
            // clip.
            if (spanToRemove.getEnd() == lowNeighbor.getEnd())
            {
                replaceElementWithNewSpan(locIndex - 1, TimeSpan.get(lowNeighbor.getStart(), spanToRemove.getStart()));
            }
            else
            {
                // Remove is in the interior, so clip the low neighbor and
                // create a new span to represent the remainder.
                long oldStart = lowNeighbor.getStart();
                long oldEnd = lowNeighbor.getEnd();
                replaceElementWithNewSpan(locIndex - 1, TimeSpan.get(oldStart, spanToRemove.getStart()));
                TimeSpan anteriorPart = TimeSpan.get(spanToRemove.getEnd(), oldEnd);
                myElementList.add(locIndex, anteriorPart);
            }
            setWasChanged = true;
        }
        return setWasChanged;
    }

    /**
     * Extracts TimeSpan type values from the provided collection of objects.
     *
     * Note: Leaves out all Timeless TimeSpan
     *
     * @param stuff collection of objects
     * @return the distilled collection containing TimeSpans
     */
    protected Collection<TimeSpan> extractUsableElements(Collection<?> stuff)
    {
        Collection<TimeSpan> stuffICanUse = new LinkedList<>();
        if (stuff != null && !stuff.isEmpty())
        {
            Iterator<?> itr = stuff.iterator();
            while (itr.hasNext())
            {
                Object thing = itr.next();
                if (thing instanceof TimeSpan && ((TimeSpan)thing).isBounded())
                {
                    stuffICanUse.add((TimeSpan)thing);
                }
            }
        }
        return stuffICanUse;
    }

    /**
     * The assist function that does forward decimation starting at a location
     * in a list using a span to remove. Will search forward from the specified
     * index determining if and how the spans in the list need to be modified,
     * removed, or subsected to honor the remove request. Stops automatically
     * when there are no more spans to process ( list end) or when it detects
     * that conditions indicate that no more spans will require decimation. See
     * assumptions in comments at the top of the remove(TimeSpan tsToRemove)
     * method.
     *
     * @param spanToRemove - the span to remove.
     * @param locIndex - the location to use to begin forward decimation.
     * @return true if set was changed as a result of this call
     */
    protected boolean forwardDecimate(TimeSpan spanToRemove, int locIndex)
    {
        boolean setWasChanged = false;
        boolean forwardDecimationComplete = false;
        int count = 0;
        int index = locIndex;
        while (!forwardDecimationComplete && index < myElementList.size())
        {
            TimeSpan currBlock = myElementList.get(index);
            RangeRelationType relation = currBlock.getRelation(spanToRemove);
            switch (relation)
            {
                case AFTER:
                case BORDERS_AFTER:
                    forwardDecimationComplete = true;
                    break;
                case OVERLAPS_BACK_EDGE:
                    replaceElementWithNewSpan(index, TimeSpan.get(spanToRemove.getEnd(), currBlock.getEnd()));
                    forwardDecimationComplete = true;
                    setWasChanged = true;
                    break;
                case OVERLAPS_FRONT_EDGE:
                    replaceElementWithNewSpan(index, TimeSpan.get(currBlock.getStart(), spanToRemove.getStart()));
                    setWasChanged = true;
                    break;
                case SUPERSET:
                    if (spanToRemove.getEnd() == currBlock.getEnd())
                    {
                        replaceElementWithNewSpan(index, TimeSpan.get(currBlock.getStart(), spanToRemove.getStart()));
                    }
                    else if (spanToRemove.getStart() == currBlock.getStart())
                    {
                        replaceElementWithNewSpan(index, TimeSpan.get(spanToRemove.getEnd(), currBlock.getEnd()));
                    }
                    else
                    {
                        // Remove is in the interior, so clip the low neighbor
                        // and create a new span to represent the remainder.
                        long oldStart = currBlock.getStart();
                        long oldEnd = currBlock.getEnd();

                        replaceElementWithNewSpan(index, TimeSpan.get(oldStart, spanToRemove.getStart()));
                        TimeSpan anteriorPart = TimeSpan.get(spanToRemove.getEnd(), oldEnd);
                        myElementList.add(locIndex + count + 1, anteriorPart);
                    }
                    forwardDecimationComplete = true;
                    setWasChanged = true;
                    break;
                case SUBSET:
                    myElementList.remove(index);
                    index--;
                    setWasChanged = true;
                    break;
                default:
                    break;
            }
            count++;
            index++;
        }
        return setWasChanged;
    }

    /**
     * Gets a {@link TimeSpan} from the object or returns null if not possible.
     *
     * @param o the object from which to get a {@link TimeSpan}
     * @return the {@link TimeSpan}
     */
    protected TimeSpan getAsTimespan(Object o)
    {
        TimeSpan ts = null;
        if (o instanceof TimeSpan)
        {
            ts = (TimeSpan)o;
        }
        else if (o instanceof TimeSpanProvider)
        {
            ts = ((TimeSpanProvider)o).getTimeSpan();
        }
        else if (o instanceof Long)
        {
            long val = ((Long)o).longValue();
            if (val > 0)
            {
                ts = TimeSpan.get(val, val);
            }
        }
        return ts;
    }

    /**
     * Gets the list of {@link RangeLongBlock} that represent the intersection
     * of the specified span with this set.
     *
     * @param tsToIntersect the TimeSpan to intersect
     * @return the {@link List} of {@link RangeLongBlock} that represent the
     *         intersection or null if no intersection.
     */
    protected List<TimeSpan> getIntersectionList(TimeSpan tsToIntersect)
    {
        List<TimeSpan> resultList = null;
        myElementListLock.lock();
        try
        {
            // First make sure that the span we are being asked to intersect is
            // not before the beginning of our list or after the
            // end.
            if (!myElementList.isEmpty() && !myElementList.get(0).isAfter(tsToIntersect)
                    && !myElementList.get(myElementList.size() - 1).isBefore(tsToIntersect))
            {
                resultList = new ArrayList<>();
                int idx = Collections.binarySearch(myElementList, tsToIntersect);

                if (idx >= 0)
                {
                    // Exact match case
                    resultList.add(tsToIntersect);
                }
                else
                {
                    // Modify binary search index to position index.
                    int locIndex = -1 * (idx + 1);

                    // Do forward decimation unless the loc index is the back
                    // edge of the list.
                    boolean doForwardCheck = locIndex != myElementList.size();
                    RangeRelationType relation = null;

                    // Only if we're at greater than zero do we need to check
                    // intersection with our lower index neighbor.
                    if (locIndex > 0)
                    {
                        TimeSpan lowNeighbor = myElementList.get(locIndex - 1);
                        relation = lowNeighbor.getRelation(tsToIntersect);
                        if (relation == RangeRelationType.OVERLAPS_FRONT_EDGE || relation == RangeRelationType.SUPERSET)
                        {
                            resultList.add(lowNeighbor.getIntersection(tsToIntersect));
                            doForwardCheck = relation != RangeRelationType.SUPERSET;
                        }
                    }

                    if (doForwardCheck)
                    {
                        ListIterator<TimeSpan> spanIter = myElementList.listIterator(locIndex);
                        TimeSpan currentBlock = null;
                        while (doForwardCheck && spanIter.hasNext())
                        {
                            currentBlock = spanIter.next();
                            relation = currentBlock.getRelation(tsToIntersect);
                            switch (relation)
                            {
                                case OVERLAPS_FRONT_EDGE:
                                case SUBSET:
                                    resultList.add(currentBlock.getIntersection(tsToIntersect));
                                    break;
                                case OVERLAPS_BACK_EDGE:
                                case SUPERSET:
                                case EQUAL:
                                    resultList.add(currentBlock.getIntersection(tsToIntersect));
                                    doForwardCheck = false;
                                    break;
                                default:
                                    doForwardCheck = false;
                                    break;
                            }
                        }
                    }
                }
                if (resultList.isEmpty())
                {
                    resultList = null;
                }
            }
        }
        finally
        {
            myElementListLock.unlock();
        }
        return resultList;
    }

    /**
     * Replace the element at the specified index with a new element that is the
     * merged span of the element at the index and the span to merge with.
     *
     * @param index the index to merge-replace
     * @param toMergeWith the span to merge with
     * @return true, if successful
     */
    protected boolean replaceElementWithMergedSpan(int index, TimeSpan toMergeWith)
    {
        if (index >= 0 && index < myElementList.size())
        {
            TimeSpan merged = myElementList.get(index).union(toMergeWith);
            if (merged != null && !merged.equals(myElementList.get(index)))
            {
                myElementList.remove(index);
                myElementList.add(index, merged);
                return true;
            }
        }
        return false;
    }

    /**
     * Replace the element at the specified index with a new time span.
     *
     * @param index the index to replace
     * @param toReplaceWith the span to replace with
     * @return true, if successful
     */
    protected boolean replaceElementWithNewSpan(int index, TimeSpan toReplaceWith)
    {
        if (index >= 0 && index < myElementList.size() && !myElementList.get(index).equals(toReplaceWith))
        {
            myElementList.remove(index);
            myElementList.add(index, toReplaceWith);
            return true;
        }
        return false;
    }

    /**
     * An interface definition used for lambda executions. Used with methods
     * that return booleans, such as {@link TimeSpan#contains(TimeSpan)}, or
     * {@link TimeSpan#overlaps(TimeSpan)}.
     */
    private interface MembershipTest
    {
        /**
         * Performs the test, by executing the function on the
         * <code>object</code> using the <code>parameter</code> as an argument.
         * For example, when TimeSpan::contains is used to implement the
         * interface, the equivalent execution is
         * <code>object.contains(parameter);</code>. Likewise, when
         * TimeSpan::overlaps is used as the implementation, the equivalent
         * execution is <code>object.overlaps(parameter);</code>.
         *
         * @param object the object on which to perform the test.
         * @param parameter the parameter to supply to the test.
         * @return true if the test passes, false otherwise.
         */
        boolean execute(TimeSpan object, TimeSpan parameter);
    }
}
