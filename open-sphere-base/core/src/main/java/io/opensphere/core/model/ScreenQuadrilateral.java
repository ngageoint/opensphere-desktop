package io.opensphere.core.model;

import java.util.List;

/** A quadrilateral which consists of screen positions. */
public class ScreenQuadrilateral extends AbstractQuadrilateral<ScreenPosition>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param vertices The vertices, which must number 4.
     */
    public ScreenQuadrilateral(List<? extends ScreenPosition> vertices)
    {
        super(vertices);
    }

    /**
     * Constructor.
     *
     * @param pos1 The first vertex.
     * @param pos2 The second vertex.
     * @param pos3 The third vertex.
     * @param pos4 The fourth vertex.
     */
    public ScreenQuadrilateral(ScreenPosition pos1, ScreenPosition pos2, ScreenPosition pos3, ScreenPosition pos4)
    {
        super(pos1, pos2, pos3, pos4);
    }

    @Override
    public boolean contains(Position position, double tolerance)
    {
        if (position instanceof ScreenPosition
                && !ScreenBoundingBox.getMinimumBoundingBox(getVertices()).contains(position, tolerance))
        {
            return false;
        }

        return PolygonUtilities.containsRayCast(getVertices(), position);
    }

    @Override
    public Position getCenter()
    {
        ScreenPosition pos = getVertices().get(0).add(getVertices().get(1).add(getVertices().get(2).add(getVertices().get(3))));
        final double quarter = 0.25;
        double x = pos.getX() * quarter;
        double y = pos.getY() * quarter;
        return new ScreenPosition(x, y);
    }

    @Override
    public boolean overlaps(Quadrilateral<? extends ScreenPosition> otherQuad, double tolerance)
    {
        if (!(otherQuad instanceof ScreenQuadrilateral))
        {
            return false;
        }

        ScreenQuadrilateral otherGeo = (ScreenQuadrilateral)otherQuad;
        return ScreenPolygonUtilities.overlaps(getVertices(), null, otherGeo.getVertices(), null, tolerance, false);
    }
}
