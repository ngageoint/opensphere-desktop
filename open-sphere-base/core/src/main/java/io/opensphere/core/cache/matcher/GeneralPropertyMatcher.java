package io.opensphere.core.cache.matcher;

import java.io.Serializable;
import java.util.Objects;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * A general-purpose property matcher for serializable values.
 *
 * @param <T> The operand type.
 */
public class GeneralPropertyMatcher<T extends Serializable> extends AbstractPropertyMatcher<T>
{
    /** The operator. */
    private final OperatorType myOperator;

    /**
     * Construct the matcher.
     *
     * @param propertyDescriptor The descriptor of the property.
     * @param operator The operator.
     * @param operand The value to match.
     */
    public GeneralPropertyMatcher(PropertyDescriptor<T> propertyDescriptor, OperatorType operator, T operand)
    {
        super(propertyDescriptor, operand);
        myOperator = operator;
    }

    /**
     * Construct a matcher with an {@link OperatorType#EQ} operator.
     *
     * @param propertyDescriptor The descriptor of the property.
     * @param operand The value to match.
     */
    public GeneralPropertyMatcher(PropertyDescriptor<T> propertyDescriptor, T operand)
    {
        this(propertyDescriptor, OperatorType.EQ, operand);
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
        GeneralPropertyMatcher<?> other = (GeneralPropertyMatcher<?>)obj;
        return myOperator == other.myOperator;
    }

    /**
     * The operator that indicates how the value should be matched.
     *
     * @return The operator.
     */
    public OperatorType getOperator()
    {
        return myOperator;
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
    public boolean matches(Object operand)
    {
        return getOperator() == OperatorType.NE ^ Objects.equals(getOperand(), operand);
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(getClass().getSimpleName()).append('[').append(getOperator()).append(' ')
                .append(getOperand()).append(']').toString();
    }

    /** Supported operator types. */
    public enum OperatorType
    {
        /** Operator that indicates the property values must match exactly. */
        EQ,

        /** Operator that indicates the property values must not match. */
        NE,
    }
}
