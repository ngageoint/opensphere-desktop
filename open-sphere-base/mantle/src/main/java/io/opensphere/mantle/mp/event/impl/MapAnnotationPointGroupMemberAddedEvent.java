package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class MapAnnotationPointGroupMemberAddedEvent.
 */
public class MapAnnotationPointGroupMemberAddedEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /** The added member. */
    private final MutableMapAnnotationPoint myAddedMember;

    /**
     * Instantiates a new data group info member added event.
     *
     * @param group the group to which the point was added.
     * @param added the added member
     * @param source - the source of the event
     */
    public MapAnnotationPointGroupMemberAddedEvent(MutableMapAnnotationPointGroup group, MutableMapAnnotationPoint added,
            Object source)
    {
        super(group, source);
        myAddedMember = added;
    }

    /**
     * Gets the added member.
     *
     * @return the added member
     */
    public MutableMapAnnotationPoint getAdded()
    {
        return myAddedMember;
    }

    @Override
    public String getDescription()
    {
        return "DataGroupInfoMemberAddedEvent";
    }
}
