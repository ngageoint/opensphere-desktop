package io.opensphere.core.cache.jdbc.type;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.PropertyMatcher;

/**
 * An implementation of the
 * {@link io.opensphere.core.cache.jdbc.type.ValueTranslator} interface for
 * use in translating {@link InputStream}s.
 */
public class InputStreamTranslator extends AbstractValueTranslator<InputStream>
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getType()
     */
    @Override
    public Class<InputStream> getType()
    {
        return InputStream.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getValue(java.lang.Class,
     *      long, int, java.sql.ResultSet,
     *      io.opensphere.core.cache.matcher.PropertyMatcher,
     *      java.util.Collection)
     */
    @Override
    public int getValue(Class<? extends InputStream> type, long sizeBytes, int column, ResultSet rs,
            PropertyMatcher<? extends InputStream> filter, Collection<? super InputStream> results) throws CacheException
    {
        try (InputStream object = rs.getBinaryStream(column))
        {
            if (filter == null || filter.matches(object))
            {
                results.add(type.isInstance(object) ? object : null);
            }
            return column + 1;
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to get input stream from result set.", e);
        }
        catch (IOException e)
        {
            throw new CacheException("Failed to get input stream from result set, unable to close stream for column " + column,
                    e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#setValue(java.sql.PreparedStatement,
     *      int, java.lang.Object, boolean)
     */
    @Override
    public int setValue(PreparedStatement pstmt, int column, InputStream value, boolean forInsert)
            throws CacheException, SQLException
    {
        pstmt.setBinaryStream(column, value);
        return column + 1;
    }
}
