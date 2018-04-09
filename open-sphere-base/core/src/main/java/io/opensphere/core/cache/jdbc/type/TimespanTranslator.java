package io.opensphere.core.cache.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;

/**
 * A translator implementation used to handle {@link TimeSpan} objects.
 */
public class TimespanTranslator extends AbstractValueTranslator<TimeSpan>
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getType()
     */
    @Override
    public Class<TimeSpan> getType()
    {
        return TimeSpan.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getValue(Class,
     *      long, int, ResultSet, PropertyMatcher, Collection)
     */
    @Override
    public int getValue(Class<? extends TimeSpan> type, long sizeBytes, int column, ResultSet rs,
            PropertyMatcher<? extends TimeSpan> filter, Collection<? super TimeSpan> results)
        throws CacheException
    {
        try
        {
            int index = column;
            long start = rs.getLong(index++);
            boolean unboundedStart = rs.wasNull() || start == Long.MIN_VALUE;
            long end = rs.getLong(index++);
            boolean unboundedEnd = rs.wasNull() || end == Long.MAX_VALUE;

            // This handles the special case where a time span is selected
            // even though there's no time span property.
            TimeSpan object = unboundedStart ? unboundedEnd ? TimeSpan.TIMELESS : TimeSpan.newUnboundedStartTimeSpan(end)
                    : unboundedEnd ? TimeSpan.newUnboundedEndTimeSpan(start) : TimeSpan.get(start, end);
            if (filter == null || filter.matches(object))
            {
                results.add(object);
            }

            return index;
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to get value from result set: " + e, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#setValue(PreparedStatement,
     *      int, Object, boolean)
     */
    @Override
    public int setValue(PreparedStatement pstmt, int column, TimeSpan value, boolean forInsert) throws SQLException
    {
        Utilities.checkNull(value, "value");
        int index = column;
        if (!value.isUnboundedStart())
        {
            pstmt.setLong(index++, value.getStart());
        }
        else if (forInsert)
        {
            pstmt.setLong(index++, Long.MIN_VALUE);
        }
        if (!value.isUnboundedEnd())
        {
            pstmt.setLong(index++, value.getEnd());
        }
        else if (forInsert)
        {
            pstmt.setLong(index++, Long.MAX_VALUE);
        }
        return index;
    }
}
