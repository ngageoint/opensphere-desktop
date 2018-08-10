package io.opensphere.core.modulestate;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.Nullable;
import net.jcip.annotations.GuardedBy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;
import com.bitsys.fade.mist.state.v4.TagsType;

import io.opensphere.core.Notify;
import io.opensphere.core.modulestate.config.v1.ModuleStateData;
import io.opensphere.core.modulestate.config.v1.ModuleStateManagerState;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.SupplierX;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.WeakReference;

/**
 * Implementation for {@link ModuleStateManager}.
 */
@SuppressWarnings("PMD.GodClass")
public class ModuleStateManagerImpl implements ModuleStateManager
{
    /** Key for the preferences. */
    static final String PREFS_KEY = "State";

    /** The JAXB context supplier. */
    private static final SupplierX<JAXBContext, JAXBException> CONTEXT_SUPPLIER = new SupplierX<JAXBContext, JAXBException>()
    {
        @Override
        public JAXBContext get() throws JAXBException
        {
            List<Class<?>> list = New.list(StateV4ReaderWriter.getClasses());
            list.add(ModuleStateManagerState.class);
            Class<?>[] array = list.toArray(new Class<?>[list.size()]);
            return JAXBContextHelper.getCachedContext(array);
        }
    };

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ModuleStateManagerImpl.class);

    /** The active states. */
    @GuardedBy("myActiveStates")
    private final Set<String> myActiveStates = New.set();

    /** A map of module display names to state controllers. */
    @GuardedBy("myControllerMap")
    private final LazyMap<String, List<Reference<ModuleStateController>>> myControllerMap = LazyMap.create(
            New.<String, List<Reference<ModuleStateController>>>insertionOrderMap(), String.class,
            New.<Reference<ModuleStateController>>listFactory());

    /**
     * The preferences.
     */
    @Nullable
    private final Preferences myPreferences;

    /** Map of state ids to {@link StateDataExtended} objects. */
    @GuardedBy("myStateMap")
    private final Map<String, StateDataExtended> myStateMap = New.map();

    /**
     * Constructor.
     *
     * @param preferencesRegistry Optional preferences registry used to persist
     *            registered states.
     */
    public ModuleStateManagerImpl(PreferencesRegistry preferencesRegistry)
    {
        myPreferences = preferencesRegistry == null ? null : preferencesRegistry.getPreferences(getClass());

        loadFromPreferences();
    }

    @Override
    public void deactivateAllStates()
    {
        Collection<? extends String> activeStateIds;
        synchronized (myActiveStates)
        {
            activeStateIds = getActiveStateIds();
            myActiveStates.clear();
        }
        for (String stateId : activeStateIds)
        {
            StateDataExtended data;
            synchronized (myStateMap)
            {
                data = myStateMap.get(stateId);
            }
            if (data != null)
            {
                deactivateStates(data);
            }
        }
        saveToPreferences();
    }

    @Override
    public Collection<String> detectModules(Node node)
    {
        Collection<String> moduleNames = New.set();
        synchronized (myControllerMap)
        {
            for (Entry<String, List<Reference<ModuleStateController>>> entry : myControllerMap.entrySet())
            {
                for (Reference<ModuleStateController> ref : entry.getValue())
                {
                    ModuleStateController moduleStateController = ref.get();
                    if (moduleStateController != null && moduleStateController.canActivateState(node)
                            && !moduleStateController.isAlwaysActivateState())
                    {
                        moduleNames.add(entry.getKey());
                    }
                }
            }
        }
        return moduleNames;
    }

    @Override
    public Collection<String> detectModules(StateType state)
    {
        Collection<String> moduleNames = New.set();
        synchronized (myControllerMap)
        {
            for (Entry<String, List<Reference<ModuleStateController>>> entry : myControllerMap.entrySet())
            {
                for (Reference<ModuleStateController> ref : entry.getValue())
                {
                    ModuleStateController moduleStateController = ref.get();
                    if (moduleStateController != null && moduleStateController.canActivateState(state)
                            && !moduleStateController.isAlwaysActivateState())
                    {
                        moduleNames.add(entry.getKey());
                    }
                }
            }
        }
        return moduleNames;
    }

    @Override
    public Collection<? extends String> getActiveStateIds()
    {
        synchronized (myActiveStates)
        {
            return New.unmodifiableSet(myActiveStates);
        }
    }

    @Override
    public Collection<? extends String> getModuleNames()
    {
        synchronized (myControllerMap)
        {
            return New.collection(myControllerMap.keySet());
        }
    }

    @Override
    public Collection<? extends String> getModulesThatCanSaveState()
    {
        Collection<String> moduleNames = New.set();
        synchronized (myControllerMap)
        {
            for (Entry<String, List<Reference<ModuleStateController>>> entry : myControllerMap.entrySet())
            {
                for (Reference<ModuleStateController> ref : entry.getValue())
                {
                    ModuleStateController moduleStateController = ref.get();
                    if (moduleStateController != null && moduleStateController.canSaveState()
                            && !moduleStateController.isAlwaysSaveState())
                    {
                        moduleNames.add(entry.getKey());
                    }
                }
            }
        }
        return moduleNames;
    }

    @Override
    public Collection<? extends String> getModulesThatSaveStateByDefault()
    {
        Collection<String> moduleNames = New.set();
        synchronized (myControllerMap)
        {
            for (Entry<String, List<Reference<ModuleStateController>>> entry : myControllerMap.entrySet())
            {
                for (Reference<ModuleStateController> ref : entry.getValue())
                {
                    ModuleStateController moduleStateController = ref.get();
                    if (moduleStateController != null && moduleStateController.isSaveStateByDefault())
                    {
                        moduleNames.add(entry.getKey());
                    }
                }
            }
        }
        return moduleNames;
    }

    @Override
    public Collection<? extends String> getRegisteredStateIds()
    {
        synchronized (myStateMap)
        {
            return New.unmodifiableSet(myStateMap.keySet());
        }
    }

    @Override
    public StateType getState(String state)
    {
        synchronized (myStateMap)
        {
            return myStateMap.get(state).getState();
        }
    }

    @Override
    public Map<String, Collection<? extends String>> getStateDependenciesForModules(Collection<? extends String> modules)
    {
        Map<String, Collection<? extends String>> stateDependencies = New.map();
        for (String module : modules)
        {
            List<Reference<ModuleStateController>> moduleControllerList = myControllerMap.get(module);
            for (Reference<ModuleStateController> ref : moduleControllerList)
            {
                ModuleStateController moduleStateController = ref.get();
                if (moduleStateController != null)
                {
                    stateDependencies.put(module, moduleStateController.getRequiredStateDependencies());
                }
            }
        }
        return stateDependencies;
    }

    @Override
    public String getStateDescription(String state)
    {
        synchronized (myStateMap)
        {
            StateDataExtended stateData = myStateMap.get(state);
            return stateData == null ? StringUtilities.EMPTY : stateData.getDescription();
        }
    }

    @Override
    public Collection<? extends String> getStateTags(String state)
    {
        synchronized (myStateMap)
        {
            StateDataExtended stateData = myStateMap.get(state);
            return stateData == null ? Collections.<String>emptyList() : stateData.getTags();
        }
    }

    @Override
    public boolean isStateActive(String state)
    {
        synchronized (myActiveStates)
        {
            return myActiveStates.contains(state);
        }
    }

    @Override
    public void registerModuleStateController(String moduleName, ModuleStateController controller)
    {
        Utilities.checkNull(controller, "controller");
        if (StringUtils.isBlank(moduleName))
        {
            throw new IllegalArgumentException("Module name cannot be blank.");
        }
        synchronized (myControllerMap)
        {
            myControllerMap.get(moduleName).add(new WeakReference<>(controller));
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Registering module state controller with id [" + moduleName + "]");
        }
        Collection<? extends String> activeStateIds = getActiveStateIds();
        Collection<StateDataExtended> statesToActivate = New.collection();
        synchronized (myStateMap)
        {
            for (String id : activeStateIds)
            {
                StateDataExtended data = myStateMap.get(id);
                if (data != null && data.getModules().contains(moduleName))
                {
                    statesToActivate.add(data);
                }
            }
        }

        for (StateDataExtended data : statesToActivate)
        {
            try
            {
                if (data.getElement() != null)
                {
                    controller.activateState(data.getId(), data.getDescription(), data.getTags(), data.getElement());
                }
                else
                {
                    controller.activateState(data.getId(), data.getDescription(), data.getTags(), data.getState());
                }
            }
            catch (InterruptedException e)
            {
                LOGGER.info("Activation cancelled for state: " + data.getId());
                break;
            }
        }
    }

    @Override
    public void registerState(String id, String description, Collection<? extends String> tags,
            Collection<? extends String> modules, Element element)
    {
        registerStateInternal(id, description, tags, modules, element);
    }

    @Override
    public void registerState(String id, String description, Collection<? extends String> tags,
            Collection<? extends String> modules, StateType state)
    {
        registerStateInternal(id, description, tags, modules, state);
    }

    /**
     * Register a saved state with the manager. This does not activate the
     * state.
     *
     * @param id The id of the state.
     * @param description The description of the state.
     * @param tags The tags for the state.
     * @param modules The modules associated with the state.
     * @param state The DOM element or state object containing the state.
     */
    private void registerStateInternal(String id, String description, Collection<? extends String> tags,
            Collection<? extends String> modules, Object state)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Registering module state with id [" + id + "]");
        }

        List<String> allModules = New.list(modules);
        allModules.addAll(getAlwaysActivateModules());
        addStateToMap(id, description, tags, modules, state);
        saveToPreferences();
    }

    @Override
    public void saveState(String id, String description, Collection<? extends String> tags, Collection<? extends String> modules,
            Node parentNode)
    {
        Document doc = parentNode instanceof Document ? (Document)parentNode : parentNode.getOwnerDocument();
        Element stateElement = (Element)parentNode.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));
        stateElement.setAttribute("source", getSource());

        stateElement.appendChild(doc.createElement("title")).setTextContent(id);

        if (!StringUtils.isBlank(description))
        {
            stateElement.appendChild(doc.createElement("description")).setTextContent(description);
        }
        if (CollectionUtilities.hasContent(tags))
        {
            try
            {
                XMLUtilities.marshalJAXBObjectToElement(new TagList(tags), stateElement);
            }
            catch (JAXBException e)
            {
                LOGGER.error("Failed to marshal tags to state: " + e, e);
            }
        }

        Collection<ModuleStateController> controllers = getControllers(modules);
        if (!controllers.isEmpty())
        {
            for (ModuleStateController controller : controllers)
            {
                try
                {
                    controller.saveState(stateElement);
                }
                catch (RuntimeException e)
                {
                    LOGGER.error("Error while saving states: " + e, e);
                }
            }
        }
    }

    @Override
    public void saveState(String id, String description, Collection<? extends String> tags, Collection<? extends String> modules,
            StateType state)
    {
        state.setTitle(id);
        if (!StringUtils.isBlank(description))
        {
            state.setDescription(description);
        }
        if (CollectionUtilities.hasContent(tags))
        {
            TagsType tagsType = new TagsType();
            tagsType.getTag().addAll(tags);
            state.setTags(tagsType);
        }
        state.setSource(getSource());
        state.setVersion("4.0");

        for (ModuleStateController controller : getControllers(modules))
        {
            try
            {
                controller.saveState(state);
            }
            catch (RuntimeException e)
            {
                LOGGER.error("Error while saving states: " + e, e);
            }
        }
    }

    @Override
    public void toggleState(String id)
    {
        StateDataExtended data;
        synchronized (myStateMap)
        {
            data = myStateMap.get(id);
        }
        synchronized (myActiveStates)
        {
            if (isStateActive(id))
            {
                myActiveStates.remove(id);

                if (data != null)
                {
                    deactivateStates(data);
                }
            }
            else
            {
                myActiveStates.add(id);

                if (data != null)
                {
                    activateStates(data);
                }
            }
        }
        saveToPreferences();
    }

    @Override
    public void unregisterModuleStateController(String moduleName, ModuleStateController controller)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Unregistering module state controller with id [" + moduleName + "]");
        }
        synchronized (myControllerMap)
        {
            List<Reference<ModuleStateController>> list = myControllerMap.getIfExists(moduleName);
            if (list != null)
            {
                for (Iterator<Reference<ModuleStateController>> iter = list.iterator(); iter.hasNext();)
                {
                    Reference<ModuleStateController> ref = iter.next();
                    ModuleStateController val = ref.get();
                    if (val == null || Utilities.sameInstance(controller, val))
                    {
                        iter.remove();
                    }
                }
                if (list.isEmpty())
                {
                    myControllerMap.remove(moduleName);
                }
            }
        }
    }

    @Override
    public void unregisterState(String id)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Registering module state with id [" + id + "]");
        }
        synchronized (myStateMap)
        {
            myStateMap.remove(id);
        }
        saveToPreferences();
    }

    /**
     * Activate the states of the specified state data.
     *
     * @param data the state data.
     */
    protected void activateStates(StateDataExtended data)
    {
        LOGGER.info("Activating module state [" + data.getId() + "]");
        Collection<ModuleStateController> controllers = getControllers(data.getModules());
        if (!controllers.isEmpty())
        {
            for (ModuleStateController controller : controllers)
            {
                try
                {
                    ThreadControl.check();
                    if (data.getElement() != null)
                    {
                        controller.activateState(data.getId(), data.getDescription(), data.getTags(), data.getElement());
                    }
                    else
                    {
                        controller.activateState(data.getId(), data.getDescription(), data.getTags(), data.getState());
                    }
                }
                catch (InterruptedException e)
                {
                    // This may leave the state partially active.
                    LOGGER.info("Activation cancelled for state: " + data.getId());
                    break;
                }
                catch (RuntimeException e)
                {
                    String message = "Error while activating states: " + e;
                    Notify.error(message);
                    LOGGER.error(message, e);
                }
            }
        }
    }

    /**
     * Deactivate the states of the specified state data.
     *
     * @param data the state data.
     */
    protected void deactivateStates(StateDataExtended data)
    {
        LOGGER.info("Deactivating module state [" + data.getId() + "]");
        Collection<ModuleStateController> controllers = getControllers(data.getModules());
        if (!controllers.isEmpty())
        {
            for (ModuleStateController controller : controllers)
            {
                try
                {
                    if (data.getElement() != null)
                    {
                        controller.deactivateState(data.getId(), data.getElement());
                    }
                    else
                    {
                        controller.deactivateState(data.getId(), data.getState());
                    }
                }
                catch (InterruptedException e)
                {
                    LOGGER.info("Deactivation cancelled for state: " + data.getId());
                    break;
                }
                catch (RuntimeException e)
                {
                    LOGGER.error("Error while deactivating states: " + e, e);
                }
            }
        }
    }

    /**
     * Get the controllers with the given module names.
     *
     * @param moduleNames The module names.
     * @return The controllers.
     */
    protected Collection<ModuleStateController> getControllers(Collection<? extends String> moduleNames)
    {
        Collection<ModuleStateController> controllers = New.collection(moduleNames.size());
        synchronized (myControllerMap)
        {
            for (Entry<String, List<Reference<ModuleStateController>>> entry : myControllerMap.entrySet())
            {
                if (moduleNames.contains(entry.getKey()))
                {
                    for (Reference<ModuleStateController> ref : entry.getValue())
                    {
                        ModuleStateController controller = ref == null ? null : ref.get();
                        if (controller == null)
                        {
                            LOGGER.warn("Module state controller with module name " + entry.getKey() + " not found.");
                        }
                        else
                        {
                            controllers.add(controller);
                        }
                    }
                }
            }

            // Now add the isAlwaysSaveState and isAlwaysActivateState
            // controllers.
            for (Entry<String, List<Reference<ModuleStateController>>> entry : myControllerMap.entrySet())
            {
                for (Reference<ModuleStateController> ref : entry.getValue())
                {
                    ModuleStateController controller = ref == null ? null : ref.get();
                    if (controller != null && (controller.isAlwaysSaveState() || controller.isAlwaysActivateState()))
                    {
                        controllers.add(controller);
                    }
                }
            }
        }
        return controllers;
    }

    /**
     * Add a state to the state map.
     *
     * @param id The id of the state.
     * @param description The description of the state.
     * @param tags The tags associated with the state.
     * @param modules The modules associated with the state.
     * @param element The DOM element containing the state.
     */
    private void addStateToMap(String id, String description, Collection<? extends String> tags,
            Collection<? extends String> modules, Object element)
    {
        StateDataExtended data;
        if (element instanceof Element)
        {
            data = new StateDataExtended(id, description, tags, modules, (Element)element);
        }
        else
        {
            data = new StateDataExtended(id, description, tags, modules, (StateType)element);
        }
        synchronized (myStateMap)
        {
            myStateMap.put(id, data);
        }
    }

    /**
     * Gets a collection of modules that are always activate.
     *
     * @return A collection of modules that need to be activated all the time.
     */
    private Collection<? extends String> getAlwaysActivateModules()
    {
        List<String> alwaysActivate = New.list();

        synchronized (myControllerMap)
        {
            for (Entry<String, List<Reference<ModuleStateController>>> entry : myControllerMap.entrySet())
            {
                for (Reference<ModuleStateController> controllerRef : entry.getValue())
                {
                    if (controllerRef.get() != null && controllerRef.get().isAlwaysActivateState())
                    {
                        alwaysActivate.add(entry.getKey());
                        break;
                    }
                }
            }
        }

        return alwaysActivate;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    private String getSource()
    {
        return StringUtilities.expandProperties(System.getProperty("opensphere.frame.title", "Unknown"), System.getProperties())
                .trim();
    }

    /**
     * Load the states from the preferences.
     */
    private void loadFromPreferences()
    {
        if (myPreferences == null)
        {
            return;
        }

        ModuleStateManagerState state = myPreferences.getJAXBObject(ModuleStateManagerState.class, PREFS_KEY, CONTEXT_SUPPLIER,
                null);
        if (state == null)
        {
            return;
        }

        synchronized (myStateMap)
        {
            Collection<ModuleStateData> stateData = state.getStateData();
            if (CollectionUtilities.hasContent(stateData))
            {
                for (ModuleStateData moduleStateData : stateData)
                {
                    if (moduleStateData.getId() != null && moduleStateData.getModules() != null
                            && moduleStateData.getElement() != null)
                    {
                        addStateToMap(moduleStateData.getId(), moduleStateData.getDescription(), moduleStateData.getTags(),
                                moduleStateData.getModules(), moduleStateData.getElement());
                    }
                    else if (moduleStateData.getId() != null && moduleStateData.getModules() != null
                            && moduleStateData.getState() != null)
                    {
                        addStateToMap(moduleStateData.getId(), moduleStateData.getDescription(), moduleStateData.getTags(),
                                moduleStateData.getModules(), moduleStateData.getState());
                    }
                    else
                    {
                        LOGGER.warn("Ignoring badly formed module state data with id [" + moduleStateData.getId() + "]");
                    }
                }
            }
        }

        if (Boolean.getBoolean("opensphere.state.activateOnStartup"))
        {
            synchronized (myActiveStates)
            {
                Collection<ModuleStateData> stateData = state.getStateData();
                if (CollectionUtilities.hasContent(stateData))
                {
                    for (ModuleStateData moduleStateData : stateData)
                    {
                        if (moduleStateData.isActive())
                        {
                            myActiveStates.add(moduleStateData.getId());
                        }
                    }
                }
            }
        }
    }

    /**
     * Save the states to the preferences.
     */
    private void saveToPreferences()
    {
        if (myPreferences == null)
        {
            return;
        }

        Collection<String> activeStates;
        synchronized (myActiveStates)
        {
            activeStates = New.collection(myActiveStates);
        }
        Collection<ModuleStateData> stateData = New.collection();
        synchronized (myStateMap)
        {
            for (Entry<String, StateDataExtended> entry : myStateMap.entrySet())
            {
                String id = entry.getKey();
                StateDataExtended data = entry.getValue();
                boolean isActive = activeStates.contains(id);
                stateData.add(new ModuleStateData(id, data.getDescription(), data.getTags(), isActive, data.getModules(),
                        data.getElement(), data.getState()));
            }
        }

        ModuleStateManagerState state = new ModuleStateManagerState(stateData);

        myPreferences.putJAXBObject(PREFS_KEY, state, false, CONTEXT_SUPPLIER, this);
    }

    /**
     * Data that describes a saved state.
     */
    private static class StateDataExtended extends StateData
    {
        /** The element containing the state information. */
        private final Element myElement;

        /** The state object. */
        private final StateType myState;

        /**
         * Constructor.
         *
         * @param id The id for the state.
         * @param description The description for the state.
         * @param tags The tags associated with the state.
         * @param modules The modules that the state applies to.
         * @param element The element containing the state information.
         */
        public StateDataExtended(String id, String description, Collection<? extends String> tags,
                Collection<? extends String> modules, Element element)
        {
            super(id, description, tags, modules);
            myElement = Utilities.checkNull(element, "element");
            myState = null;
        }

        /**
         * Constructor.
         *
         * @param id The id for the state.
         * @param description The description for the state.
         * @param tags The tags associated with the state.
         * @param modules The modules that the state applies to.
         * @param state The state object
         */
        public StateDataExtended(String id, String description, Collection<? extends String> tags,
                Collection<? extends String> modules, StateType state)
        {
            super(id, description, tags, modules);
            myElement = null;
            myState = Utilities.checkNull(state, "state");
        }

        /**
         * Get the element containing the state information.
         *
         * @return The DOM element.
         */
        public Element getElement()
        {
            return myElement;
        }

        /**
         * Gets the state.
         *
         * @return the state
         */
        public StateType getState()
        {
            return myState;
        }
    }
}
