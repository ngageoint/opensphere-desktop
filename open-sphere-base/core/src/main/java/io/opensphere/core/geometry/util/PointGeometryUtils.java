package io.opensphere.core.geometry.util;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;

/**
 * A set of utility methods used to interact with {@link PointGeometry}s.
 */
public final class PointGeometryUtils
{
    /**
     * Default constructor, hidden from use.
     */
    private PointGeometryUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Converts the supplied collection of {@link PointGeometry}s to an instance
     * of the JTS {@link MultiPoint} class.
     *
     * @param points the points to convert.
     * @return a {@link MultiPoint} containing all of the supplied points.
     */
    public static MultiPoint convertToMultiPoint(Collection<PointGeometry> points)
    {
        Point[] convertedPoints = new Point[points.size()];
        for (PointGeometry pointGeometry : points)
        {
            pointGeometry.getPosition().asVector3d();
            pointGeometry.getPosition();
        }

        List<Position> positions = New.list(points.size());
        points.forEach(pg -> positions.add(pg.getPosition()));
        Coordinate[] coordinates = JTSUtilities.generateCoords(positions);

        for (int i = 0; i < coordinates.length; i++)
        {
            convertedPoints[i] = new Point(new CoordinateArraySequence(new Coordinate[] { coordinates[i] }),
                    JTSUtilities.GEOMETRY_FACTORY);
        }

        return new MultiPoint(convertedPoints, JTSUtilities.GEOMETRY_FACTORY);
    }
}
