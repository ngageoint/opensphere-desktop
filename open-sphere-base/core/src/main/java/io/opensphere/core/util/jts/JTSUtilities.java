package io.opensphere.core.util.jts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.GeographicPositionArrayList;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.model.GeographicUtilities.PolygonWinding;

/** JTS utilities which do not depend on core Geometries.. */
@SuppressWarnings("PMD.GodClass")
public final class JTSUtilities
{
    /**
     * A geometry factor which creates geometries in the system default
     * geographic reference system.
     */
    public static final GeometryFactory GEOGRAPHIC_REFERENCED_FACTORY = new GeometryFactory(
            new PrecisionModel(PrecisionModel.FLOATING), 4326);

    /** The default factory to use for ordinary geometries. */
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * The default number of line segments to use when approximating a circle.
     */
    public static final int NUM_CIRCLE_SEGMENTS = 36;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(JTSUtilities.class);

    /** A polygon encompassing the whole globe. */
    private static final Polygon WHOLE_GLOBE = createJTSPolygon(GeographicBoundingBox.WHOLE_GLOBE.getVertices(),
            Collections.emptyList());

    /**
     * Decompose the exterior ring and interior rings of the polygon and create
     * lists of geographic positions from the parts.
     *
     * @param poly The polygon to convert.
     * @return The first element is the exterior ring the second part is the
     *         collection of interior rings.
     */
    public static Pair<List<GeographicPosition>, Collection<List<? extends GeographicPosition>>> convertToGeographicPositions(
            Polygon poly)
    {
        Coordinate[] outerCoords = poly.getExteriorRing().getCoordinates();
        double[] outerRingBuffer = new double[outerCoords.length * 2];
        for (int i = 0, j = 0; i < outerCoords.length; ++i, j += 2)
        {
            Coordinate cd = outerCoords[i];
            outerRingBuffer[j] = cd.y;
            outerRingBuffer[j + 1] = cd.x;
        }
        GeographicPositionArrayList outerRing = GeographicPositionArrayList.createFromDegrees(outerRingBuffer);

        Collection<List<? extends GeographicPosition>> innerRings = null;
        if (poly.getNumInteriorRing() > 0)
        {
            innerRings = New.collection(poly.getNumInteriorRing());
            for (int k = 0; k < poly.getNumInteriorRing(); ++k)
            {
                Coordinate[] coords = poly.getInteriorRingN(k).getCoordinates();
                double[] innerRingBuffer = new double[coords.length * 2];
                for (int i = 0, j = 0; i < coords.length; ++i, j += 2)
                {
                    Coordinate cd = coords[i];
                    innerRingBuffer[j] = cd.y;
                    innerRingBuffer[j + 1] = cd.x;
                }

                GeographicPositionArrayList innerRing = GeographicPositionArrayList.createFromDegrees(innerRingBuffer);
                innerRings.add(innerRing);
            }
        }

        return new Pair<>(outerRing, innerRings);
    }

    /**
     * Convert the line string to a list of LatLonAlt with the given reference
     * level.
     *
     * @param coordinates The coordinates to convert.
     * @param reference The reference level of the converted positions.
     * @return The converted positions.
     */
    public static List<LatLonAlt> convertToLatLonAlt(Coordinate[] coordinates, Altitude.ReferenceLevel reference)
    {
        return convertToLatLonAlt(reference, coordinates);
    }

    /**
     * Convert the line string to a list of LatLonAlt with the given reference
     * level.
     *
     * @param coordinates The coordinates to convert.
     * @param reference The reference level of the converted positions.
     * @return The converted positions.
     */
    public static List<LatLonAlt> convertToLatLonAlt(Altitude.ReferenceLevel reference, Coordinate... coordinates)
    {
        List<LatLonAlt> llas = New.list(coordinates.length);
        for (int i = 0; i < coordinates.length; ++i)
        {
            llas.add(LatLonAlt.createFromDegrees(coordinates[i].y, coordinates[i].x, reference));
        }
        return llas;
    }

    /**
     * Decompose the exterior ring and interior rings of the polygon and create
     * lists of geographic positions from the parts.
     *
     * @param poly The polygon to convert.
     * @param reference The reference level for the generated geographic
     *            positions.
     * @return The first element is the exterior ring the second part is the
     *         collection of interior rings.
     */
    public static Pair<List<LatLonAlt>, Collection<List<LatLonAlt>>> convertToLatLonAlt(Polygon poly, ReferenceLevel reference)
    {
        List<LatLonAlt> outerRing = convertToLatLonAlt(poly.getExteriorRing().getCoordinates(), reference);

        Collection<List<LatLonAlt>> innerRings = null;
        if (poly.getNumInteriorRing() > 0)
        {
            innerRings = New.collection(poly.getNumInteriorRing());
            for (int k = 0; k < poly.getNumInteriorRing(); ++k)
            {
                innerRings.add(convertToLatLonAlt(poly.getInteriorRingN(k).getCoordinates(), reference));
            }
        }

        return new Pair<>(outerRing, innerRings);
    }

    /**
     * Convert a list of positions to a linear ring.
     *
     * @param vertices The positions to convert.
     * @return The newly generated linear ring.
     */
    public static LinearRing convertToLinearRing(List<? extends Position> vertices)
    {
        boolean closePolygon = !vertices.get(0).equals(vertices.get(vertices.size() - 1));
        Coordinate[] coords = new Coordinate[closePolygon ? vertices.size() + 1 : vertices.size()];
        for (int i = 0; i < vertices.size(); ++i)
        {
            Position position = vertices.get(i);
            Vector3d vec = position.asVector3d();
            coords[i] = new Coordinate(vec.getX(), vec.getY(), vec.getZ());
        }
        if (closePolygon)
        {
            coords[coords.length - 1] = coords[0];
        }
        return GEOMETRY_FACTORY.createLinearRing(coords);
    }

    /**
     * A helper method to compare coordinates since,
     * Coordinate.equals(Coordinate) uses == to compare doubles.
     *
     * @param vertex1 The first vertex to compare.
     * @param vertex2 The second vertex to compare.
     * @param dimension The dimension of the vertices.
     * @return True when the vertices are at the same location.
     */
    public static boolean coordinateEquals(Coordinate vertex1, Coordinate vertex2, int dimension)
    {
        if (vertex1 == null && vertex2 == null)
        {
            return true;
        }
        if (vertex1 == null || vertex2 == null)
        {
            return false;
        }

        if (!MathUtil.isZero(vertex1.x - vertex2.x) || !MathUtil.isZero(vertex1.y - vertex2.y))
        {
            return false;
        }

        return dimension != 3 || MathUtil.isZero(vertex1.z - vertex2.z);
    }

    /**
     * Create a {@link Polygon} that approximates a circle with a given number
     * of segments. TODO this does not produce correct circles. They will be
     * distorted as they get closer to the poles. This may be correct behavior
     * when generating bounding circles for geographic polygons, but incorrect
     * when generating projected circles for display purposes. See
     * EllipseGeometry for correct projected circle implementation. Also, be
     * aware that adding a dependency on the projection package will create a
     * cyclic dependency.
     *
     * @param center The center of the circle.
     * @param edge A point at the edge of the circle.
     * @param numSegments the number of segments.
     * @return The polygon.
     */
    public static Polygon createCircle(LatLonAlt center, LatLonAlt edge, int numSegments)
    {
        // Just use a unit sphere for quick calculations.
        double lat1 = Math.toRadians(center.getLatD());
        double lon1 = Math.toRadians(center.getLonD());
        double lat2 = Math.toRadians(edge.getLatD());
        double lon2 = Math.toRadians(edge.getLonD());
        double sinLat1 = Math.sin(lat1);
        double cosLat1 = Math.cos(lat1);
        double sinLon1 = Math.sin(lon1);
        double cosLon1 = Math.cos(lon1);
        double sinLat2 = Math.sin(lat2);
        double cosLat2 = Math.cos(lat2);
        double sinLon2 = Math.sin(lon2);
        double cosLon2 = Math.cos(lon2);
        double x1 = sinLon1 * cosLat1;
        double y1 = cosLon1 * cosLat1;
        double z1 = sinLat1;
        double x2 = sinLon2 * cosLat2;
        double y2 = cosLon2 * cosLat2;
        double z2 = sinLat2;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        Vector3d axis = new Vector3d(x1, y1, z1);
        Vector3d arm = new Vector3d(dx, dy, dz);

        // TODO: There has to be a better way to do this.

        int numVertices = numSegments;
        Coordinate[] coord = new Coordinate[numVertices + 1];
        for (int i = 0; i < numVertices; i++)
        {
            double theta = i * MathUtil.TWO_PI / numVertices;

            Vector3d rotatedArm = arm.rotate(axis, theta);

            Vector3d point = axis.add(rotatedArm);
            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();
            double lat = Math.asin(z);
            double lon = Math.atan2(x, y);

            double lonD = Math.toDegrees(lon);
            double latD = Math.toDegrees(lat);
            coord[i] = new Coordinate(lonD, latD);
        }
        coord[numVertices] = coord[0];
        return new Polygon(GEOMETRY_FACTORY.createLinearRing(coord), null, GEOMETRY_FACTORY);
    }

    /**
     * Create a polygon from a list of geographic positions.
     *
     * @param vertices The vertices of the polygon.
     * @param holes The holes in the polygon or {@code null} if there are no
     *            holes.
     * @return The newly created polygon.
     */
    public static Polygon createJTSPolygon(List<? extends Position> vertices,
            Collection<? extends List<? extends Position>> holes)
    {
        if (vertices == null)
        {
            return null;
        }

        LinearRing outerRing = JTSUtilities.convertToLinearRing(vertices);
        if (holes == null || holes.isEmpty())
        {
            return GEOMETRY_FACTORY.createPolygon(outerRing, null);
        }

        LinearRing[] innerRings = new LinearRing[holes.size()];
        int index = 0;
        for (List<? extends Position> innerVertices : holes)
        {
            innerRings[index++] = JTSUtilities.convertToLinearRing(innerVertices);
        }
        return GEOMETRY_FACTORY.createPolygon(outerRing, innerRings);
    }

    /**
     * Create a polygon from a list of geographic locations.
     *
     * @param vertices The vertices of the polygon.
     * @param holes The holes in the polygon or {@code null} if there are no
     *            holes.
     * @return The newly created polygon.
     */
    public static Polygon createJTSPolygonFromLatLonAlt(List<? extends LatLonAlt> vertices,
            Collection<? extends List<? extends LatLonAlt>> holes)
    {
        List<GeographicPosition> convertedVerts = New.list(vertices.size());
        for (LatLonAlt lla : vertices)
        {
            convertedVerts.add(new GeographicPosition(lla));
        }

        if (holes == null)
        {
            return createJTSPolygon(convertedVerts, null);
        }

        Collection<List<? extends Position>> convertedHoles = New.collection();
        for (List<? extends LatLonAlt> hole : holes)
        {
            List<GeographicPosition> convertedHole = New.list(hole.size());
            for (LatLonAlt lla : hole)
            {
                convertedHole.add(new GeographicPosition(lla));
            }
            convertedHoles.add(convertedHole);
        }

        return createJTSPolygon(convertedVerts, convertedHoles);
    }

    /**
     * Construct a polygon that caps one of the poles. TODO currently this only
     * works for polygons which have no holes.
     *
     * @param coordinates The coordinates describing the edge of the cap.
     * @return A polygon that circumscribes the pole closest to the input
     *         coordinates.
     */
    public static Geometry createPolarPolygon(Coordinate[] coordinates)
    {
        Geometry result;
        Coordinate[] newCoords = new Coordinate[coordinates.length + 4];
        for (int i = 1; i < coordinates.length; i++)
        {
            double dx = coordinates[i].x - coordinates[i - 1].x;
            if (Math.abs(dx) > 180.)
            {
                System.arraycopy(coordinates, 0, newCoords, 0, i);
                System.arraycopy(coordinates, i, newCoords, i + 4, coordinates.length - i);

                double polarLat = Math.signum(newCoords[i - 1].y) * 90.;
                if (dx < 180.)
                {
                    newCoords[i] = new Coordinate(180., newCoords[i - 1].y);
                    newCoords[i + 1] = new Coordinate(180., polarLat);
                    newCoords[i + 2] = new Coordinate(-180., polarLat);
                    newCoords[i + 3] = new Coordinate(-180., newCoords[i + 4].y);
                }
                else
                {
                    newCoords[i] = new Coordinate(-180., newCoords[i - 1].y);
                    newCoords[i + 1] = new Coordinate(-180., polarLat);
                    newCoords[i + 2] = new Coordinate(180., polarLat);
                    newCoords[i + 3] = new Coordinate(180., newCoords[i + 4].y);
                }
            }
        }
        GeometryFactory geomFactory = new GeometryFactory();
        result = geomFactory.createPolygon(geomFactory.createLinearRing(newCoords), null);
        return result;
    }

    /**
     * Create a JTS polygon.
     *
     * @param west The west longitude.
     * @param east The east longitude.
     * @param south The south latitude.
     * @param north The north latitude.
     * @param geomFactory A geometry factory to use or <code>null</code> to use
     *            the default factory.
     * @return The geometry.
     */
    public static Polygon createPolygon(double west, double east, double south, double north, GeometryFactory geomFactory)
    {
        GeometryFactory factory = geomFactory == null ? JTSUtilities.GEOMETRY_FACTORY : geomFactory;
        Coordinate[] coord;
        double dx = east - west;
        if (dx == 360. && north == 90. ^ south == -90.)
        {
            double lat = north == 90 ? south : north;
            coord = new Coordinate[4];
            for (int i = 0; i < 3; i++)
            {
                coord[i] = new Coordinate(-180. + i * 360. / 3, lat);
            }
            coord[3] = coord[0];
        }
        else if (dx < 0. && dx > -180 || dx > 180.)
        {
            double midX = west + dx * .5;
            if (dx < 0.)
            {
                if (midX < 0.)
                {
                    midX += 180.;
                }
                else
                {
                    midX -= 180.;
                }
            }
            coord = new Coordinate[7];
            coord[0] = new Coordinate(west, south);
            coord[1] = new Coordinate(midX, south);
            coord[2] = new Coordinate(east, south);
            coord[3] = new Coordinate(east, north);
            coord[4] = new Coordinate(midX, north);
            coord[5] = new Coordinate(west, north);
            coord[6] = coord[0];
        }
        else
        {
            coord = new Coordinate[5];
            coord[0] = new Coordinate(west, south);
            coord[1] = new Coordinate(east, south);
            coord[2] = new Coordinate(east, north);
            coord[3] = new Coordinate(west, north);
            coord[4] = coord[0];
        }
        return new Polygon(factory.createLinearRing(coord), null, factory);
    }

    /**
     * Creates the polygon from bounds.
     *
     * @param geomFactory the geom factory or <code>null</code> to use the
     *            default factory.
     * @param minLon the min lon
     * @param maxLon the max lon
     * @param minLat the min lat
     * @param maxLat the max lat
     * @return the polygon
     */
    public static Polygon createPolygonFromBounds(GeometryFactory geomFactory, double minLon, double maxLon, double minLat,
            double maxLat)
    {
        GeometryFactory factory = geomFactory == null ? JTSUtilities.GEOMETRY_FACTORY : geomFactory;
        List<Coordinate> negSideIntersectPolyCds = New.list();
        negSideIntersectPolyCds.add(new Coordinate(minLon, maxLat));
        negSideIntersectPolyCds.add(new Coordinate(maxLon, maxLat));
        negSideIntersectPolyCds.add(new Coordinate(maxLon, minLat));
        negSideIntersectPolyCds.add(new Coordinate(minLon, minLat));
        negSideIntersectPolyCds.add(new Coordinate(minLon, maxLat));
        return createPolygonFromCoordinateList(factory, negSideIntersectPolyCds);
    }

    /**
     * Creates the polygon from coordinate list.
     *
     * @param geomFactory the geom factory or <code>null</code> to use the
     *            default factory.
     * @param cl the cl
     * @return the polygon
     */
    public static Polygon createPolygonFromCoordinateList(GeometryFactory geomFactory, List<Coordinate> cl)
    {
        GeometryFactory factory = geomFactory == null ? JTSUtilities.GEOMETRY_FACTORY : geomFactory;
        com.vividsolutions.jts.geom.LinearRing linearRing = factory.createLinearRing(cl.toArray(new Coordinate[cl.size()]));
        return factory.createPolygon(linearRing, null);
    }

    /**
     * Create a list of positions from an array of JTS coordinates. This will
     * also subdivide the segments to help out the <code>Globe</code>.
     *
     * @param coordinates The coordinates.
     * @return The positions.
     */
    public static List<GeographicPosition> createPositions(Coordinate[] coordinates)
    {
        List<GeographicPosition> vertices = New.list(coordinates.length);
        vertices.add(createPosition(coordinates[0]));
        for (int i = 1; i < coordinates.length; i++)
        {
            vertices.addAll(subdivide(vertices.get(vertices.size() - 1), createPosition(coordinates[i])));
        }
        vertices.remove(vertices.size() - 1);

        return vertices;
    }

    /**
     * Generate the JTS coordinates for the vertices.
     *
     * @param vertices The vertices for which the coordinates are desired.
     * @return The JTS coordinates.
     */
    public static Coordinate[] generateCoords(List<? extends Position> vertices)
    {
        Coordinate[] coords = null;
        if (!vertices.isEmpty())
        {
            coords = new Coordinate[vertices.size() + 1];
            Vector3d position = null;
            for (int i = 0; i < vertices.size(); i++)
            {
                Position vertex = vertices.get(i);
                if (vertex instanceof GeographicPosition)
                {
                    GeographicPosition geo = (GeographicPosition)vertex;
                    coords[i] = new Coordinate(geo.getLatLonAlt().getLonD(), geo.getLatLonAlt().getLatD(),
                            geo.getLatLonAlt().getAltM());
                }
                if (vertex instanceof ScreenPosition)
                {
                    position = vertices.get(i).asVector3d();
                    coords[i] = new Coordinate(position.getX(), position.getY());
                }
                else
                {
                    position = vertices.get(i).asVector3d();
                    coords[i] = new Coordinate(position.getX(), position.getY(), position.getZ());
                }
            }
            coords[coords.length - 1] = coords[0];
        }
        return coords;
    }

    /**
     * Get the boundary of the given polygon.
     *
     * @param poly The polygon.
     * @return The boundary, or {@code null} if the boundary cannot be computed.
     */
    public static Polygon getBoundaryPolygon(Polygon poly)
    {
        com.vividsolutions.jts.geom.Geometry boundary = poly.getBoundary();
        LinearRing ring;
        if (boundary instanceof MultiLineString)
        {
            MultiLineString mls = (MultiLineString)boundary;
            ring = null;
            for (int index = 0; index < mls.getNumGeometries(); ++index)
            {
                com.vividsolutions.jts.geom.Geometry geom = mls.getGeometryN(index);
                if (geom instanceof LinearRing && (ring == null || ring.getNumPoints() < ((LinearRing)geom).getNumPoints()))
                {
                    ring = (LinearRing)geom;
                }
            }
        }
        else if (boundary instanceof LinearRing)
        {
            ring = (LinearRing)boundary;
        }
        else
        {
            LOGGER.warn("Resulting geometry is unexpected type: " + boundary.getClass());
            ring = null;
        }
        return ring == null ? null : GEOMETRY_FACTORY.createPolygon(ring, null);
    }

    /**
     * Determine which direction is the natural winding order for the polygon
     * defined by the vertices; it is expected that the polygon is closed.
     *
     * @param vertices The vertices which form the polygon.
     * @return the winding direction of the polygon.
     */
    public static PolygonWinding getNaturalWinding(Coordinate[] vertices)
    {
        if (vertices.length < 3)
        {
            return PolygonWinding.UNKNOWN;
        }

        // TODO this method can be adapted to work in three dimensions by
        // determining the polygon's plane using the cross product of any two
        // segments, then projecting the vertices using the projection that
        // projects the polygon's plane onto the x-y plane (this should make all
        // of the z values for the vertices 0).

        double sum = 0.;
        Coordinate previous = vertices[0];
        for (int i = 1; i < vertices.length; ++i)
        {
            Coordinate current = vertices[i];
            sum += (current.x - previous.x) * (current.y + previous.y);
            previous = current;
        }
        return sum > 0. ? PolygonWinding.CLOCKWISE : PolygonWinding.COUNTER_CLOCKWISE;
    }

    /**
     * Eliminate extra colinear vertices in a coordinate sequence.
     *
     * @param coordinateSequence The input coordinate sequence.
     * @return The result coordinate sequence with the extra vertices removed,
     *         or {@code null} if no colinear points were found.
     */
    public static CoordinateSequence removeColinearCoordinates(CoordinateSequence coordinateSequence)
    {
        if (coordinateSequence.size() < 3)
        {
            return null;
        }

        List<Coordinate> result = null;

        Coordinate a = coordinateSequence.getCoordinate(0);
        Coordinate b = coordinateSequence.getCoordinate(1);
        Coordinate c;
        int coordIndex = 2;

        do
        {
            c = coordinateSequence.getCoordinate(coordIndex);

            if (duplicateOrColinear(a, b, c))
            {
                if (result == null)
                {
                    // Copy the coordinate over to a new sequence, skipping "b"
                    result = New.list(coordinateSequence.size() - 1);
                    for (int ix = 0; ix < coordIndex - 1; ++ix)
                    {
                        result.add(coordinateSequence.getCoordinate(ix));
                    }
                    result.add(c);
                }
                else
                {
                    // Replace "b" in the new sequence with "c".
                    result.set(result.size() - 1, c);
                }
                b = c;
            }
            else
            {
                a = b;
                b = c;
                if (result != null)
                {
                    result.add(c);
                }
            }
        }
        while (++coordIndex < coordinateSequence.size());

        c = result == null ? coordinateSequence.getCoordinate(1) : result.get(1);
        if (duplicateOrColinear(a, b, c))
        {
            if (result == null)
            {
                result = New.list(coordinateSequence.size() - 1);
                for (int ix = 1; ix < coordinateSequence.size() - 1; ++ix)
                {
                    result.add(coordinateSequence.getCoordinate(ix));
                }
                result.add(coordinateSequence.getCoordinate(1));
            }
            else
            {
                result.remove(0);
                result.set(result.size() - 1, result.get(0));
            }
        }
        return result == null ? null
                : GEOMETRY_FACTORY.getCoordinateSequenceFactory().create(New.array(result, Coordinate.class));
    }

    /**
     * Confine longitude to the interval [-180, 180).
     * 
     * @param p bla
     * @return bla
     */
    public static Polygon cutLon180(Polygon p)
    {
        if (p == null)
        {
            return null;
        }
        Coordinate[] ex = cutLon180(p.getExteriorRing().getCoordinates(), true);
        int nHole = p.getNumInteriorRing();
        LinearRing[] holes = new LinearRing[nHole];
        for (int i = 0; i < nHole; i++)
        {
            holes[i] = GEOMETRY_FACTORY.createLinearRing(cutLon180(p.getInteriorRingN(i).getCoordinates(), true));
        }
        return GEOMETRY_FACTORY.createPolygon(GEOMETRY_FACTORY.createLinearRing(ex), holes);
    }

    /**
     * Confine longitude to the interval [-180, 180).
     * 
     * @param coords bla
     * @param close use true if the endpoints much match
     * @return bla
     */
    private static Coordinate[] cutLon180(Coordinate[] coords, boolean close)
    {
        Coordinate[] newCoords = new Coordinate[coords.length];
        int n = coords.length - 1;
        for (int i = 0; i < n; i++)
        {
            newCoords[i] = cutLon180(coords[i]);
        }
        if (close)
        {
            newCoords[n] = newCoords[0];
        }
        else
        {
            newCoords[n] = cutLon180(coords[n]);
        }
        return newCoords;
    }

    /**
     * Confine longitude to the interval [-180, 180).
     * 
     * @param c bla
     * @return bla
     */
    private static Coordinate cutLon180(Coordinate c)
    {
        if (-180.0 <= c.x && c.x < 180.0)
        {
            return c;
        }
        return new Coordinate(mod360Cut180(c.x), c.y, c.z);
    }

    /**
     * Adjusts the vertices of a LineString much as the namesake method for
     * Polygon.
     * 
     * @param ln a LineString
     * @return an adjusted LineString
     */
    public static LineString joinLon(LineString ln)
    {
        if (ln == null)
        {
            return null;
        }
        return GEOMETRY_FACTORY.createLineString(joinLon(ln.getCoordinates(), false));
    }

    /**
     * Adjusts the vertices of a Polygon that may span the antimeridian in
     * degrees of longitude. The result is a new Polygon that is geometrically
     * representative of the original but whose longitude coordinates may not be
     * confined to a specific 360-degree interval.
     * 
     * @param p the Polygon
     * @return the modified Polygon
     */
    public static Polygon joinLon(Polygon p)
    {
        GeometryFactory fact = new GeometryFactory();
        Coordinate[] ex = joinLon(p.getExteriorRing().getCoordinates(), true);
        int nHole = p.getNumInteriorRing();
        LinearRing[] holes = new LinearRing[nHole];
        for (int i = 0; i < nHole; i++)
        {
            holes[i] = fact.createLinearRing(joinLon(p.getInteriorRingN(i).getCoordinates(), ex[0], true));
        }
        return fact.createPolygon(fact.createLinearRing(ex), holes);
    }

    /**
     * As the namesake but with no external point of reference.
     * 
     * @param coords set of points
     * @param close use true if the endpoints much match
     * @return adjusted points
     */
    private static Coordinate[] joinLon(Coordinate[] coords, boolean close)
    {
        return joinLon(coords, coords[0], close);
    }

    /**
     * Adjust a set of points to be in a contiguous region of longitude with the
     * specified origin point.
     * 
     * @param coords set of points
     * @param origin origin point
     * @param close use true if the endpoints much match
     * @return adjusted points
     */
    private static Coordinate[] joinLon(Coordinate[] coords, Coordinate origin, boolean close)
    {
        Coordinate[] newCoords = new Coordinate[coords.length];
        Coordinate c0 = lonStep(origin, coords[0]);
        newCoords[0] = c0;
        int n = coords.length - 1;
        for (int i = 1; i < n; i++)
        {
            c0 = lonStep(c0, coords[i]);
            newCoords[i] = c0;
        }
        if (close)
        {
            newCoords[n] = newCoords[0];
        }
        else
        {
            newCoords[n] = lonStep(c0, coords[n]);
        }
        return newCoords;
    }

    /**
     * Given a starting point as (lon, lat, alt) and an ending point, calculate
     * an equivalent ending point such that the difference in longitude always
     * lies in the half-open interval [-180, 180). If the original ending point
     * already satisfies the desired condition, then it is returned unmodified;
     * otherwise a new Coordinate with the altered longitude value is created
     * and returned.
     * 
     * @param c0 a starting point with longitude in degrees
     * @param c1 an ending point with longitude in degrees
     * @return see above
     */
    private static Coordinate lonStep(Coordinate c0, Coordinate c1)
    {
        double dx = c1.x - c0.x;
        if (-180.0 <= dx && dx < 180.0)
        {
            return c1;
        }
        return new Coordinate(c0.x + mod360Cut180(dx), c1.y, c1.z);
    }

    /**
     * Convert a longitude value in degrees to an equivalent value that is in
     * the half-open interval [-180, 180).
     * 
     * @param x longitude in degrees
     * @return see above
     */
    private static double mod360Cut180(double x)
    {
        double modX = x;
        while (modX < -180.0)
        {
            modX += 360.0;
        }
        while (modX >= 180.0)
        {
            modX -= 360.0;
        }
        return modX;
    }

    /**
     * Split this polygon into multiple polygons divided on the antimeridian
     * when division is necessary. If the bounds and winding are both unknown it
     * will be assumed that no segment of the the polygon spans more than 180
     * degrees longitudinally.
     *
     * @param polygon the polygon to split if necessary.
     * @param bounds If known the bounding box provides more information because
     *            it is always defined from left to right even when the box
     *            crosses the antimeridian. This may be {@code null} when it is
     *            unknown.
     * @param winding The winding direction.
     * @return the resultant polygons or the original polygon if no division was
     *         necessary.
     */
    public static List<Polygon> splitOnAntimeridian(Polygon polygon, GeographicBoundingBox bounds, PolygonWinding winding)
    {
        if (polygon.getCoordinates().length < 3 || bounds != null && !bounds.crossesAntimeridian() || polygon.equals(WHOLE_GLOBE))
        {
            return Collections.singletonList(polygon);
        }

        if (bounds != null)
        {
            // We know the bounds and we know that this polygon crosses the
            // antimeridian.
            Polygon copy = (Polygon)polygon.clone();
            normalizeByBoundingBox(copy, bounds);
            return doDivideOnAntimeridian(copy);
        }
        else
        {
            if (winding == PolygonWinding.UNKNOWN)
            {
                Polygon copy = (Polygon)polygon.clone();
                boolean normalized = normalizeBySegmentLength(copy);
                if (normalized)
                {
                    return doDivideOnAntimeridian(copy);
                }
                else
                {
                    return Collections.singletonList(polygon);
                }
            }
            else
            {
                // Get the natural winding. If this is different then the
                // winding we know we should have then we crossed the
                // antimeridian.
                PolygonWinding naturalWinding = getNaturalWinding(polygon.getExteriorRing().getCoordinates());
                if (naturalWinding.equals(winding))
                {
                    return Collections.singletonList(polygon);
                }
                else
                {
                    // TODO we can use the winding direction to determine the
                    // interior of the polygon. We should be able to use this to
                    // correctly normalize the polygon and allow for segments
                    // longer than 180 degrees longitudinally.
                    Polygon copy = (Polygon)polygon.clone();
                    normalizeBySegmentLength(copy);
                    return doDivideOnAntimeridian(copy);
                }
            }
        }
    }

    /**
     * Create a geographic position from a JTS {@link Coordinate}.
     *
     * @param coordinate The coordinates.
     * @return The position.
     */
    public static GeographicPosition createPosition(Coordinate coordinate)
    {
        return new GeographicPosition(LatLonAlt.createFromDegrees(coordinate.y, coordinate.x, Altitude.ReferenceLevel.TERRAIN));
    }

    /**
     * This method should be used only for polygons whose coordinates are in the
     * longitude range of 180 to 540. 360 will be subtracted from each
     * coordinate's longitude.
     *
     * @param polygon The polygon to shift.
     */
    private static void denormalizePolygon(Polygon polygon)
    {
        if (polygon.getCentroid().getCoordinate().x > 180.)
        {
            for (Coordinate coord : polygon.getExteriorRing().getCoordinates())
            {
                coord.setCoordinate(new Coordinate(coord.x - 360., coord.y, coord.z));
            }

            for (int i = 0; i < polygon.getNumInteriorRing(); ++i)
            {
                for (Coordinate coord : polygon.getInteriorRingN(i).getCoordinates())
                {
                    coord.setCoordinate(new Coordinate(coord.x - 360., coord.y, coord.z));
                }
            }
        }
    }

    /**
     * Divide a normalized polygon which we know crosses the antimeridian.
     *
     * @param polygon The polygon to divide.
     * @return The polygons produced by dividing at the antimeridian.
     */
    private static List<Polygon> doDivideOnAntimeridian(Polygon polygon)
    {
        List<Polygon> polygons = New.list();

        // Get the polygon(s) on the left of the antimeridian.
        Polygon crop = createPolygonFromBounds(JTSUtilities.GEOMETRY_FACTORY, -180., 180., -90., 90.);
        Geometry intersection = crop.intersection(polygon);
        for (int i = 0; i < intersection.getNumGeometries(); ++i)
        {
            Geometry part = intersection.getGeometryN(i);
            if (part instanceof Polygon)
            {
                Polygon partPoly = (Polygon)part;
                denormalizePolygon(partPoly);
                polygons.add(partPoly);
            }
        }

        // Get the polygon(s) on the right of the antimeridian.
        crop = createPolygonFromBounds(JTSUtilities.GEOGRAPHIC_REFERENCED_FACTORY, 180., 180. + 360., -90., 90.);
        intersection = crop.intersection(polygon);
        for (int i = 0; i < intersection.getNumGeometries(); ++i)
        {
            Geometry part = intersection.getGeometryN(i);
            if (part instanceof Polygon)
            {
                Polygon partPoly = (Polygon)part;
                denormalizePolygon(partPoly);
                polygons.add(partPoly);
            }
        }

        return polygons;
    }

    /**
     * Iff a==b or b==c or a and b and c lie on the same line, return
     * {@code true}.
     *
     * @param a A coordinate.
     * @param b A coordinate.
     * @param c A coordinate.
     * @return The result.
     */
    private static boolean duplicateOrColinear(Coordinate a, Coordinate b, Coordinate c)
    {
        boolean remove;
        if (MathUtil.isZero(a.x - b.x) && MathUtil.isZero(a.y - b.y) || MathUtil.isZero(c.x - b.x) && MathUtil.isZero(c.y - b.y))
        {
            remove = true;
        }
        else
        {
            // Test to see if the coordinates are colinear.
            double delta = (a.y - b.y) / (a.x - b.x) - (c.y - b.y) / (c.x - b.x);
            if (Double.isInfinite(delta))
            {
                delta = (a.x - b.x) / (a.y - b.y) - (c.x - b.x) / (c.y - b.y);
            }
            remove = MathUtil.isZero(delta);
        }
        return remove;
    }

    /**
     * Shift the longitude of parts of the polygon which span the antimeridian
     * to make the polygon contiguous in Cartesian coordinates. Antimeridian
     * crossings will be determined by forcing the longitudes to be within the
     * bounding box. This method modifies the provided polygon.
     *
     * @param polygon the polygon to normalize
     * @param bounds the bounding envelope of the polygon
     */
    private static void normalizeByBoundingBox(Polygon polygon, GeographicBoundingBox bounds)
    {
        double minX = bounds.getLowerLeft().getLatLonAlt().getLonD();
        for (Coordinate coord : polygon.getExteriorRing().getCoordinates())
        {
            if (coord.x < minX)
            {
                coord.setCoordinate(new Coordinate(coord.x + 360., coord.y, coord.z));
            }
        }

        for (int i = 0; i < polygon.getNumInteriorRing(); ++i)
        {
            for (Coordinate coord : polygon.getInteriorRingN(i).getCoordinates())
            {
                if (coord.x < minX)
                {
                    coord.setCoordinate(new Coordinate(coord.x + 360., coord.y, coord.z));
                }
            }
        }
    }

    /**
     * Shift the longitude of parts of the polygon which span the antimeridian
     * to make the polygon contiguous in Cartesian coordinates. It is assumed
     * that any segment of the polygon whose longitude length is greater than
     * 180 degrees represents a antimeridian crossing. This method modifies the
     * provided polygon.
     *
     * @param polygon the polygon to normalize
     * @return true when the polygon was normalized or false if the polygon is
     *         already contiguous.
     */
    private static boolean normalizeBySegmentLength(Polygon polygon)
    {
        // If the outer ring doesn't require normalization none of the inner
        // rings will either.
        boolean normalizedOuter = normalizeRingBySegmentLength(polygon.getExteriorRing().getCoordinates());
        if (normalizedOuter)
        {
            for (int i = 0; i < polygon.getNumInteriorRing(); ++i)
            {
                normalizeRingBySegmentLength(polygon.getInteriorRingN(i).getCoordinates());
            }
        }
        return normalizedOuter;
    }

    /**
     * A helper method to normalize a ring of a polygon. Shift the longitude of
     * parts of the ring which span the antimeridian to make the ring contiguous
     * in Cartesian coordinates. It is assumed that any segment of the ring
     * whose longitude length is greater than 180 degrees represents a
     * antimeridian crossing.
     *
     * @param ring the ring to normalize
     * @return true when the ring was normalized or false if the ring is already
     *         contiguous.
     */
    private static boolean normalizeRingBySegmentLength(Coordinate[] ring)
    {
        boolean shifted = false;
        Coordinate lastCoord = null;
        boolean negativeShift = false;
        for (Coordinate coord : ring)
        {
            // find the crossings and move the right hand side by + 360.
            if (lastCoord != null && Math.abs(coord.x - lastCoord.x) > 180.)
            {
                if (coord.x < 0)
                {
                    coord.setCoordinate(new Coordinate(coord.x + 360., coord.y, coord.z));
                }
                else
                {
                    // if this happens once, it will always happen.
                    negativeShift = true;
                    coord.setCoordinate(new Coordinate(coord.x - 360., coord.y, coord.z));
                }
                shifted = true;
            }
            lastCoord = coord;
        }

        if (negativeShift)
        {
            for (Coordinate coord : ring)
            {
                coord.setCoordinate(new Coordinate(coord.x + 360., coord.y, coord.z));
            }
        }

        return shifted;
    }

    /**
     * Subdivide a line to help the terrain projection out. TODO I don't think
     * we need to do this anymore. This should be looked into more.
     *
     * @param start The start point.
     * @param end The end point.
     * @return The result list of positions.
     */
    private static List<GeographicPosition> subdivide(GeographicPosition start, GeographicPosition end)
    {
        final double interval = 10.;
        LatLonAlt endLLA = end.getLatLonAlt();
        LatLonAlt startLLA = start.getLatLonAlt();
        double diff = endLLA.getLonD() - startLLA.getLonD();
        final double circleDegrees = 360.;
        if (diff < -180. && diff > -circleDegrees)
        {
            diff = 360. + diff;
        }
        else if (diff > 180. && diff < circleDegrees)
        {
            diff = 360. - diff;
        }
        List<GeographicPosition> result = new ArrayList<>();

        int splits = (int)Math.abs(diff / interval) + 1;
        for (int i = 1; i < splits; i++)
        {
            result.add(start.interpolate(end, (double)i / splits, false));
        }

        result.add(end);
        return result;
    }

    /** Disallow instantiation. */
    private JTSUtilities()
    {
    }
}
