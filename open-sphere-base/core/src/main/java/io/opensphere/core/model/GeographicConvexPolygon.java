package io.opensphere.core.model;

import java.util.List;

/**
 * A geographic polygon which is convex.
 */
public class GeographicConvexPolygon extends GeographicPolygon
{
    /**
     * Constructor.
     *
     * @param vertices The vertices of the polygon.
     */
    public GeographicConvexPolygon(List<? extends GeographicPosition> vertices)
    {
        super(vertices);
    }

    @Override
    public boolean contains(GeographicPolygon testPolygon, double tolerance)
    {
        return PolygonUtilities.containsConvex(getVertices(), testPolygon.getVertices(), tolerance);
    }

    @Override
    public boolean contains(GeographicPosition position, double tolerance)
    {
        if (isDegenerate() || !getBoundingBox().contains(position, tolerance))
        {
            return false;
        }

        return PolygonUtilities.containsConvex(getVertices(), position, tolerance);
    }

    @Override
    public boolean overlaps(GeographicPolygon polygon, double tolerance)
    {
        return GeographicPolygonUtilities.overlaps(getVertices(), getBoundingCircle(), polygon.getVertices(),
                polygon.getBoundingCircle(), tolerance, true);
    }
}
