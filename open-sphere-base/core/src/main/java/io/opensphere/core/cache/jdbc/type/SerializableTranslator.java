package io.opensphere.core.cache.jdbc.type;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.util.lang.Serialization;

/**
 * A translator used to handle serialized Java objects.
 */
public class SerializableTranslator extends AbstractValueTranslator<Serializable>
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getType()
     */
    @Override
    public Class<Serializable> getType()
    {
        return Serializable.class;
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
    public int getValue(Class<? extends Serializable> type, long sizeBytes, int column, ResultSet rs,
            PropertyMatcher<? extends Serializable> filter, Collection<? super Serializable> results) throws CacheException
    {
        try
        {
            Object object = Serialization.deserialize(rs.getBytes(column));
            if (filter == null || filter.matches(object))
            {
                results.add(type.isInstance(object) ? (Serializable)object : null);
            }
            return column + 1;
        }
        catch (IOException e)
        {
            throw new CacheException("Failed to deserialize object: " + e, e);
        }
        catch (ClassNotFoundException e)
        {
            throw new CacheException("Failed to find class for object: " + e, e);
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to get value from result set: " + e, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#setValue(java.sql.PreparedStatement,
     *      int, java.lang.Object, boolean)
     */
    @Override
    public int setValue(PreparedStatement pstmt, int column, Serializable value, boolean forInsert)
            throws CacheException, SQLException
    {
        try
        {
            pstmt.setBytes(column, Serialization.serialize(value));
            return column + 1;
        }
        catch (IOException e)
        {
            throw new CacheException("Cannot insert object into prepared statement: " + e, e);
        }
    }
}
