package io.opensphere.core.common.filter.expression.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import io.opensphere.core.common.filter.dto.FilterDTO;
import io.opensphere.core.common.filter.expression.BinaryOperationEx;
import io.opensphere.core.common.filter.expression.Expression;
import io.opensphere.core.common.filter.expression.FunctionEx;

/**
 * This class is a {@link FunctionEx} that returns the median value from a set
 * of DTOs.
 */
public class MedianEx extends FunctionEx
{
    /**
     * Compares the keys from two map entries.
     */
    private static final Comparator<Entry<BigDecimal, Comparable<? extends Number>>> COMPARATOR = new Comparator<Entry<BigDecimal, Comparable<? extends Number>>>()
    {
        @Override
        public int compare(Entry<BigDecimal, Comparable<? extends Number>> o1, Entry<BigDecimal, Comparable<? extends Number>> o2)
        {
            return o1.getKey().compareTo(o2.getKey());
        }
    };

    /**
     * The <code>MathContext</code> for calculating the average.
     */
    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);

    /**
     * The current median value.
     */
    private Comparable<? extends Number> median;

    /**
     * Constructor.
     *
     * @param name the function name.
     * @param expression the
     */
    public MedianEx(String name, Expression expression)
    {
        super(name, expression);
    }

    /**
     * Returns the median value or <code>null</code> if no median has been
     * computed.
     *
     * @see io.opensphere.core.common.filter.expression.FunctionEx#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO)
     */
    @Override
    public Comparable<? extends Number> getValueFrom(FilterDTO dto)
    {
        return median;
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
        median = null;
        Expression expression = getArguments().get(0);

        // Loop through the DTOs and add them to a list of numbers.
        List<Entry<BigDecimal, Comparable<? extends Number>>> numbers = new ArrayList<>(dtos.length);
        for (FilterDTO dto : dtos)
        {
            Comparable<? extends Number> number = (Comparable<? extends Number>)expression.getValueFrom(dto);
            if (number != null)
            {
                BigDecimal tmp = BinaryOperationEx.toBigDecimal(number);
                Entry<BigDecimal, Comparable<? extends Number>> entry = new AbstractMap.SimpleEntry<>(tmp, number);
                numbers.add(entry);
            }
        }

        // Find the median value.
        if (!numbers.isEmpty())
        {
            // Sort the numbers.
            Collections.sort(numbers, COMPARATOR);

            // If there are an even number of numbers, average the middle two.
            if (numbers.size() % 2 == 0)
            {
                Entry<BigDecimal, Comparable<? extends Number>> first = numbers.get(numbers.size() / 2 - 1);
                Entry<BigDecimal, Comparable<? extends Number>> second = numbers.get(numbers.size() / 2);

                median = BinaryOperationEx.convert(
                        first.getKey().add(second.getKey()).divide(BigDecimal.valueOf(2), MATH_CONTEXT), first.getValue(),
                        second.getValue());
            }
            else
            {
                median = numbers.get(numbers.size() / 2).getValue();
            }
        }
        return median;
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
