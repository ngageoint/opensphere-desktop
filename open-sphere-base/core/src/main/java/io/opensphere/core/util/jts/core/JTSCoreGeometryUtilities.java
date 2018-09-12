package io.opensphere.core.util.jts.core;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryGroupGeometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultZOrderRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.geometry.util.PointGeometryUtils;
import io.opensphere.core.geometry.util.PolygonGeometryUtils;
import io.opensphere.core.geometry.util.PolylineGeometryUtils;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.BoundingBoxes;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.model.GeographicUtilities.PolygonWinding;

/** Utilities which utilize both JTS geometries and core geometries. */
public final class JTSCoreGeometryUtilities
{
    /** The default Z order. */
    private static final int DEFAULT_ZORDER = ZOrderRenderProperties.TOP_Z - 1000;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(JTSCoreGeometryUtilities.class);

    /**
     * Builds the displayable set of PolygonGeometry to designate a selection
     * region, which could be a rectangle, circle, or polygon.
     *
     * @param region the region to convert into a drawable selection set.
     * @param color the color
     * @return the list of {@link PolygonGeometry} that represents the region.
     */
    public static List<PolygonGeometry> buildSelectionPolygonSet(com.vividsolutions.jts.geom.Geometry region, Color color)
    {
        if (region == null)
        {
            return Collections.<PolygonGeometry>emptyList();
        }

        if (region instanceof Polygon)
        {
            return Collections.singletonList(convertToPolygonGeometry((Polygon)region, polyProps(color, 3, false)));
        }
        else if (region instanceof MultiPolygon)
        {
            PolygonRenderProperties props = polyProps(color, 3, false);
            MultiPolygon mPoly = (MultiPolygon)region;
            List<PolygonGeometry> geometries = New.list();
            for (int i = 0; i < mPoly.getNumGeometries(); ++i)
            {
                geometries.add(convertToPolygonGeometry((Polygon)mPoly.getGeometryN(i), props));
            }
            return geometries;
        }
        else
        {
            List<GeographicPosition> positions = JTSUtilities.createPositions(region.getCoordinates());
            // Draw a box around the bottom of the volume.
            return Collections.singletonList(buildBox(positions, color));
        }
    }

    /**
     * Converts the supplied {@link PointGeometry} to an instance of the JTS
     * {@link Point} class.
     *
     * @param geometry the geometry to convert.
     * @return a JTS Point instance generated from the supplied geometry.
     */
    public static Point convertToJTSPoint(PointGeometry geometry)
    {
        Position position = geometry.getPosition();
        Vector3d vec = position.asVector3d();
        Coordinate coordinate = new Coordinate(vec.getX(), vec.getY(), vec.getZ());

        return JTSUtilities.GEOMETRY_FACTORY.createPoint(coordinate);
    }

    /**
     * Convert to jts polygon.
     *
     * @param geom the geom
     * @return the polygon
     */
    public static Polygon convertToJTSPolygon(PolygonGeometry geom)
    {
        if (geom.getVertices().isEmpty())
        {
            return null;
        }
        return JTSUtilities.createJTSPolygon(geom.getVertices(), geom.getHoles());
    }

    /**
     * Convert to JTS polygons and split on antimeridian.
     *
     * @param geoms The geometries to convert.
     * @return A collection of JTS polygons that do not cross the antimeridian.
     */
    @SuppressWarnings("unchecked")
    public static List<Polygon> convertToJTSPolygonsAndSplit(Collection<? extends PolygonGeometry> geoms)
    {
        List<Polygon> polygons = New.list();
        for (PolygonGeometry geom : geoms)
        {
            if (!geom.getVertices().isEmpty())
            {
                Polygon p = JTSUtilities.createJTSPolygon(geom.getVertices(), geom.getHoles());
                GeographicBoundingBox boundingBox = (GeographicBoundingBox)BoundingBoxes
                        .getMinimumBoundingBox((Collection<? extends GeographicPosition>)geom.getVertices());
                polygons.addAll(JTSUtilities.splitOnAntimeridian(p, boundingBox, PolygonWinding.UNKNOWN));
            }
        }
        return polygons;
    }

    /**
     * Creates the polygon geometry from jts polygon.
     *
     * @param poly the poly
     * @param props The render properties for the resulting polygon.
     * @return the polygon geometry
     */
    public static PolygonGeometry convertToPolygonGeometry(Polygon poly, PolygonRenderProperties props)
    {
        if (poly == null)
        {
            return null;
        }
        Pair<List<GeographicPosition>, Collection<List<? extends GeographicPosition>>> positions = JTSUtilities
                .convertToGeographicPositions(poly);
        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<>();
        builder.setVertices(positions.getFirstObject());
        builder.addHoles(positions.getSecondObject());
        builder.setLineType(LineType.STRAIGHT_LINE);
        return new PolygonGeometry(builder, props, null);
    }

    /**
     * Converts the supplied {@link MultiPolygon} to a
     * {@link GeometryGroupGeometry}.
     *
     * @param multiPolygon the multipolygon to convert.
     * @param polyProps the properties to apply to converted children of the
     *            supplied polygon.
     * @param groupProperties the properties the new
     *            {@link GeometryGroupGeometry}.
     * @return a group geometry generated from the supplied multipolygon.
     */
    private static GeometryGroupGeometry convertToMultiPolygonGeometry(MultiPolygon multiPolygon,
            PolygonRenderProperties polyProps, ZOrderRenderProperties groupProperties)
    {
        if (multiPolygon == null)
        {
            return null;
        }

        GeometryGroupGeometry.Builder builder = new GeometryGroupGeometry.Builder(GeographicPosition.class);

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++)
        {
            Polygon initialGeometry = (Polygon)multiPolygon.getGeometryN(i);
            builder.getInitialGeometries().add(convertToPolygonGeometry(initialGeometry, polyProps));
        }

        return new GeometryGroupGeometry(builder, groupProperties);
    }

    /**
     * Create a "buffer" PolygonGeometry for a Geometry which is a
     * PolygonGeometry, a PolylineGeometry (but not also a PolygonGeometry), or
     * a PointGeometry.
     *
     * @param g the Geometry
     * @param distM the buffer distance in meters
     * @return the buffer, if supported, or null
     */
    public static AbstractGeometry getBufferGeom(Geometry g, double distM)
    {
        if (g instanceof PolygonGeometry)
        {
            return createBufferAroundPolygon((PolygonGeometry)g, distM);
        }
        else if (g instanceof PolylineGeometry)
        {
            return createBufferAroundLine((PolylineGeometry)g, distM);
        }
        else if (g instanceof PointGeometry)
        {
            return createBufferAroundPoint((PointGeometry)g, distM);
        }
        else if (g instanceof GeometryGroupGeometry)
        {
            return createBufferAroundGroup((GeometryGroupGeometry)g, distM);
        }
        return null;
    }

    /**
     * Create a buffer {@link PolygonGeometry} for the supplied geometry group.
     * The group may be composed of one or more constituent geometries, however,
     * all children of the group must be of the same type.
     *
     * @param pGroup the group for which to create the buffer.
     * @param pDistanceInMeters the distance, expressed in meters, of away from
     *            the group at which the buffer will be drawn.
     * @return a {@link PolygonGeometry} in which the buffer is expressed, or
     *         null if none could be created.
     */
    public static AbstractGeometry createBufferAroundGroup(GeometryGroupGeometry pGroup, double pDistanceInMeters)
    {
        Collection<Geometry> geometries = pGroup.getGeometries();

        if (geometries.isEmpty())
        {
            return null;
        }

        Geometry firstGeometry = geometries.iterator().next();
        com.vividsolutions.jts.geom.Geometry unifiedJtsGeometry = null;
        if (firstGeometry instanceof PolylineGeometry)
        {
            Set<PolylineGeometry> polylines = New.set(geometries.size());
            geometries.forEach(g -> polylines.add((PolylineGeometry)g));
            unifiedJtsGeometry = PolylineGeometryUtils.convertToMultiLineString(polylines);
        }
        else if (firstGeometry instanceof PointGeometry)
        {
            Set<PointGeometry> points = New.set(geometries.size());
            geometries.forEach(g -> points.add((PointGeometry)g));
            unifiedJtsGeometry = PointGeometryUtils.convertToMultiPoint(points);
        }
        else if (firstGeometry instanceof PolygonGeometry)
        {
            Set<PolygonGeometry> polygons = New.set(geometries.size());
            geometries.forEach(g -> polygons.add((PolygonGeometry)g));
            unifiedJtsGeometry = PolygonGeometryUtils.convertToMultiPolygon(polygons);
        }
        else
        {
            LOGGER.warn("Unrecognized geometry in group. No buffer created (geometry was instance of '"
                    + firstGeometry.getClass().getName() + "')");
            return null;
        }

        return createBuffer(unifiedJtsGeometry, pDistanceInMeters);
    }

    /**
     * Creates a buffer around the supplied JTS Geometry, returned as an
     * instance of {@link PolygonGeometry}. The buffer size is supplied in
     * meters, and expressed as a distance.
     *
     * @param pJtsGeometry the geometry around which the buffer will be created.
     * @param pDistanceInMeters the size of the buffer, expressed as a distance
     *            in meters.
     * @return a {@link PolygonGeometry} in which the buffer is expressed.
     */
    public static AbstractGeometry createBuffer(com.vividsolutions.jts.geom.Geometry pJtsGeometry, double pDistanceInMeters)
    {
        LatLonAlt center = JTSUtilities.convertToLatLonAlt(ReferenceLevel.ELLIPSOID, pJtsGeometry.getCoordinate()).get(0);
        LatLonAlt edge = azRadiusPt(center, 0, pDistanceInMeters);
        double latDist = Math.abs(center.getLatD() - edge.getLatD());
        if (pJtsGeometry instanceof GeometryCollection)
        {
            com.vividsolutions.jts.geom.Geometry buffer = pJtsGeometry.buffer(latDist);
            if (buffer instanceof Polygon)
            {
                Polygon bufferPolygon = JTSUtilities.cutLon180((Polygon)buffer);
                return convertToPolygonGeometry(bufferPolygon, polyProps());
            }
            MultiPolygon bufferPolygon = JTSUtilities.cutLon180((MultiPolygon)buffer);
            return convertToMultiPolygonGeometry(bufferPolygon, polyProps(), createZOrderRenderProperties());
        }

        Polygon bufferPolygon = JTSUtilities.cutLon180((Polygon)pJtsGeometry.buffer(latDist));
        return convertToPolygonGeometry(bufferPolygon, polyProps());
    }

    /**
     * Create a polygon which contains the line.
     *
     * @param geom the geom
     * @param distanceMeters the distance meters
     * @return the line string
     */
    private static PolygonGeometry createBufferAroundLine(PolylineGeometry geom, double distanceMeters)
    {
        LatLonAlt center = ((GeographicPosition)geom.getVertices().get(0)).getLatLonAlt();
        LatLonAlt edge = azRadiusPt(center, 0, distanceMeters);
        double latDist = Math.abs(center.getLatD() - edge.getLatD());
        // convert to Coordinate array
        List<? extends Position> verts = geom.getVertices();
        Coordinate[] coords = new Coordinate[verts.size()];
        int i = 0;
        for (Position p : verts)
        {
            coords[i++] = toCoord(p.asVector3d());
        }

        try
        {
            LineString ls = JTSUtilities.joinLon(JTSUtilities.GEOMETRY_FACTORY.createLineString(coords));
            Polygon poly = JTSUtilities.cutLon180((Polygon)ls.buffer(latDist));
            return convertToPolygonGeometry(poly, polyProps());
        }
        catch (RuntimeException e)
        {
            // Bail if we failed to create the line string.
            LOGGER.error("Failed to create JTS polygon" + e, e);
        }
        return null;
    }

    /**
     * Convert a Vector3d (Core) into a Coordinate (JTS).
     *
     * @param v a Vector3d
     * @return a Coordinate
     */
    private static Coordinate toCoord(Vector3d v)
    {
        return new Coordinate(v.getX(), v.getY(), v.getZ());
    }

    /**
     * Creates a circle around the point with the given radius.
     *
     * @param point the point
     * @param bufferM the distance meters
     * @return the geometry
     */
    private static PolygonGeometry createBufferAroundPoint(PointGeometry point, double bufferM)
    {
        LatLonAlt center = ((GeographicPosition)point.getPosition()).getLatLonAlt();
        LatLonAlt edge = azRadiusPt(center, 0, bufferM);
        try
        {
            Polygon poly = JTSUtilities.createCircle(center, edge, JTSUtilities.NUM_CIRCLE_SEGMENTS);
            return convertToPolygonGeometry(poly, polyProps());
        }
        catch (RuntimeException e)
        {
            // Bail if we failed to create the line string.
            LOGGER.error("Failed to create JTS polygon" + e, e);
        }
        return null;
    }

    /**
     * Create a buffer around the outside of the polygon.
     *
     * @param polyGeom the poly geom
     * @param bufferM the distance meters
     * @return the polygon geometry
     */
    private static PolygonGeometry createBufferAroundPolygon(PolygonGeometry polyGeom, double bufferM)
    {
        LatLonAlt center = ((GeographicPosition)polyGeom.getVertices().get(0)).getLatLonAlt();
        LatLonAlt edge = azRadiusPt(center, 0, bufferM);
        double latDist = Math.signum(bufferM) * Math.abs(center.getLatD() - edge.getLatD());
        try
        {
            Polygon cvtPoly = JTSUtilities.joinLon(convertToJTSPolygon(polyGeom));
            Polygon buffer = (Polygon)cvtPoly.buffer(latDist);
            if (buffer.isEmpty())
            {
                // defend against negative buffers that are too small to display
                // (JTS creates empty buffers for these):
                return null;
            }
            Polygon poly = JTSUtilities.cutLon180(buffer);
            return convertToPolygonGeometry(poly, polyProps());
        }
        catch (RuntimeException e)
        {
            // This can occur on a failure to create the polygon?
            LOGGER.error("Failed to create JTS polygon" + e, e);
        }
        return null;
    }

    /**
     * Build a polygon.
     *
     * @param vertices The vertices of the polygon.
     * @param c the c
     * @return The polygon geometry.
     */
    private static PolygonGeometry buildBox(List<GeographicPosition> vertices, Color c)
    {
        PolygonGeometry.Builder<GeographicPosition> polyBuilder = new PolygonGeometry.Builder<>();
        polyBuilder.setVertices(vertices);
        return new PolygonGeometry(polyBuilder, polyProps(c, 3, false), null);
    }

    /**
     * Find a point that is a specified distance and direction from another
     * point.
     *
     * @param p starting point
     * @param azRad azimuth angle in radians
     * @param distM distance in meters
     * @return bla
     */
    private static LatLonAlt azRadiusPt(LatLonAlt p, double azRad, double distM)
    {
        return GeographicBody3D.greatCircleEndPosition(p, azRad, WGS84EarthConstants.RADIUS_MEAN_M, distM);
    }

    /**
     * Most common case for the namesake method (q.v.).
     *
     * @return common properties
     */
    public static PolygonRenderProperties polyProps()
    {
        return polyProps(Color.WHITE, 2, true);
    }

    /**
     * Create properties with the specified color and line width.
     *
     * @param c the Color
     * @param w the line width
     * @param pick indicates whether the polygon is pickable
     * @return properties
     */
    public static PolygonRenderProperties polyProps(Color c, float w, boolean pick)
    {
        PolygonRenderProperties props = new DefaultPolygonRenderProperties(DEFAULT_ZORDER, true, pick);
        props.setColor(c);
        props.setWidth(w);
        return props;
    }

    public static ZOrderRenderProperties createZOrderRenderProperties()
    {
        ZOrderRenderProperties properties = new DefaultZOrderRenderProperties(DEFAULT_ZORDER, true);
        return properties;
    }

    /** Disallow instantiation. */
    private JTSCoreGeometryUtilities()
    {
    }
}
