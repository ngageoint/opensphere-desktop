package io.opensphere.core.model;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * A quadrilateral which consists of geographic positions which is known to be
 * convex.
 */
public class GeographicConvexQuadrilateral extends GeographicQuadrilateral
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeographicConvexQuadrilateral.class);

    /**
     * Constructor.
     *
     * @param pos1 The first vertex.
     * @param pos2 The second vertex.
     * @param pos3 The third vertex.
     * @param pos4 The fourth vertex.
     */
    public GeographicConvexQuadrilateral(GeographicPosition pos1, GeographicPosition pos2, GeographicPosition pos3,
            GeographicPosition pos4)
    {
        super(pos1, pos2, pos3, pos4);
    }

    /**
     * Constructor.
     *
     * @param vertices The vertices, which must number 4.
     */
    public GeographicConvexQuadrilateral(List<GeographicPosition> vertices)
    {
        super(vertices);
        if (!PolygonUtilities.isConvex(vertices))
        {
            LOGGER.warn("The given vertices may not form a convex polygon.");
        }
    }

    @Override
    public boolean contains(Position position, double tolerance)
    {
        // Fail faster if the point is not within the minimum bounding box.
        if (!(position instanceof GeographicPosition)
                || !GeographicBoundingBox.getMinimumBoundingBox(getVertices()).contains(position, tolerance))
        {
            return false;
        }

        return PolygonUtilities.containsConvex(getVertices(), position, tolerance);
    }
}
