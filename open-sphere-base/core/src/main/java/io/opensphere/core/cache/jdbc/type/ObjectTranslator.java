package io.opensphere.core.cache.jdbc.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.PropertyMatcher;

/**
 * A translator implementation for handling generic objects.
 */
public class ObjectTranslator extends AbstractValueTranslator<Object>
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getType()
     */
    @Override
    public Class<Object> getType()
    {
        return Object.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getValue(Class,
     *      long, int, ResultSet, PropertyMatcher, Collection)
     */
    @Override
    public int getValue(Class<? extends Object> type, long sizeBytes, int column, ResultSet rs,
            PropertyMatcher<? extends Object> filter, Collection<Object> results)
                    throws CacheException
    {
        try
        {
            Object object = rs.getObject(column);
            if (filter == null || filter.matches(object))
            {
                results.add(type.isInstance(object) ? (Serializable)object : null);
            }
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to get object from result set: " + e, e);
        }
        return column + 1;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#setValue(java.sql.PreparedStatement,
     *      int, java.lang.Object, boolean)
     */
    @Override
    public int setValue(PreparedStatement pstmt, int column, Object value, boolean forInsert) throws SQLException
    {
        pstmt.setObject(column, value);
        return column + 1;
    }
}
