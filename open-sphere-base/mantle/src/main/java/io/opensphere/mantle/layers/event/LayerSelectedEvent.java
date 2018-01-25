package io.opensphere.mantle.layers.event;

import java.util.Collection;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class LayerSelectedEvent.
 */
public class LayerSelectedEvent extends AbstractSingleStateEvent
{
    /** The data groups. */
    private final Collection<DataGroupInfo> myDataGroups;

    /** The data types. */
    private final Collection<DataTypeInfo> myDataTypes;

    /** The Is selected. */
    private final boolean myIsSelected;

    /**
     * Instantiates a new layer selected event.
     *
     * @param isSelected the is selected
     * @param dataGroups the data groups
     * @param dataTypes the data types
     */
    public LayerSelectedEvent(boolean isSelected, Collection<DataGroupInfo> dataGroups, Collection<DataTypeInfo> dataTypes)
    {
        myIsSelected = isSelected;
        myDataGroups = dataGroups;
        myDataTypes = dataTypes;
    }

    /**
     * Gets the data group info.
     *
     * @return the data group info
     */
    public DataGroupInfo getDataGroupInfo()
    {
        return CollectionUtilities.hasContent(myDataGroups) ? myDataGroups.iterator().next() : null;
    }

    /**
     * Gets the data group infos.
     *
     * @return the data group infos
     */
    public Collection<DataGroupInfo> getDataGroupInfos()
    {
        return myDataGroups;
    }

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    public DataTypeInfo getDataTypeInfo()
    {
        return CollectionUtilities.hasContent(myDataTypes) ? myDataTypes.iterator().next() : null;
    }

    /**
     * Gets the data type infos.
     *
     * @return the data type infos
     */
    public Collection<DataTypeInfo> getDataTypeInfos()
    {
        return myDataTypes;
    }

    @Override
    public String getDescription()
    {
        return "An event indicating that a base layer has been activated or deactivated.";
    }

    /**
     * Checks if is selected.
     *
     * @return true, if is selected
     */
    public boolean isBaseLayerSelected()
    {
        return myIsSelected;
    }
}
