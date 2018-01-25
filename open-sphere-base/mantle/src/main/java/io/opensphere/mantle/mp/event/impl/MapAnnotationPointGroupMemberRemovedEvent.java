package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class MapAnnotationPointGroupMemberRemovedEvent.
 */
public class MapAnnotationPointGroupMemberRemovedEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /** The added member. */
    private final MutableMapAnnotationPoint myRemovedMember;

    /**
     * Instantiates a new data group info member added event.
     *
     * @param group the {@link MutableMapAnnotationPointGroup} from which the
     *            point was removed.
     * @param removed the removed member
     * @param source the source of the event
     */
    public MapAnnotationPointGroupMemberRemovedEvent(MutableMapAnnotationPointGroup group, MutableMapAnnotationPoint removed,
            Object source)
    {
        super(group, source);
        myRemovedMember = removed;
    }

    @Override
    public String getDescription()
    {
        return "MapAnnotationPointGroupMemberRemovedEvent";
    }

    /**
     * Gets the added member.
     *
     * @return the added member
     */
    public MutableMapAnnotationPoint getRemoved()
    {
        return myRemovedMember;
    }
}
