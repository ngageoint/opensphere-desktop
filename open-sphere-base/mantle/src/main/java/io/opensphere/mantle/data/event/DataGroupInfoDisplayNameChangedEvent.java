package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The Class DataGroupInfoMemberAddedEvent.
 */
public class DataGroupInfoDisplayNameChangedEvent extends AbstractDataGroupInfoChangeEvent
{
    /** The new id. */
    private final String myNewName;

    /** The old id. */
    private final String myOldName;

    /**
     * Instantiates a new data group info display name changed event.
     *
     * @param dgi the dgi
     * @param oldName the old id
     * @param newName the new id
     * @param source - the source of the event
     */
    public DataGroupInfoDisplayNameChangedEvent(DataGroupInfo dgi, String oldName, String newName, Object source)
    {
        super(dgi, source);
        myOldName = oldName;
        myNewName = newName;
    }

    @Override
    public String getDescription()
    {
        return "DataGroupInfoDisplayNameChangedEvent";
    }

    /**
     * Gets the new name.
     *
     * @return the new name
     */
    public String getNewName()
    {
        return myNewName;
    }

    /**
     * Gets the old name.
     *
     * @return the old name
     */
    public String getOldName()
    {
        return myOldName;
    }
}
