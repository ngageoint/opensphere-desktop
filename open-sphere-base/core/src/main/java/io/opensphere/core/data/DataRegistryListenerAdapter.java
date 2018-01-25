package io.opensphere.core.data;

import io.opensphere.core.data.util.DataModelCategory;

/**
 * Empty implementation of {@link DataRegistryListener} for convenience.
 *
 * @param <T> The type of the property values of interest to the listener.
 */
public abstract class DataRegistryListenerAdapter<T> implements DataRegistryListener<T>
{
    @Override
    public void allValuesRemoved(Object source)
    {
    }

    @Override
    public boolean isWantingRemovedObjects()
    {
        return false;
    }

    @Override
    public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends T> newValues, Object source)
    {
    }

    @Override
    public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends T> removedValues, Object source)
    {
    }

    @Override
    public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Object source)
    {
    }

    @Override
    public void valuesUpdated(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends T> newValues, Object source)
    {
    }
}
