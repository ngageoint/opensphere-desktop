package io.opensphere.core.mgrs;

import io.opensphere.core.model.GeographicPosition;

/**
 * Simple class that holds two geographic points that determine a line segment.
 */
public class LineSegment
{
    /** The first point in the segment. */
    private final GeographicPosition myFirstPoint;

    /** The second point in the segment. */
    private final GeographicPosition mySecondPoint;

    /**
     * Constructor.
     *
     * @param firstPoint The first GeographicPosition point.
     * @param secondPoint The second GeographicPosition point.
     */
    public LineSegment(GeographicPosition firstPoint, GeographicPosition secondPoint)
    {
        myFirstPoint = firstPoint;
        mySecondPoint = secondPoint;
    }

    /**
     * Standard getter.
     *
     * @return The first point.
     */
    public GeographicPosition getFirstPoint()
    {
        return myFirstPoint;
    }

    /**
     * Standard getter.
     *
     * @return The second point.
     */
    public GeographicPosition getSecondPoint()
    {
        return mySecondPoint;
    }
}
