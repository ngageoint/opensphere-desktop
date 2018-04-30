package io.opensphere.core.common.filter.expression.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import io.opensphere.core.common.filter.dto.FilterDTO;
import io.opensphere.core.common.filter.expression.BinaryOperationEx;
import io.opensphere.core.common.filter.expression.Expression;
import io.opensphere.core.common.filter.expression.FunctionEx;

/**
 * This class is a {@link FunctionEx} that returns the average value from a set
 * of DTOs.
 */
public class AverageEx extends FunctionEx
{
    /**
     * The <code>MathContext</code> for calculating the average.
     */
    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);

    /**
     * The current average value.
     */
    private BigDecimal average;

    /**
     * Constructor.
     *
     * @param name the function name.
     * @param expression the
     */
    public AverageEx(String name, Expression expression)
    {
        super(name, expression);
    }

    /**
     * Returns the average value or <code>null</code> if no average has been
     * computed.
     *
     * @see io.opensphere.core.common.filter.expression.FunctionEx#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO)
     */
    @Override
    public BigDecimal getValueFrom(FilterDTO dto)
    {
        return average;
    }

    /**
     * Returns the average value or <code>null</code> if no average has been
     * computed.
     *
     * @see io.opensphere.core.common.filter.expression.FunctionEx#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO[])
     */
    @Override
    @SuppressWarnings("unchecked")
    public BigDecimal getValueFrom(FilterDTO... dtos)
    {
        long count = 0;
        average = null;
        BigDecimal sum = BigDecimal.ZERO;
        Expression expression = getArguments().get(0);

        // Loop through the DTOs and only include non-null values in the
        // average.
        for (FilterDTO dto : dtos)
        {
            Comparable<? extends Number> number = (Comparable<? extends Number>)expression.getValueFrom(dto);
            if (number != null)
            {
                count++;
                sum = sum.add(BinaryOperationEx.toBigDecimal(number));
            }
        }

        // If non-null values were found, compute the average.
        if (count > 0)
        {
            average = sum.divide(BigDecimal.valueOf(count), MATH_CONTEXT);
        }
        return average;
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
