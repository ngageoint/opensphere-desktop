package io.opensphere.mantle.data;

import java.util.Collection;

import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;

/**
 * A marker interface to provide a common base for {@link DataGroupContextKey}
 * and {@link MultiDataGroupContextKey} to implement.
 */
public interface ContextKey
{
    /**
     * Get the dataGroups.
     *
     * @return the dataGroups
     */
    Collection<DataGroupInfo> getDataGroups();

    /**
     * Get the dataTypes.
     *
     * @return the dataTypes
     */
    Collection<DataTypeInfo> getDataTypes();
}
