package io.opensphere.core.common.filter.operator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.geom.prep.PreparedPolygon;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class represents a spatial operator. This operator provides the ability
 * to perform spatial operations on geometries.
 */
public class SpatialOp extends Operator
{
    /**
     * The spatial operator type. The Simple Feature Specification for SQL 1.1
     * operators are defined here.
     */
    public enum SpatialOperatorType
    {
        /**
         * From Simple Feature Specification for SQL 1.1: <code>true</code> if
         * two geometries are equal.
         */
        EQUALS,

        /**
         * From Simple Feature Specification for SQL 1.1: <code>true</code> if
         * the intersection of two geometries is the empty set.
         */
        DISJOINT,

        /**
         * From Simple Feature Specification for SQL 1.1: <code>true</code> if
         * the only points in common between two geometries list in the union of
         * the boundaries of the geometries.
         */
        TOUCHES,

        /**
         * From Simple Feature Specification for SQL 1.1: <code>true</code> if
         * the geometry is completely contained in the operator's geometry.
         */
        WITHIN,

        /**
         * From Simple Feature Specification for SQL 1.1: <code>true</code> if
         * the intersection of two geometries results in a value of the same
         * dimension as the geometries but is different from the original
         * geometries.
         */
        OVERLAPS,

        /**
         * From Simple Feature Specification for SQL 1.1: <code>true</code> if
         * the intersection of two geometries results in a value whose dimension
         * is less than the maximum dimension of the geometries and the
         * intersection value includes points interior to both geometries, and
         * the intersection value is not equal to either geometry.
         */
        CROSSES,

        /**
         * From Simple Feature Specification for SQL 1.1: <code>true</code> if
         * the intersection of a geometry and the operator's geometry is not
         * empty. This is the opposite of {@link #DISJOINT}.
         */
        INTERSECTS,

        /**
         * From Simple Feature Specification for SQL 1.1: <code>true</code> if
         * the geometry is completely contained in the operator's geometry.
         */
        CONTAINS,

        /**
         * From Filter Encoding Implementation Specification 1.1.0:
         * <code>true</code> if a geometry is within a specified distance from
         * the operator's geometry.
         */
        DWITHIN,

        /**
         * From Filter Encoding Implementation Specification 1.1.0:
         * <code>true</code> if a geometry is beyond a specified distance from
         * the operator's geometry.
         */
        BEYOND,
    }

    /**
     * The geometry to use in comparisons.
     */
    private Geometry geometry;

    /**
     * The prepared geometry for improved performance.
     */
    private PreparedGeometry preparedGeometry;

    /**
     * This polygon should guarantee that everything within it is also inside of
     * the original polygon.
     */
    private PreparedPolygon innerPolygon;

    /**
     * An approximation of the concave hull of the original polygon. This
     * geometry should guarantee that everything outside of it is also outside
     * of the original polygon.
     */
    private PreparedPolygon outerPolygon;

    /**
     * The spatial operator type.
     */
    private SpatialOperatorType type;

    /**
     * The property name on which the operation is performed.
     */
    private String name;

    /**
     * The distance between two geometries for comparison.
     */
    private Double distance;

    /**
     * The units for the distance.
     */
    private String units;

    /**
     * Constructor. The type must not be {@link SpatialOperatorType#DWITHIN} or
     * {@link SpatialOperatorType#BEYOND}. If those are desired, use the
     * constructor
     * {@link #SpatialOp(Geometry, SpatialOperatorType, String, Double)}.
     *
     * @param geometry the geometry to use in comparisons.
     * @param type the spatial operator type.
     * @param name the property name on which the operation is performed.
     */
    public SpatialOp(Geometry geometry, SpatialOperatorType type, String name)
    {
        this(geometry, type, name, null, null);
    }

    /**
     * Constructor.
     *
     * @param geometry the geometry to use in comparisons.
     * @param type the spatial operator type.
     * @param name the property name on which the operation is performed.
     * @param distance the distance between two geometries for use with
     *            {@link SpatialOperatorType#DWITHIN} and
     *            {@link SpatialOperatorType#BEYOND}.
     * @param units the units for the <code>distance</code> parameter.
     */
    public SpatialOp(Geometry geometry, SpatialOperatorType type, String name, Double distance, String units)
    {
        setName(name);
        setType(type);
        setGeometry(geometry);

        // Ensure that the distance is provided.
        if ((SpatialOperatorType.DWITHIN == type || SpatialOperatorType.BEYOND == type) && (distance == null || units == null))
        {
            throw new IllegalArgumentException(
                    "The spatial operator type " + type + " requires non-null distance and unit values");
        }
        setDistance(distance);
        setUnits(units);
    }

    /**
     * Sets the geometry for use in comparisons.
     *
     * @param geometry the geometry.
     */
    public void setGeometry(Geometry geometry)
    {
        innerPolygon = outerPolygon = null;
        this.geometry = geometry;
        preparedGeometry = PreparedGeometryFactory.prepare(geometry);

        // If the geometry consists of many points and is a
        // polygon/multi-polygon,
        // calculate simpler inner/outer geometries to improve performance.
        if (geometry.getNumPoints() > 500 && (geometry instanceof Polygon || geometry instanceof MultiPolygon))
        {
            try
            {
                // This is a semi-arbitrary number but I found it to be better
                // than
                // 0.04 and 0.075. We want a drastic reduction in the number of
                // points and this works pretty well for polygons like country
                // borders. You want this number small to minimize the distance
                // between the inner and outer polygons but large enough to
                // simplify
                // the original geometry significantly.
                final double distanceTolerance = 0.05;
                Geometry simplifiedGeometry = TopologyPreservingSimplifier.simplify(geometry, distanceTolerance);

                // If the simplified geometry is less than half the number of
                // points
                // of the original geometry, create inner and outer polygons.
                // Again,
                // this is arbitrary but considering that more comparisons may
                // occur, it makes sense that we ensure a certain level of
                // simplification.
                if (simplifiedGeometry.getNumPoints() < geometry.getNumPoints() / 2)
                {
                    // Create two polygons that represent the "inner" and
                    // "outer"
                    // polygons.
                    innerPolygon = new PreparedPolygon((Polygonal)simplifiedGeometry.buffer(-distanceTolerance, 0));
                    outerPolygon = new PreparedPolygon((Polygonal)simplifiedGeometry.buffer(distanceTolerance, 0));
                }
            }
            catch (Exception e)
            {
                innerPolygon = outerPolygon = null;
            }
        }
    }

    /**
     * Returns the geometry for use in comparisons.
     *
     * @return the geometry.
     */
    public Geometry getGeometry()
    {
        return geometry;
    }

    /**
     * Sets the spatial operator type.
     *
     * @param type the spatial operator type.
     */
    public void setType(SpatialOperatorType type)
    {
        this.type = type;
    }

    /**
     * Returns the spatial operator type.
     *
     * @return the spatial operator type.
     */
    public SpatialOperatorType getType()
    {
        return type;
    }

    /**
     * Sets the property name on which the operation is performed.
     *
     * @param name the property name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the property name on which the operation is performed.
     *
     * @return the property name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the distance between two geometries.
     *
     * @param distance the distance.
     */
    public void setDistance(Double distance)
    {
        this.distance = distance;
    }

    /**
     * Returns the distance between two geometries.
     *
     * @return the distance.
     */
    public Double getDistance()
    {
        return distance;
    }

    /**
     * Sets the units for the distance value.
     *
     * @param units the units for the distance value.
     */
    public void setUnits(String units)
    {
        this.units = units;
    }

    /**
     * Returns the units for the distance value.
     *
     * @return the units for the distance value.
     */
    public String getUnits()
    {
        return units;
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#evaluate(FilterDTO)
     */
    @Override
    public boolean evaluate(FilterDTO dto)
    {
        boolean result = false;

        // Fetch the value from the data object.
        Geometry otherGeometry = (Geometry)dto.get(getName());

        // Ensure that the other geometry is non-null.
        if (otherGeometry != null)
        {
            // Invoke the appropriate geometry operation.
            switch (getType())
            {
                case EQUALS:
                    result = geometry.equals(otherGeometry);
                    break;
                case DISJOINT:
                    result = preparedGeometry.disjoint(otherGeometry);
                    break;
                case TOUCHES:
                    result = preparedGeometry.touches(otherGeometry);
                    break;
                case WITHIN:
                    result = preparedGeometry.within(otherGeometry);
                    break;
                case OVERLAPS:
                    result = preparedGeometry.overlaps(otherGeometry);
                    break;
                case CROSSES:
                    result = preparedGeometry.crosses(otherGeometry);
                    break;
                case INTERSECTS:
                    if (innerPolygon == null)
                    {
                        result = preparedGeometry.intersects(otherGeometry);
                    }

                    // If the other geometry intersects the interior of our
                    // geometry,
                    // we're done.
                    else if (innerPolygon.intersects(otherGeometry))
                    {
                        result = true;
                    }

                    // If the result does not intersect with the convex hull, it
                    // definitely does not intersect with our geometry.
                    else if (!outerPolygon.intersects(otherGeometry))
                    {
                        result = false;
                    }

                    // Otherwise, check the intersection with our geometry.
                    else
                    {
                        result = preparedGeometry.intersects(otherGeometry);
                    }
                    break;
                case CONTAINS:
                    result = preparedGeometry.contains(otherGeometry);
                    break;
                case DWITHIN:
                    result = geometry.isWithinDistance(otherGeometry, getDistance());
                    break;
                case BEYOND:
                    result = !geometry.isWithinDistance(otherGeometry, getDistance());
                    break;
                default:
                    throw new UnsupportedOperationException("Unable to perform a " + getType() + " on the given object");
            }
        }
        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName() + " " + getType() + (getDistance() == null ? " " : " (" + getDistance() + ") ") + getGeometry();
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#clone()
     */
    @Override
    public SpatialOp clone() throws CloneNotSupportedException
    {
        if (type == SpatialOperatorType.BEYOND || type == SpatialOperatorType.DWITHIN)
        {
            return new SpatialOp(geometry, type, name, distance, units);
        }
        return new SpatialOp(geometry, type, name);
    }
}
