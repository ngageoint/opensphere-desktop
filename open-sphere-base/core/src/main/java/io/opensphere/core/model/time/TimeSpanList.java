package io.opensphere.core.model.time;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.opensphere.core.util.collections.New;

/**
 * A list of non-overlapping {@link TimeSpan}s. Concrete implementations should
 * call {@link TimeSpanList#mergeOverlaps(List)} to ensure that the time spans
 * in the list do not overlap.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class TimeSpanList extends AbstractList<TimeSpan> implements Serializable
{
    /** A timeless time span. */
    public static final TimeSpanList TIMELESS = singleton(TimeSpan.TIMELESS);

    /** Empty list. */
    private static final TimeSpanList EMPTY_LIST = new TimeSpanList()
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        @Override
        public TimeSpanList clone(Collection<? extends TimeSpan> spans)
        {
            return new TimeSpanArrayList(spans);
        }

        @Override
        public TimeSpan get(int index)
        {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 0");
        }

        @Override
        public TimeSpan getExtent()
        {
            return TimeSpan.ZERO;
        }

        @Override
        public TimeSpanList intersection(TimeSpan ts)
        {
            return emptyList();
        }

        @Override
        public TimeSpanList intersection(TimeSpanList other)
        {
            return emptyList();
        }

        @Override
        public int size()
        {
            return 0;
        }

        @Override
        public TimeSpanList union(TimeSpan ts)
        {
            return singleton(ts);
        }

        @Override
        public TimeSpanList union(TimeSpanList other)
        {
            return clone(other);
        }
    };

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Get an empty list.
     *
     * @return The empty list.
     */
    public static TimeSpanList emptyList()
    {
        return EMPTY_LIST;
    }

    /**
     * Find any overlapping time spans in the list and merge them.
     *
     * @param list The list of time spans.
     */
    public static void mergeOverlaps(final List<TimeSpan> list)
    {
        mergeOverlaps(list, false);
    }

    /**
     * Find any overlapping time spans in the list and merge them.
     *
     * @param list The list of time spans.
     * @param mergeTouching Whether to merge spans that touch
     */
    public static void mergeOverlaps(final List<TimeSpan> list, boolean mergeTouching)
    {
        if (list.size() > 1)
        {
            Integer[] arr = new Integer[list.size()];
            for (int i = 0; i < list.size();)
            {
                arr[i] = Integer.valueOf(i++);
            }
            Arrays.sort(arr, (o1, o2) -> list.get(o1.intValue()).compareTo(list.get(o2.intValue())));

            int index0 = 0;
            TimeSpan span0 = list.get(arr[index0].intValue());
            int index1 = 1;
            do
            {
                TimeSpan span1 = list.get(arr[index1].intValue());
                if (span0.overlaps(span1) || mergeTouching && span0.touches(span1))
                {
                    span0 = span0.union(span1);
                    list.set(arr[index0].intValue(), span0);
                    list.set(arr[index1].intValue(), null);
                }
                else
                {
                    index0 = index1;
                    span0 = span1;
                }
            }
            while (++index1 < arr.length);

            for (Iterator<TimeSpan> iter = list.iterator(); iter.hasNext();)
            {
                TimeSpan span = iter.next();
                if (span == null)
                {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Get a singleton time span list.
     *
     * @param obj The time span for the list.
     * @return The list.
     */
    public static TimeSpanList singleton(TimeSpan obj)
    {
        return new SingletonList(obj);
    }

    /**
     * Clone this list, but with different contents.
     *
     * @param spans The contents of the new list.
     * @return The new list.
     */
    public abstract TimeSpanList clone(Collection<? extends TimeSpan> spans);

    /**
     * Get if this time span list covers all of the time in a time span.
     *
     * @param other The other time span.
     * @return {@code true} if all the time is covered.
     */
    public boolean covers(TimeSpan other)
    {
        List<TimeSpan> remainder = null;
        List<TimeSpan> remainder2 = null;
        for (TimeSpan span : this)
        {
            if (remainder == null)
            {
                remainder = other.subtract(span);
            }
            else
            {
                if (remainder2 == null)
                {
                    remainder2 = New.list();
                }
                else
                {
                    remainder2.clear();
                }
                for (TimeSpan span2 : remainder)
                {
                    remainder2.addAll(span2.subtract(span));
                }
                List<TimeSpan> tmp = remainder2;
                remainder2 = remainder;
                remainder = tmp;
            }
            if (remainder.isEmpty())
            {
                return true;
            }
            // Make the remainder mutable.
            remainder = New.list(remainder);
        }

        return false;
    }

    /**
     * Get if this time span list covers all of the time in another time span
     * list.
     *
     * @param other The other time spans.
     * @return {@code true} if all the time is covered.
     */
    public boolean covers(TimeSpanList other)
    {
        for (TimeSpan span : other)
        {
            if (!covers(span))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the time span that comprises all the time spans in this list.
     *
     * @return The overall time span.
     */
    public TimeSpan getExtent()
    {
        if (isEmpty())
        {
            return TimeSpan.ZERO;
        }
        else if (size() == 1)
        {
            return get(0);
        }
        else
        {
            long minTime = Long.MAX_VALUE;
            long maxTime = Long.MIN_VALUE;
            for (int index = 0; index < size(); ++index)
            {
                if (get(index).getStart() < minTime)
                {
                    minTime = get(index).getStart();
                }

                if (get(index).getEnd() > maxTime)
                {
                    maxTime = get(index).getEnd();
                }
            }
            return TimeSpan.get(minTime, maxTime);
        }
    }

    /**
     * Creates a new TimeSpanList that represents the intersection of this
     * {@link TimeSpanList} with a {@link TimeSpan} or empty list if no
     * intersection.
     *
     * @param ts the {@link TimeSpan} to intersect
     * @return the intersected list
     */
    public abstract TimeSpanList intersection(TimeSpan ts);

    /**
     * Creates a new TimeSpanList that represents the intersection of this list
     * with another list. Note: could be an empty list.
     *
     * @param other the other list to intersect
     * @return the intersected list.
     */
    public abstract TimeSpanList intersection(TimeSpanList other);

    /**
     * Determine if any of the time spans in this list intersect the input time
     * span.
     *
     * @param timeSpan The time span of interest.
     * @return {@code true} if an intersection is found.
     */
    public boolean intersects(TimeSpan timeSpan)
    {
        for (TimeSpan span : this)
        {
            if (span.overlaps(timeSpan))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates and returns the union of this {@link TimeSpanList} and another
     * {@link TimeSpan}.
     *
     * @param ts the {@link TimeSpan} to union with this {@link TimeSpanList}
     * @return the union
     */
    public abstract TimeSpanList union(TimeSpan ts);

    /**
     * Creates and returns the union of this {@link TimeSpanList} and another
     * {@link TimeSpanList}.
     *
     * @param other the other {@link TimeSpanList}
     * @return the union
     */
    public abstract TimeSpanList union(TimeSpanList other);

    /** Singleton implementation. */
    private static class SingletonList extends TimeSpanList
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The single time span. */
        private final TimeSpan myTimeSpan;

        /**
         * Constructor.
         *
         * @param obj The single object.
         */
        SingletonList(TimeSpan obj)
        {
            myTimeSpan = obj;
        }

        @Override
        public TimeSpanList clone(Collection<? extends TimeSpan> spans)
        {
            return new TimeSpanArrayList(spans);
        }

        @Override
        public boolean contains(Object obj)
        {
            return Objects.equals(obj, myTimeSpan);
        }

        @Override
        public TimeSpan get(int index)
        {
            if (index != 0)
            {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
            }
            return myTimeSpan;
        }

        @Override
        public TimeSpan getExtent()
        {
            return myTimeSpan;
        }

        @Override
        public TimeSpanList intersection(TimeSpan ts)
        {
            if (myTimeSpan.overlaps(ts))
            {
                TimeSpan intersection = myTimeSpan.getIntersection(ts);
                return intersection == null ? emptyList() : singleton(intersection);
            }
            return emptyList();
        }

        @Override
        public TimeSpanList intersection(TimeSpanList other)
        {
            TimeSpanList resultList = null;
            if (myTimeSpan != null)
            {
                List<TimeSpan> intersectList = New.list();
                for (TimeSpan oTS : other)
                {
                    if (myTimeSpan.overlaps(oTS))
                    {
                        TimeSpan intersection = myTimeSpan.getIntersection(oTS);
                        if (intersection != null)
                        {
                            intersectList.add(intersection);
                        }
                    }
                }
                if (intersectList.size() == 1)
                {
                    resultList = singleton(intersectList.get(0));
                }
                else if (intersectList.size() > 1)
                {
                    resultList = clone(intersectList);
                }
            }
            return resultList == null ? emptyList() : resultList;
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public TimeSpanList union(TimeSpan ts)
        {
            List<TimeSpan> unionList = New.list();
            unionList.add(myTimeSpan);
            unionList.add(ts);
            return clone(unionList);
        }

        @Override
        public TimeSpanList union(TimeSpanList other)
        {
            List<TimeSpan> unionList = New.list(other);
            unionList.add(myTimeSpan);
            return other.clone(unionList);
        }
    }
}
