package io.opensphere.core.util.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;

/** Utilities for geographic manipulations. */
public final class GeographicUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeographicUtilities.class);

    /**
     * Decompose a list of points into its constituent polygons. Each ring in
     * the list must be closed and exterior rings must be defined in clockwise
     * order. Inner rings of the polygon must immediately follow their
     * associated exterior ring and must be defined in counter-clockwise order.
     *
     * @param rings the list of positions which form the polygon ring(s).
     * @return The keys in the map are the exterior rings the values are their
     *         associated inner rings.
     */
    public static Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> decomposePositionsToPolygons(List<LatLonAlt> rings)
    {
        Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> polygons = New.map();

        LatLonAlt startPoint = null;
        List<LatLonAlt> curPoly = New.list();
        Collection<List<LatLonAlt>> holes = null;
        // Some shapefiles have duplicate successive points, so we remove them
        // for the following logic work
        List<LatLonAlt> deduped = removeDuplicates(rings);
        for (LatLonAlt lla : deduped)
        {
            curPoly.add(lla);

            if (startPoint == null)
            {
                startPoint = lla;
                continue;
            }

            if (startPoint.equals(lla))
            {
                if (curPoly.size() > 2)
                {
                    /* Exterior rings are defined in clockwise order and inner
                     * rings are defined in counter-clockwise order. Make an
                     * exception to allow a counter-clockwise polygon if it's
                     * the only one in the list. */
                    PolygonWinding winding = getNaturalWinding(curPoly);
                    if (winding == PolygonWinding.CLOCKWISE || deduped.size() == curPoly.size())
                    {
                        holes = New.collection();
                        polygons.put(curPoly, holes);
                    }
                    else
                    {
                        if (holes != null)
                        {
                            holes.add(curPoly);
                        }
                        else
                        {
                            LOGGER.error("Polygon hole listed before polygon or incorrect winding for polygon.");
                        }
                    }
                }
                curPoly = New.list();
                startPoint = null;
            }
        }

        if (!curPoly.isEmpty())
        {
            LOGGER.warn("Encountered unclosed polygon while reading shapefile.");
        }

        return polygons;
    }

    /**
     * Determine which direction is the natural winding order for the polygon
     * defined by the vertices; it is expected that the polygon is closed.
     *
     * @param vertices The vertices which form the polygon.
     * @return the winding direction of the polygon.
     */
    public static PolygonWinding getNaturalWinding(List<LatLonAlt> vertices)
    {
        if (vertices.size() < 3)
        {
            return PolygonWinding.UNKNOWN;
        }

        // TODO this method can be adapted to work in three dimensions by
        // determining the polygon's plane using the cross product of any two
        // segments, then projecting the vertices using the projection that
        // projects the polygon's plane onto the x-y plane (this should make all
        // of the z values for the vertices 0).

        double sum = 0.;
        Vector2d previous = vertices.get(0).asVec2d();
        for (int i = 1; i < vertices.size(); ++i)
        {
            Vector2d current = vertices.get(i).asVec2d();
            sum += (current.getX() - previous.getX()) * (current.getY() + previous.getY());
            previous = current;
        }
        return sum > 0. ? PolygonWinding.CLOCKWISE : PolygonWinding.COUNTER_CLOCKWISE;
    }

    /**
     * Converts the geographic positions to screen coordinates.
     *
     * @param positions The geographic positions to convert.
     * @param screenBounds The screen boundaries.
     * @return The converted screen coordinates.
     */
    public static List<ScreenPosition> toScreenPositions(List<? extends GeographicPosition> positions,
            ScreenBoundingBox screenBounds)
    {
        List<ScreenPosition> screenPositions = New.list();

        for (GeographicPosition geoPos : positions)
        {
            int x = (int)(screenBounds.getWidth() * (geoPos.getLatLonAlt().getLonD() + 180) / 360);
            int y = (int)(screenBounds.getHeight() * (geoPos.getLatLonAlt().getLatD() + 90) / 180);

            screenPositions
                    .add(new ScreenPosition(screenBounds.getUpperLeft().getX() + x, screenBounds.getLowerRight().getY() - y));
        }

        return screenPositions;
    }

    /**
     * Creates a new list with successive duplicate points removed.
     *
     * @param points the points
     * @return the de-duplicated list
     */
    static List<LatLonAlt> removeDuplicates(List<? extends LatLonAlt> points)
    {
        List<LatLonAlt> list = New.list(points.size());
        LatLonAlt lastPoint = null;
        for (LatLonAlt point : points)
        {
            if (!point.equals(lastPoint))
            {
                list.add(point);
                lastPoint = point;
            }
        }
        return list;
    }

    /** Disallow instantiation. */
    private GeographicUtilities()
    {
    }

    /** Winding direction for polygons. */
    public enum PolygonWinding
    {
        /** Winding in the negative theta direction. */
        CLOCKWISE,

        /** Winding in the positive theta direction. */
        COUNTER_CLOCKWISE,

        /** Undetermined direction. */
        UNKNOWN,

        ;
    }
}
