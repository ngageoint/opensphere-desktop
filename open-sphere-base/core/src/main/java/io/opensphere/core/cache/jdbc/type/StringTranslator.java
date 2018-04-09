package io.opensphere.core.cache.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.PropertyMatcher;

/**
 * A translator implementation used to handle generic {@link String} objects.
 */
public class StringTranslator extends AbstractValueTranslator<String>
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getType()
     */
    @Override
    public Class<String> getType()
    {
        return String.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getValue(Class,
     *      long, int, ResultSet, PropertyMatcher, Collection)
     */
    @Override
    public int getValue(Class<? extends String> type, long sizeBytes, int column, ResultSet rs,
            PropertyMatcher<? extends String> filter, Collection<? super String> results)
        throws CacheException
    {
        try
        {
            String object = rs.getString(column);
            if (filter == null || filter.matches(object))
            {
                results.add(object);
            }
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to get string value from result set: " + e, e);
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
    public int setValue(PreparedStatement pstmt, int column, String value, boolean forInsert) throws SQLException
    {
        pstmt.setString(column, value);
        return column + 1;
    }
}
