package io.opensphere.geopackage.importer.feature;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import io.opensphere.core.util.MathUtil;
import io.opensphere.geopackage.model.GeoPackageColumns;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.wkb.geom.CompoundCurve;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryCollection;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.MultiLineString;
import mil.nga.wkb.geom.MultiPoint;
import mil.nga.wkb.geom.MultiPolygon;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;
import mil.nga.wkb.geom.PolyhedralSurface;

/**
 * Imports the geometry from the GeoPackage row and converts it to a
 * {@link com.vividsolutions.jts.geom.Geometry}.
 */
public class GeometryImporter
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(GeometryImporter.class);

    /**
     * Used by the created jts geometries.
     */
    private final GeometryFactory myGeometryFactory = new GeometryFactory();

    /**
     * Imports the geometry from the specified GeoPackage row.
     *
     * @param importedRow The row to contain the imported
     *            {@link com.vividsolutions.jts.geom.Geometry}.
     * @param rowToImport The GeoPackage row to import.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     */
    public void importGeometry(Map<String, Serializable> importedRow, FeatureRow rowToImport, ProjectionTransform toGeodetic)
    {
        GeoPackageGeometryData geometryData = rowToImport.getGeometry();

        if (geometryData != null)
        {
            Geometry geometry = geometryData.getGeometry();

            com.vividsolutions.jts.geom.Geometry jtsGeom = convertGeometry(geometry, toGeodetic);

            if (jtsGeom != null)
            {
                importedRow.put(GeoPackageColumns.GEOMETRY_COLUMN, jtsGeom);
            }
        }
    }

    /**
     * Converts from a {@link CompoundCurve} to a similar
     * {@link com.vividsolutions.jts.geom.Geometry}.
     *
     * @param geometry The geometry to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted geometry.
     */
    private com.vividsolutions.jts.geom.Geometry convertCompoundCurve(CompoundCurve geometry, ProjectionTransform toGeodetic)
    {
        return toMultiLineString(geometry.getLineStrings(), toGeodetic);
    }

    /**
     * Converts a {@link Geometry} to a jts geometry.
     *
     * @param geometry The geometry to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted geometry.
     */
    @SuppressWarnings("unchecked")
    private com.vividsolutions.jts.geom.Geometry convertGeometry(Geometry geometry, ProjectionTransform toGeodetic)
    {
        com.vividsolutions.jts.geom.Geometry jtsGeom = null;
        if (geometry instanceof CompoundCurve)
        {
            jtsGeom = convertCompoundCurve((CompoundCurve)geometry, toGeodetic);
        }
        else if (geometry instanceof LineString)
        {
            jtsGeom = convertLineString((LineString)geometry, toGeodetic);
        }
        else if (geometry instanceof MultiLineString)
        {
            jtsGeom = convertMultiLineString((MultiLineString)geometry, toGeodetic);
        }
        else if (geometry instanceof MultiPoint)
        {
            jtsGeom = convertMultiPoint((MultiPoint)geometry, toGeodetic);
        }
        else if (geometry instanceof MultiPolygon)
        {
            jtsGeom = convertMultiPolygon((MultiPolygon)geometry, toGeodetic);
        }
        else if (geometry instanceof Point)
        {
            jtsGeom = convertPoint((Point)geometry, toGeodetic);
        }
        else if (geometry instanceof Polygon)
        {
            jtsGeom = convertPolygon((Polygon)geometry, toGeodetic);
        }
        else if (geometry instanceof PolyhedralSurface)
        {
            jtsGeom = convertPolyhedralSurface((PolyhedralSurface)geometry, toGeodetic);
        }
        else if (geometry instanceof GeometryCollection)
        {
            jtsGeom = convertGeometryCollection((GeometryCollection<Geometry>)geometry, toGeodetic);
        }
        else
        {
            LOGGER.warn("Unknown geometry type of " + geometry.getClass());
        }

        return jtsGeom;
    }

    /**
     * Converts a {@link GeometryCollection} to a jts geometry collection.
     *
     * @param collection The collection to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted collection.
     */
    private com.vividsolutions.jts.geom.Geometry convertGeometryCollection(GeometryCollection<Geometry> collection,
            ProjectionTransform toGeodetic)
    {
        com.vividsolutions.jts.geom.Geometry[] geometries = new com.vividsolutions.jts.geom.Geometry[collection.getGeometries()
                .size()];
        int index = 0;
        for (Geometry geometry : collection.getGeometries())
        {
            com.vividsolutions.jts.geom.Geometry jtsGeom = convertGeometry(geometry, toGeodetic);
            geometries[index] = jtsGeom;
            index++;
        }

        return myGeometryFactory.createGeometryCollection(geometries);
    }

    /**
     * Converts from a {@link LineString} to a similar
     * {@link com.vividsolutions.jts.geom.Geometry}.
     *
     * @param geometry The geometry to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted geometry.
     */
    private com.vividsolutions.jts.geom.LineString convertLineString(LineString geometry, ProjectionTransform toGeodetic)
    {
        Coordinate[] coords = new Coordinate[geometry.getPoints().size()];
        int index = 0;
        for (Point point : geometry.getPoints())
        {
            Coordinate jtsPoint = toCoordinate(point, toGeodetic);
            coords[index] = jtsPoint;
            index++;
        }
        CoordinateSequence sequence = new CoordinateArraySequence(coords);
        return new com.vividsolutions.jts.geom.LineString(sequence, myGeometryFactory);
    }

    /**
     * Converts from a {@link MultiLineString} to a similar
     * {@link com.vividsolutions.jts.geom.Geometry}.
     *
     * @param geometry The geometry to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted geometry.
     */
    private com.vividsolutions.jts.geom.Geometry convertMultiLineString(MultiLineString geometry, ProjectionTransform toGeodetic)
    {
        return toMultiLineString(geometry.getLineStrings(), toGeodetic);
    }

    /**
     * Converts from a {@link MultiPoint} to a similar
     * {@link com.vividsolutions.jts.geom.Geometry}.
     *
     * @param geometry The geometry to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted geometry.
     */
    private com.vividsolutions.jts.geom.Geometry convertMultiPoint(MultiPoint geometry, ProjectionTransform toGeodetic)
    {
        com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[geometry.getPoints().size()];

        int index = 0;
        for (Point point : geometry.getPoints())
        {
            com.vividsolutions.jts.geom.Point jtsPoint = convertPoint(point, toGeodetic);
            points[index] = jtsPoint;
            index++;
        }

        return new com.vividsolutions.jts.geom.MultiPoint(points, myGeometryFactory);
    }

    /**
     * Converts from a {@link MultiPolygon} to a similar
     * {@link com.vividsolutions.jts.geom.Geometry}.
     *
     * @param geometry The geometry to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted geometry.
     */
    private com.vividsolutions.jts.geom.Geometry convertMultiPolygon(MultiPolygon geometry, ProjectionTransform toGeodetic)
    {
        return toMultiPolygon(geometry.getPolygons(), toGeodetic);
    }

    /**
     * Converts from a {@link Point} to a similar
     * {@link com.vividsolutions.jts.geom.Geometry}.
     *
     * @param point The point to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted geometry.
     */
    private com.vividsolutions.jts.geom.Point convertPoint(Point point, ProjectionTransform toGeodetic)
    {
        Coordinate coord = toCoordinate(point, toGeodetic);
        return myGeometryFactory.createPoint(coord);
    }

    /**
     * Converts from a {@link Polygon} to a similar
     * {@link com.vividsolutions.jts.geom.Geometry}.
     *
     * @param geometry The geometry to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted geometry.
     */
    private com.vividsolutions.jts.geom.Polygon convertPolygon(Polygon geometry, ProjectionTransform toGeodetic)
    {
        LinearRing shell = null;
        LinearRing[] holes = null;

        int holeIndex = 0;
        for (LineString line : geometry.getRings())
        {
            Coordinate[] coords = new Coordinate[line.getPoints().size()];
            int pointIndex = 0;
            for (Point point : line.getPoints())
            {
                Coordinate coord = toCoordinate(point, toGeodetic);
                coords[pointIndex] = coord;
                pointIndex++;
            }

            LinearRing ring = myGeometryFactory.createLinearRing(coords);

            if (shell == null)
            {
                shell = ring;
            }
            else
            {
                if (holes == null)
                {
                    holes = new LinearRing[geometry.getRings().size() - 1];
                }

                holes[holeIndex] = ring;

                holeIndex++;
            }
        }

        return myGeometryFactory.createPolygon(shell, holes);
    }

    /**
     * Converts from a {@link PolyhedralSurface} to a similar
     * {@link com.vividsolutions.jts.geom.Geometry}.
     *
     * @param geometry The geometry to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted geometry.
     */
    private com.vividsolutions.jts.geom.Geometry convertPolyhedralSurface(PolyhedralSurface geometry,
            ProjectionTransform toGeodetic)
    {
        return toMultiPolygon(geometry.getPolygons(), toGeodetic);
    }

    /**
     * Converts the wkb point to a jts {@link Coordinate}.
     *
     * @param point The point to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The coordinate.
     */
    private Coordinate toCoordinate(Point point, ProjectionTransform toGeodetic)
    {
        double z = 0;
        if (point.hasZ())
        {
            z = point.getZ().doubleValue();
        }

        Point transformed = toGeodetic.transform(point);
        double roundedX = MathUtil.roundDecimalPlace(transformed.getX(), 10);
        double roundedY = MathUtil.roundDecimalPlace(transformed.getY(), 10);

        return new Coordinate(roundedX, roundedY, toGeodetic.getFromProjection().toMeters(z));
    }

    /**
     * Converts the list of {@link LineString} to a jts
     * {@link com.vividsolutions.jts.geom.MultiLineString}.
     *
     * @param lineStrings The line strings to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted multi line string.
     */
    private com.vividsolutions.jts.geom.MultiLineString toMultiLineString(List<LineString> lineStrings,
            ProjectionTransform toGeodetic)
    {
        com.vividsolutions.jts.geom.LineString[] jtsLineStrings = new com.vividsolutions.jts.geom.LineString[lineStrings.size()];
        int index = 0;
        for (LineString lineString : lineStrings)
        {
            com.vividsolutions.jts.geom.LineString jtsLineString = convertLineString(lineString, toGeodetic);
            jtsLineStrings[index] = jtsLineString;
            index++;
        }

        return new com.vividsolutions.jts.geom.MultiLineString(jtsLineStrings, myGeometryFactory);
    }

    /**
     * Converts a list of polygons to a
     * {@link com.vividsolutions.jts.geom.MultiPolygon}.
     *
     * @param polygons The polygons to convert.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The converted multi polygon.
     */
    private com.vividsolutions.jts.geom.MultiPolygon toMultiPolygon(List<Polygon> polygons, ProjectionTransform toGeodetic)
    {
        com.vividsolutions.jts.geom.Polygon[] jtsPolygons = new com.vividsolutions.jts.geom.Polygon[polygons.size()];
        int index = 0;
        for (Polygon polygon : polygons)
        {
            com.vividsolutions.jts.geom.Polygon jtsPolygon = convertPolygon(polygon, toGeodetic);
            jtsPolygons[index] = jtsPolygon;
            index++;
        }

        return myGeometryFactory.createMultiPolygon(jtsPolygons);
    }
}
