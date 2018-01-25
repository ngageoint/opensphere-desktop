package io.opensphere.mantle.controller.event.impl;

import io.opensphere.mantle.controller.event.AbstractRootDataGroupControllerEvent;
import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Signals that a the tree structure of a root group node has changed.
 */
public class RootDataGroupStructureChangeEvent extends AbstractRootDataGroupControllerEvent
{
    /**
     * Indicates that the node structure has changed for the indicated root
     * group instigated by a child somewhere in this root's hierarchy.
     *
     * @param rootGroup the root group that has changed
     * @param originEvent the event that caused this event to be generated ( or
     *            null if none)
     * @param source - the source of the event ( object that caused the event to
     *            be generated )
     */
    public RootDataGroupStructureChangeEvent(DataGroupInfo rootGroup, AbstractDataGroupInfoChangeEvent originEvent, Object source)
    {
        super(rootGroup, originEvent, source);
    }

    @Override
    public String getDescription()
    {
        return "RootDataGroupStructureChangeEvent";
    }
}
