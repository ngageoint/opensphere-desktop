package io.opensphere.controlpanels.state;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.modulestate.StateV4ReaderWriter;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/** Implementation of a controller for the state plugin. */
@ThreadSafe
public class StateControllerImpl implements StateController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StateControllerImpl.class);

    /** The module state manager. */
    private final ModuleStateManager myModuleStateManager;

    /**
     * Constructor.
     *
     * @param moduleStateManager The module state manager.
     */
    public StateControllerImpl(ModuleStateManager moduleStateManager)
    {
        myModuleStateManager = Utilities.checkNull(moduleStateManager, "moduleStateManager");
    }

    @Override
    public void deactivateAllStates()
    {
        myModuleStateManager.deactivateAllStates();
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
        for (String id : stateIds)
        {
            if (myModuleStateManager.isStateActive(id))
            {
                myModuleStateManager.toggleState(id);
            }
            myModuleStateManager.unregisterState(id);
        }
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
        }
    }

    @Override
    public void toggleState(String id)
    {
        myModuleStateManager.toggleState(id);
    }
}
