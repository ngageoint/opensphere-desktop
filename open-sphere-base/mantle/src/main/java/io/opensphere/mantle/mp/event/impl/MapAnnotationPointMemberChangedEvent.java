package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.MapAnnotationPointChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class MapAnnotationPointMemberChangedEvent.
 */
public class MapAnnotationPointMemberChangedEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /** The changed member. */
    private final MapAnnotationPoint myChangedMember;

    /** The my source event. */
    private final MapAnnotationPointChangeEvent mySourceEvent;

    /**
     * Instantiates a new data group info member added event.
     *
     * @param group the group to which the point was added.
     * @param changed the changed
     * @param sourceEvent the source event
     * @param source - the source of the event
     */
    public MapAnnotationPointMemberChangedEvent(MutableMapAnnotationPointGroup group, MapAnnotationPoint changed,
            MapAnnotationPointChangeEvent sourceEvent, Object source)
    {
        super(group, source);
        myChangedMember = changed;
        mySourceEvent = sourceEvent;
    }

    /**
     * Gets the added member.
     *
     * @return the added member
     */
    public MapAnnotationPoint getChangedPoint()
    {
        return myChangedMember;
    }

    @Override
    public String getDescription()
    {
        return "MapAnnotationPointMemberChangedEvent";
    }

    /**
     * Gets the source event.
     *
     * @return the source event
     */
    public MapAnnotationPointChangeEvent getSourceEvent()
    {
        return mySourceEvent;
    }
}
