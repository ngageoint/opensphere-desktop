package io.opensphere.core.common.filter.expression.math;

import java.math.BigDecimal;

import io.opensphere.core.common.filter.dto.FilterDTO;
import io.opensphere.core.common.filter.expression.BinaryOperationEx;
import io.opensphere.core.common.filter.expression.Expression;
import io.opensphere.core.common.filter.expression.FunctionEx;

/**
 * This class is a {@link FunctionEx} that returns the minimum value from a set
 * of DTOs.
 */
public class MinimumEx extends FunctionEx
{
    /**
     * The current minimum value.
     */
    private Comparable<? extends Number> minimum;

    /**
     * Constructor.
     *
     * @param name the function name.
     * @param expression the
     */
    public MinimumEx(String name, Expression expression)
    {
        super(name, expression);
    }

    /**
     * Returns the minimum value or <code>null</code> if no minimum has been
     * computed.
     *
     * @see io.opensphere.core.common.filter.expression.FunctionEx#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO)
     */
    @Override
    public Comparable<? extends Number> getValueFrom(FilterDTO dto)
    {
        return minimum;
    }

    /**
     * Returns the average value or <code>null</code> if no average has been
     * computed.
     *
     * @see io.opensphere.core.common.filter.expression.FunctionEx#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO[])
     */
    @Override
    @SuppressWarnings("unchecked")
    public Comparable<? extends Number> getValueFrom(FilterDTO... dtos)
    {
        BigDecimal minValue = null;
        minimum = null;
        Expression expression = getArguments().get(0);

        // Loop through the DTOs and find the minimum value.
        for (FilterDTO dto : dtos)
        {
            Comparable<? extends Number> number = (Comparable<? extends Number>)expression.getValueFrom(dto);
            if (number != null)
            {
                BigDecimal tmp = BinaryOperationEx.toBigDecimal(number);
                if (minimum == null || tmp.compareTo(minValue) < 0)
                {
                    minValue = tmp;
                    minimum = number;
                }
            }
        }
        return minimum;
    }

    /**
     * @see io.opensphere.core.common.filter.expression.FunctionEx#toString()
     */
    @Override
    public String toString()
    {
        return super.toString() + "=" + getValueFrom((FilterDTO)null);
    }
}
