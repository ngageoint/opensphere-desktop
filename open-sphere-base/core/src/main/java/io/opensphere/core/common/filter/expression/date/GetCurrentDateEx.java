package io.opensphere.core.common.filter.expression.date;

import java.sql.Date;
import java.util.Calendar;

import io.opensphere.core.common.filter.dto.FilterDTO;
import io.opensphere.core.common.filter.expression.Expression;
import io.opensphere.core.common.filter.expression.FunctionEx;

/**
 * This class is a {@link FunctionEx} that returns the current date when
 * {@link #getValueFrom(FilterDTO)} is invoked.
 */
public class GetCurrentDateEx extends FunctionEx
{
    /**
     * Constructor.
     *
     * @param name the function name.
     */
    public GetCurrentDateEx(String name)
    {
        super(name, new Expression[0]);
    }

    /**
     * Returns the current date.
     *
     * @see io.opensphere.core.common.filter.expression.FunctionEx#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO)
     */
    @Override
    public Comparable<? extends Object> getValueFrom(FilterDTO dto)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return new Date(calendar.getTimeInMillis());
    }

    /**
     * Returns the current date.
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
