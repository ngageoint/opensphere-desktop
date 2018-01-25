package io.opensphere.mantle.controller.event.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The Class ActiveDataGroupsChangedEvent.
 */
public class ActiveDataGroupsChangedEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The activated groups. */
    private final Set<DataGroupInfo> myActivatedGroups;

    /** The deactivated groups. */
    private final Set<DataGroupInfo> myDeactivatedGroups;

    /** The source. */
    private final Object mySource;

    /**
     * Instantiates a new active data groups changed event.
     *
     * @param source the source
     * @param activatedGroups the activated groups
     * @param deactivatedGroups the deactivated groups
     */
    public ActiveDataGroupsChangedEvent(Object source, Set<DataGroupInfo> activatedGroups, Set<DataGroupInfo> deactivatedGroups)
    {
        mySource = source;
        myActivatedGroups = activatedGroups == null ? Collections.<DataGroupInfo>emptySet()
                : Collections.unmodifiableSet(new HashSet<DataGroupInfo>(activatedGroups));
        myDeactivatedGroups = deactivatedGroups == null ? Collections.<DataGroupInfo>emptySet()
                : Collections.unmodifiableSet(new HashSet<DataGroupInfo>(deactivatedGroups));
    }

    /**
     * Gets the activated groups.
     *
     * @return the activated groups
     */
    public Set<DataGroupInfo> getActivatedGroups()
    {
        return myActivatedGroups;
    }

    /**
     * Gets the deactivated groups.
     *
     * @return the deactivated groups
     */
    public Set<DataGroupInfo> getDeactivatedGroups()
    {
        return myDeactivatedGroups;
    }

    @Override
    public String getDescription()
    {
        return "ActiveDataGroupsChangedEvent";
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }
}
