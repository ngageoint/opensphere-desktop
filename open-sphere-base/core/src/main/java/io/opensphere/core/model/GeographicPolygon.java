package io.opensphere.core.model;

import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * A polygon whose vertices are geographic positions. The vertices are expected
 * to be given in counter-clockwise order.
 */
public class GeographicPolygon
{
    /** The minimum box which bounds all of the vertices. */
    private GeographicBoundingBox myBoundingBox;

    /**
     * A circle which bounds all of the vertices. This may not be the minimal
     * bounding circle, but it will minimally bound the bounding box.
     */
    private GeographicBoundingCircle myBoundingCircle;

    /** The vertices of the polygon in counter-clockwise order. */
    private final List<? extends GeographicPosition> myVertices;

    /**
     * Constructor.
     *
     * @param vertices The vertices of the polygon in counter-clockwise order.
     */
    public GeographicPolygon(List<? extends GeographicPosition> vertices)
    {
        myVertices = New.unmodifiableList(vertices);
    }

    /**
     * Determine whether the test polygon is completely contained within this
     * polygon.
     *
     * @param testPolygon the polygon to test for containment.
     * @param tolerance The testing tolerance.
     * @return true when the test polygon is completely contained within this
     *         polygon.
     */
    public boolean contains(GeographicPolygon testPolygon, double tolerance)
    {
        return PolygonUtilities.contains(getVertices(), testPolygon.getVertices());
    }

    /**
     * Determine whether the position falls within this polygon.
     *
     * @param position The position to test for containment.
     * @param tolerance The tolerance for containment.
     * @return true when the position is within this polygon.
     */
    public boolean contains(GeographicPosition position, double tolerance)
    {
        return PolygonUtilities.containsRayCast(getVertices(), position);
    }

    /**
     * Get the boundingBox. The bounding box produced by this method is similar
     * to the one given by
     * {@link GeographicBoundingBox#getMinimumBoundingBox(java.util.Collection)}
     * but it makes no attempt to normalize values.
     *
     * @return the boundingBox
     */
    public GeographicBoundingBox getBoundingBox()
    {
        if (myBoundingBox == null && !myVertices.isEmpty())
        {
            double minLat = Double.MAX_VALUE;
            double maxLat = -Double.MAX_VALUE;
            double minLon = Double.MAX_VALUE;
            double maxLon = -Double.MAX_VALUE;
            for (GeographicPosition vertex : myVertices)
            {
                minLat = Math.min(minLat, vertex.getLatLonAlt().getLatD());
                maxLat = Math.max(maxLat, vertex.getLatLonAlt().getLatD());
                minLon = Math.min(minLon, vertex.getLatLonAlt().getLonD());
                maxLon = Math.max(maxLon, vertex.getLatLonAlt().getLonD());
            }

            myBoundingBox = new GeographicBoundingBox(new GeographicPosition(LatLonAlt.createFromDegrees(minLat, minLon)),
                    new GeographicPosition(LatLonAlt.createFromDegrees(maxLat, maxLon)));
        }
        return myBoundingBox;
    }

    /**
     * Get the circle which minimally bounds the bounding box.
     *
     * @return the circle which minimally bounds the bounding box.
     */
    public GeographicBoundingCircle getBoundingCircle()
    {
        if (myBoundingCircle == null && !myVertices.isEmpty())
        {
            GeographicPosition center = getBoundingBox().getCenter();
            double radius = center.getLatLonAlt().asVec2d().distance(getBoundingBox().getUpperRight().getLatLonAlt().asVec2d());
            myBoundingCircle = new GeographicBoundingCircle(center, radius);
        }
        return myBoundingCircle;
    }

    /**
     * Get the vertices.
     *
     * @return the vertices
     */
    public List<? extends GeographicPosition> getVertices()
    {
        return myVertices;
    }

    /**
     * Determine whether this polygon is degenerate.
     *
     * @return true when the polygon is degenerate.
     */
    public boolean isDegenerate()
    {
        return getVertices().size() < 3;
        // TODO this should also check to see if the vertices are collinear if
        // it is not too computationally expensive.
    }

    /**
     * Determine whether the polygon overlaps this polygon.
     *
     * @param polygon The polygon to test for overlap.
     * @param tolerance The tolerance for overlap.
     * @return true when the polygon overlaps this polygon.
     */
    public boolean overlaps(GeographicPolygon polygon, double tolerance)
    {
        return GeographicPolygonUtilities.overlaps(getVertices(), getBoundingCircle(), polygon.getVertices(),
                polygon.getBoundingCircle(), tolerance, false);
    }
}
