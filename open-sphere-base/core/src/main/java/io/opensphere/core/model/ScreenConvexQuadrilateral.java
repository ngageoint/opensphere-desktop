package io.opensphere.core.model;

import java.util.List;

/**
 * A quadrilateral which consists of screen positions which is known to be
 * convex.
 */
public class ScreenConvexQuadrilateral extends ScreenQuadrilateral
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param pos1 The first vertex.
     * @param pos2 The second vertex.
     * @param pos3 The third vertex.
     * @param pos4 The fourth vertex.
     */
    public ScreenConvexQuadrilateral(ScreenPosition pos1, ScreenPosition pos2, ScreenPosition pos3, ScreenPosition pos4)
    {
        super(pos1, pos2, pos3, pos4);
    }

    /**
     * Constructor.
     *
     * @param vertices The vertices, which must number 4.
     */
    public ScreenConvexQuadrilateral(List<ScreenPosition> vertices)
    {
        super(vertices);
        if (!PolygonUtilities.isConvex(vertices))
        {
            throw new IllegalArgumentException("The given vertices to not form a convex polygon.");
        }
    }

    @Override
    public boolean contains(Position position, double tolerance)
    {
        // Fail faster if the point is not within the minimum bounding box.
        if (!(position instanceof ScreenPosition)
                || !ScreenBoundingBox.getMinimumBoundingBox(getVertices()).contains(position, tolerance))
        {
            return false;
        }

        return PolygonUtilities.containsConvex(getVertices(), position, tolerance);
    }

    @Override
    public boolean overlaps(Quadrilateral<? extends ScreenPosition> otherQuad, double tolerance)
    {
        if (!(otherQuad instanceof ScreenQuadrilateral))
        {
            return false;
        }
        ScreenQuadrilateral otherGeo = (ScreenQuadrilateral)otherQuad;
        return ScreenPolygonUtilities.overlaps(getVertices(), null, otherGeo.getVertices(), null, tolerance,
                otherGeo instanceof ScreenConvexQuadrilateral);
    }
}
