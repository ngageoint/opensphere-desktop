package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DataGroupInfoMemberAddedEvent.
 */
public class DataGroupInfoMemberRemovedEvent extends AbstractDataGroupInfoChangeEvent
{
    /** The added member. */
    private final DataTypeInfo myReovedMember;

    /**
     * Instantiates a new data group info member added event.
     *
     * @param dgi the dgi
     * @param removed the removed member
     * @param source the source of the event
     */
    public DataGroupInfoMemberRemovedEvent(DataGroupInfo dgi, DataTypeInfo removed, Object source)
    {
        super(dgi, source);
        myReovedMember = removed;
    }

    @Override
    public String getDescription()
    {
        return "DataGroupInfoMemberAddedEvent";
    }

    /**
     * Gets the added member.
     *
     * @return the added member
     */
    public DataTypeInfo getRemoved()
    {
        return myReovedMember;
    }
}
