package io.opensphere.mantle.util;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.common.geospatial.JTSUtils;
import net.opengis.gml._311.AbstractGeometricPrimitiveType;
import net.opengis.gml._311.AbstractGeometryType;
import net.opengis.gml._311.AbstractRingPropertyType;
import net.opengis.gml._311.DirectPositionListType;
import net.opengis.gml._311.DirectPositionType;
import net.opengis.gml._311.LineStringType;
import net.opengis.gml._311.LinearRingType;
import net.opengis.gml._311.MultiPolygonType;
import net.opengis.gml._311.MultiSurfaceType;
import net.opengis.gml._311.PointType;
import net.opengis.gml._311.PolygonPropertyType;
import net.opengis.gml._311.PolygonType;
import net.opengis.gml._311.SurfacePropertyType;

/** Utilities from managing JTS to GML conversions. */
public final class JTSGMLUtilities
{
    /** Factory from creating GML classes. */
    public static final net.opengis.gml._311.ObjectFactory GML_OBJECT_FACTORY = new net.opengis.gml._311.ObjectFactory();

    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(JTSGMLUtilities.class);

    /**
     * Factory used to make jts objects.
     */
    private static GeometryFactory ourJtsFactory = JTSUtils.createDefaultGeometryFactory();

    /**
     * Build a MultiPolygon GML object from a JTS MultiPolygon.
     *
     * @param isLatBeforeLon Determine the order of the geographic coordinates.
     * @param multiPolygon The JTS polygon.
     * @return A GML MultiPolygon.
     */
    public static JAXBElement<? extends AbstractGeometryType> buildMultiPolygon(boolean isLatBeforeLon, MultiPolygon multiPolygon)
    {
        JAXBElement<? extends AbstractGeometryType> geomElement;
        MultiPolygonType multiPolygonType = GML_OBJECT_FACTORY.createMultiPolygonType();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++)
        {
            Polygon polygon = (Polygon)multiPolygon.getGeometryN(i);

            PolygonType polygonType = createGMLPolygonType(isLatBeforeLon, polygon);

            PolygonPropertyType polygonPropertyType = GML_OBJECT_FACTORY.createPolygonPropertyType();
            polygonPropertyType.setPolygon(polygonType);
            multiPolygonType.getPolygonMember().add(polygonPropertyType);
        }

        geomElement = GML_OBJECT_FACTORY.createMultiPolygon(multiPolygonType);
        return geomElement;
    }

    /**
     * Build a MultiSurface GML object from a JTS MultiPolygon.
     *
     * @param isLatBeforeLon Determine the order of the geographic coordinates.
     * @param multiPolygon The JTS polygon.
     * @return A GML MultiSurface.
     */
    public static JAXBElement<? extends AbstractGeometryType> buildMultiSurface(boolean isLatBeforeLon, MultiPolygon multiPolygon)
    {
        JAXBElement<? extends AbstractGeometryType> geomElement;
        MultiSurfaceType multiSurfaceType = GML_OBJECT_FACTORY.createMultiSurfaceType();

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++)
        {
            Polygon polygon = (Polygon)multiPolygon.getGeometryN(i);

            PolygonType polygonType = createGMLPolygonType(isLatBeforeLon, polygon);
            JAXBElement<PolygonType> subGeomElement = GML_OBJECT_FACTORY.createPolygon(polygonType);

            SurfacePropertyType surface = GML_OBJECT_FACTORY.createSurfacePropertyType();
            surface.setSurface(subGeomElement);
            multiSurfaceType.getSurfaceMember().add(surface);
        }

        geomElement = GML_OBJECT_FACTORY.createMultiSurface(multiSurfaceType);
        return geomElement;
    }

    /**
     * Convert a gis geometry to a jts geometry.
     *
     * @param pGeometry the {@link Geometry} to convert
     * @return converted {@link AbstractGeometricPrimitiveType}
     */
    public static Geometry convertGeometry(AbstractGeometryType pGeometry)
    {
        Geometry geometry = null;
        double[] lonlat = getLonLatArray(pGeometry);
        Coordinate[] coordinates = JTSUtils.toCoordinateArray(lonlat);
        if (pGeometry instanceof PointType)
        {
            geometry = ourJtsFactory.createPoint(coordinates[0]);
        }
        else if (pGeometry instanceof LineStringType)
        {
            geometry = ourJtsFactory.createLineString(coordinates);
        }
        else if (pGeometry instanceof PolygonType)
        {
            geometry = ourJtsFactory.createPolygon(ourJtsFactory.createLinearRing(JTSUtils.closeCoordinates(coordinates)), null);
        }
        else if (pGeometry instanceof MultiPolygonType)
        {
            geometry = convertGeometry((MultiPolygonType)pGeometry);
        }

        return geometry;
    }

    /**
     * Convert a jts geometry to a gis geometry.
     *
     * @param pGeometry the {@link Geometry} to convert
     * @return converted {@link AbstractGeometricPrimitiveType}
     */
    public static AbstractGeometricPrimitiveType convertGeometry(Geometry pGeometry)
    {
        AbstractGeometricPrimitiveType gisGeometry = null;
        if (pGeometry instanceof Point)
        {
            gisGeometry = JTSGMLUtilities.convertPointToPointType((Point)pGeometry);
        }
        else if (pGeometry instanceof LineString)
        {
            gisGeometry = JTSGMLUtilities.convertLineStringToLineStringType((LineString)pGeometry);
        }
        else if (pGeometry instanceof Polygon)
        {
            gisGeometry = JTSGMLUtilities.createGMLPolygonType(true, (Polygon)pGeometry);
        }
        else
        {
            LOGGER.error("Unable to convert geometry type.");
        }
        return gisGeometry;
    }

    /**
     * Convert from a geom LineString to a gis LineStringType.
     *
     * @param pLine A LineString to convert.
     * @return A LineStringType converted from a Point.
     */
    public static LineStringType convertLineStringToLineStringType(LineString pLine)
    {
        LineStringType returnLineStringType = new LineStringType();

        DirectPositionListType positionListType = new DirectPositionListType();
        positionListType.setSrsDimension(new BigInteger("2"));
        Coordinate[] coordinates = pLine.getCoordinates();
        for (int i = 0; i < coordinates.length; i++)
        {
            positionListType.getValue().add(coordinates[i].x);
            positionListType.getValue().add(coordinates[i].y);
        }
        returnLineStringType.setPosList(positionListType);

        return returnLineStringType;
    }

    /**
     * Convert from a geom Point to a gis PointType.
     *
     * @param pPoint A Point to convert.
     * @return A PointType converted from a Point.
     */
    public static PointType convertPointToPointType(Point pPoint)
    {
        PointType returnPointType = new PointType();

        DirectPositionType positionType = new DirectPositionType();
        positionType.setSrsDimension(new BigInteger("2"));
        positionType.getValue().add(pPoint.getX());
        positionType.getValue().add(pPoint.getY());

        returnPointType.setPos(positionType);

        return returnPointType;
    }

    /**
     * Build a GML polygon from a JTS polygon.
     *
     * @param isLatBeforeLon Determine the order of the geographic coordinates.
     * @param polygon The JTS polygon.
     * @return The filter JAXB element.
     */
    public static PolygonType createGMLPolygonType(boolean isLatBeforeLon, Polygon polygon)
    {
        DirectPositionListType pointList = new DirectPositionListType();

        // TODO: Should this use the SRS from the class or always CRS:84?
        pointList.setSrsName("CRS:84");
        pointList.setSrsDimension(BigInteger.valueOf(2));

        for (Coordinate point : polygon.getExteriorRing().getCoordinates())
        {
            pointList.getValue().add(Double.valueOf(isLatBeforeLon ? point.y : point.x));
            pointList.getValue().add(Double.valueOf(isLatBeforeLon ? point.x : point.y));
        }

        LinearRingType ringType = GML_OBJECT_FACTORY.createLinearRingType();
        ringType.setPosList(pointList);

        AbstractRingPropertyType exterior = GML_OBJECT_FACTORY.createAbstractRingPropertyType();
        exterior.setRing(GML_OBJECT_FACTORY.createLinearRing(ringType));

        PolygonType polygonType = GML_OBJECT_FACTORY.createPolygonType();
        polygonType.setExterior(GML_OBJECT_FACTORY.createExterior(exterior));

        for (int i = 0; i < polygon.getNumInteriorRing(); i++)
        {
            LineString interiorRingN = polygon.getInteriorRingN(i);

            pointList = new DirectPositionListType();
            // TODO: Should this use the SRS from the class or always CRS:84?
            pointList.setSrsName("CRS:84");
            pointList.setSrsDimension(BigInteger.valueOf(2));

            for (Coordinate point : interiorRingN.getCoordinates())
            {
                pointList.getValue().add(Double.valueOf(isLatBeforeLon ? point.y : point.x));
                pointList.getValue().add(Double.valueOf(isLatBeforeLon ? point.x : point.y));
            }

            ringType = GML_OBJECT_FACTORY.createLinearRingType();
            ringType.setPosList(pointList);

            AbstractRingPropertyType interior = GML_OBJECT_FACTORY.createAbstractRingPropertyType();
            interior.setRing(GML_OBJECT_FACTORY.createAbstractRing(ringType));

            polygonType.getInterior().add(GML_OBJECT_FACTORY.createInterior(interior));
        }

        return polygonType;
    }

    /**
     * Converts a {@link MultiPolygonType} to a jts geometry.
     *
     * @param multiPolygon The {@link MultiPolygonType}.
     * @return The jts geometry.
     */
    private static Geometry convertGeometry(MultiPolygonType multiPolygon)
    {
        Polygon[] polygons = new Polygon[multiPolygon.getPolygonMember().size()];

        int index = 0;
        for (PolygonPropertyType polygonProperty : multiPolygon.getPolygonMember())
        {
            PolygonType polygonType = polygonProperty.getPolygon();
            Polygon polygon = (Polygon)convertGeometry(polygonType);
            polygons[index] = polygon;
            index++;
        }

        return ourJtsFactory.createMultiPolygon(polygons);
    }

    /**
     * Generate a lonLatArray for the given gml geom node.
     *
     * @param geometryValue GML geometry
     * @return array containing longitude and latitudes for all the points
     *         contained in the gml order is lon,lat,lon,lat...
     */
    private static double[] getLonLatArray(AbstractGeometryType geometryValue)
    {
        double[] lonLatArray = null;

        if (geometryValue instanceof PointType)
        {
            List<Double> posList = ((PointType)geometryValue).getPos().getValue();
            if (posList.size() == 2)
            {
                lonLatArray = new double[2];
                lonLatArray[0] = posList.get(0).doubleValue();
                lonLatArray[1] = posList.get(1).doubleValue();
            }
        }
        else if (geometryValue instanceof LineStringType)
        {
            List<Double> posList = ((LineStringType)geometryValue).getPosList().getValue();
            if (!posList.isEmpty() && posList.size() % 2 == 0)
            {
                lonLatArray = new double[posList.size()];
                for (int i = 0; i < posList.size(); i++)
                {
                    lonLatArray[i] = posList.get(i).doubleValue();
                }
            }
        }
        else if (geometryValue instanceof PolygonType)
        {
            LinearRingType ringType = (LinearRingType)((PolygonType)geometryValue).getExterior().getValue().getRing().getValue();

            List<Double> posList = ringType.getPosList().getValue();
            if (!posList.isEmpty() && posList.size() % 2 == 0)
            {
                lonLatArray = new double[posList.size()];
                for (int i = 0; i < posList.size(); i++)
                {
                    lonLatArray[i] = posList.get(i).doubleValue();
                }
            }
        }
        // Most of the code expects at least 2 values
        if (lonLatArray == null)
        {
            lonLatArray = new double[2];
            lonLatArray[0] = 0.0;
            lonLatArray[1] = 0.0;
        }
        return lonLatArray;
    }

    /** Disallow instantiation. */
    private JTSGMLUtilities()
    {
    }
}
