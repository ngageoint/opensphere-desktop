package io.opensphere.core.common.filter.expression.math;

import java.math.BigDecimal;

import io.opensphere.core.common.filter.dto.FilterDTO;
import io.opensphere.core.common.filter.expression.BinaryOperationEx;
import io.opensphere.core.common.filter.expression.Expression;
import io.opensphere.core.common.filter.expression.FunctionEx;

/**
 * This class is a {@link FunctionEx} that returns the maximum value from a set
 * of DTOs.
 */
public class MaximumEx extends FunctionEx
{
    /**
     * The current maximum value.
     */
    private Comparable<? extends Number> maximum;

    /**
     * Constructor.
     *
     * @param name the function name.
     * @param expression the
     */
    public MaximumEx(String name, Expression expression)
    {
        super(name, expression);
    }

    /**
     * Returns the maximum value or <code>null</code> if no maximum has been
     * computed.
     *
     * @see io.opensphere.core.common.filter.expression.FunctionEx#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO)
     */
    @Override
    public Comparable<? extends Number> getValueFrom(FilterDTO dto)
    {
        return maximum;
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
        BigDecimal maxValue = null;
        maximum = null;
        Expression expression = getArguments().get(0);

        // Loop through the DTOs and find the maximum value.
        for (FilterDTO dto : dtos)
        {
            Comparable<? extends Number> number = (Comparable<? extends Number>)expression.getValueFrom(dto);
            if (number != null)
            {
                BigDecimal tmp = BinaryOperationEx.toBigDecimal(number);
                if (maximum == null || tmp.compareTo(maxValue) > 0)
                {
                    maxValue = tmp;
                    maximum = number;
                }
            }
        }
        return maximum;
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
