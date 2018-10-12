package io.opensphere.core.model.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import io.opensphere.core.util.collections.New;

/**
 * An unmodifiable array list of non-overlapping {@link TimeSpan}s.
 */
public class TimeSpanArrayList extends TimeSpanList implements RandomAccess
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My time spans. */
    private final List<TimeSpan> myTimeSpans;

    /**
     * Constructor.
     *
     * @param timeSpans The list of time spans.
     */
    public TimeSpanArrayList(Collection<? extends TimeSpan> timeSpans)
    {
        if (timeSpans.isEmpty())
        {
            myTimeSpans = Collections.emptyList();
        }
        else
        {
            ArrayList<TimeSpan> list = new ArrayList<>(timeSpans);
            mergeOverlaps(list);
            myTimeSpans = Collections.unmodifiableList(list);
        }
    }

    @Override
    public TimeSpanList clone(Collection<? extends TimeSpan> spans)
    {
        return new TimeSpanArrayList(spans);
    }

    @Override
    public TimeSpan get(int index)
    {
        return myTimeSpans.get(index);
    }

    /**
     * Get my time spans.
     *
     * @return The list of time spans.
     */
    public List<TimeSpan> getTimeSpans()
    {
        return myTimeSpans;
    }

    @Override
    public TimeSpanList intersection(TimeSpan ts)
    {
        List<TimeSpan> intersectList = New.list();
        for (TimeSpan part : this)
        {
            if (part != null && part.overlaps(ts))
            {
                TimeSpan intersection = part.getIntersection(ts);
                if (intersection != null)
                {
                    intersectList.add(intersection);
                }
            }
        }
        if (intersectList.isEmpty())
        {
            return emptyList();
        }
        return new TimeSpanArrayList(intersectList);
    }

    @Override
    public TimeSpanList intersection(TimeSpanList other)
    {
        List<TimeSpan> intersectList = New.list();
        for (TimeSpan aTS : this)
        {
            if (aTS != null)
            {
                for (TimeSpan oTS : other)
                {
                    if (oTS != null && aTS.overlaps(oTS))
                    {
                        TimeSpan intersection = aTS.getIntersection(oTS);
                        if (intersection != null)
                        {
                            intersectList.add(intersection);
                        }
                    }
                }
            }
        }
        if (intersectList.isEmpty())
        {
            return emptyList();
        }
        return new TimeSpanArrayList(intersectList);
    }

    @Override
    public int size()
    {
        return myTimeSpans.size();
    }

    @Override
    public TimeSpanList union(TimeSpan ts)
    {
        List<TimeSpan> unionList = New.list(this);
        unionList.add(ts);
        return new TimeSpanArrayList(unionList);
    }

    @Override
    public TimeSpanList union(TimeSpanList other)
    {
        List<TimeSpan> unionList = New.list(this);
        unionList.addAll(other);
        return new TimeSpanArrayList(unionList);
    }
}
