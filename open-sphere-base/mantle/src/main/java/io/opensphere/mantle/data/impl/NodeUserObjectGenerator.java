package io.opensphere.mantle.data.impl;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Interface NodeUserObjectGenerator.
 */
public interface NodeUserObjectGenerator
{
    /**
     * Creates the node user object for a data group.
     *
     * @param dgi the {@link DataGroupInfo}
     * @return the object
     */
    GroupByNodeUserObject createNodeUserObject(DataGroupInfo dgi);

    /**
     * Creates the node user object for an individual data type info with links
     * for its group.
     *
     * @param dgi the {@link DataGroupInfo}
     * @param dti the {@link DataTypeInfo}
     * @return the object
     */
    GroupByNodeUserObject createNodeUserObject(DataGroupInfo dgi, DataTypeInfo dti);

    /**
     * Creates the node user object with no reference to a data group or data
     * type but is an organization node only.
     *
     * @param label the label
     * @return the object
     */
    GroupByNodeUserObject createNodeUserObject(String label);
}
