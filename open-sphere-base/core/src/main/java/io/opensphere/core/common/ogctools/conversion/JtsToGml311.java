package io.opensphere.core.common.ogctools.conversion;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.opengis.gml._311.AbstractGeometricAggregateType;
import net.opengis.gml._311.AbstractGeometricPrimitiveType;
import net.opengis.gml._311.AbstractGeometryType;
import net.opengis.gml._311.AbstractRingPropertyType;
import net.opengis.gml._311.AbstractRingType;
import net.opengis.gml._311.CurvePropertyType;
import net.opengis.gml._311.CurveSegmentArrayPropertyType;
import net.opengis.gml._311.CurveType;
import net.opengis.gml._311.DirectPositionListType;
import net.opengis.gml._311.DirectPositionType;
import net.opengis.gml._311.EnvelopeType;
import net.opengis.gml._311.GeometryPropertyType;
import net.opengis.gml._311.LineStringSegmentType;
import net.opengis.gml._311.LinearRingType;
import net.opengis.gml._311.MultiCurveType;
import net.opengis.gml._311.MultiGeometryType;
import net.opengis.gml._311.MultiPointType;
import net.opengis.gml._311.MultiPolygonType;
import net.opengis.gml._311.ObjectFactory;
import net.opengis.gml._311.PointPropertyType;
import net.opengis.gml._311.PointType;
import net.opengis.gml._311.PolygonPropertyType;
import net.opengis.gml._311.PolygonType;

/**
 * This class transforms JTS geometries to GML 3.1.1 geometries.
 */
public class JtsToGml311
{
    /** The GML 3.1.1 JAXB Object Factory. */
    private static final ObjectFactory GML_OBJECT_FACTORY = new ObjectFactory();

    /**
     * Transforms a JTS <code>Geometry</code> an
     * <code>AbstractGeometryType</code>.
     *
     * @param geometry the JTS <code>Geometry</code>.
     * @return the <code>AbstractGeometryType</code> to transform.
     * @throws UnsupportedOperationException if the specific
     *             <code>Geometry</code> is not supported.
     */
    public AbstractGeometryType transform(final Geometry geometry)
    {
        final AbstractGeometryType abstractGeometryType;
        if (geometry == null)
        {
            throw new IllegalArgumentException("Geometry cannot be null");
        }
        else if (geometry instanceof Point)
        {
            abstractGeometryType = transform((Point)geometry);
        }
        else if (geometry instanceof LinearRing)
        {
            abstractGeometryType = transform((LinearRing)geometry);
        }
        else if (geometry instanceof LineString)
        {
            abstractGeometryType = transform((LineString)geometry);
        }
        else if (geometry instanceof Polygon)
        {
            abstractGeometryType = transform((Polygon)geometry);
        }
        else if (geometry instanceof GeometryCollection)
        {
            abstractGeometryType = transform((GeometryCollection)geometry);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported Geometry: " + geometry.getClass());
        }
        return abstractGeometryType;
    }

    /**
     * Transforms a JTS <code>Point</code> to a <code>PointType</code>.
     *
     * @param point the <code>Point</code> to transform.
     * @return the GML <code>PointType</code>.
     */
    public PointType transform(final Point point)
    {
        if (point == null)
        {
            throw new IllegalArgumentException("Point cannot be null");
        }

        final DirectPositionType pos = new DirectPositionType();
        final Coordinate coordinate = point.getCoordinate();
        if (!Double.isNaN(coordinate.x))
        {
            pos.getValue().add(coordinate.x);
            if (!Double.isNaN(coordinate.y))
            {
                pos.getValue().add(coordinate.y);
                if (!Double.isNaN(coordinate.z))
                {
                    pos.getValue().add(coordinate.z);
                }
            }
        }
        pos.setSrsDimension(BigInteger.valueOf(pos.getValue().size()));

        PointType pointType = null;
        pointType = new PointType();
        pointType.setSrsName(generateSrsName(point.getSRID()));
        pointType.setPos(pos);
        return pointType;
    }

    /**
     * Transforms a JTS <code>LinearRing</code> to a
     * <code>LinearRingType</code>.
     *
     * @param linearRing the JTS <code>LinearRing</code> to transform.
     * @return the <code>LinearRingType</code>.
     */
    public LinearRingType transform(final LinearRing linearRing)
    {
        Preconditions.checkNotNull(linearRing, "LinearRing cannot be null");
        final DirectPositionListType posList = createDirectPositionListType(linearRing);
        final LinearRingType linearRingType = new LinearRingType();
        linearRingType.setSrsName(generateSrsName(linearRing.getSRID()));
        linearRingType.setPosList(posList);

        return linearRingType;
    }

    /**
     * Transforms a JTS <code>LineString</code> to a <code>CurveType</code>.
     *
     * @param lineString the JTS <code>LineString</code> to transform.
     * @return the <code>CurveType</code>.
     */
    public CurveType transform(final LineString lineString)
    {
        Preconditions.checkNotNull(lineString, "LineString cannot be null");
        final DirectPositionListType posList = createDirectPositionListType(lineString);
        final LineStringSegmentType lineStringSegment = new LineStringSegmentType();
        lineStringSegment.setPosList(posList);
        final CurveSegmentArrayPropertyType segments = new CurveSegmentArrayPropertyType();
        segments.getCurveSegment().add(GML_OBJECT_FACTORY.createLineStringSegment(lineStringSegment));

        final CurveType curveType = new CurveType();
        curveType.setSrsName(generateSrsName(lineString.getSRID()));
        curveType.setSegments(segments);
        return curveType;
    }

    /**
     * Transforms a JTS <code>LinearRing</code> to an
     * <code>AbstractRingType</code>.
     *
     * @param polygon the JTS <code>Polygon</code> to transform.
     * @return the <code>PolygonType</code>.
     */
    public PolygonType transform(final Polygon polygon)
    {
        Preconditions.checkNotNull(polygon, "Polygon cannot be null");

        final PolygonType polygonType = new PolygonType();
        polygonType.setSrsName(generateSrsName(polygon.getSRID()));

        // Transform the exterior ring.
        final LinearRing linearRing = (LinearRing)polygon.getExteriorRing();
        final AbstractRingPropertyType ringProperty = createAbstractRingPropertyType(linearRing);
        final JAXBElement<AbstractRingPropertyType> exterior = GML_OBJECT_FACTORY.createExterior(ringProperty);
        polygonType.setExterior(exterior);

        // Transform each of the interior rings.
        for (int ii = 0; ii < polygon.getNumInteriorRing(); ii++)
        {
            final LinearRing interiorRing = (LinearRing)polygon.getInteriorRingN(ii);
            final AbstractRingPropertyType interiorRingProperty = createAbstractRingPropertyType(interiorRing);
            final JAXBElement<AbstractRingPropertyType> interior = GML_OBJECT_FACTORY.createInterior(interiorRingProperty);
            polygonType.getInterior().add(interior);
        }

        return polygonType;
    }

    /**
     * Transforms an <code>AbstractGeometricAggregateType</code> to a JTS
     * <code>GeometryCollection</code>.
     *
     * @param abstractGeometricAggregateType the
     *            <code>AbstractGeometricAggregateType</code> to transform.
     * @return the JTS <code>GeometryCollection</code>.
     * @throws UnsupportedOperationException if the specific
     *             <code>AbstractGeometryType</code> is not supported.
     */
    public AbstractGeometricAggregateType transform(final GeometryCollection geometryCollection)
    {
        Preconditions.checkNotNull(geometryCollection, "GeometryCollection cannot be null");
        final AbstractGeometricAggregateType abstractGeometricAggregateType;
        if (geometryCollection instanceof MultiPoint)
        {
            abstractGeometricAggregateType = transform((MultiPoint)geometryCollection);
        }
        else if (geometryCollection instanceof MultiLineString)
        {
            abstractGeometricAggregateType = transform((MultiLineString)geometryCollection);
        }
        else if (geometryCollection instanceof MultiPolygon)
        {
            abstractGeometricAggregateType = transform((MultiPolygon)geometryCollection);

        }
        else
        {
            final MultiGeometryType multiGeometry = new MultiGeometryType();
            multiGeometry.setSrsName(generateSrsName(geometryCollection.getSRID()));

            for (int ii = 0; ii < geometryCollection.getNumGeometries(); ii++)
            {
                final Geometry geometry = geometryCollection.getGeometryN(ii);
                final AbstractGeometryType gmlGeometry = transform(geometry);
                final GeometryPropertyType geometryProperty = new GeometryPropertyType();
                final JAXBElement<? extends AbstractGeometryType> geometryElement = GML_OBJECT_FACTORY
                        .createGeometry(gmlGeometry);
                geometryProperty.setGeometry(geometryElement);
                multiGeometry.getGeometryMember().add(geometryProperty);
            }
            abstractGeometricAggregateType = multiGeometry;
        }

        return abstractGeometricAggregateType;
    }

    /**
     * Transforms a JTS <code>MultiPoint</code> to a
     * <code>MultiPointType</code>.
     *
     * @param multiPoint the JTS <code>MultiPoint</code> to transform.
     * @return the <code>MultiPointType</code>.
     */
    public MultiPointType transform(final MultiPoint multiPoint)
    {
        Preconditions.checkNotNull(multiPoint, "MultiPoint cannot be null");
        final MultiPointType gmlMultiPoint = new MultiPointType();
        gmlMultiPoint.setSrsName(generateSrsName(multiPoint.getSRID()));

        for (int ii = 0; ii < multiPoint.getNumGeometries(); ii++)
        {
            final Point point = (Point)multiPoint.getGeometryN(ii);
            final PointType gmlPoint = transform(point);
            final PointPropertyType pointProperty = new PointPropertyType();
            pointProperty.setPoint(gmlPoint);
            gmlMultiPoint.getPointMember().add(pointProperty);
        }
        return gmlMultiPoint;
    }

    /**
     * Transforms a JTS <code>MultiLineString</code> to an
     * <code>MultiCurveType</code>.
     *
     * @param multiLineString the JTS <code>MultiLineString</code> to transform.
     * @return the <code>MultiCurveType</code>.
     */
    public MultiCurveType transform(final MultiLineString multiLineString)
    {
        Preconditions.checkNotNull(multiLineString, "MultiLineString cannot be null");
        final MultiCurveType multiCurve = new MultiCurveType();
        multiCurve.setSrsName(generateSrsName(multiLineString.getSRID()));

        for (int ii = 0; ii < multiLineString.getNumGeometries(); ii++)
        {
            final LineString lineString = (LineString)multiLineString.getGeometryN(ii);
            final CurveType curve = transform(lineString);
            final CurvePropertyType curveProperty = new CurvePropertyType();
            curveProperty.setCurve(GML_OBJECT_FACTORY.createAbstractCurve(curve));
            multiCurve.getCurveMember().add(curveProperty);
        }

        return multiCurve;
    }

    /**
     * Transforms a JTS <code>MultiPolygon</code> to an
     * <code>MultiPolygonType</code>.
     *
     * @param multiPolygon the JTS <code>MultiPolygon</code> to transform.
     * @return the <code>MultiPolygonType</code>.
     */
    public MultiPolygonType transform(final MultiPolygon multiPolygon)
    {
        Preconditions.checkNotNull(multiPolygon, "MultiPolygon cannot be null");
        final MultiPolygonType gmlMultiPolygon = new MultiPolygonType();
        gmlMultiPolygon.setSrsName(generateSrsName(multiPolygon.getSRID()));

        for (int ii = 0; ii < multiPolygon.getNumGeometries(); ii++)
        {
            final Polygon polygon = (Polygon)multiPolygon.getGeometryN(ii);
            final PolygonType gmlPolygon = transform(polygon);
            final PolygonPropertyType polygonProperty = new PolygonPropertyType();
            polygonProperty.setPolygon(gmlPolygon);
            gmlMultiPolygon.getPolygonMember().add(polygonProperty);
        }

        return gmlMultiPolygon;
    }

    /**
     * Transforms a JTS <code>Envelope</code> to a <code>EnvelopeType</code>.
     *
     * @param envelope the JTS <code>Envelope</code> to transform.
     * @return the <code>EnvelopeType</code>.
     */
    public EnvelopeType transform(final Envelope envelope)
    {
        Preconditions.checkNotNull(envelope, "Envelope cannot be null");
        final EnvelopeType gmlEnvelope = new EnvelopeType();
        final DirectPositionType lowerCorner = createDirectPositionType(envelope.getMinX(), envelope.getMinY());
        final DirectPositionType upperCorner = createDirectPositionType(envelope.getMaxX(), envelope.getMaxY());
        gmlEnvelope.setLowerCorner(lowerCorner);
        gmlEnvelope.setUpperCorner(upperCorner);

        return gmlEnvelope;
    }

    /**
     * Creates a GML <code>DirectPositionType</code> from the given doubles. The
     * dimension of the position will be determined from the number of doubles
     * provided.
     *
     * @param values the doubles.
     * @return the GML <code>DirectPositionType</code>.
     */
    protected DirectPositionType createDirectPositionType(final double... values)
    {
        final DirectPositionType directPosition = new DirectPositionType();
        for (int ii = 0; ii < values.length; ii++)
        {
            directPosition.getValue().add(values[ii]);
        }
        directPosition.setSrsDimension(BigInteger.valueOf(values.length));
        return directPosition;
    }

    /**
     * Creates a GML <code>DirectPositionListType</code> from the given JTS
     * <code>LineString</code>.
     *
     * @param lineString the JTS <code>LineString</code> to transform.
     * @return the <code>DirectPositionListType</code>.
     */
    protected DirectPositionListType createDirectPositionListType(final LineString lineString)
    {
        final DirectPositionListType posList = new DirectPositionListType();
        if (lineString.getNumPoints() > 0)
        {
            final int dimension = getDimension(lineString.getCoordinate());
            final List<Double> list = posList.getValue();
            for (int ii = 0; ii < lineString.getNumPoints(); ii++)
            {
                final Coordinate coordinate = lineString.getCoordinateN(ii);
                switch (dimension)
                {
                    case 3:
                        list.add(coordinate.x);
                        list.add(coordinate.y);
                        list.add(coordinate.z);
                        break;
                    case 2:
                        list.add(coordinate.x);
                        list.add(coordinate.y);
                        break;
                    case 1:
                        list.add(coordinate.x);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported dimension: " + dimension);
                }
            }
            posList.setSrsDimension(BigInteger.valueOf(dimension));
            posList.setCount(BigInteger.valueOf(list.size()));
        }
        return posList;
    }

    /**
     * Creates a GML <code>AbstractRingPropertyType</code> from the given JTS
     * <code>LinearRing</code> .
     *
     * @param linearRing the JTS <code>LinearRing</code> to transform.
     * @return the <code>AbstractRingPropertyType</code>.
     */
    protected AbstractRingPropertyType createAbstractRingPropertyType(final LinearRing linearRing)
    {
        final AbstractRingPropertyType ringProperty = new AbstractRingPropertyType();
        final LinearRingType linearRingType = transform(linearRing);
        final JAXBElement<? extends AbstractRingType> ring = GML_OBJECT_FACTORY.createAbstractRing(linearRingType);
        ringProperty.setRing(ring);
        return ringProperty;
    }

    /**
     * Transforms the given SRID code to the EPSG string.
     *
     * @param srid the SRID code.
     * @return the EPSG string or <code>null</code> if the code is
     *         <code>0</code>.
     */
    protected String generateSrsName(final int srid)
    {
        String srsName = null;
        if (srid != 0)
        {
            srsName = "EPSG:" + srid;
        }
        return srsName;
    }

    /**
     * Determines the dimension of the given JTS <code>Coordinate</code>.
     *
     * @param coordinate the JTS <code>Coordinate</code>.
     * @return the dimension of the coordinate.
     */
    protected int getDimension(final Coordinate coordinate)
    {
        int dimension = 0;
        if (!Double.isNaN(coordinate.x))
        {
            dimension++;
            if (!Double.isNaN(coordinate.y))
            {
                dimension++;
                if (!Double.isNaN(coordinate.z))
                {
                    dimension++;
                }
            }
        }
        return dimension;
    }

    /**
     * Transforms an <code>AbstractGeometricPrimitiveType</code> to a JTS
     * <code>Geometry</code>.
     *
     * @param abstractGeometricPrimitiveType the
     *            <code>AbstractGeometricPrimitiveType</code> to transform.
     * @return the JTS <code>Geometry</code>.
     * @throws UnsupportedOperationException if the specific
     *             <code>AbstractGeometryType</code> is not supported.
     */
    public Geometry transform(final AbstractGeometricPrimitiveType abstractGeometricPrimitiveType)
    {
        final Geometry geometry;
        if (abstractGeometricPrimitiveType == null)
        {
            throw new IllegalArgumentException("AbstractGeometricPrimitiveType cannot be null");
        }
        else if (abstractGeometricPrimitiveType instanceof PolygonType)
        {
            geometry = transform(abstractGeometricPrimitiveType);
        }
        else if (abstractGeometricPrimitiveType instanceof PointType)
        {
            geometry = transform(abstractGeometricPrimitiveType);
        }
        else
        {
            throw new UnsupportedOperationException(
                    "Unsupported AbstractGeometricPrimitiveType: " + abstractGeometricPrimitiveType.getClass());
        }
        return geometry;
    }

    /**
     * Transforms an <code>AbstractRingType</code> to a JTS
     * <code>LinearRing</code>.
     *
     * @param abstractRingType the <code>AbstractRingType</code> to transform.
     * @return the JTS <code>LinearRing</code>.
     * @throws UnsupportedOperationException if the specific
     *             <code>AbstractRingType</code> (e.g. RingType) is not
     *             supported.
     */
    public LinearRing transform(final AbstractRingType abstractRingType)
    {
        final LinearRing linearRing;
        if (abstractRingType == null)
        {
            throw new IllegalArgumentException("AbstractRingType cannot be null");
        }
        else if (abstractRingType instanceof LinearRingType)
        {
            linearRing = transform(abstractRingType);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported AbstractRingType: " + abstractRingType.getClass());
        }
        return linearRing;
    }
}
