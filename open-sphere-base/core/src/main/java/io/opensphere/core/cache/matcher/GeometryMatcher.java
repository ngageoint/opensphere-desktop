package io.opensphere.core.cache.matcher;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.Accumulator;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * A geometry property matcher. Currently this only supports intersections.
 */
public class GeometryMatcher extends AbstractPropertyMatcher<Geometry> implements IntervalPropertyMatcher<Geometry>
{
    /** An infinite envelope. */
    private static final Envelope INFINITE_ENVELOPE = new Envelope(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    /** The operator that indicates how the geometries should be compared. */
    private final GeometryMatcher.OperatorType myOperator;

    /**
     * Construct the matcher.
     *
     * @param propertyName The name of the geometry property.
     * @param operator The operator.
     * @param geometry The geometry to intersect.
     */
    public GeometryMatcher(String propertyName, GeometryMatcher.OperatorType operator, Geometry geometry)
    {
        super(new PropertyDescriptor<Geometry>(propertyName, Geometry.class), (Geometry)geometry.clone());
        myOperator = operator;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        GeometryMatcher other = (GeometryMatcher)obj;
        return myOperator == other.myOperator;
    }

    @Override
    public Accumulator<Geometry> getAccumulator()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("PMD.MissingBreakInSwitch")
    public IntervalPropertyMatcher<Geometry> getGroupMatcher()
    {
        switch (getOperator())
        {
            case CONTAINS:
            case COVERS:
            case CROSSES:
            case EQUALS:
            case OVERLAPS:
            case TOUCHES:
                return new GeometryMatcher(getPropertyDescriptor().getPropertyName(), OperatorType.INTERSECTS,
                        getOperandDirect());
            case COVEREDBY:
            case INTERSECTS:
            case INTERSECTS_NO_TOUCH:
            case WITHIN:
                return this;
            case DISJOINT:
                return new GeometryMatcher(getPropertyDescriptor().getPropertyName(), OperatorType.INTERSECTS,
                        getOperandDirect().getFactory().toGeometry(INFINITE_ENVELOPE));
            default:
                throw new UnexpectedEnumException(getOperator());
        }
    }

    @Override
    public Geometry getMinimumOverlapInterval()
    {
        return getOperator() == OperatorType.DISJOINT ? null : getOperand();
    }

    @Override
    public Geometry getOperand()
    {
        return (Geometry)super.getOperand().clone();
    }

    /**
     * The operator that indicates how the geometries should be compared.
     *
     * @return The operator.
     */
    public GeometryMatcher.OperatorType getOperator()
    {
        return myOperator;
    }

    @Override
    public Geometry getSimplifiedBounds()
    {
        return getOperandDirect().getEnvelope();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myOperator == null ? 0 : myOperator.hashCode());
        return result;
    }

    @Override
    public boolean isIndefinite(Object value)
    {
        return false;
    }

    @Override
    public boolean matches(Object operand)
    {
        if (!(operand instanceof Geometry))
        {
            return false;
        }
        Geometry geom = (Geometry)operand;
        switch (getOperator())
        {
            case CONTAINS:
                return getOperandDirect().contains(geom);
            case COVEREDBY:
                return getOperandDirect().coveredBy(geom);
            case COVERS:
                return getOperandDirect().covers(geom);
            case CROSSES:
                return getOperandDirect().crosses(geom);
            case DISJOINT:
                return getOperandDirect().disjoint(geom);
            case EQUALS:
                return getOperandDirect().equalsTopo(geom);
            case INTERSECTS_NO_TOUCH:
                return getOperandDirect().intersects(geom) && !getOperandDirect().touches(geom);
            case INTERSECTS:
                return getOperandDirect().intersects(geom);
            case OVERLAPS:
                return getOperandDirect().overlaps(geom);
            case TOUCHES:
                return getOperandDirect().touches(geom);
            case WITHIN:
                return getOperandDirect().within(geom);
            default:
                throw new UnexpectedEnumException(getOperator());
        }
    }

    @Override
    public boolean overlaps(IntervalPropertyMatcher<?> other)
    {
        return other.getPropertyDescriptor().equals(getPropertyDescriptor())
                && getOperandDirect().overlaps((Geometry)other.getOperand())
                && !getOperandDirect().touches((Geometry)other.getOperand());
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(getClass().getSimpleName()).append('[').append(getOperator()).append(' ')
                .append(getOperandDirect()).append(']').toString();
    }

    /** Supported operator types. */
    public enum OperatorType
    {
        /**
         * Operator analogous to {@link Geometry#contains(Geometry)}.
         */
        CONTAINS,

        /**
         * Operator analogous to {@link Geometry#coveredBy(Geometry)}.
         */
        COVEREDBY,

        /**
         * Operator analogous to {@link Geometry#covers(Geometry)}.
         */
        COVERS,

        /**
         * Operator analogous to {@link Geometry#crosses(Geometry)}.
         */
        CROSSES,

        /**
         * Operator analogous to {@link Geometry#disjoint(Geometry)}.
         */
        DISJOINT,

        /**
         * Operator analogous to {@link Geometry#equalsTopo(Geometry)}.
         */
        EQUALS,

        /**
         * Operator analogous to {@link Geometry#intersects(Geometry)}.
         */
        INTERSECTS,

        /**
         * Operator similar to {@link OperatorType#INTERSECTS}, but does not
         * match geometries that only touch.
         */
        INTERSECTS_NO_TOUCH,

        /**
         * Operator analogous to {@link Geometry#overlaps(Geometry)}.
         */
        OVERLAPS,

        /**
         * Operator analogous to {@link Geometry#touches(Geometry)}.
         */
        TOUCHES,

        /**
         * Operator analogous to {@link Geometry#within(Geometry)}.
         */
        WITHIN
    }
}
