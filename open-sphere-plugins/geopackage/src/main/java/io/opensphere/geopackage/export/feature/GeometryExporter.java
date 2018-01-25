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
    public mil.nga.wkb.geom.Geometry convertGeometry(MapDataElement element)
    {
        MapGeometrySupport support = element.getMapGeometrySupport();

        mil.nga.wkb.geom.Geometry wkbGeometry = null;

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
    private mil.nga.wkb.geom.Geometry convertGeometry(Geometry jtsGeometry, String mapGeometrySupportClass)
    {
        mil.nga.wkb.geom.Geometry wkbGeometry = null;

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
     * {@link mil.nga.wkb.geom.GeometryCollection}.
     *
     * @param geometryCollection The geometry to convert.
     * @param mapSupportClass Used to log warning message if geometry couldn't
     *            be created.
     * @return The converted geometry.
     */
    private mil.nga.wkb.geom.GeometryCollection<mil.nga.wkb.geom.Geometry> convertGeometryCollection(
            GeometryCollection geometryCollection, String mapSupportClass)
    {
        mil.nga.wkb.geom.GeometryCollection<mil.nga.wkb.geom.Geometry> collection = new mil.nga.wkb.geom.GeometryCollection<>(
                true, false);

        for (int i = 0; i < geometryCollection.getNumGeometries(); i++)
        {
            mil.nga.wkb.geom.Geometry geometry = convertGeometry(geometryCollection.getGeometryN(i), mapSupportClass);

            if (geometry != null)
            {
                collection.addGeometry(geometry);
            }
        }

        return collection;
    }

    /**
     * Converts the {@link LineString} to a {@link mil.nga.wkb.geom.LineString}.
     *
     * @param lineString The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.wkb.geom.LineString convertLineString(LineString lineString)
    {
        mil.nga.wkb.geom.LineString wkbLineString = new mil.nga.wkb.geom.LineString(true, false);
        for (int i = 0; i < lineString.getNumPoints(); i++)
        {
            mil.nga.wkb.geom.Point point = convertPoint(lineString.getPointN(i));
            wkbLineString.addPoint(point);
        }

        return wkbLineString;
    }

    /**
     * Converts the {@link MultiLineString} to a
     * {@link mil.nga.wkb.geom.MultiLineString}.
     *
     * @param multiLineString The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.wkb.geom.MultiLineString convertMultiLineString(MultiLineString multiLineString)
    {
        mil.nga.wkb.geom.MultiLineString wkbMultiLineString = new mil.nga.wkb.geom.MultiLineString(true, false);

        for (int i = 0; i < multiLineString.getNumGeometries(); i++)
        {
            mil.nga.wkb.geom.LineString wkbLineString = convertLineString((LineString)multiLineString.getGeometryN(i));
            wkbMultiLineString.addGeometry(wkbLineString);
        }

        return wkbMultiLineString;
    }

    /**
     * Converts the {@link MultiPoint} to a {@link mil.nga.wkb.geom.MultiPoint}.
     *
     * @param multiPoint The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.wkb.geom.MultiPoint convertMultiPoint(MultiPoint multiPoint)
    {
        mil.nga.wkb.geom.MultiPoint wkbMultiPoint = new mil.nga.wkb.geom.MultiPoint(true, false);

        for (int i = 0; i < multiPoint.getNumGeometries(); i++)
        {
            mil.nga.wkb.geom.Point wkbPoint = convertPoint((Point)multiPoint.getGeometryN(i));
            wkbMultiPoint.addGeometry(wkbPoint);
        }

        return wkbMultiPoint;
    }

    /**
     * Converts the {@link MultiPolygon} to a
     * {@link mil.nga.wkb.geom.MultiPolygon}.
     *
     * @param multiPolygon The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.wkb.geom.MultiPolygon convertMultiPolygon(MultiPolygon multiPolygon)
    {
        mil.nga.wkb.geom.MultiPolygon wkbMultiPolygon = new mil.nga.wkb.geom.MultiPolygon(true, false);

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++)
        {
            mil.nga.wkb.geom.Polygon wkbPolygon = convertPolygon((Polygon)multiPolygon.getGeometryN(i));
            wkbMultiPolygon.addGeometry(wkbPolygon);
        }

        return wkbMultiPolygon;
    }

    /**
     * Converts the {@link Point} to a {@link mil.nga.wkb.geom.Point}.
     *
     * @param point The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.wkb.geom.Point convertPoint(Point point)
    {
        mil.nga.wkb.geom.Point wkbPoint = new mil.nga.wkb.geom.Point(true, false, point.getX(), point.getY());
        wkbPoint.setZ(point.getCoordinate().z);

        return wkbPoint;
    }

    /**
     * Converts the {@link Polygon} to a {@link mil.nga.wkb.geom.Polygon}.
     *
     * @param polygon The geometry to convert.
     * @return The converted geometry.
     */
    private mil.nga.wkb.geom.Polygon convertPolygon(Polygon polygon)
    {
        mil.nga.wkb.geom.Polygon wkbPolygon = new mil.nga.wkb.geom.Polygon(true, false);

        mil.nga.wkb.geom.LineString ring = convertLineString(polygon.getExteriorRing());
        wkbPolygon.addRing(ring);

        for (int i = 0; i < polygon.getNumInteriorRing(); i++)
        {
            ring = convertLineString(polygon.getInteriorRingN(i));
            wkbPolygon.addRing(ring);
        }

        return wkbPolygon;
    }
}
