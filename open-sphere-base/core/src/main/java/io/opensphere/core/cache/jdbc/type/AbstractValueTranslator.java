package io.opensphere.core.cache.jdbc.type;

import java.sql.ResultSet;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Abstract implementation of the {@link ValueTranslator} interface.
 *
 * @param <T> The type of Java object handled.
 */
public abstract class AbstractValueTranslator<T> implements ValueTranslator<T>
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getValue(io.opensphere.core.cache.util.PropertyDescriptor,
     *      int, java.sql.ResultSet,
     *      io.opensphere.core.cache.matcher.PropertyMatcher,
     *      java.util.Collection)
     */
    @Override
    public int getValue(PropertyDescriptor<? extends T> propertyDescriptor, int column, ResultSet rs,
            PropertyMatcher<? extends T> filter, Collection<? super T> results)
        throws CacheException
    {
        return getValue(propertyDescriptor.getType(), propertyDescriptor.getEstimatedValueSizeBytes(), column, rs, filter,
                results);
    }
}
