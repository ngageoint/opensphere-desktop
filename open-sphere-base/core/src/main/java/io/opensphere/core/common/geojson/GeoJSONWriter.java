package io.opensphere.core.common.geojson;

import java.io.IOException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This class provides utility methods for formatting GeoJSON output.
 */
public class GeoJSONWriter extends GeoJSON
{
    /**
     * Creates a geometry GeoJSON string from the given {@link Geometry} and
     * appends it in the given <code>Appendable</code>.
     *
     * @param geometry the geometry from which GeoJSON will be generated.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendGeometry(Geometry geometry, Appendable appender) throws IOException
    {
        if (geometry instanceof Point)
        {
            appendPoint((Point)geometry, appender);
        }
        else if (geometry instanceof MultiPoint)
        {
            appendMultiPoint((MultiPoint)geometry, appender);
        }
        else if (geometry instanceof LineString)
        {
            appendLineString((LineString)geometry, appender);
        }
        else if (geometry instanceof MultiLineString)
        {
            appendMultiLineString((MultiLineString)geometry, appender);
        }
        else if (geometry instanceof Polygon)
        {
            appendPolygon((Polygon)geometry, appender);
        }
        else if (geometry instanceof MultiPolygon)
        {
            appendMultiPolygon((MultiPolygon)geometry, appender);
        }
        else if (geometry instanceof GeometryCollection)
        {
            appendGeometryCollection((GeometryCollection)geometry, appender);
        }
        else if (geometry == null)
        {
            appender.append("null");
        }
    }

    /**
     * Appends a Point GeoJSON string.
     *
     * @param point the {@link Point} instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendPoint(Point point, Appendable appender) throws IOException
    {
        appendPoint(point.getCoordinate(), appender);
    }

    /**
     * Appends a Point GeoJSON string.
     *
     * @param point the {@link Point} instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendPoint(Coordinate point, Appendable appender) throws IOException
    {
        appender.append("{ \"type\":\"" + Type.Point + "\",\"coordinates\": ");
        writeCoordinate(point, appender);
        appender.append(" }");
    }

    /**
     * Appends a MultiPoint GeoJSON string.
     *
     * @param multiPoint the {@link MultiPoint} instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendMultiPoint(MultiPoint multiPoint, Appendable appender) throws IOException
    {
        appender.append("{ \"type\":\"" + Type.MultiPoint + "\", \"coordinates\": ");
        writeCoordinates(multiPoint.getCoordinates(), appender);
        appender.append(" }");
    }

    /**
     * Appends a LineString GeoJSON string.
     *
     * @param lineString the {@link LineString} instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendLineString(LineString lineString, Appendable appender) throws IOException
    {
        appender.append("{ \"type\":\"" + Type.LineString + "\", \"coordinates\": ");
        writeCoordinates(lineString.getCoordinates(), appender);
        appender.append(" }");
    }

    /**
     * Appends a MultiLineString GeoJSON string.
     *
     * @param multiLine the {@link MultiLineString} instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendMultiLineString(MultiLineString multiLine, Appendable appender) throws IOException
    {
        appender.append("{ \"type\":\"" + Type.MultiLineString + "\", \"coordinates\":[");
        for (int i = 0; i < multiLine.getNumGeometries(); i++)
        {
            LineString lineString = (LineString)multiLine.getGeometryN(i);
            if (i != 0)
            {
                appender.append(",");
            }
            writeCoordinates(lineString.getCoordinates(), appender);

        }

        appender.append(" ] }");
    }

    /**
     * Appends a Polygon GeoJSON string.
     *
     * @param polygon the {@link Polygon} instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendPolygon(Polygon polygon, Appendable appender) throws IOException
    {
        appender.append("{ \"type\":\"" + Type.Polygon + "\", \"coordinates\": ");
        writePolygonCoordinates(polygon, appender);
        appender.append(" }");
    }

    /**
     * Appends a MultiPolygon GeoJSON string.
     *
     * @param multiPolygon the {@link MultiPolygon}instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendMultiPolygon(MultiPolygon multiPolygon, Appendable appender) throws IOException
    {
        appender.append("{ \"type\":\"" + Type.MultiPolygon + "\", \"coordinates\": [ ");
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++)
        {
            Polygon polygon = (Polygon)multiPolygon.getGeometryN(i);
            if (i != 0)
            {
                appender.append(",");
            }
            writePolygonCoordinates(polygon, appender);
        }
        appender.append(" ] }");
    }

    /**
     * Appends a GeometryCollection GeoJSON string.
     *
     * @param collection the {@link GeometryCollection} instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendGeometryCollection(GeometryCollection collection, Appendable appender) throws IOException
    {
        appender.append("{ \"type\":\"" + Type.GeometryCollection + "\", \"geometries\": [ ");
        for (int i = 0; i < collection.getNumGeometries(); i++)
        {
            Geometry geometry = collection.getGeometryN(i);
            if (i != 0)
            {
                appender.append(",");
            }
            appendGeometry(geometry, appender);
        }
        appender.append(" ] }");
    }

    /**
     * Appends a <code>crs</code> GeoJSON string. The output starts with
     * <code>"crs":</code> and what follows is according to section 3 of the
     * GeoJSON specification.
     *
     * @param geometry the {@link Geometry} instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendCrsMember(Geometry geometry, Appendable appender) throws IOException
    {
        appender.append("\"crs\":");
        if (geometry.getSRID() == 0)
        {
            appender.append("null");
        }
        else
        {
            appender.append("{\"type\":\"EPSG\",\"properties\":{\"code\":\"");
            appender.append(Integer.toString(geometry.getSRID()));
            appender.append("\"}}");
        }
    }

    /**
     * Appends a <code>bbox</code> GeoJSON string. The output starts with
     * <code>"bbox":</code> and what follows is according to section 4 of the
     * GeoJSON specification.
     *
     * @param point the {@link Point} instance.
     * @param appender the output for the GeoJSON.
     * @throws IOException if an exception occurs writing the output.
     */
    public static void appendBboxMember(Envelope envelope, Appendable appender) throws IOException
    {
        appender.append("\"bbox\":");
        appender.append('[');
        appender.append(Double.toString(envelope.getMinX()));
        appender.append(", ").append(Double.toString(envelope.getMinY()));
        appender.append(", ").append(Double.toString(envelope.getMaxX()));
        appender.append(", ").append(Double.toString(envelope.getMaxY()));
        appender.append(']');
    }

    /**
     * Generates an array of coordinates for the outer and each of the inner
     * rings.
     *
     * @param polygon the {@link Polygon} instance.
     * @param appender the output for the JSON.
     * @throws IOException if an exception occurs writing the output.
     */
    protected static void writePolygonCoordinates(Polygon polygon, Appendable appender) throws IOException
    {
        appender.append("[ ");

        // Add an array for the outer ring.
        writeCoordinates(polygon.getExteriorRing().getCoordinates(), appender);

        // Add an array for each of the inner rings.
        for (int ii = 0; ii < polygon.getNumInteriorRing(); ii++)
        {
            appender.append(", ");
            writeCoordinates(polygon.getInteriorRingN(ii).getCoordinates(), appender);
        }
        appender.append(" ]");
    }

    /**
     * Generates an array of coordinates
     *
     * @param coordinates the coordinate array.
     * @param appender the output for the JSON.
     * @throws IOException if an exception occurs writing the output.
     */
    protected static void writeCoordinates(Coordinate[] coordinates, Appendable appender) throws IOException
    {
        appender.append("[ ");
        for (int ii = 0; ii < coordinates.length; ii++)
        {
            if (ii > 0)
            {
                appender.append(", ");
            }

            Coordinate coordinate = coordinates[ii];
            writeCoordinate(coordinate, appender);
        }
        appender.append(" ]");
    }

    /**
     * Generates an array for the given coordinate.
     *
     * @param coordinate the coordinate.
     * @param appender the output for the JSON.
     * @throws IOException if an exception occurs writing the output.
     */
    protected static void writeCoordinate(Coordinate coordinate, Appendable appender) throws IOException
    {
        appender.append('[').append(Double.toString(coordinate.x));
        appender.append(", ").append(Double.toString(coordinate.y)).append(']');
    }
}
