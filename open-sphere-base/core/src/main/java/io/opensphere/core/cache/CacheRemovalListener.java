package io.opensphere.core.cache;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;

/**
 * Interface for objects interested in cache removals.
 */
public interface CacheRemovalListener
{
    /**
     * Method called when values are removed from the cache.
     *
     * @param dmc The concrete data model category for the values.
     * @param ids The combined ids of the models.
     */
    void valuesRemoved(DataModelCategory dmc, long[] ids);

    /**
     * Method called for each property when values are removed from the cache.
     * This method may not be called by certain cache implementations.
     *
     * @param <T> The type of the property.
     * @param dmc The concrete data model category for the values.
     * @param ids The combined ids of the models.
     * @param propertyDescriptor The descriptor for the property.
     * @param values The removed values.
     */
    <T> void valuesRemoved(DataModelCategory dmc, long[] ids, PropertyDescriptor<T> propertyDescriptor,
            Iterable<? extends T> values);
}
