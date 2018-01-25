package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class DataGroupInfoMemberAddedEvent.
 */
public class MapAnnotationPointGroupChildAddedEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /** The added member. */
    private final MutableMapAnnotationPointGroup myAddedChild;

    /**
     * Instantiates a new MapAnnotationPointGroup child added event.
     *
     * @param addedTo the MapAnnotationPointGroup to which the child was added.
     * @param added the added child
     * @param source - the source of the event
     */
    public MapAnnotationPointGroupChildAddedEvent(MutableMapAnnotationPointGroup addedTo, MutableMapAnnotationPointGroup added,
            Object source)
    {
        super(addedTo, source);
        myAddedChild = added;
    }

    /**
     * Gets the added child.
     *
     * @return the added child
     */
    public MutableMapAnnotationPointGroup getAdded()
    {
        return myAddedChild;
    }

    @Override
    public String getDescription()
    {
        return "MapAnnotationPointGroupChildAddedEvent";
    }
}
