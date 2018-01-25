package io.opensphere.core.cache;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;

/**
 * A {@link CacheDeposit} that simply deposits some un-serializable objects with
 * no keys. Objects deposited will not be persisted beyond the current session.
 *
 * @param <T> The type of the objects.
 */
public class SimpleSessionOnlyCacheDeposit<T> extends DefaultCacheDeposit<T>
{
    /**
     * Construct the deposit.
     *
     * @param category The data model category.
     * @param propertyDescriptor The property descriptor.
     * @param input The input objects.
     */
    public SimpleSessionOnlyCacheDeposit(DataModelCategory category, PropertyDescriptor<T> propertyDescriptor,
            Collection<? extends T> input)
    {
        super(category, Collections.singleton(UnserializableAccessor.getHomogeneousAccessor(propertyDescriptor)), input, true,
                CacheDeposit.SESSION_END, true);
    }
}
