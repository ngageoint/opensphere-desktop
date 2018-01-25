package io.opensphere.mantle.data;

import java.util.Set;

/**
 * The Interface GroupInfoTreeNodeData.
 */
public interface GroupInfoTreeNodeData
{
    /**
     * Gets the {@link DataGroupInfo} for this node.
     *
     * @return the {@link DataGroupInfo}
     */
    DataGroupInfo getDataGroupInfo();

    /**
     * Gets the {@link Set} of {@link DataTypeInfo} for this node.
     *
     * @return the data types
     */
    Set<DataTypeInfo> getDataTypes();

    /**
     * Gets the display name of the data group ( also returned by toString() ).
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Gets the id of the data group.
     *
     * @return the id
     */
    String getId();

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    String toString();
}
