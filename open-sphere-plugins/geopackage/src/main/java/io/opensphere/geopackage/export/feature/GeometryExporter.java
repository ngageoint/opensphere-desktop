package io.opensphere.geopackage.export.feature;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.util.jts.GeometrySupportToJTSGeometryFactory;

/**
 * Translates {@link MapDataElement} to {@link Geometry} so they can be exported
 * to a geopackage file.
 */
public class GeometryExporter
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(GeometryExporter.class);

    /**
     * Converts the element's geometry to a geometry the geopackage api can
     * export.
     *
     * @param element The element containing the geometry to convert.
     * @return The geometry to export, or null if we could not convert it.
     */
    public mil.nga.sf.Geometry convertGeometry(MapDataElement element)
    {
        MapGeometrySupport support = element.getMapGeometrySupport();

        mil.nga.sf.Geometry wkbGeometry = null;

        if (support != null)
        {
            Geometry jtsGeometry = GeometrySupportToJTSGeometryFactory.convertToJTSGeometry(support);

            wkbGeometry = convertGeometry(jtsGeometry, support.getClass().toString());
        }

        return wkbGeometry;
    }

    /**
     * Converts geometry to a geometry the geopackage api can export.
     *
     * @param jtsGeometry The geometry to convert.
     * @param mapGeometrySupportClass Used to log warning message if geometry
     *            couldn't be created.
     * @return The geometry to export, or null if we could not convert it.
     */
    private mil.nga.sf.Geometry convertGeometry(Geometry jtsGeometry, String mapGeometrySupportClass)
    {
        mil.nga.sf.Geometry wkbGeometry = null;

        if (jtsGeometry instanceof MultiLineString)
        {
            wkbGeometry = convertMultiLineString((MultiLineString)jtsGeometry);
        }
        else if (jtsGeometry instanceof MultiPoint)
        {
            wkbGeometry = convertMultiPoint((MultiPoint)jtsGeometry);
        }
        else if (jtsGeometry instanceof MultiPolygon)
        {
            wkbGeometry = convertMultiPolygon((MultiPolygon)jtsGeometry);
        }
        else if (jtsGeometry instanceof GeometryCollection)
        {
            wkbGeometry = convertGeometryCollection((GeometryCollection)jtsGeometry, mapGeometrySupportClass);
        }
        else if (jtsGeometry instanceof LineString)
        {
            wkbGeometry = convertLineString((LineString)jtsGeometry);
        }
        else if (jtsGeometry instanceof Point)
        {
            wkbGeometry = convertPoint((Point)jtsGeometry);
        }
        else if (jtsGeometry instanceof Polygon)
        {
            wkbGeometry = convertPolygon((Polygon)jtsGeometry);
        }
        else if (jtsGeometry != null)
        {
            LOGGER.warn("Unknown geometry type of " + jtsGeometry.getClass());
        }
        else
        {
            LOGGER.warn("Unknown geometry support type of " + mapGeometrySupportClass);
        }

        return wkbGeometry;
    }

    /**
     * Converts the {@link GeometryCollection} to a
     * {@link mil.nga.sf.GeometryCollection}.
     *
     * @param geometryCollection The geometry to convert.
     * @param mapSupportClass Used to log warning message if geometry couldn't
     *            be created.
     * @return The converted geometry.
     */
    private mil.nga.sf.GeometryCollection<mil.nga.sf.Geometry> convertGeometryCollection(
            GeometryCollection geometryCollection, String mapSupportClass)
    {
        mil.nga.sf.GeometryCollection<mil.nga.sf.Geometry> collection = new mil.nga.sf.GeometryCollection<>(
                true, false);

        for (int i = 0; i < geometryCollection.getNumGeometries(); i++)
        {
            mil.nga.sf.Geometry geometry = convertGeometry(geometryCollection.getGeometryN(i), mapSupportClass);

            if (geometry != null)
            {
                collection.addGeometry(geometry);
            }
        }

        return collection;
    }

    /**
     * Converts the {@link LineString} to a {@link mil.nga.sf.LineString}.
     *
     * @param lineString The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.sf.LineString convertLineString(LineString lineString)
    {
        mil.nga.sf.LineString wkbLineString = new mil.nga.sf.LineString(true, false);
        for (int i = 0; i < lineString.getNumPoints(); i++)
        {
            mil.nga.sf.Point point = convertPoint(lineString.getPointN(i));
            wkbLineString.addPoint(point);
        }

        return wkbLineString;
    }

    /**
     * Converts the {@link MultiLineString} to a
     * {@link mil.nga.sf.MultiLineString}.
     *
     * @param multiLineString The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.sf.MultiLineString convertMultiLineString(MultiLineString multiLineString)
    {
        mil.nga.sf.MultiLineString wkbMultiLineString = new mil.nga.sf.MultiLineString(true, false);

        for (int i = 0; i < multiLineString.getNumGeometries(); i++)
        {
            mil.nga.sf.LineString wkbLineString = convertLineString((LineString)multiLineString.getGeometryN(i));
            wkbMultiLineString.addGeometry(wkbLineString);
        }

        return wkbMultiLineString;
    }

    /**
     * Converts the {@link MultiPoint} to a {@link mil.nga.sf.MultiPoint}.
     *
     * @param multiPoint The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.sf.MultiPoint convertMultiPoint(MultiPoint multiPoint)
    {
        mil.nga.sf.MultiPoint wkbMultiPoint = new mil.nga.sf.MultiPoint(true, false);

        for (int i = 0; i < multiPoint.getNumGeometries(); i++)
        {
            mil.nga.sf.Point wkbPoint = convertPoint((Point)multiPoint.getGeometryN(i));
            wkbMultiPoint.addGeometry(wkbPoint);
        }

        return wkbMultiPoint;
    }

    /**
     * Converts the {@link MultiPolygon} to a
     * {@link mil.nga.sf.MultiPolygon}.
     *
     * @param multiPolygon The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.sf.MultiPolygon convertMultiPolygon(MultiPolygon multiPolygon)
    {
        mil.nga.sf.MultiPolygon wkbMultiPolygon = new mil.nga.sf.MultiPolygon(true, false);

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++)
        {
            mil.nga.sf.Polygon wkbPolygon = convertPolygon((Polygon)multiPolygon.getGeometryN(i));
            wkbMultiPolygon.addGeometry(wkbPolygon);
        }

        return wkbMultiPolygon;
    }

    /**
     * Converts the {@link Point} to a {@link mil.nga.sf.Point}.
     *
     * @param point The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.sf.Point convertPoint(Point point)
    {
        mil.nga.sf.Point wkbPoint = new mil.nga.sf.Point(true, false, point.getX(), point.getY());
        wkbPoint.setZ(Double.valueOf(point.getCoordinate().z));

        return wkbPoint;
    }

    /**
     * Converts the {@link Polygon} to a {@link mil.nga.sf.Polygon}.
     *
     * @param polygon The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.sf.Polygon convertPolygon(Polygon polygon)
    {
        mil.nga.sf.Polygon wkbPolygon = new mil.nga.sf.Polygon(true, false);

        mil.nga.sf.LineString ring = convertLineString(polygon.getExteriorRing());
        wkbPolygon.addRing(ring);

        for (int i = 0; i < polygon.getNumInteriorRing(); i++)
        {
            ring = convertLineString(polygon.getInteriorRingN(i));
            wkbPolygon.addRing(ring);
        }

        return wkbPolygon;
    }
}
