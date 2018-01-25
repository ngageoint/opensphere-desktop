package io.opensphere.mantle.controller.event;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * The Class ActiveGroupSavedSetsChangedEvent.
 */
public class ActiveDataGroupSavedSetsChangedEvent extends AbstractSingleStateEvent
{
    /** The set name. */
    private final String mySetName;

    /** The type. */
    private final ChangeType myType;

    /**
     * Instantiates a new active group saved sets changed event.
     *
     * @param setName the set name
     * @param type the type
     */
    public ActiveDataGroupSavedSetsChangedEvent(String setName, ChangeType type)
    {
        mySetName = setName;
        myType = type;
    }

    @Override
    public String getDescription()
    {
        return "Saved active group sets changed";
    }

    /**
     * Gets the sets the name.
     *
     * @return the sets the name
     */
    public String getSetName()
    {
        return mySetName;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public ChangeType getType()
    {
        return myType;
    }

    /**
     * The Enum Type.
     */
    public enum ChangeType
    {
        /** The ADD. */
        ADD,

        /** The CHANGED. */
        CHANGED,

        /** The REMOVE. */
        REMOVE
    }
}
