package io.opensphere.wfs.state.model;

import java.util.Collection;
import java.util.List;

import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Keeps track of the set of WFS data types associated with a state id.
 */
public class WFSStateGroup
{
    /** The State id. */
    private final String myStateId;

    /** The Data groups. */
    private final List<DataGroupInfo> myDataGroups;

    /** The Layer states. */
    private final Collection<? extends WFSLayerState> myLayerStates;

    /**
     * Instantiates a new wFS state group.
     *
     * @param id the id
     * @param toActivate the types
     * @param states the states
     */
    public WFSStateGroup(String id, List<DataGroupInfo> toActivate, Collection<? extends WFSLayerState> states)
    {
        myStateId = id;
        myDataGroups = toActivate;
        myLayerStates = states;
    }

    /**
     * Gets the data groups.
     *
     * @return the data groups
     */
    public List<DataGroupInfo> getDataGroups()
    {
        return myDataGroups;
    }

    /**
     * Gets the layer states.
     *
     * @return the layer states
     */
    public Collection<? extends WFSLayerState> getLayerStates()
    {
        return myLayerStates;
    }

    /**
     * Gets the state id.
     *
     * @return the state id
     */
    public String getStateId()
    {
        return myStateId;
    }
}
