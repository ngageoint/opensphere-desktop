package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DataGroupInfoMemberAddedEvent.
 */
public class DataGroupInfoMemberAddedEvent extends AbstractDataGroupInfoChangeEvent
{
    /** The added member. */
    private final DataTypeInfo myAddedMember;

    /**
     * Instantiates a new data group info member added event.
     *
     * @param dgi the dgi
     * @param added the added member
     * @param source - the source of the event
     */
    public DataGroupInfoMemberAddedEvent(DataGroupInfo dgi, DataTypeInfo added, Object source)
    {
        super(dgi, source);
        myAddedMember = added;
    }

    /**
     * Gets the added member.
     *
     * @return the added member
     */
    public DataTypeInfo getAdded()
    {
        return myAddedMember;
    }

    @Override
    public String getDescription()
    {
        return "DataGroupInfoMemberAddedEvent";
    }
}
