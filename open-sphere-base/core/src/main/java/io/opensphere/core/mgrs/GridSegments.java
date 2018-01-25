package io.opensphere.core.mgrs;

/** Simple class to hold four line segments that define a grid. */
public class GridSegments
{
    /** The east line segment. */
    private final LineSegment myEastSegment;

    /** The north line segment. */
    private final LineSegment myNorthSegment;

    /** The south line segment. */
    private final LineSegment mySouthSegment;

    /** The west line segment. */
    private final LineSegment myWestSegment;

    /**
     * Constructor.
     *
     * @param southSeg The south line segment.
     * @param northSeg The north line segment.
     * @param eastSeg The east line segment.
     * @param westSeg The west line segment.
     */
    public GridSegments(LineSegment southSeg, LineSegment northSeg, LineSegment eastSeg, LineSegment westSeg)
    {
        mySouthSegment = southSeg;
        myNorthSegment = northSeg;
        myEastSegment = eastSeg;
        myWestSegment = westSeg;
    }

    /**
     * Standard getter.
     *
     * @return The east line segment.
     */
    public LineSegment getEastSegment()
    {
        return myEastSegment;
    }

    /**
     * Standard getter.
     *
     * @return The north line segment.
     */
    public LineSegment getNorthSegment()
    {
        return myNorthSegment;
    }

    /**
     * Standard getter.
     *
     * @return The south line segment.
     */
    public LineSegment getSouthSegment()
    {
        return mySouthSegment;
    }

    /**
     * Standard getter.
     *
     * @return The west line segment.
     */
    public LineSegment getWestSegment()
    {
        return myWestSegment;
    }
}
