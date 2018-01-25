package io.opensphere.mantle.data.element.mdfilter;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.model.GeographicUtilities.PolygonWinding;
import io.opensphere.mantle.util.JTSGMLUtilities;
import net.opengis.gml._311.AbstractGeometryType;
import net.opengis.gml._311.EnvelopeType;
import net.opengis.ogc._110.BinaryComparisonOpType;
import net.opengis.ogc._110.BinaryLogicOpType;
import net.opengis.ogc._110.BinarySpatialOpType;
import net.opengis.ogc._110.FilterType;
import net.opengis.ogc._110.LiteralType;
import net.opengis.ogc._110.LogicOpsType;
import net.opengis.ogc._110.PropertyNameType;
import net.opengis.ogc._110.SpatialOpsType;

/**
 * A utility class for generating filter and filter related OGC classes.
 */
public final class OGCFilterGenerator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(OGCFilterGenerator.class);

    /** Factory from creating OGC classes. */
    private static final net.opengis.ogc._110.ObjectFactory OGC_OBJECT_FACTORY = new net.opengis.ogc._110.ObjectFactory();

    /**
     * Build the WFS filter.
     *
     * @param params The parameters which describe the filter being built.
     * @param typeDisplayName The type name used for message generation.
     * @return The filter.
     */
    public static FilterType buildQuery(OGCFilterParameters params, String typeDisplayName)
    {
        return buildQuery(params, typeDisplayName, false);
    }

    /**
     * Build the WFS filter.
     *
     * @param params The parameters which describe the filter being built.
     * @param typeDisplayName The type name used for message generation.
     * @param endInclusive Whether the end time should be queried inclusively
     * @return The filter.
     */
    public static FilterType buildQuery(OGCFilterParameters params, String typeDisplayName, boolean endInclusive)
    {
        FilterType filterType = new FilterType();

        List<JAXBElement<?>> elementList = New.list(3);

        // Create and add spatial filter if provided
        JAXBElement<? extends SpatialOpsType> spatialOp = null;
        if (params.getRegion() != null && params.getGeometryTagName() != null)
        {
            BinarySpatialOpType intersectType = new BinarySpatialOpType();
            PropertyNameType geomPropertyName = new PropertyNameType();
            geomPropertyName.setValue(params.getGeometryTagName());
            intersectType.setPropertyName(geomPropertyName);
            intersectType.setGeometry(buildGeomElement(params));
            spatialOp = OGC_OBJECT_FACTORY.createIntersects(intersectType);
            elementList.add(spatialOp);
        }

        // Create and add user filter if provided
        if (params.getUserFilter() != null)
        {
            try
            {
                JAXBElement<?> filter = FilterToWFS110Converter.convert(params.getUserFilter());
                if (filter != null)
                {
                    elementList.add(filter);
                }
            }
            catch (FilterException e)
            {
                LOGGER.warn("Failed to translate load filter for type [" + typeDisplayName + "]", e);
            }
        }

        // Create and add time filter if provided
        if (params.getTimeSpan() != null && params.getTimeSpan() != TimeSpan.TIMELESS)
        {
            elementList.addAll(createTimeElements(params.getTimeSpan(), params.getTimeFieldNames(), endInclusive));
        }

        // Combine everything
        if (spatialOp != null && elementList.size() == 1)
        {
            filterType.setSpatialOps(spatialOp);
        }
        else
        {
            filterType.setLogicOps(createLogicalAnd(elementList));
        }

        return filterType;
    }

    /**
     * Build the WFS filter string.
     *
     * @param params The parameters which describe the filter being built.
     * @param typeDisplayName The type name used for message generation.
     * @return The filter string.
     */
    public static String buildQueryString(OGCFilterParameters params, String typeDisplayName)
    {
        FilterType filterType = buildQuery(params, typeDisplayName);
        try
        {
            String target = BinarySpatialOpType.class.getPackage().getName() + ":" + EnvelopeType.class.getPackage().getName();
            JAXBContext jc = JAXBContextHelper.getCachedContext(target);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(OGC_OBJECT_FACTORY.createFilter(filterType), writer);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("This is the request: " + writer.getBuffer());
            }
            return URLEncoder.encode(writer.toString(), "UTF-8");
        }
        catch (JAXBException | UnsupportedEncodingException e)
        {
            LOGGER.error("Failed to encode WFS GetFeature request: " + e, e);
        }
        return null;
    }

    /**
     * Builds the geometry element for a JAXB request.
     *
     * @param params The parameters which describe the filter being built.
     * @return the JAXB geometry element representing the region
     */
    private static JAXBElement<? extends AbstractGeometryType> buildGeomElement(OGCFilterParameters params)
    {
        Geometry input = params.getRegion();
        JAXBElement<? extends AbstractGeometryType> geomElement = null;

        Geometry region = input instanceof Polygon ? processPolygon((Polygon)input) : input;

        if (region instanceof Polygon)
        {
            geomElement = JTSGMLUtilities.GML_OBJECT_FACTORY
                    .createPolygon(JTSGMLUtilities.createGMLPolygonType(params.isLatBeforeLon(), (Polygon)region));
        }
        else if (region instanceof MultiPolygon)
        {
            MultiPolygon multiPolygon = (MultiPolygon)region;

            if (params.isUseMultisurfaces())
            {
                geomElement = JTSGMLUtilities.buildMultiSurface(params.isLatBeforeLon(), multiPolygon);
            }
            else
            {
                geomElement = JTSGMLUtilities.buildMultiPolygon(params.isLatBeforeLon(), multiPolygon);
            }
        }
        else
        {
            throw new UnsupportedOperationException(region.getClass() + " is not supported.");
        }

        return geomElement;
    }

    /**
     * Creates a logical 'and' GML element out of a list of JAXB elements.
     *
     * @param elements the elements to add into a logical 'and'
     * @return the binary logic operator that 'ands' the elements together
     */
    private static JAXBElement<? extends LogicOpsType> createLogicalAnd(Collection<JAXBElement<?>> elements)
    {
//        if (elements.size() < 2)
//        {
//            throw new IllegalArgumentException("Cannot create logical \'and\' type with fewer than 2 elements.");
//        }
        BinaryLogicOpType logicOps = new BinaryLogicOpType();
        for (JAXBElement<?> element : elements)
        {
            logicOps.getComparisonOpsOrSpatialOpsOrLogicOps().add(element);
        }
        return OGC_OBJECT_FACTORY.createAnd(logicOps);
    }

    /**
     * Create an OGC logic operation that will add a time filter with an "up"
     * time and "down" time (for features with duration) to the given operation
     * and return the result.
     *
     * @param timeSpan The time span.
     * @param timeFields The field names to which the time span should be
     *            mapped.
     * @param endInclusive Whether the end time should be queried inclusively
     * @return The OGC logic operation.
     */
    private static Collection<JAXBElement<?>> createTimeElements(TimeSpan timeSpan, Pair<String, String> timeFields,
            boolean endInclusive)
    {
        PropertyNameType uptimeName = new PropertyNameType();
        uptimeName.setValue(timeFields.getFirstObject());
        PropertyNameType downtimeName = new PropertyNameType();
        downtimeName.setValue(timeFields.getSecondObject());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        BinaryComparisonOpType greaterThanOrEqualToLowestTime;
        if (timeSpan.isUnboundedStart())
        {
            greaterThanOrEqualToLowestTime = null;
        }
        else
        {
            // Create expression for down time >= start
            greaterThanOrEqualToLowestTime = new BinaryComparisonOpType();
            greaterThanOrEqualToLowestTime.getExpression().add(OGC_OBJECT_FACTORY.createPropertyName(downtimeName));
            LiteralType lower = new LiteralType();
            String startString = format.format(timeSpan.getStartDate());
            lower.getContent().add(startString);
            greaterThanOrEqualToLowestTime.getExpression().add(OGC_OBJECT_FACTORY.createLiteral(lower));
        }

        BinaryComparisonOpType lessThanGreatestTime;
        if (timeSpan.isUnboundedEnd())
        {
            lessThanGreatestTime = null;
        }
        else
        {
            // Create expression for up time < end
            lessThanGreatestTime = new BinaryComparisonOpType();
            lessThanGreatestTime.getExpression().add(OGC_OBJECT_FACTORY.createPropertyName(uptimeName));
            LiteralType upper = new LiteralType();
            String endString = format.format(timeSpan.getEndDate());
            upper.getContent().add(endString);
            lessThanGreatestTime.getExpression().add(OGC_OBJECT_FACTORY.createLiteral(upper));
        }

        // Create list of JAXB elements with the time filters that were just
        // created
        Collection<JAXBElement<?>> timeElements = New.list(2);
        if (greaterThanOrEqualToLowestTime != null)
        {
            timeElements.add(OGC_OBJECT_FACTORY.createPropertyIsGreaterThanOrEqualTo(greaterThanOrEqualToLowestTime));
        }
        if (lessThanGreatestTime != null)
        {
            JAXBElement<BinaryComparisonOpType> propertyIsLessThan = endInclusive
                    ? OGC_OBJECT_FACTORY.createPropertyIsLessThanOrEqualTo(lessThanGreatestTime)
                    : OGC_OBJECT_FACTORY.createPropertyIsLessThan(lessThanGreatestTime);
            timeElements.add(propertyIsLessThan);
        }

        return timeElements;
    }

    /**
     * Analyze the polygon to determine if it circumscribes one of the poles or
     * crosses the 180 longitude line and needs special processing.
     *
     * TODO currently this only works for polygons which have no holes and which
     * do not have multiple antimeridian crossings.
     *
     * @param input The input polygon.
     * @return The processed polygon.
     */
    private static Geometry processPolygon(Polygon input)
    {
        Coordinate[] coordinates = input.getExteriorRing().getCoordinates();
        double total = 0.;
        boolean cross180 = false;
        for (int i = 1; i < coordinates.length; i++)
        {
            double dx = coordinates[i].x - coordinates[i - 1].x;
            if (dx > 180.)
            {
                dx -= 360.;
                cross180 = true;
            }
            else if (dx < -180.)
            {
                dx += 360.;
                cross180 = true;
            }
            total += dx;
        }

        Geometry result;
        if (Math.abs(total) > 1.)
        {
            result = JTSUtilities.createPolarPolygon(coordinates);
        }
        else if (cross180)
        {
            List<Polygon> split = JTSUtilities.splitOnAntimeridian(input, null, PolygonWinding.UNKNOWN);
            Polygon[] polys = new Polygon[split.size()];
            split.toArray(polys);

            GeometryFactory geomFactory = new GeometryFactory();
            result = geomFactory.createMultiPolygon(polys);
        }
        else
        {
            result = input;
        }
        return result;
    }

    /** Disallow instantiation. */
    private OGCFilterGenerator()
    {
    }
}
