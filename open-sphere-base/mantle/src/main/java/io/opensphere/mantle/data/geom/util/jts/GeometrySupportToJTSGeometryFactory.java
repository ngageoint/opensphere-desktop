package io.opensphere.mantle.data.geom.util.jts;

import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.MapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.MapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;

/**
 * A factory for creating GeometrySupportToJTSGeomery objects.
 */
@SuppressWarnings("PMD.GodClass")
public final class GeometrySupportToJTSGeometryFactory
{
    /** The Constant ourLogger. */
    private static final Logger LOGGER = Logger.getLogger(GeometrySupportToJTSGeometryFactory.class);

    /**
     * Convert a {@link MapGeometrySupport} to a JTS {@link Geometry}.
     *
     * @param geomSupport the {@link MapGeometrySupport} to convert.
     * @return the JTS {@link Geometry}
     */
    public static Geometry convertToJTSGeometry(MapGeometrySupport geomSupport)
    {
        return convertToJTSGeometry(geomSupport, null);
    }

    /**
     * Convert a {@link MapGeometrySupport} to a JTS {@link Geometry}.
     *
     * @param geomSupport the {@link MapGeometrySupport} to convert.
     * @param gf the {@link GeometryFactory} if null one will be created
     *            internally.
     * @return the JTS {@link Geometry}
     */
    public static Geometry convertToJTSGeometry(MapGeometrySupport geomSupport, GeometryFactory gf)
    {
        Geometry result = null;
        if (geomSupport != null)
        {
            GeometryFactory geomFactory = gf == null ? new GeometryFactory() : gf;

            List<Geometry> children = null;
            if (geomSupport.hasChildren())
            {
                children = New.list();
                for (MapGeometrySupport child : geomSupport.getChildren())
                {
                    children.add(convertToJTSGeometry(child, geomFactory));
                }
            }

            if (geomSupport instanceof MapLocationGeometrySupport)
            {
                result = convertToPointGeometry((MapLocationGeometrySupport)geomSupport, geomFactory);
            }
            else if (geomSupport instanceof MapEllipseGeometrySupport)
            {
                result = convertToPointGeometry((MapEllipseGeometrySupport)geomSupport, geomFactory);
            }
            else if (geomSupport instanceof MapCircleGeometrySupport)
            {
                result = convertToPointGeometry((MapCircleGeometrySupport)geomSupport, geomFactory);
            }
            else if (geomSupport instanceof MapPolylineGeometrySupport)
            {
                result = convertToLineString((MapPolylineGeometrySupport)geomSupport, geomFactory);
            }
            else if (geomSupport instanceof MapPolygonGeometrySupport)
            {
                result = convertToPolygon((MapPolygonGeometrySupport)geomSupport, geomFactory);
            }
            else
            {
                LOGGER.error("Encountered unknown MapGeometrySupport type " + geomSupport.getClass().getName()
                        + " unable to convert to JTS Geometry");
            }
            if (result != null && children != null)
            {
                result = convertToMultiGeometryType(result, geomFactory, children);
            }
        }

        return result;
    }

    /**
     * Are all children line strings.
     *
     * @param children the children
     * @return true, if successful
     */
    private static boolean areAllChildrenLineStrings(List<Geometry> children)
    {
        boolean allLineStrings = true;
        if (children != null && !children.isEmpty())
        {
            for (Geometry g : children)
            {
                if (!(g instanceof LineString))
                {
                    allLineStrings = false;
                    break;
                }
            }
        }
        return allLineStrings;
    }

    /**
     * Are all children points.
     *
     * @param children the children
     * @return true, if successful
     */
    private static boolean areAllChildrenPoints(List<Geometry> children)
    {
        boolean allPoints = true;
        if (children != null && !children.isEmpty())
        {
            for (Geometry g : children)
            {
                if (!(g instanceof Point))
                {
                    allPoints = false;
                    break;
                }
            }
        }
        return allPoints;
    }

    /**
     * Are all children polygons.
     *
     * @param children the children
     * @return true, if successful
     */
    private static boolean areAllChildrenPolygons(List<Geometry> children)
    {
        boolean allPolygons = true;
        if (children != null && !children.isEmpty())
        {
            for (Geometry g : children)
            {
                if (!(g instanceof Polygon))
                {
                    allPolygons = false;
                    break;
                }
            }
        }
        return allPolygons;
    }

    /**
     * Convert to coordinate array.
     *
     * @param locations the locations
     * @param closeIfOpen the close if open
     * @return the coordinate[]
     */
    private static Coordinate[] convertToCoordinateArray(List<LatLonAlt> locations, boolean closeIfOpen)
    {
        boolean isClosed = locations.size() >= 2 && locations.get(0).equals(locations.get(locations.size() - 1));
        boolean closeIt = closeIfOpen && !isClosed;
        Coordinate[] locs = new Coordinate[locations.size() + (closeIt ? 1 : 0)];
        int index = 0;
        for (LatLonAlt lla : locations)
        {
            locs[index] = new Coordinate(lla.getLonD(), lla.getLatD(), lla.getAltM());
            index++;
        }
        if (closeIt)
        {
            locs[index] = new Coordinate(locations.get(0).getLonD(), locations.get(0).getLatD(), locations.get(0).getAltM());
        }
        return locs;
    }

    /**
     * Converts a {@link MapPolylineGeometrySupport} to a JTS {@link LineString}
     * .
     *
     * @param mpgs the mpgs
     * @param gf the {@link GeometryFactory}
     * @return the line string
     */
    private static LineString convertToLineString(MapPolylineGeometrySupport mpgs, GeometryFactory gf)
    {
        return gf.createLineString(convertToCoordinateArray(mpgs.getLocations(), false));
    }

    /**
     * Converts a {@link Geometry} and a set of its children to a Multi Geometry
     * type..
     *
     * @param pGeom the {@link Geometry}
     * @param factory the {@link GeometryFactory}
     * @param children the {@link List} of {@link Geometry} that are children to
     *            this geom.
     * @return the resultant {@link Geometry}
     */
    private static Geometry convertToMultiGeometryType(Geometry pGeom, GeometryFactory factory, List<Geometry> children)
    {
        Geometry geom = pGeom;
        Geometry current = geom;
        if (areAllChildrenPoints(children))
        {
            if (geom instanceof Point)
            {
                Point[] points = new Point[children.size() + 1];
                points[0] = (Point)current;
                int counter = 1;
                for (Geometry child : children)
                {
                    points[counter] = (Point)child;
                    counter++;
                }
                geom = factory.createMultiPoint(points);
            }
            else
            {
                geom = factory.createGeometryCollection(createGeometryArray(children, current));
            }
        }
        else if (areAllChildrenLineStrings(children))
        {
            if (geom instanceof LineString)
            {
                LineString[] lineStrings = new LineString[children.size() + 1];
                lineStrings[0] = (LineString)current;
                int counter = 1;
                for (Geometry child : children)
                {
                    lineStrings[counter] = (LineString)child;
                    counter++;
                }
                geom = factory.createMultiLineString(lineStrings);
            }
            else
            {
                geom = factory.createGeometryCollection(createGeometryArray(children, current));
            }
        }
        else if (areAllChildrenPolygons(children))
        {
            if (geom instanceof Polygon)
            {
                Polygon[] polygons = new Polygon[children.size() + 1];
                polygons[0] = (Polygon)current;
                int counter = 1;
                for (Geometry child : children)
                {
                    polygons[counter] = (Polygon)child;
                    counter++;
                }
                geom = factory.createMultiPolygon(polygons);
            }
            else
            {
                geom = factory.createGeometryCollection(createGeometryArray(children, current));
            }
        }
        else
        {
            geom = factory.createGeometryCollection(createGeometryArray(children, current));
        }
        return geom;
    }

    /**
     * Converts a {@link MapCircleGeometrySupport} to a JTS {@link Point}.
     *
     * @param megs the megs
     * @param gf the {@link GeometryFactory}
     * @return the point
     */
    private static Point convertToPointGeometry(MapCircleGeometrySupport megs, GeometryFactory gf)
    {
        return gf.createPoint(
                new Coordinate(megs.getLocation().getLonD(), megs.getLocation().getLatD(), megs.getLocation().getAltM()));
    }

    /**
     * Converts a {@link MapEllipseGeometrySupport} to a JTS {@link Point}.
     *
     * @param megs the megs
     * @param gf the {@link GeometryFactory}
     * @return the point
     */
    private static Point convertToPointGeometry(MapEllipseGeometrySupport megs, GeometryFactory gf)
    {
        return gf.createPoint(
                new Coordinate(megs.getLocation().getLonD(), megs.getLocation().getLatD(), megs.getLocation().getAltM()));
    }

    /**
     * Converts a {@link MapLocationGeometrySupport} to JTS {@link Point}.
     *
     * @param mlgs the {@link MapLocationGeometrySupport}
     * @param gf the {@link GeometryFactory}
     * @return the point
     */
    private static Point convertToPointGeometry(MapLocationGeometrySupport mlgs, GeometryFactory gf)
    {
        return gf.createPoint(
                new Coordinate(mlgs.getLocation().getLonD(), mlgs.getLocation().getLatD(), mlgs.getLocation().getAltM()));
    }

    /**
     * Converts a {@link MapPolygonGeometrySupport} to a JTS {@link Polygon}.
     *
     * @param mpgs the mpgs
     * @param gf the {@link GeometryFactory}
     * @return the polygon
     */
    private static Polygon convertToPolygon(MapPolygonGeometrySupport mpgs, GeometryFactory gf)
    {
        LinearRing outerRing = gf.createLinearRing(convertToCoordinateArray(mpgs.getLocations(), true));
        LinearRing[] innerRings = new LinearRing[mpgs.getHoles().size()];

        int index = 0;
        for (List<? extends LatLonAlt> hole : mpgs.getHoles())
        {
            LinearRing innerRing = gf.createLinearRing(convertToCoordinateArray(New.list(hole), true));
            innerRings[index] = innerRing;
            index++;
        }

        return gf.createPolygon(outerRing, innerRings);
    }

    /**
     * Creates a new GeometrySupportToJTSGeometry object.
     *
     * @param children the children
     * @param current the current
     * @return the geometry[]
     */
    private static Geometry[] createGeometryArray(List<Geometry> children, Geometry current)
    {
        Geometry[] geoms = new Geometry[children.size() + 1];
        geoms[0] = current;
        int counter = 1;
        for (Geometry child : children)
        {
            geoms[counter] = child;
            counter++;
        }
        return geoms;
    }

    /**
     * Instantiates a new geometry support to jts geomery factory.
     */
    private GeometrySupportToJTSGeometryFactory()
    {
        // Don't allow instantiation.
    }
}
