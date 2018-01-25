package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class MapAnnotationPointGroupMembersClearedEvent.
 */
public class MapAnnotationPointGroupMembersClearedEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /**
     * Instantiates a new map annotation point group members cleared event.
     *
     * @param clearedGroup the {@link MutableMapAnnotationPointGroup} that was
     *            cleared.
     * @param source the source
     */
    public MapAnnotationPointGroupMembersClearedEvent(MutableMapAnnotationPointGroup clearedGroup, Object source)
    {
        super(clearedGroup, source);
    }

    @Override
    public String getDescription()
    {
        return "MapAnnotationPointGroupMembersClearedEvent";
    }
}
