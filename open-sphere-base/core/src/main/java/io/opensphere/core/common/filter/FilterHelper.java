package io.opensphere.core.common.filter;

import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.opengis.gml._311.AbstractRingPropertyType;
import net.opengis.gml._311.DirectPositionListType;
import net.opengis.gml._311.DirectPositionType;
import net.opengis.gml._311.EnvelopeType;
import net.opengis.gml._311.LinearRingType;
import net.opengis.gml._311.PolygonType;
import net.opengis.ogc._110.BBOXType;
import net.opengis.ogc._110.BinaryComparisonOpType;
import net.opengis.ogc._110.BinaryLogicOpType;
import net.opengis.ogc._110.BinarySpatialOpType;
import net.opengis.ogc._110.ComparisonOpsType;
import net.opengis.ogc._110.FilterType;
import net.opengis.ogc._110.LiteralType;
import net.opengis.ogc._110.LogicOpsType;
import net.opengis.ogc._110.LowerBoundaryType;
import net.opengis.ogc._110.ObjectFactory;
import net.opengis.ogc._110.PropertyIsBetweenType;
import net.opengis.ogc._110.PropertyIsLikeType;
import net.opengis.ogc._110.PropertyIsNullType;
import net.opengis.ogc._110.PropertyNameType;
import net.opengis.ogc._110.SortByType;
import net.opengis.ogc._110.SpatialOpsType;
import net.opengis.ogc._110.UnaryLogicOpType;
import net.opengis.ogc._110.UpperBoundaryType;

/******
 * NOTE: This class is not thread safe!
 */
public class FilterHelper
{
    public static final String WILDCARD = "*";

    protected static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static SimpleDateFormat createDateTimeFormat()
    {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }

    private final ObjectFactory ogcFactory;

    private final net.opengis.gml._311.ObjectFactory gmlFactory;

    private JAXBContext jc;

    private static FilterHelper instance = null;

    private String VALID_TIME_PROPERTY_NAME = "";

    private static final String GEOM_PROPERTY_NAME = "GEOM";

    private static final Log LOGGER = LogFactory.getLog(FilterHelper.class);

    private FilterHelper()
    {
        long start = System.currentTimeMillis();
        ogcFactory = new ObjectFactory();
        gmlFactory = new net.opengis.gml._311.ObjectFactory();
        VALID_TIME_PROPERTY_NAME = gmlFactory.createValidTime(null).getName().getLocalPart();
        try
        {
            jc = JAXBContext
                    .newInstance(FilterType.class.getPackage().getName() + ":" + EnvelopeType.class.getPackage().getName());
        }
        catch (JAXBException e)
        {
            LOGGER.error(null, e);
        }
        long end = System.currentTimeMillis();
        LOGGER.debug("Took " + (end - start) + " ms to create the object factories");
    }

    public static FilterHelper instance()
    {
        if (instance == null)
        {
            instance = new FilterHelper();
        }
        return instance;
    }

    public JAXBElement<BinaryComparisonOpType> getPropertyIsEqualTo(String columnName, String value)
    {
        PropertyNameType name = new PropertyNameType();
        name.setValue(columnName);

        LiteralType literal = new LiteralType();
        literal.getContent().add(value);
        BinaryComparisonOpType equal = new BinaryComparisonOpType();
        equal.getExpression().add(ogcFactory.createPropertyName(name));
        equal.getExpression().add(ogcFactory.createLiteral(literal));
        return ogcFactory.createPropertyIsEqualTo(equal);
    }

    public JAXBElement<BinaryComparisonOpType> getPropertyIsNotEqualTo(String columnName, String value)
    {
        PropertyNameType name = new PropertyNameType();
        name.setValue(columnName);

        LiteralType literal = new LiteralType();
        literal.getContent().add(value);
        BinaryComparisonOpType equal = new BinaryComparisonOpType();
        equal.getExpression().add(ogcFactory.createPropertyName(name));
        equal.getExpression().add(ogcFactory.createLiteral(literal));
        return ogcFactory.createPropertyIsNotEqualTo(equal);
    }

    public JAXBElement<? extends ComparisonOpsType> getPropertyIsNull(String columnName)
    {
        PropertyIsNullType nullType = new PropertyIsNullType();
        PropertyNameType name = new PropertyNameType();
        name.setValue(columnName);
        nullType.setPropertyName(name);

        return ogcFactory.createPropertyIsNull(nullType);
    }

    public JAXBElement<BinaryComparisonOpType> getStartDateFilterFragment(Date startDate)
    {

        return getStartDateFilterFragment(startDate, VALID_TIME_PROPERTY_NAME, createDateTimeFormat());
    }

    public JAXBElement<BinaryComparisonOpType> getStartDateFilterFragment(Date startDate, String propertyName,
            SimpleDateFormat sdf)
    {
        PropertyNameType upDateTimePropertyName = new PropertyNameType();
        upDateTimePropertyName.setValue(propertyName);

        // Create the lower bound.

        LiteralType lowerValue = new LiteralType();
        lowerValue.getContent().add(sdf.format(startDate));
        BinaryComparisonOpType lowerBound = new BinaryComparisonOpType();
        lowerBound.getExpression().add(ogcFactory.createPropertyName(upDateTimePropertyName));
        lowerBound.getExpression().add(ogcFactory.createLiteral(lowerValue));
        return ogcFactory.createPropertyIsGreaterThanOrEqualTo(lowerBound);
    }

    public JAXBElement<BinaryComparisonOpType> getEndDateFilterFragment(Date endDate)
    {
        return getEndDateFilterFragment(endDate, VALID_TIME_PROPERTY_NAME, createDateTimeFormat());
    }

    public JAXBElement<BinaryComparisonOpType> getEndDateFilterFragment(Date endDate, String propertyName, SimpleDateFormat sdf)
    {
        PropertyNameType downDateTimePropertyName = new PropertyNameType();
        downDateTimePropertyName.setValue(propertyName);

        // Create the upper bound.
        LiteralType upperValue = new LiteralType();
        upperValue.getContent().add(sdf.format(endDate));
        BinaryComparisonOpType upperBound = new BinaryComparisonOpType();
        upperBound.getExpression().add(ogcFactory.createPropertyName(downDateTimePropertyName));
        upperBound.getExpression().add(ogcFactory.createLiteral(upperValue));
        return ogcFactory.createPropertyIsLessThan(upperBound);
    }

    public JAXBElement<PropertyIsLikeType> getPropertyIsLikeFragment(String name, String value)
    {
        PropertyIsLikeType property = new PropertyIsLikeType();
        property.setWildCard(WILDCARD);
        property.setEscapeChar("\\");
        property.setSingleChar(".");
        PropertyNameType nameType = new PropertyNameType();
        nameType.setValue(name);
        LiteralType literal = new LiteralType();
        literal.getContent().add(value);
        property.setLiteral(literal);
        property.setPropertyName(nameType);
        return ogcFactory.createPropertyIsLike(property);
    }

    public JAXBElement<PropertyIsBetweenType> getPropertyIsBetweenFragment(String propertyName, String lowerBound,
            String upperBound)
    {
        PropertyIsBetweenType property = new PropertyIsBetweenType();

        // set the name
        PropertyNameType nameType = new PropertyNameType();
        nameType.setValue(propertyName);
        property.setExpression(ogcFactory.createPropertyName(nameType));

        // set the lower boundary
        LowerBoundaryType lbt = new LowerBoundaryType();
        LiteralType lower = new LiteralType();
        lower.getContent().add(lowerBound);
        lbt.setExpression(ogcFactory.createLiteral(lower));
        property.setLowerBoundary(lbt);

        // set the upper boundary
        UpperBoundaryType ubt = new UpperBoundaryType();
        LiteralType upper = new LiteralType();
        upper.getContent().add(upperBound);
        ubt.setExpression(ogcFactory.createLiteral(upper));
        property.setUpperBoundary(ubt);
        return ogcFactory.createPropertyIsBetween(property);
    }

    public JAXBElement<BBOXType> getBBOXFilterFragment(double[] bbox)
    {
        if (bbox == null || bbox.length != 4)
        {
            throw new IllegalArgumentException("bounding box array must be of length 4");
        }

        BBOXType bboxType = new BBOXType();
        PropertyNameType geom = new PropertyNameType();
        geom.setValue(GEOM_PROPERTY_NAME);
        bboxType.setPropertyName(geom);

        EnvelopeType envelope = new EnvelopeType();

        DirectPositionType lowerCorner = new DirectPositionType();
        lowerCorner.getValue().add(bbox[0]);
        lowerCorner.getValue().add(bbox[1]);
        envelope.setLowerCorner(lowerCorner);

        DirectPositionType upperCorner = new DirectPositionType();
        upperCorner.getValue().add(bbox[2]);
        upperCorner.getValue().add(bbox[3]);
        envelope.setUpperCorner(upperCorner);
        bboxType.setEnvelope(gmlFactory.createEnvelope(envelope));
        return ogcFactory.createBBOX(bboxType);
    }

    public JAXBElement<? extends LogicOpsType> createAndFromComparisonOpsList(
            List<JAXBElement<? extends ComparisonOpsType>> elements)
    {
        BinaryLogicOpType binary = new BinaryLogicOpType();
        for (int i = 0; i < elements.size(); i++)
        {
            binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(elements.get(i));
        }
        return ogcFactory.createAnd(binary);
    }

    public JAXBElement<? extends LogicOpsType> createAndFromLogicOpsList(List<JAXBElement<? extends LogicOpsType>> elements)
    {
        BinaryLogicOpType binary = new BinaryLogicOpType();
        for (int i = 0; i < elements.size(); i++)
        {
            binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(elements.get(i));
        }
        return ogcFactory.createAnd(binary);
    }

    public JAXBElement<? extends LogicOpsType> createAndFromLogicAndComparison(JAXBElement<? extends LogicOpsType> logic,
            JAXBElement<? extends ComparisonOpsType> compare)
    {
        BinaryLogicOpType binary = new BinaryLogicOpType();
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(logic);
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(compare);
        return ogcFactory.createAnd(binary);
    }

    public JAXBElement<? extends LogicOpsType> createNotForLogicOps(JAXBElement<? extends LogicOpsType> element)
    {
        UnaryLogicOpType unary = ogcFactory.createUnaryLogicOpType();

        unary.setLogicOps(element);

        return ogcFactory.createNot(unary);
    }

    public JAXBElement<? extends LogicOpsType> createNotForComparisonOps(JAXBElement<? extends ComparisonOpsType> element)
    {
        UnaryLogicOpType unary = ogcFactory.createUnaryLogicOpType();

        unary.setComparisonOps(element);

        return ogcFactory.createNot(unary);
    }

    public JAXBElement<? extends LogicOpsType> createOrFromLogicList(List<JAXBElement<? extends LogicOpsType>> elements)
    {
        BinaryLogicOpType binary = new BinaryLogicOpType();
        for (int i = 0; i < elements.size(); i++)
        {
            binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(elements.get(i));
        }
        return ogcFactory.createOr(binary);
    }

    public JAXBElement<? extends LogicOpsType> createOrFromComparisonList(List<JAXBElement<? extends ComparisonOpsType>> elements)
    {
        BinaryLogicOpType binary = new BinaryLogicOpType();
        for (int i = 0; i < elements.size(); i++)
        {
            binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(elements.get(i));
        }
        return ogcFactory.createOr(binary);
    }

    public String filterTypeToString(FilterType filter) throws JAXBException
    {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(ogcFactory.createFilter(filter), bos);

        long end = System.currentTimeMillis();
        LOGGER.debug("Took " + (end - start) + " to marshall the filter");
        return new String(bos.toByteArray());
    }

    public String sortByTypeToString(SortByType sortBy) throws JAXBException
    {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(ogcFactory.createSortBy(sortBy), bos);

        long end = System.currentTimeMillis();
        LOGGER.debug("Took " + (end - start) + " to marshall the filter");
        return new String(bos.toByteArray());
    }

    public FilterType createBasicFilter(double[] bbox, Date start, Date end)
    {
        FilterType filter = ogcFactory.createFilterType();

        BinaryLogicOpType binary = new BinaryLogicOpType();
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getStartDateFilterFragment(start));
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getEndDateFilterFragment(end));
        if (bbox != null)
        {
            binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getBBOXFilterFragment(bbox));
        }
        filter.setLogicOps(ogcFactory.createAnd(binary));
        try
        {
            LOGGER.debug("Created Filter\n" + filterTypeToString(filter));
        }
        catch (JAXBException e)
        {
            LOGGER.error(null, e);
        }
        return filter;
    }

    public FilterType createBasicFilter(double[] bbox, Date start, Date end, String timePropertyName, SimpleDateFormat sdf)
    {
        FilterType filter = ogcFactory.createFilterType();

        BinaryLogicOpType binary = new BinaryLogicOpType();
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getStartDateFilterFragment(start, timePropertyName, sdf));
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getEndDateFilterFragment(end, timePropertyName, sdf));
        if (bbox != null)
        {
            binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getBBOXFilterFragment(bbox));
        }
        filter.setLogicOps(ogcFactory.createAnd(binary));
        try
        {
            LOGGER.debug("Created Filter\n" + filterTypeToString(filter));
        }
        catch (JAXBException e)
        {
            LOGGER.error(null, e);
        }
        return filter;
    }

    /**
     * Creates a Filter from a list of points defining an enclosed polygon, and
     * a time interval.
     *
     * @param points - degrees
     * @param start
     * @param end
     * @return FilterType
     */
    public FilterType createPolygonFilter(List<Point2D> points, Date start, Date end)
    {

        BinaryLogicOpType binary = new BinaryLogicOpType();

        PropertyNameType geom = new PropertyNameType();
        geom.setValue("GEOM");

        BinarySpatialOpType intersectType = new BinarySpatialOpType();
        intersectType.setPropertyName(geom);

        PolygonType polygonType = new PolygonType();
        AbstractRingPropertyType exterior = new AbstractRingPropertyType();
        LinearRingType ringType = new LinearRingType();
        DirectPositionListType pointList = new DirectPositionListType();

        pointList.setSrsName("EPSG:4326");
        pointList.setSrsDimension(BigInteger.valueOf(2));

        for (Point2D pt : points)
        {
            pointList.getValue().add(pt.getX());
            pointList.getValue().add(pt.getY());
        }
        ringType.setPosList(pointList);
        exterior.setRing(gmlFactory.createLinearRing(ringType));
        polygonType.setExterior(gmlFactory.createExterior(exterior));
        intersectType.setGeometry(gmlFactory.createPolygon(polygonType));
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(ogcFactory.createIntersects(intersectType));

        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getStartDateFilterFragment(start));
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getEndDateFilterFragment(end));

        FilterType filter = ogcFactory.createFilterType();
        filter.setLogicOps(ogcFactory.createAnd(binary));
        try
        {
            LOGGER.debug("Created Filter\n" + filterTypeToString(filter));
        }
        catch (JAXBException e)
        {
            LOGGER.error(null, e);
        }
        return filter;
    }

    public FilterType createFilter(JAXBElement<? extends LogicOpsType> logicOps,
            JAXBElement<? extends ComparisonOpsType> compareOps, JAXBElement<? extends SpatialOpsType> spatialOps)
    {
        FilterType filter = ogcFactory.createFilterType();
        BinaryLogicOpType binary = new BinaryLogicOpType();
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(logicOps);
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(compareOps);
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(spatialOps);
        filter.setLogicOps(ogcFactory.createAnd(binary));
        return filter;
    }

    public FilterType createFilter(double[] bbox, Date start, Date end, JAXBElement<? extends LogicOpsType> logicOps,
            JAXBElement<? extends ComparisonOpsType> compareOps, JAXBElement<? extends SpatialOpsType> spatialOps)
    {
        if (logicOps == null && compareOps == null && spatialOps == null)
        {
            return createBasicFilter(bbox, start, end);
        }
        FilterType filter = ogcFactory.createFilterType();

        BinaryLogicOpType binary = new BinaryLogicOpType();
        if (start != null)
        {
            binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getStartDateFilterFragment(start));
        }

        if (end != null)
        {
            binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getEndDateFilterFragment(end));
        }
        if (bbox != null)
        {
            binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(getBBOXFilterFragment(bbox));
        }
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(logicOps);
        binary.getComparisonOpsOrSpatialOpsOrLogicOps().add(compareOps);
        filter.setLogicOps(ogcFactory.createAnd(binary));
        filter.setSpatialOps(spatialOps);
        try
        {
            LOGGER.debug("Created Filter\n" + filterTypeToString(filter));
        }
        catch (JAXBException e)
        {
            LOGGER.error(null, e);
        }
        return filter;
    }

    public void main(String[] args)
    {
        Date start = new Date();
        Date end = new Date();
        double[] bbox = { 0, 0, 90, 90 };
        List<JAXBElement<? extends ComparisonOpsType>> elements = new ArrayList<>();
        JAXBElement<PropertyIsLikeType> like = getPropertyIsLikeFragment("COLUMN", "VALUE*");
        elements.add(like);
        JAXBElement<PropertyIsBetweenType> between = getPropertyIsBetweenFragment("COLUMN", "0", "10");
        elements.add(between);
        FilterType filter = createFilter(bbox, start, end, createAndFromComparisonOpsList(elements), null, null);

        try
        {
            System.out.println(filterTypeToString(filter));
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
    }

}
