package io.opensphere.auxiliary.cache.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.ClassProvider;
import io.opensphere.core.cache.jdbc.TypeMapper;
import io.opensphere.core.cache.jdbc.type.AbstractValueTranslator;
import io.opensphere.core.cache.jdbc.type.ValueTranslator;
import io.opensphere.core.cache.matcher.PropertyMatcher;

/**
 * Specialization of an {@link TypeMapper} that inserts objects using
 * {@link PreparedStatement#setObject(int, Object)} rather than serializing the
 * object before inserting it. This allows H2 to perform the serialization,
 * which is faster.
 */
public class H2TypeMapper extends TypeMapper
{
    /**
     * The class provider.
     */
    @Nullable
    private ClassProvider myProvider;

    /** A prepared statement setter for serializable values. */
    private final ValueTranslator<Serializable> myTranslator = new AbstractValueTranslator<Serializable>()
    {
        @Override
        public Class<Serializable> getType()
        {
            return Serializable.class;
        }

        @Override
        public int getValue(Class<? extends Serializable> type, long sizeBytes, int column, ResultSet rs,
                PropertyMatcher<? extends Serializable> filter, Collection<? super Serializable> results)
            throws CacheException
        {
            try
            {
                InputStream stream = rs.getBinaryStream(column);
                try (CustomObjectInputStream objectStream = new CustomObjectInputStream(myProvider, stream))
                {
                    Object object = objectStream.readObject();
                    if (filter == null || filter.matches(object))
                    {
                        results.add(type.isInstance(object) ? (Serializable)object : null);
                    }
                }
            }
            catch (SQLException | IOException | ClassNotFoundException e)
            {
                throw new CacheException("Cannot retrieve object from result set: " + e, e);
            }
            return column + 1;
        }

        @Override
        public int setValue(PreparedStatement pstmt, int column, Serializable value, boolean forInsert)
            throws CacheException, SQLException
        {
            try
            {
                pstmt.setObject(column, value, Types.JAVA_OBJECT);
                return column + 1;
            }
            catch (SQLException e)
            {
                throw new CacheException("Cannot insert object [" + value + "] into prepared statement: " + e, e);
            }
        }
    };

    @Override
    public String getSqlType(Class<?> valueType)
    {
        if (valueType.equals(Serializable.class))
        {
            // H2 uses the "other" type for serializable Java objects.
            return "other";
        }
        else
        {
            return super.getSqlType(valueType);
        }
    }

    /**
     * Sets the class provider.
     *
     * @param classProvider The class provider to use when the system class
     *            loader fails to load a class.
     */
    public void setClassProvider(@Nullable ClassProvider classProvider)
    {
        myProvider = classProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> ValueTranslator<? super T> getValueTranslator(Class<T> type)
    {
        ValueTranslator<? super T> valueTranslator = super.getValueTranslator(type);
        if (valueTranslator != null && valueTranslator.getType().equals(myTranslator.getType()))
        {
            valueTranslator = (ValueTranslator<? super T>)myTranslator;
        }
        return valueTranslator;
    }
}
