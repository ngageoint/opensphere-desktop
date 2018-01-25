package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class MapAnnotationPointGroupClearedEvent.
 */
public class MapAnnotationPointGroupClearedEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /**
     * Instantiates a new data group info cleared event.
     *
     * @param group the group that was cleared.
     * @param source the source
     */
    public MapAnnotationPointGroupClearedEvent(MutableMapAnnotationPointGroup group, Object source)
    {
        super(group, source);
    }

    @Override
    public String getDescription()
    {
        return "MapAnnotationPointGroupClearedEvent";
    }
}
