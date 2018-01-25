package io.opensphere.mantle.data.impl;

import java.util.Collections;
import java.util.Set;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.GroupInfoTreeNodeData;

/**
 * Default implementation of GroupInfoTreeNodeData.
 */
public class DefaultGroupInfoTreeNodeData implements GroupInfoTreeNodeData
{
    /** The Data group info. */
    private final DataGroupInfo myDataGroupInfo;

    /** The data types. */
    private final Set<DataTypeInfo> myDataTypes;

    /** The display name. */
    private final String myDisplayName;

    /** The id. */
    private final String myId;

    /**
     * Instantiates a new default group info tree node data.
     *
     * @param id the id
     * @param displayName the display name
     * @param dataTypes the data types
     * @param dgi the {@link DataGroupInfo}
     */
    public DefaultGroupInfoTreeNodeData(String id, String displayName, Set<DataTypeInfo> dataTypes, DataGroupInfo dgi)
    {
        myDataTypes = dataTypes;
        myId = id;
        myDisplayName = displayName;
        myDataGroupInfo = dgi;
    }

    @Override
    public DataGroupInfo getDataGroupInfo()
    {
        return myDataGroupInfo;
    }

    @Override
    public Set<DataTypeInfo> getDataTypes()
    {
        return Collections.unmodifiableSet(myDataTypes);
    }

    @Override
    public String getDisplayName()
    {
        return myDisplayName;
    }

    @Override
    public String getId()
    {
        return myId;
    }

    @Override
    public String toString()
    {
        return myDisplayName;
    }
}
