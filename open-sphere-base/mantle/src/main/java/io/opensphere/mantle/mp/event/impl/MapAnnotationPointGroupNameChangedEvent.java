package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class MapAnnotationPointGroupNameChangedEvent.
 */
public class MapAnnotationPointGroupNameChangedEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /** The new id. */
    private final String myNewGroupName;

    /** The old id. */
    private final String myOldGroupName;

    /**
     * Instantiates a new data group info display name changed event.
     *
     * @param group the group that had its name changed.
     * @param oldName the old id
     * @param newName the new id
     * @param source - the source of the event
     */
    public MapAnnotationPointGroupNameChangedEvent(MutableMapAnnotationPointGroup group, String oldName, String newName,
            Object source)
    {
        super(group, source);
        myOldGroupName = oldName;
        myNewGroupName = newName;
    }

    @Override
    public String getDescription()
    {
        return "MapAnnotationPointGroupNameChangedEvent";
    }

    /**
     * Gets the new name.
     *
     * @return the new name
     */
    public String getNewGroupName()
    {
        return myNewGroupName;
    }

    /**
     * Gets the old name.
     *
     * @return the old name
     */
    public String getOldGroupName()
    {
        return myOldGroupName;
    }
}
