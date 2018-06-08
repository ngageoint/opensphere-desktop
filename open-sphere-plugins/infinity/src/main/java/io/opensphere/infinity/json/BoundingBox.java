package io.opensphere.infinity.json;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.jts.JTSUtilities;

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
        GeographicBoundingBox queryBbox = GeographicBoundingBox.getMinimumBoundingBoxLLA(
                JTSUtilities.convertToLatLonAlt(geometry.getCoordinates(), Altitude.ReferenceLevel.ELLIPSOID));
        myBottomRight = new Coordinate(queryBbox.getLowerRight());
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
}
