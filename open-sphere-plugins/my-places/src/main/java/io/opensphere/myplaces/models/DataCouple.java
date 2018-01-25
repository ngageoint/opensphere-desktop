package io.opensphere.myplaces.models;

import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Contains a data type info and its parent.
 */
public class DataCouple extends Pair<DataTypeInfo, DataGroupInfo>
{
    /**
     * Constructs a new data couple.
     *
     * @param dataType The data type.
     * @param dataGroup The data group.
     */
    public DataCouple(DataTypeInfo dataType, DataGroupInfo dataGroup)
    {
        super(dataType, dataGroup);
    }

    /**
     * Gets the data group.
     *
     * @return The data group.
     */
    public DataGroupInfo getDataGroup()
    {
        return getSecondObject();
    }

    /**
     * Gets the data type.
     *
     * @return The data type.
     */
    public DataTypeInfo getDataType()
    {
        return getFirstObject();
    }
}
