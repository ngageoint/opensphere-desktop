package io.opensphere.core.geometry.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.time.TimeSpan;

/**
 * A set of utility methods used to work with polylines.
 */
public final class PolylineGeometryUtils
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(PolylineGeometryUtils.class);

    /**
     * The JTS factory used to create new geometries.
     */
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Default constructor to prevent instantiation.
     */
    private PolylineGeometryUtils()
    {
        throw new UnsupportedOperationException("Instantiation of Utility Classes is not Permitted.");
    }

    /**
     * Converts the supplied {@link Vector3d} to a JTS {@link Coordinate}
     * instance.
     *
     * @param pVector the source value to convert.
     * @return a JTS {@link Coordinate} generated from the supplied source
     *         vector.
     */
    public static Coordinate convertToCoordinate(Vector3d pVector)
    {
        return new Coordinate(pVector.getX(), pVector.getY(), pVector.getZ());
    }

    /**
     * Converts the supplied JTS {@link Coordinate} to a
     * {@link GeographicPosition}, using the z-coordinate as an altitude
     * expressed from the Ellipsoid reference.
     *
     * @param pCoordinate the coordinate to convert.
     * @return a {@link GeographicPosition} created from the supplied
     *         coordinate.
     */
    public static Position convertToPosition(Coordinate pCoordinate)
    {
        LatLonAlt latLonAlt = LatLonAlt.createFromDegreesMeters(pCoordinate.y, pCoordinate.x, pCoordinate.z,
                ReferenceLevel.ELLIPSOID);

        GeographicPosition position = new GeographicPosition(latLonAlt);

        return position;
    }

    /**
     * Converts the supplied open sphere {@link PolylineGeometry} into a JTS
     * {@link LineString}. This method will omit any duplicate positions from
     * the generated line string.
     *
     * @param pSource the source line to convert.
     * @return a line string generated from the supplied polyline geometry.
     */
    public static LineString convertToLineString(PolylineGeometry pSource)
    {
        Set<Coordinate> usedCoordinates = new HashSet<>();
        List<Coordinate> coordinates = new ArrayList<>();
        List<? extends Position> vertices = pSource.getVertices();
        for (Position position : vertices)
        {
            Coordinate coordinateCopy = convertToCoordinate(position.asVector3d());
            if (!usedCoordinates.contains(coordinateCopy))
            {
                usedCoordinates.add(coordinateCopy);
                coordinates.add(coordinateCopy);
            }
        }

        Coordinate[] coordinateArray = new Coordinate[coordinates.size()];
        coordinateArray = coordinates.toArray(coordinateArray);

        if (coordinateArray.length < 2)
        {
            return null;
        }
        return new LineString(new CoordinateArraySequence(coordinateArray), GEOMETRY_FACTORY);
    }

    /**
     * Converts the supplied {@link LineString} to a {@link PolylineGeometry}.
     *
     * @param pSource the line string to convert.
     * @return a {@link PolylineGeometry} equivalent to the supplied
     *         {@link LineString}.
     */
    public static PolylineGeometry convertToPolyline(LineString pSource)
    {
        Set<Position> usedPositions = new HashSet<>();
        List<Position> positions = new ArrayList<>();
        CoordinateSequence sequence = pSource.getCoordinateSequence();
        for (int coordinateIndex = 0; coordinateIndex < sequence.size(); coordinateIndex++)
        {
            Coordinate coordinate = sequence.getCoordinate(coordinateIndex);
            Position position = convertToPosition(coordinate);
            if (!usedPositions.contains(position))
            {
                usedPositions.add(position);
                positions.add(position);
            }
        }

        PolylineGeometry.Builder<Position> builder = new PolylineGeometry.Builder<>();
        builder.setVertices(positions);

        PolylineRenderProperties polylineRenderProperties = new DefaultPolylineRenderProperties(0, true, true);
        Constraints constraints = new Constraints(TimeConstraint.getTimeConstraint(TimeSpan.TIMELESS));

        return new PolylineGeometry(builder, polylineRenderProperties, constraints);
    }

    /**
     * Combines the supplied line strings into a single {@link LineString}. This
     * method will not duplicate points.
     *
     * @param pLineStrings the set of line strings to join.
     * @return a LineString composed of the coordinates specified in the
     *         supplied source set.
     */
    public static LineString createSingleLineString(Set<LineString> pLineStrings)
    {
        Set<Coordinate> usedCoordinates = new HashSet<>();
        List<Coordinate> coordinates = new ArrayList<>();
        for (LineString lineString : pLineStrings)
        {
            CoordinateSequence sequence = lineString.getCoordinateSequence();
            for (int coordinateIndex = 0; coordinateIndex < sequence.size(); coordinateIndex++)
            {
                Coordinate coordinateCopy = sequence.getCoordinateCopy(coordinateIndex);
                if (!usedCoordinates.contains(coordinateCopy))
                {
                    usedCoordinates.add(coordinateCopy);
                    coordinates.add(coordinateCopy);
                }
            }
        }

        Coordinate[] coordinateArray = new Coordinate[coordinates.size()];
        coordinateArray = coordinates.toArray(coordinateArray);

        return new LineString(new CoordinateArraySequence(coordinateArray), GEOMETRY_FACTORY);
    }

    /**
     * Combines the supplied {@link PolylineGeometry} lines into a single
     * {@link LineString}. This method will not duplicate points.
     *
     * @param pPolylineGeometries the set of {@link PolylineGeometry}s to join.
     * @return a {@link PolylineGeometry} composed of the coordinates specified
     *         in the supplied source set.
     */
    public static PolylineGeometry createSinglePolyline(Set<PolylineGeometry> pPolylineGeometries)
    {
        Set<LineString> lineStrings = new HashSet<>();
        for (PolylineGeometry polylineGeometry : pPolylineGeometries)
        {
            lineStrings.add(convertToLineString(polylineGeometry));
        }

        LineString combinedLineString = createSingleLineString(lineStrings);

        return convertToPolyline(combinedLineString);
    }

    /**
     * Combines the supplied line strings into a {@link MultiLineString}. This
     * method will not duplicate points.
     *
     * @param pLineStrings the set of line strings to join.
     * @return a {@link MultiLineString} composed of the {@link LineString}s
     *         specified in the supplied source set.
     */
    public static MultiLineString createMultiLineString(Set<LineString> pLineStrings)
    {
        LineString[] lineStrings = new LineString[pLineStrings.size()];
        lineStrings = pLineStrings.toArray(lineStrings);

        return new MultiLineString(lineStrings, GEOMETRY_FACTORY);
    }

    /**
     * Combines the supplied {@link PolylineGeometry}s into a
     * {@link MultiLineString}. This method will not duplicate points.
     *
     * @param pPolylineGeometries the set of polyline geometries to join.
     * @return a {@link MultiLineString} composed of the
     *         PolylineGeometry-equivalent {@link LineString}s specified in the
     *         supplied source set.
     */
    public static MultiLineString convertToMultiLineString(Set<PolylineGeometry> pPolylineGeometries)
    {
        Set<LineString> lineStrings = new HashSet<>();
        for (PolylineGeometry polylineGeometry : pPolylineGeometries)
        {
            LineString lineString = convertToLineString(polylineGeometry);
            if (lineString != null)
            {
                lineStrings.add(lineString);
            }
            else
            {
                LOG.info("Omitted polyline with single vertex.");
            }
        }

        LineString[] lineStringArray = new LineString[lineStrings.size()];
        lineStringArray = lineStrings.toArray(lineStringArray);

        return new MultiLineString(lineStringArray, GEOMETRY_FACTORY);
    }
}
