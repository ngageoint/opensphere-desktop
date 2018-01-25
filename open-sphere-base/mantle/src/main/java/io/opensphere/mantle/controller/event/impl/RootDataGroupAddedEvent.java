package io.opensphere.mantle.controller.event.impl;

import io.opensphere.mantle.controller.event.AbstractRootDataGroupControllerEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Signals that a new root data group has been added to the controller.
 */
public class RootDataGroupAddedEvent extends AbstractRootDataGroupControllerEvent
{
    /**
     * Instantiates a new RootDataGroupAddedEvent.
     *
     * @param addedRoot the root group that has been added
     * @param source - the source of the event ( object that caused the event to
     *            be generated )
     */
    public RootDataGroupAddedEvent(DataGroupInfo addedRoot, Object source)
    {
        super(addedRoot, null, source);
    }

    @Override
    public String getDescription()
    {
        return "RootDataGroupAddedEvent";
    }
}
