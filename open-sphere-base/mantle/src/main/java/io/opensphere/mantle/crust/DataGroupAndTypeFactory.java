package io.opensphere.mantle.crust;

import java.util.function.Consumer;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/**
 * Creates new instances of {@link DefaultDataGroupInfo} and
 * {@link DefaultDataTypeInfo} or child classes of those types.
 */
public interface DataGroupAndTypeFactory
{
    /**
     * Creates a new data group.
     *
     * @param toolbox The system toolbox.
     * @param providerType The provider type.
     * @param folderName The name of the group.
     * @param deleteListener An object wanting notification when the group is
     *            deleted. This can be null if the layer can not be deleted by
     *            the user.
     * @return A newly created {@link DefaultDataGroupInfo}.
     */
    DefaultDataGroupInfo createGroup(Toolbox toolbox, String providerType, String folderName,
            Consumer<DataGroupInfo> deleteListener);

    /**
     * Creates a new data type.
     *
     * @param toolbox The system toolbox.
     * @param providerType The provider type.
     * @param id The id of the layer.
     * @param typeName The semi unique name for the layer.
     * @param layerName The user facing layer name.
     * @return A newly created {@link DefaultDataTypeInfo}.
     */
    DefaultDataTypeInfo createType(Toolbox toolbox, String providerType, String id, String typeName, String layerName);
}
