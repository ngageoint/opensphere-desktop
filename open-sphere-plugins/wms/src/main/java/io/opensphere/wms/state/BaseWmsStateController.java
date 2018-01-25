package io.opensphere.wms.state;

import java.util.Collection;
import java.util.Map;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Plugin;
import io.opensphere.core.Toolbox;
import io.opensphere.core.modulestate.AbstractModuleStateController;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.impl.DefaultDataGroupActivator;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.wms.state.activate.StateActivator;
import io.opensphere.wms.state.deactivate.StateDeactivator;
import io.opensphere.wms.state.model.StateGroup;
import io.opensphere.wms.state.save.StateSaver;

/**
 * Base class for WMS state controller and contains the common functionality
 * between the controllers.
 */
public abstract class BaseWmsStateController extends AbstractModuleStateController
{
    /**
     * Used to save layer states.
     */
    private final StateSaver mySaver;

    /**
     * Activates saved layer states.
     */
    private final StateActivator myActivator;

    /**
     * Deactivates an activated state.
     */
    private final StateDeactivator myDeactivator;

    /**
     * The map of state id's to active state groups.
     */
    private final Map<String, StateGroup> myActiveStates = New.map();

    /**
     * Constructs a new WMS state controller.
     *
     * @param wmsPlugin The wms plugin used to get the get capabilities envoys.
     * @param toolbox The system toolbox.
     */
    public BaseWmsStateController(Plugin wmsPlugin, Toolbox toolbox)
    {
        mySaver = new StateSaver(toolbox);
        myActivator = new StateActivator(wmsPlugin, toolbox);
        myDeactivator = new StateDeactivator(new DefaultDataGroupActivator(toolbox.getEventManager()),
                MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController());
    }

    @Override
    public void activateState(final String id, String description, final Collection<? extends String> tags, final Node node)
        throws InterruptedException
    {
        StateGroup stateGroup = myActivator.activateState(id, node, isDataLayer(), tags);
        synchronized (myActiveStates)
        {
            myActiveStates.put(stateGroup.getStateId(), stateGroup);
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
        throws InterruptedException
    {
        StateGroup stateGroup = myActivator.activateState(id, state, isDataLayer(), tags);
        synchronized (myActiveStates)
        {
            myActiveStates.put(stateGroup.getStateId(), stateGroup);
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        return myActivator.canActivate(node, isDataLayer());
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return myActivator.canActivate(state, isDataLayer());
    }

    @Override
    public void deactivateState(String id, Node node) throws InterruptedException
    {
        StateGroup stateGroup = null;
        synchronized (myActiveStates)
        {
            stateGroup = myActiveStates.get(id);
        }

        if (stateGroup != null)
        {
            myDeactivator.deactivateState(stateGroup);
        }

        synchronized (myActiveStates)
        {
            myActiveStates.remove(id);
        }
    }

    @Override
    public void deactivateState(String id, StateType state) throws InterruptedException
    {
        deactivateState(id, (Node)null);
    }

    @Override
    public void saveState(Node node)
    {
        mySaver.saveState(node, isDataLayer());
    }

    @Override
    public void saveState(StateType state)
    {
        mySaver.saveState(state, isDataLayer());
    }

    /**
     * Indicates if this controller is used for map layers or data layers.
     *
     * @return True if this state controller is responsible for data layers,
     *         false if this controller is responsible for map layers.
     */
    protected abstract boolean isDataLayer();
}
