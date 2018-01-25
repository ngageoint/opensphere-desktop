package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The Class DataGroupInfoMemberAddedEvent.
 */
public class DataGroupInfoChildAddedEvent extends AbstractDataGroupInfoChangeEvent
{
    /** The added member. */
    private final DataGroupInfo myAddedChild;

    /**
     * Instantiates a new data group info child added event.
     *
     * @param dgi the dgi
     * @param added the added child
     * @param source - the source of the event
     */
    public DataGroupInfoChildAddedEvent(DataGroupInfo dgi, DataGroupInfo added, Object source)
    {
        super(dgi, source);
        myAddedChild = added;
    }

    /**
     * Gets the added child.
     *
     * @return the added child
     */
    public DataGroupInfo getAdded()
    {
        return myAddedChild;
    }

    @Override
    public String getDescription()
    {
        return "DataGroupInfoChildAddedEvent";
    }
}
