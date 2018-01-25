package io.opensphere.core.common.geospatial;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * This utility class provides useful methods for working with JTS geometries.
 */
public class JTSUtils
{

    /**
     * Default geometry factory
     */
    private static final GeometryFactory DEFAULT_GEOMETRY_FACTORY = new GeometryFactory(
            new PrecisionModel(PrecisionModel.FLOATING), SRID.EPSG4326.getCrsCode());

    /**
     * Creates the default <code>GeometryFactory</code>.
     *
     * @return the default <code>GeometryFactory</code>.
     */
    public static GeometryFactory createDefaultGeometryFactory()
    {
        return DEFAULT_GEOMETRY_FACTORY;
    }

    /**
     * Converts the given array of longitude/latitude pairs to an array of JTS
     * <code>Coordinate</code>s. The structure of the array must be as follows:
     *
     * <pre>
     * lonlatArray[0] = longitude1
     * lonlatArray[1] = latitude1
     * lonlatArray[2] = longitude2
     * lonlatArray[3] = latitude2
     * ...
     * </pre>
     *
     * @param lonLatArray the array of longitude/latitude pairs.
     * @return the <code>Coordinate</code> array.
     */
    public static Coordinate[] toCoordinateArray(final double[] lonLatArray)
    {
        return toCoordinateArray(lonLatArray, false);
    }

    /**
     * Converts the given array of longitude/latitude pairs to an array of JTS
     * <code>Coordinate</code>s. The structure of the array must be as follows:
     *
     * <pre>
     * lonlatArray[0] = longitude1
     * lonlatArray[1] = latitude1
     * lonlatArray[2] = longitude2
     * lonlatArray[3] = latitude2
     * ...
     * </pre>
     *
     * @param lonLatArray the array of longitude/latitude pairs.
     * @param closeCoordinates indicates if the coordinates should be closed in
     *            the resulting <code>Coordinate</code> array.
     * @return the <code>Coordinate</code> array.
     */
    public static Coordinate[] toCoordinateArray(final double[] lonLatArray, boolean closeCoordinates)
    {
        if (closeCoordinates)
        {
            final int lastIndex = lonLatArray.length - 2;

            // If the first and last points equal, don't do anything more to
            // close
            // the array.
            if (lonLatArray.length >= 2 && lonLatArray[0] == lonLatArray[lastIndex]
                    && lonLatArray[1] == lonLatArray[lastIndex + 1])
            {
                closeCoordinates = false;
            }
            if (lonLatArray.length < 2)
            {
                closeCoordinates = false;
            }
        }

        // Convert the lonLatArray to an array of Coordinates.
        final int arrayLength = lonLatArray.length / 2 + (closeCoordinates ? 1 : 0);
        final Coordinate[] coordinates = new Coordinate[arrayLength];
        for (int ii = 0; ii < lonLatArray.length - 1; ii += 2)
        {
            coordinates[ii / 2] = new Coordinate(lonLatArray[ii], lonLatArray[ii + 1]);
        }

        // Close the array.
        if (closeCoordinates && coordinates.length >= 1)
        {
            coordinates[coordinates.length - 1] = (Coordinate)coordinates[0].clone();
        }
        return coordinates;
    }

    /**
     * Closes the coordinate array such that the first and last coordinates are
     * equal. If the coordinates are already equal, nothing is changed.
     *
     * @param coordinates the array of <code>Coordinate</code>s to close.
     * @return the closed <code>Coordinate</code> array.
     */
    public static Coordinate[] closeCoordinates(final Coordinate[] coordinates)
    {
        Coordinate[] closedCoordinates = coordinates;

        if (!coordinates[0].equals3D(coordinates[coordinates.length - 1]))
        {
            closedCoordinates = new Coordinate[coordinates.length + 1];
            System.arraycopy(coordinates, 0, closedCoordinates, 0, coordinates.length);
            closedCoordinates[closedCoordinates.length - 1] = (Coordinate)coordinates[0].clone();
        }

        return closedCoordinates;
    }

    /**
     * Constructs a point geometry from the given longitude and latitude.
     *
     * @param longitude the longitude of the point.
     * @param latitude the latitude of the point.
     * @return the point geometry.
     */
    public static Point createPoint(final Number longitude, final Number latitude)
    {
        final GeometryFactory factory = createDefaultGeometryFactory();
        return factory.createPoint(new Coordinate(longitude.doubleValue(), latitude.doubleValue()));
    }

    /**
     * Constructs a line string geometry from the given list of coordinates.
     *
     * @param coordinates the list of coordinates of the line string.
     * @return the line string geometry.
     */
    public static LineString createLineString(final List<Coordinate> coordinates)
    {
        return createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
    }

    /**
     * Constructs a line string geometry from the given array of coordinates.
     *
     * @param coordinates the array of coordinates of the line string.
     * @return the line string geometry.
     */
    public static LineString createLineString(final Coordinate... coordinates)
    {
        final GeometryFactory factory = createDefaultGeometryFactory();
        return factory.createLineString(coordinates);
    }

    /**
     * Constructs a rectangle polygon from the given rectangle bounds.
     *
     * @param minX the minimum X value (e.g. longitude).
     * @param minY the minimum Y value (e.g. latitude).
     * @param maxX the maximum X value (e.g. longitude).
     * @param maxY the maximum Y value (e.g. latitude).
     * @return the rectangle polygon.
     */
    public static Polygon createRectangle(final Number minX, final Number minY, final Number maxX, final Number maxY)
    {
        // Create a closed array of coordinates for the rectangle.
        final double[] lonLatArray = new double[10];
        lonLatArray[0] = minX.doubleValue();
        lonLatArray[1] = minY.doubleValue();
        lonLatArray[2] = lonLatArray[0];
        lonLatArray[3] = maxY.doubleValue();
        lonLatArray[4] = maxX.doubleValue();
        lonLatArray[5] = lonLatArray[3];
        lonLatArray[6] = lonLatArray[4];
        lonLatArray[7] = lonLatArray[1];
        lonLatArray[8] = lonLatArray[0];
        lonLatArray[9] = lonLatArray[1];

        // Construct the rectangle geometry.
        final GeometryFactory factory = createDefaultGeometryFactory();
        final LinearRing shell = factory.createLinearRing(toCoordinateArray(lonLatArray));
        return factory.createPolygon(shell, new LinearRing[0]);
    }

    /**
     * Converts the <code>List</code> of numbers to a <code>Coordinate</code>
     * array. The optional dimension hint parameter indicates the number of
     * dimensions described by the list. If <code>null</code>, the data is
     * assumed to be 2-dimensions. Regardless of the dimensions of the data,
     * <code>Coordinate</code>s can only represent 2-3 dimensions.
     *
     * @param values the list of data to convert to coordinates.
     * @param dimensionHint the number of dimensions or <code>null</code>.
     * @return the <code>Coordinate</code> array.
     */
    public static Coordinate[] toCoordinateArray(final List<? extends Number> values, final Number dimensionHint)
    {
        int dimensions = 2;
        if (dimensionHint != null)
        {
            dimensions = dimensionHint.intValue();
        }

        // Force dim to a valid range.
        final int dim = Math.max(1, Math.min(dimensions, 3));

        final Coordinate[] coordinates = new Coordinate[values.size() / dimensions];
        for (int index = 0; index + dimensions - 1 < values.size(); index += dimensions)
        {
            final Coordinate coordinate = new Coordinate();
            coordinates[index / dimensions] = coordinate;
            switch (dim)
            {
                case 3:
                    coordinate.z = values.get(index + 2).doubleValue();
                case 2:
                    coordinate.y = values.get(index + 1).doubleValue();
                case 1:
                    coordinate.x = values.get(index).doubleValue();
            }
        }
        return coordinates;
    }

    /**
     * Creates a polygon that represents the geographic extents of the WGS 84
     * coordinate system. The longitude ranges from -180 to 180 and the latitude
     * ranges from -90 to 90.
     *
     * @return a polygon representing the geographic extents of the WGS 84
     *         coordinate system.
     */
    public static Polygon getWgs84Extents()
    {
        return createRectangle(-180, -90, 180, 90);
    }
}
