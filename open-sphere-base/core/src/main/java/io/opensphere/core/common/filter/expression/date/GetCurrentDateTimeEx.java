package io.opensphere.core.common.filter.expression.date;

import java.sql.Timestamp;

import io.opensphere.core.common.filter.dto.FilterDTO;
import io.opensphere.core.common.filter.expression.Expression;
import io.opensphere.core.common.filter.expression.FunctionEx;

/**
 * This class is a {@link FunctionEx} that returns the current date when
 * {@link #getValueFrom(FilterDTO)} is invoked.
 */
public class GetCurrentDateTimeEx extends FunctionEx
{
    /**
     * Constructor.
     *
     * @param name the function name.
     */
    public GetCurrentDateTimeEx(String name)
    {
        super(name, new Expression[0]);
    }

    /**
     * Returns the current date/time.
     *
     * @see io.opensphere.core.common.filter.expression.FunctionEx#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO)
     */
    @Override
    public Comparable<? extends Object> getValueFrom(FilterDTO dto)
    {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Returns the current date/time.
     *
     * @see io.opensphere.core.common.filter.expression.FunctionEx#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO[])
     */
    @Override
    public Comparable<? extends Object> getValueFrom(FilterDTO... dtos)
    {
        return getValueFrom((FilterDTO)null);
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
