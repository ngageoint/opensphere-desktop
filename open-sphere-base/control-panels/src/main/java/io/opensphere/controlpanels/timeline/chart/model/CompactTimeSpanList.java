package io.opensphere.controlpanels.timeline.chart.model;

import java.util.Collection;
import java.util.Iterator;

import gnu.trove.set.hash.TIntHashSet;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.CompactLongArrayList;

/**
 * A "list" that can store TimeSpans in a more memory-efficient way.
 */
class CompactTimeSpanList implements Iterable<TimeSpan>
{
    /** The indices in the array that are the start of a span. */
    private final TIntHashSet mySpanIndices = new TIntHashSet();

    /**
     * The times. Instances are stored as a single element, and spans are stored
     * as consecutive elements.
     */
    private final CompactLongArrayList myTimes = new CompactLongArrayList();

    /** The indices in the array that are unbounded end spans. */
    private final TIntHashSet myUnboundedEndIndices = new TIntHashSet();

    /** The indices in the array that are unbounded start spans. */
    private final TIntHashSet myUnboundedStartIndices = new TIntHashSet();

    /**
     * Adds the given spans to the list.
     *
     * @param spans the spans to add
     * @return whether the list was modified
     */
    public boolean addAll(Collection<? extends TimeSpan> spans)
    {
//        myTimes.ensureCapacity(myTimes.size() + spans.size());
        int index;
        for (TimeSpan span : spans)
        {
            index = myTimes.size();

            if (span.isBounded())
            {
                myTimes.add(span.getStart());
                if (!span.isInstantaneous())
                {
                    myTimes.add(span.getEnd());
                    mySpanIndices.add(index);
                }
            }
            else if (span.isTimeless())
            {
                myTimes.add(0);
            }
            else if (span.isUnboundedStart())
            {
                myTimes.add(span.getEnd());
                myUnboundedStartIndices.add(index);
            }
            else if (span.isUnboundedEnd())
            {
                myTimes.add(span.getStart());
                myUnboundedEndIndices.add(index);
            }
        }
        return !spans.isEmpty();
    }

    /**
     * Clears the list.
     */
    public void clear()
    {
        myTimes.clear();
        mySpanIndices.clear();
        myUnboundedStartIndices.clear();
        myUnboundedEndIndices.clear();
    }

    /**
     * Gets the extents of the list.
     *
     * @return the time extents
     */
    public TimeSpan getExtents()
    {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long time;
        for (int i = 0; i < myTimes.size(); ++i)
        {
            time = myTimes.get(i);
            if (time != 0)
            {
                if (time > max)
                {
                    max = time;
                }
                if (time < min)
                {
                    min = time;
                }
            }
        }
        return min == Long.MAX_VALUE ? TimeSpan.TIMELESS : TimeSpan.get(min, max);
    }

    @Override
    public Iterator<TimeSpan> iterator()
    {
        return new Iterator<TimeSpan>()
        {
            private int myIndex;

            @Override
            public boolean hasNext()
            {
                return myIndex < myTimes.size();
            }

            @Override
            public TimeSpan next()
            {
                TimeSpan span;
                long time = myTimes.get(myIndex);
                if (time == 0)
                {
                    span = TimeSpan.TIMELESS;
                }
                else if (mySpanIndices.contains(myIndex))
                {
                    span = TimeSpan.get(time, myTimes.get(++myIndex));
                }
                else if (myUnboundedStartIndices.contains(myIndex))
                {
                    span = TimeSpan.newUnboundedStartTimeSpan(time);
                }
                else if (myUnboundedEndIndices.contains(myIndex))
                {
                    span = TimeSpan.newUnboundedEndTimeSpan(time);
                }
                else
                {
                    span = TimeSpan.get(time);
                }
                ++myIndex;
                return span;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("Remove is not supported");
            }
        };
    }
}
