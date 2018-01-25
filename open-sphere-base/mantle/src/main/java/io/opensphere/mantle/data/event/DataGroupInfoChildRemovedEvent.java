package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The Class DataGroupInfoMemberAddedEvent.
 */
public class DataGroupInfoChildRemovedEvent extends AbstractDataGroupInfoChangeEvent
{
    /** The added member. */
    private final DataGroupInfo myRemovedChild;

    /**
     * Indicates if the removed data group should stay active.
     */
    private final boolean myKeepActive;

    /**
     * Instantiates a new data group info child added event.
     *
     * @param dgi the dgi
     * @param removed the added child
     * @param source - the source of the event
     */
    public DataGroupInfoChildRemovedEvent(DataGroupInfo dgi, DataGroupInfo removed, Object source)
    {
        this(dgi, removed, source, false);
    }

    /**
     * Instantiates a new data group info child added event.
     *
     * @param dgi the dgi
     * @param removed the added child
     * @param source - the source of the event
     * @param keepActive True if the removed child should stay active, false
     *            otherwise.
     */
    public DataGroupInfoChildRemovedEvent(DataGroupInfo dgi, DataGroupInfo removed, Object source, boolean keepActive)
    {
        super(dgi, source);
        myRemovedChild = removed;
        myKeepActive = keepActive;
    }

    @Override
    public String getDescription()
    {
        return "DataGroupInfoChildRemovedEvent";
    }

    /**
     * Gets the removed child.
     *
     * @return the removed child
     */
    public DataGroupInfo getRemoved()
    {
        return myRemovedChild;
    }

    /**
     * Indicates if the remove event should not trigger a state change.
     *
     * @return True if no state change is required, false otherwise.
     */
    public boolean isMyKeepActive()
    {
        return myKeepActive;
    }
}
