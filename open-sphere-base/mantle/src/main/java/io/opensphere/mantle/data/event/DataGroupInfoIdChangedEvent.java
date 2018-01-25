package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The Class DataGroupInfoIdChangedEvent.
 */
public class DataGroupInfoIdChangedEvent extends AbstractDataGroupInfoChangeEvent
{
    /** The new id. */
    private final String myNewId;

    /** The old id. */
    private final String myOldId;

    /**
     * Instantiates a new data group info id changed event.
     *
     * @param dgi the dgi
     * @param oldId the old id
     * @param newId the new id
     * @param source - the source of the event
     */
    public DataGroupInfoIdChangedEvent(DataGroupInfo dgi, String oldId, String newId, Object source)
    {
        super(dgi, source);
        myOldId = oldId;
        myNewId = newId;
    }

    @Override
    public String getDescription()
    {
        return "DataGroupInfoIdChangedEvent";
    }

    /**
     * Gets the new id.
     *
     * @return the new id
     */
    public String getNewId()
    {
        return myNewId;
    }

    /**
     * Gets the old id.
     *
     * @return the old id
     */
    public String getOldId()
    {
        return myOldId;
    }
}
