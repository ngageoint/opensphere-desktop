package io.opensphere.controlpanels.state;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.modulestate.StateV4ReaderWriter;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupActivationException;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoOrderManager;
import io.opensphere.mantle.data.DefaultDataTypeInfoOrderManager;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Implementation of a controller for the state plugin. */
@ThreadSafe
public class StateControllerImpl implements StateController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StateControllerImpl.class);

    /** The module state manager. */
    private final ModuleStateManager myModuleStateManager;

    /** PropertyDescriptor defined for StateTypes. */
    private static final PropertyDescriptor<StateType> STATE_DESCRIPTOR = new PropertyDescriptor<>("state", StateType.class);

    /** The root data group info. */
    private final DefaultDataGroupInfo myRootGroupInfo;

    /** The data group controller. */
    private final DataGroupController myDataController;

    /** The order manager for data type info. */
    private final DataTypeInfoOrderManager myOrderManager;

    /** The lock to control calls for toggling state. */
    private boolean myToggleLock;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The activation listener. */
    private final ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public void handleDeactivating(DataGroupInfo dgi)
        {
            dgi.getMembers(false).stream().map(dti -> dti.getDisplayName()).filter(n -> isStateActive(n)).
                forEach(n -> toggleStateWithLock(n));
        }

        @Override
        public boolean handleActivating(DataGroupInfo dgi, io.opensphere.core.util.lang.PhasedTaskCanceller canceller)
                throws DataGroupActivationException, InterruptedException
        {
            dgi.getMembers(false).stream().map(dti -> dti.getDisplayName()).filter(n -> !isStateActive(n)).
                forEach(n -> toggleStateWithLock(n));
            return true;
        }
    };
    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public StateControllerImpl(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myModuleStateManager = Utilities.checkNull(myToolbox.getModuleStateManager(), "moduleStateManager");
        myRootGroupInfo = new DefaultDataGroupInfo(true, myToolbox, "State", "State", "State");
        myDataController = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController();
        myDataController.addRootDataGroupInfo(myRootGroupInfo, this);
        myOrderManager = new DefaultDataTypeInfoOrderManager(myToolbox.getOrderManagerRegistry());
        myOrderManager.open();
        myToggleLock = false;
        checkInitialStates();
    }

    @Override
    public void deactivateAllStates()
    {
        myModuleStateManager.getActiveStateIds().stream().forEach(n -> toggleState(n));
    }

    @Override
    public Collection<? extends String> getAvailableModules()
    {
        List<String> modules = New.list(myModuleStateManager.getModuleNames());
        Collections.sort(modules);
        return modules;
    }

    @Override
    public Collection<? extends String> getAvailableStates()
    {
        List<String> states = New.list(myModuleStateManager.getRegisteredStateIds());
        Collections.sort(states);
        return states;
    }

    @Override
    public Collection<? extends String> getModulesThatCanSaveState()
    {
        return myModuleStateManager.getModulesThatCanSaveState();
    }

    @Override
    public Collection<? extends String> getModulesThatSaveStateByDefault()
    {
        return myModuleStateManager.getModulesThatSaveStateByDefault();
    }

    @Override
    public Map<String, Collection<? extends String>> getStateDependenciesForModules(Collection<? extends String> modules)
    {
        return myModuleStateManager.getStateDependenciesForModules(modules);
    }

    @Override
    public String getStateDescription(String state)
    {
        return myModuleStateManager.getStateDescription(state);
    }

    @Override
    public Collection<? extends String> getStateTags(String state)
    {
        return myModuleStateManager.getStateTags(state);
    }

    @Override
    public boolean isStateActive(String state)
    {
        return myModuleStateManager.isStateActive(state);
    }

    @Override
    public void removeStates(Collection<? extends String> stateIds)
    {
        stateIds.stream().filter(n -> isStateActive(n)).forEach(n -> myModuleStateManager.toggleState(n));
        stateIds.stream().forEach(n ->
        {
            myModuleStateManager.unregisterState(n);
            unhookState(n);
        });
    }

    @Override
    public void saveState(String id, String description, Collection<? extends String> tags, Collection<? extends String> modules,
            boolean saveToApplication, OutputStream outputStream)
    {
        StateType state = new StateType();

        myModuleStateManager.saveState(id, description, tags, modules, state);

        if (outputStream != null)
        {
            try
            {
                new StateV4ReaderWriter().write(state, outputStream);
            }
            catch (JAXBException e)
            {
                LOGGER.error(e, e);
            }
        }

        if (saveToApplication)
        {
            myModuleStateManager.registerState(id, description, tags, modules, state);

            hookState(id, state);
        }
    }

    @Override
    public void toggleState(String id)
    {
        myToggleLock = true;
        if (!isStateActive(id))
        {
            myRootGroupInfo.getGroupById(id).activationProperty().setActive(true);
        }
        else
        {
            myRootGroupInfo.getGroupById(id).activationProperty().setActive(false);
        }
        myToggleLock = false;
        myModuleStateManager.toggleState(id);
    }

    @Override
    public void hookState(String id, StateType state)
    {
        DefaultDataGroupInfo ddgi = new DefaultDataGroupInfo(false, myToolbox, "State", id, id);
        ddgi.setAssistant(new StateDataGroupInfoAssistant(this));
        DataTypeInfo dti = new DefaultDataTypeInfo(myToolbox, "State", "state:" + id, "State", id, true);
        myOrderManager.activateParticipant(dti);
        ddgi.activationProperty().addListener(myActivationListener);
        ddgi.addMember(dti, this);
        ddgi.activationProperty().setActive(false);
        myRootGroupInfo.addChild(ddgi, this);
        DataModelCategory category = new DataModelCategory("state", StateType.class.getName(), id);
        myToolbox.getDataRegistry().addModels(new SimpleSessionOnlyCacheDeposit<>(category, STATE_DESCRIPTOR,
                Collections.singleton(state)));
    }

    /**
     * Removes a state from the data group collection.
     *
     * @param id The name of the state.
     */
    private void unhookState(String id)
    {
        DataGroupInfo dgi = myRootGroupInfo.getGroupById(id);
        if (dgi != null)
        {
            dgi.getMembers(false).stream().forEach(n -> myOrderManager.deactivateParticipant(n));
            myRootGroupInfo.removeChild(dgi, this);
            myToolbox.getDataRegistry().removeModels(new DataModelCategory("state", StateType.class.getName(), id), false);
            myDataController.cleanUpGroup(dgi);
        }
    }

    /**
     * During startup, checks to see if any states exist that aren't hooked,
     * and if they aren't, adds the state to the data group collection.
     */
    private void checkInitialStates()
    {
        myModuleStateManager.getRegisteredStateIds().stream().filter(n -> myRootGroupInfo.getGroupById(n) == null)
            .forEach(n -> hookState(n, myModuleStateManager.getState(n)));
    }
    /**
     * Toggle the state if the toggle was initiated from the activation listener.
     *
     * @param id The id of the state.
     */
    private void toggleStateWithLock(String id)
    {
        if (!myToggleLock)
        {
            myModuleStateManager.toggleState(id);
        }
    }
}
