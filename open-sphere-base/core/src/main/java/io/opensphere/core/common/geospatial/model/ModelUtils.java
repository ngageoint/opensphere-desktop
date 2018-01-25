package io.opensphere.core.common.geospatial.model;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.common.geospatial.JTSUtils;
import io.opensphere.core.common.geospatial.model.interfaces.IDataPoint;

/**
 * This utility class provides useful methods for working with JTS geometries.
 */
public class ModelUtils
{
    /**
     * Constructs a Polygon from the DataPolygon
     *
     * @param polygon the DataPolygon to construct the JTS polygon from
     * @return the polygon.
     */
    public static Polygon createPolygon(DataPolygon polygon)
    {
        double[] lonLatArray = new double[polygon.getPoints().size() * 2];
        int i = 0;
        for (IDataPoint point : polygon.getPoints())
        {
            lonLatArray[i] = point.getLon();
            i++;
            lonLatArray[i] = point.getLat();
            i++;
        }
        GeometryFactory factory = JTSUtils.createDefaultGeometryFactory();
        LinearRing shell = factory.createLinearRing(JTSUtils.toCoordinateArray(lonLatArray, true));
        return factory.createPolygon(shell, new LinearRing[0]);
    }

    /**
     * Constructs a LineString from the DataTrack
     *
     * @param track the DataTrack to construct the JTS LineString from
     * @return the linestring.
     */
    public static LineString createLineString(DataTrack track)
    {
        double[] lonLatArray = new double[track.getPoints().size() * 2];
        int i = 0;
        for (IDataPoint point : track.getPoints())
        {
            lonLatArray[i++] = point.getLon();
            lonLatArray[i++] = point.getLat();
        }
        GeometryFactory factory = JTSUtils.createDefaultGeometryFactory();
        return factory.createLineString(JTSUtils.toCoordinateArray(lonLatArray));
    }

}
