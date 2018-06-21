package io.opensphere.infinity.json;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;

/** Elasticsearch bounding box JSON bean. */
@JsonPropertyOrder({ "bottom_right", "top_left" })
public class BoundingBox
{
    /** The bottom right coordinate. */
    private Coordinate myBottomRight;

    /** The top left coordinate. */
    private Coordinate myTopLeft;

    /**
     * Constructor.
     */
    public BoundingBox()
    {
    }

    /**
     * Constructor from a JTS geometry.
     *
     * @param geometry the geometry
     */
    public BoundingBox(Geometry geometry)
    {
        GeographicBoundingBox queryBbox = getMinimumBoundingBoxLLA(geometry.getCoordinates());
        myBottomRight = new Coordinate(queryBbox.getLowerRight());
        myBottomRight.setLon(adjustLon(myBottomRight.getLon()));
        myTopLeft = new Coordinate(queryBbox.getUpperLeft());
    }

    /**
     * Gets the bottomRight.
     *
     * @return the bottomRight
     */
    public Coordinate getBottom_right()
    {
        return myBottomRight;
    }

    /**
     * Sets the bottomRight.
     *
     * @param bottomRight the bottomRight
     */
    public void setBottom_right(Coordinate bottomRight)
    {
        myBottomRight = bottomRight;
    }

    /**
     * Gets the topLeft.
     *
     * @return the topLeft
     */
    public Coordinate getTop_left()
    {
        return myTopLeft;
    }

    /**
     * Sets the topLeft.
     *
     * @param topLeft the topLeft
     */
    public void setTop_left(Coordinate topLeft)
    {
        myTopLeft = topLeft;
    }

    /**
     * Get the smallest bounding box which contains all of the coordinates.
     *
     * @param coordinates The coordinates which must be contained in the box.
     * @return The smallest bounding box which contains all of the coordinates.
     */
    static GeographicBoundingBox getMinimumBoundingBoxLLA(com.vividsolutions.jts.geom.Coordinate[] coordinates)
    {
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (com.vividsolutions.jts.geom.Coordinate coord : coordinates)
        {
            double latD = coord.y;
            minLat = Math.min(minLat, latD);
            maxLat = Math.max(maxLat, latD);

            double lonD = coord.x;
            minLon = Math.min(minLon, lonD);
            maxLon = Math.max(maxLon, lonD);
        }

        return new GeographicBoundingBox(LatLonAlt.createFromDegrees(minLat, minLon),
                LatLonAlt.createFromDegrees(maxLat, maxLon));
    }

    /**
     * Adjusts the longitude if it's greater than 180.
     *
     * @param lon the longitude
     * @return the adjusted longitude
     */
    static double adjustLon(double lon)
    {
        return lon > 180 ? -(360 - lon) : lon;
    }
}
