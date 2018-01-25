package io.opensphere.core.geometry.util;

import java.util.Collection;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;

/**
 * A set of utility methods used to interact with {@link PolygonGeometry}s.
 */
public final class PolygonGeometryUtils
{
    /**
     * Default constructor, hidden from use.
     */
    private PolygonGeometryUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Converts the supplied collection of {@link PolygonGeometry} instances
     * into a JTS {@link MultiPolygon}.
     *
     * @param polygons the {@link Collection} of {@link PolygonGeometry}
     *            instances to convert.
     * @return A JTS {@link MultiPolygon} composed of the converted
     *         {@link PolygonGeometry} instances.
     */
    public static MultiPolygon convertToMultiPolygon(Collection<PolygonGeometry> polygons)
    {
        int index = 0;
        Polygon[] convertedPolygons = new Polygon[polygons.size()];

        for (PolygonGeometry polygon : polygons)
        {
            convertedPolygons[index++] = JTSCoreGeometryUtilities.convertToJTSPolygon(polygon);
        }

        return new MultiPolygon(convertedPolygons, JTSUtilities.GEOMETRY_FACTORY);
    }
}
