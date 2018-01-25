package io.opensphere.core.model;

import java.util.List;

/** A quadrilateral which consists of geographic positions. */
public class GeographicQuadrilateral extends AbstractQuadrilateral<GeographicPosition>
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param vertices The vertices, which must number 4.
     */
    public GeographicQuadrilateral(List<GeographicPosition> vertices)
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
    public GeographicQuadrilateral(GeographicPosition pos1, GeographicPosition pos2, GeographicPosition pos3,
            GeographicPosition pos4)
    {
        super(pos1, pos2, pos3, pos4);
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

        return PolygonUtilities.containsRayCast(getVertices(), position);
    }

    @Override
    public GeographicPosition getCenter()
    {
        GeographicPosition pos = getVertices().get(0)
                .add(getVertices().get(1).add(getVertices().get(2).add(getVertices().get(3))));
        final double quarter = 0.25;
        double latD = pos.getLatLonAlt().getLatD() * quarter;
        double lonD = pos.getLatLonAlt().getLonD() * quarter;
        double altM = pos.getLatLonAlt().getAltM() * quarter;

        return new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(latD, lonD, altM, getVertices().get(0).getLatLonAlt().getAltitudeReference()));
    }

    @Override
    public boolean overlaps(Quadrilateral<? extends GeographicPosition> otherQuad, double tolerance)
    {
        return GeographicPolygonUtilities.overlaps(getVertices(), null, otherQuad.getVertices(), null, tolerance, false);
    }
}
