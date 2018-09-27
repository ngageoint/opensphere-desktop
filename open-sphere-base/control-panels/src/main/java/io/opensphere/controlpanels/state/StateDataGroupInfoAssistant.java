package io.opensphere.controlpanels.state;

import java.util.Collections;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant;

/** The data group info assistant class for states. */
public class StateDataGroupInfoAssistant extends DefaultDataGroupInfoAssistant
{
    /** The state controller. */
    private final StateController myController;

    /**
     * Constructor.
     *
     * @param controller The state controller.
     */
    public StateDataGroupInfoAssistant(StateController controller)
    {
        super();
        myController = controller;
    }

    @Override
    public boolean canDeleteGroup(DataGroupInfo dgi)
    {
        return true;
    }

    @Override
    public void deleteGroup(DataGroupInfo dgi, Object source)
    {
        if (dgi.hasMembers(false))
        {
            myController.removeStates(Collections.singleton(dgi.getId()));
        }
    }
}
