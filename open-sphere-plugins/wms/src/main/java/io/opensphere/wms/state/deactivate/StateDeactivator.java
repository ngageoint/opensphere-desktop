package io.opensphere.wms.state.deactivate;

import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DataGroupActivator;
import io.opensphere.wms.state.model.StateGroup;

/**
 * Deactivates certain states.
 */
public class StateDeactivator
{
    /** The data group activator. */
    private final DataGroupActivator myActivator;

    /**
     * Constructs a new state deactivator.
     *
     * @param activator The data group activator.
     * @param dataGroupController The data group controller.
     */
    public StateDeactivator(DataGroupActivator activator)
    {
        myActivator = Utilities.checkNull(activator, "activator");
    }

    /**
     * Deactivates the state.
     *
     * @param stateGroup The state group to deactivate.
     * @throws InterruptedException If the thread is interrupted.
     */
    public void deactivateState(StateGroup stateGroup) throws InterruptedException
    {
        List<DataGroupInfo> stateLayers = stateGroup.getStateLayers();
        myActivator.setGroupsActive(stateLayers, false);
    }
}
