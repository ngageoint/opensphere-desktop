package io.opensphere.core.model.time;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A body of space-time that may or may not be contiguous.
 */
public class SpaceTime
{
    /** The space. */
    private final Geometry[] myGeometries;

    /** The time. */
    private final TimeSpan[] myTimeSpans;

    /**
     * Construct a space-time object from a single geometry and a single time
     * span.
     *
     * @param geometry The geometry.
     * @param timeSpan The list of time spans.
     */
    public SpaceTime(Geometry geometry, TimeSpan timeSpan)
    {
        myGeometries = new Geometry[] { geometry };
        myTimeSpans = new TimeSpan[] { timeSpan };
    }

    /**
     * Construct a space-time object from a list of geometries and a list of
     * time spans. The geometries and time spans will be associated in iteration
     * order of the lists. The lists must be the same size.
     *
     * @param geometries The geometries.
     * @param timeSpans The list of time spans.
     */
    public SpaceTime(List<? extends Geometry> geometries, List<? extends TimeSpan> timeSpans)
    {
        myGeometries = geometries.toArray(new Geometry[geometries.size()]);
        myTimeSpans = timeSpans.toArray(new TimeSpan[timeSpans.size()]);
        if (myGeometries.length != myTimeSpans.length)
        {
            throw new IllegalArgumentException("Input lists must have same size, but provided lists have " + myGeometries.length
                    + " and " + myTimeSpans.length + " elements respectively.");
        }
    }

    /**
     * Find the space-time that exists in this space-time but not in the input
     * space-time. Only identical time spans are actually compared.
     *
     * @param input The other space-time.
     * @return A space-time representing the difference between two space-times.
     */
    public SpaceTime difference(SpaceTime input)
    {
        List<Geometry> spaces = new ArrayList<>();
        List<TimeSpan> times = new ArrayList<>();

        for (int i = 0; i < myTimeSpans.length; ++i)
        {
            boolean foundTime = false;
            for (int j = 0; j < input.myTimeSpans.length; ++j)
            {
                if (myTimeSpans[i].equals(input.myTimeSpans[j]))
                {
                    foundTime = true;

                    spaces.add(myGeometries[i].difference(input.myGeometries[j]));
                    times.add(myTimeSpans[i]);
                }
            }

            if (!foundTime)
            {
                spaces.add(myGeometries[i]);
                times.add(myTimeSpans[i]);
            }
        }

        return new SpaceTime(spaces, times);
    }

    /**
     * Get the geometry.
     *
     * @param index The index of the geometry.
     * @return The geometry.
     */
    public Geometry getGeometry(int index)
    {
        return myGeometries[index];
    }

    /**
     * Get the time span.
     *
     * @param index The index of the time span.
     * @return The time span.
     */
    public TimeSpan getTimeSpan(int index)
    {
        return myTimeSpans[index];
    }

    /**
     * Determine if this space-time intersects a geometry.
     *
     * @param input The geometry.
     * @return <code>true</code> if there is an intersection.
     */
    public boolean intersects(Geometry input)
    {
        for (Geometry geom : myGeometries)
        {
            if (geom.intersects(input))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if this space-time intersects another space-time.
     *
     * @param input The other space-time.
     * @return <code>true</code> if there is an intersection.
     */
    public boolean intersects(SpaceTime input)
    {
        for (int i = 0; i < myTimeSpans.length; ++i)
        {
            for (int j = 0; j < input.myTimeSpans.length; ++j)
            {
                if (myTimeSpans[i].overlaps(input.myTimeSpans[j]) && myGeometries[i].intersects(input.myGeometries[j]))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine if this space-time intersects a time span.
     *
     * @param input The time span.
     * @return <code>true</code> if there is an intersection.
     */
    public boolean intersects(TimeSpan input)
    {
        for (TimeSpan ts : myTimeSpans)
        {
            if (ts.overlaps(input))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if this space-time comprises no space or time.
     *
     * @return <code>true</code> if this space-time is empty.
     */
    public boolean isEmpty()
    {
        if (myTimeSpans.length == 0)
        {
            return true;
        }
        for (Geometry geom : myGeometries)
        {
            if (!geom.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine if this space-time has time spans that are identical, and if
     * so, returns a new space-time with the identical time-spans merged. If no
     * merging is necessary, <code>this</code> is returned.
     *
     * @return A merged space-time or <code>this</code> if no merged was needed.
     */
    public SpaceTime merge()
    {
        if (myTimeSpans.length > 1)
        {
            boolean changed = false;
            TreeMap<TimeSpan, Geometry> map = new TreeMap<>();
            map.put(myTimeSpans[0], myGeometries[0]);
            for (int i = 1; i < myTimeSpans.length; ++i)
            {
                Geometry geometry = map.get(myTimeSpans[i]);
                if (geometry == null)
                {
                    map.put(myTimeSpans[i], myGeometries[i]);
                }
                else
                {
                    changed = true;
                    map.put(myTimeSpans[i], myGeometries[i].union(geometry));
                }
            }
            if (changed)
            {
                return new SpaceTime(new ArrayList<>(map.values()), new ArrayList<>(map.keySet()));
            }
        }
        return this;
    }

    /**
     * Get the number of individual space-time associations in this object.
     *
     * @return The size.
     */
    public int size()
    {
        return myGeometries.length;
    }

    /**
     * Find the union of this space-time with another space-time.
     *
     * @param input The other space-time.
     * @return The union of the two space-times.
     */
    public SpaceTime union(SpaceTime input)
    {
        List<Geometry> spaces = new ArrayList<>();
        List<TimeSpan> times = new ArrayList<>();

        boolean[] inputMatched = new boolean[input.myGeometries.length];

        for (int i = 0; i < myTimeSpans.length; ++i)
        {
            boolean foundTime = false;
            for (int j = 0; j < input.myTimeSpans.length; ++j)
            {
                if (myTimeSpans[i].equals(input.myTimeSpans[j]))
                {
                    foundTime = true;
                    inputMatched[j] = true;

                    spaces.add(myGeometries[i].union(input.myGeometries[j]));
                    times.add(myTimeSpans[i]);
                }
            }

            if (!foundTime)
            {
                spaces.add(myGeometries[i]);
                times.add(myTimeSpans[i]);
            }
        }

        for (int j = 0; j < inputMatched.length; ++j)
        {
            if (!inputMatched[j])
            {
                spaces.add(input.myGeometries[j]);
                times.add(input.myTimeSpans[j]);
            }
        }
        return new SpaceTime(spaces, times);
    }
}
