package io.opensphere.wps;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.wps.util.WpsUtilities;
import net.opengis.gml._311.AbstractRingPropertyType;
import net.opengis.gml._311.DirectPositionListType;
import net.opengis.gml._311.LinearRingType;
import net.opengis.gml._311.PolygonType;
import net.opengis.ogc._110.BinarySpatialOpType;
import net.opengis.ogc._110.FilterType;
import net.opengis.ogc._110.ObjectFactory;
import net.opengis.ogc._110.PropertyNameType;

/** Creates OGC filters. */
public final class FilterCreator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FilterCreator.class);

    /** The OGC object factory. */
    private static final ObjectFactory OGC_FACTORY = new ObjectFactory();

    /**
     * Converts a bounding box string of the format
     * "minLon,minLat,maxLon,maxLat" to an OGC filter XML string.
     *
     * @param bbox the bounding box string
     * @return the filter XML string
     * @throws IllegalArgumentException if the bbox string can't be parsed
     * @throws JAXBException If the filter cannot be marshalled
     */
    public static String bboxToFilterString(String bbox) throws IllegalArgumentException, JAXBException
    {
        FilterType filter = bboxToFilter(bbox);
        return toString(filter);
    }

    /**
     * Converts a bounding box string of the format
     * "minLon,minLat,maxLon,maxLat" to an OGC filter object.
     *
     * @param bbox the bounding box string
     * @return the filter object
     * @throws IllegalArgumentException if the bbox string can't be parsed
     */
    public static FilterType bboxToFilter(String bbox) throws IllegalArgumentException
    {
        FilterType filter = new FilterType();

        BinarySpatialOpType binarySpatialOp = new BinarySpatialOpType();

        PropertyNameType geom = new PropertyNameType();
        geom.setValue("GEOM");
        binarySpatialOp.setPropertyName(geom);

        PolygonType polygonType = new PolygonType();
        AbstractRingPropertyType exterior = new AbstractRingPropertyType();
        LinearRingType ringType = new LinearRingType();
        DirectPositionListType pointList = new DirectPositionListType();
        pointList.setSrsName("CRS:84");
        pointList.setSrsDimension(BigInteger.valueOf(2));
        double[] coords = WpsUtilities.toPolygonCoordinates(WpsUtilities.parseCoordinates(bbox));
        for (double coord : coords)
        {
            pointList.getValue().add(Double.valueOf(coord));
        }
        ringType.setPosList(pointList);
        net.opengis.gml._311.ObjectFactory gmlFactory = new net.opengis.gml._311.ObjectFactory();
        exterior.setRing(gmlFactory.createLinearRing(ringType));
        polygonType.setExterior(gmlFactory.createExterior(exterior));
        binarySpatialOp.setGeometry(gmlFactory.createPolygon(polygonType));

        filter.setSpatialOps(OGC_FACTORY.createIntersects(binarySpatialOp));

        return filter;
    }

    /**
     * Formats the filter as an XML string.
     *
     * @param filter the filter
     * @return the XML string
     * @throws JAXBException If the filter cannot be marshalled
     */
    private static String toString(FilterType filter) throws JAXBException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(768);
        XMLUtilities.writeXMLObject(OGC_FACTORY.createFilter(filter), outputStream, FilterType.class);
        String filterString = null;
        try
        {
            filterString = outputStream.toString("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            LOGGER.error(e, e);
        }
        return filterString;
    }

    /** Disallow instantiation. */
    private FilterCreator()
    {
    }
}
