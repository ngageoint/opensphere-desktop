package io.opensphere.core.cache.matcher;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * A numeric property matcher.
 *
 * @param <T> The type of number to be matched.
 */
public final class NumberPropertyMatcher<T extends Number> extends AbstractPropertyMatcher<T>
{
    /** The operator. */
    private final NumberPropertyMatcher.OperatorType myOperator;

    /**
     * Construct the property matcher.
     *
     * @param propertyDescriptor The property descriptor.
     * @param operator The operator.
     * @param operand The number to be matched.
     */
    public NumberPropertyMatcher(PropertyDescriptor<T> propertyDescriptor, OperatorType operator, T operand)
    {
        super(propertyDescriptor, operand);
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
        NumberPropertyMatcher<?> other = (NumberPropertyMatcher<?>)obj;
        return myOperator == other.myOperator;
    }

    /**
     * The comparison operator.
     *
     * @return The operator.
     */
    public NumberPropertyMatcher.OperatorType getOperator()
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
        if (operand instanceof Number)
        {
            double doub0 = getOperand().doubleValue();
            double doub1 = ((Number)operand).doubleValue();
            if (myOperator == OperatorType.EQ)
            {
                return doub0 == doub1;
            }
            else if (myOperator == OperatorType.GT)
            {
                return doub0 > doub1;
            }
            else if (myOperator == OperatorType.GTE)
            {
                return doub0 >= doub1;
            }
            else if (myOperator == OperatorType.LT)
            {
                return doub0 < doub1;
            }
            else if (myOperator == OperatorType.LTE)
            {
                return doub0 <= doub1;
            }
            else if (myOperator == OperatorType.NE)
            {
                return doub0 != doub1;
            }
            else
            {
                throw new UnexpectedEnumException(myOperator);
            }
        }
        else
        {
            return false;
        }
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
        /** == operator. */
        EQ,

        /** &gt; operator. */
        GT,

        /** &gt;= operator. */
        GTE,

        /** &lt; operator. */
        LT,

        /** &lt;= operator. */
        LTE,

        /** &lt;&gt; operator. */
        NE,
    }
}
