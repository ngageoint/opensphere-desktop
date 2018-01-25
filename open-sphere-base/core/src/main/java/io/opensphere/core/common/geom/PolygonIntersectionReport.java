package io.opensphere.core.common.geom;

import java.util.ArrayList;

/**
 * A simple class to hold an intersection report where the details of which
 * polygons that intersected with this reports base polygon are stored.
 */
public class PolygonIntersectionReport
{

    PolygonHD myPolygon;

    ArrayList<PolygonHD> myIntersectingPolygons;

    /**
     * CTOR with a base polygon provided.
     *
     * @param poly
     */
    public PolygonIntersectionReport(PolygonHD poly)
    {
        myPolygon = poly;
        myIntersectingPolygons = new ArrayList<>();
    }

    /**
     * Gets the base polygon for this intersection report
     *
     * @return
     */
    public PolygonHD getPolygon()
    {
        return myPolygon;
    }

    /**
     * Adds a polygon to the list of intersecting polygons note: if poly is the
     * same as the base poly for this report it is ignored
     *
     * @param poly
     */
    public void addIntersectingPolygon(PolygonHD poly)
    {
        if (poly != null && poly != myPolygon)
        {
            myIntersectingPolygons.add(poly);
        }
    }

    /**
     * Returns true if this Report has at least one intersecting polygon.
     *
     * @return true if at least one, false if none
     */
    public boolean hasIntersections()
    {
        return myIntersectingPolygons.size() > 0;
    }

    /**
     * Retrieves the list of Polygon that intersect with the base polygon
     *
     * @return
     */
    public ArrayList<PolygonHD> getIntersectingPolygons()
    {
        return myIntersectingPolygons;
    }

}
