package io.opensphere.mantle.data;

import io.opensphere.core.util.Service;

/**
 * Helper method for managing the Z-order of a {@link DataTypeInfo}.
 */
public interface DataTypeInfoOrderManager extends Service
{
    /**
     * Add a data type to the order manager and set its order.
     *
     * @param type The data type info.
     */
    void activateParticipant(DataTypeInfo type);

    /**
     * Deactivate a data type in the order manager. Remember the order of this
     * type in case it's reactivated.
     *
     * @param type The data type info.
     */
    void deactivateParticipant(DataTypeInfo type);

    /**
     * Remove a data type from the order manager permanently. This should not be
     * used if the same data type might be re-added.
     *
     * @param type The data type info.
     */
    void expungeDataType(DataTypeInfo type);
}
