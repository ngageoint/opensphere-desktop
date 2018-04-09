package io.opensphere.core.cache.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Interface for objects that can set values in prepared statements and retrieve
 * values from result sets.
 *
 * @param <T> The type of Java object handled.
 */
public interface ValueTranslator<T>
{
    /**
     * Get the type of object handled by this retriever.
     *
     * @return The Java type.
     */
    Class<T> getType();

    /**
     * Get a value from the result set.
     *
     * @param type The type of the property value being retrieved.
     * @param sizeBytes The estimated size of the property value, or <tt>-1</tt>
     *            if the size is unknown.
     * @param column The column index for the result set.
     * @param rs The result set.
     * @param filter Optional filter on the results.
     * @param results The return collection of results.
     * @return The new column index.
     * @throws CacheException If the value cannot be retrieved.
     */
    int getValue(Class<? extends T> type, long sizeBytes, int column, ResultSet rs, PropertyMatcher<? extends T> filter,
            Collection<? super T> results)
        throws CacheException;

    /**
     * Get a value from the result set.
     *
     * @param propertyDescriptor A description of the property being retrieved.
     * @param column The column index for the result set.
     * @param rs The result set.
     * @param filter Optional filter on the results.
     * @param results The return collection of results.
     * @return The new column index.
     * @throws CacheException If the value cannot be retrieved.
     */
    int getValue(PropertyDescriptor<? extends T> propertyDescriptor, int column, ResultSet rs,
            PropertyMatcher<? extends T> filter, Collection<? super T> results)
        throws CacheException;

    /**
     * Set a value in a statement.
     *
     * @param pstmt The statement.
     * @param column The column index.
     * @param value The value to set.
     * @param forInsert If this statement is for an insert.
     * @return The new column index.
     * @throws CacheException If there is a database error.
     * @throws SQLException If there is a database error.
     */
    int setValue(PreparedStatement pstmt, int column, T value, boolean forInsert) throws CacheException, SQLException;
}
