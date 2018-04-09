package io.opensphere.core.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;

/**
 * A {@link CacheDeposit} that simply deposits some serializable objects with no
 * keys.
 *
 * @param <T> The type of the objects being deposited.
 */
public class SimplePersistentCacheDeposit<T extends Serializable> extends DefaultCacheDeposit<T>
{
    /**
     * Construct the deposit.
     *
     * @param category The data model category.
     * @param propertyDescriptor The property descriptor.
     * @param input The input objects.
     * @param expiration The expiration date for the objects.
     */
    public SimplePersistentCacheDeposit(DataModelCategory category, PropertyDescriptor<T> propertyDescriptor,
            Collection<? extends T> input, Date expiration)
    {
        super(category, Collections.singleton(SerializableAccessor.getHomogeneousAccessor(propertyDescriptor)), input, true,
                expiration, false);
    }

    /**
     * Construct the deposit.
     *
     * @param category The data model category.
     * @param propertyDescriptor The property descriptor.
     * @param input The input objects.
     * @param isNew True if this deposit is a new insert, false if its an
     *            update.
     * @param expiration The expiration date for the objects.
     */
    public SimplePersistentCacheDeposit(DataModelCategory category, PropertyDescriptor<T> propertyDescriptor,
            Collection<? extends T> input, boolean isNew, Date expiration)
    {
        super(category, Collections.singleton(SerializableAccessor.getHomogeneousAccessor(propertyDescriptor)), input, isNew,
                expiration, false);
    }
}
