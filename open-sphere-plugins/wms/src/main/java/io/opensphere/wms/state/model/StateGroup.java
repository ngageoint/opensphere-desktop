package io.opensphere.wms.state.model;

import java.util.List;

import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Contains a state id and a list of layers that were restored and activated
 * from that state.
 */
public class StateGroup
{
    /**
     * The id of the state the layers were saved in.
     */
    private final String myStateId;

    /**
     * The layers that were activated belonging to the state.
     */
    private final List<DataGroupInfo> myStateLayers;

    /**
     * Constructs a new state group.
     *
     * @param stateId The id of the state the layers were restored from.
     * @param stateLayers The layers that were restored and activated.
     */
    public StateGroup(String stateId, List<DataGroupInfo> stateLayers)
    {
        myStateId = stateId;
        myStateLayers = stateLayers;
    }

    /**
     * Gets the id of the state the layers were restored from.
     *
     * @return The state id.
     */
    public String getStateId()
    {
        return myStateId;
    }

    /**
     * Gets the restored and activated layers.
     *
     * @return The activated layers.
     */
    public List<DataGroupInfo> getStateLayers()
    {
        return myStateLayers;
    }
}
