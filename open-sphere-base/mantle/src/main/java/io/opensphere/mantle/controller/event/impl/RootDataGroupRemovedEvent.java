package io.opensphere.mantle.controller.event.impl;

import io.opensphere.mantle.controller.event.AbstractRootDataGroupControllerEvent;
import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Signals that a root data group has been removed from the controller.
 */
public class RootDataGroupRemovedEvent extends AbstractRootDataGroupControllerEvent
{
    /**
     * Instantiates a new RootDataGroupAddedEvent.
     *
     * @param removedRoot the root group that has been removed
     * @param originEvent the event that caused this event to be generated ( or
     *            null if none)
     * @param source - the source of the event ( object that caused the event to
     *            be generated )
     */
    public RootDataGroupRemovedEvent(DataGroupInfo removedRoot, AbstractDataGroupInfoChangeEvent originEvent, Object source)
    {
        super(removedRoot, originEvent, source);
    }

    @Override
    public String getDescription()
    {
        return "RootDataGroupRemovedEvent";
    }
}
