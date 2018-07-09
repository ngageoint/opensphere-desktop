package io.opensphere.featureactions.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.Aggregator;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.registry.FeatureActionCreator;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.event.impl.DataElementsAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataElementsRemovedEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.mdfilter.impl.DataFilterEvaluator;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener;
import io.opensphere.mantle.data.geom.style.config.v1.DataTypeStyleConfig;
import io.opensphere.mantle.data.geom.style.config.v1.StyleManagerConfig;
import io.opensphere.mantle.data.geom.style.dialog.StyleManagerController;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Listens for features and applies actions to them. */
public class FeatureActionsController extends EventListenerService
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FeatureActionsController.class);

    /** The action appliers. */
    private final List<ActionApplier> myActionAppliers;

    /** The executor on which to handle events. */
    private final ExecutorService myExecutor = ThreadUtilities.newTerminatingFixedThreadPool("FeatureActionsController", 1);

    /** Executor used to handle style registry changes. */
    private final transient Executor myProcrastinatingExecutor = CommonTimer.createProcrastinatingExecutor(200);

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /**
     * The {@link PreferencesRegistry} used to get style information for layers.
     */
    private final PreferencesRegistry myPrefs;

    /** The registry of feature actions. */
    private final FeatureActionsRegistry myRegistry;

    /** The type keys for which actions have been applied. */
    @ThreadConfined("FeatureActionsController")
    private final Map<String, DataTypeStyleConfig> myTypeKeysAndStyles = New.map();

    /** The style registry listener. */
    private final VisualizationStyleRegistryChangeListener myStyleRegistryListener;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param registry the registry of feature actions
     */
    public FeatureActionsController(Toolbox toolbox, FeatureActionsRegistry registry)
    {
        super(toolbox.getEventManager());
        myRegistry = registry;
        myMantleToolbox = MantleToolboxUtils.getMantleToolbox(toolbox);
        myActionAppliers = New.list(new StyleApplier(toolbox), new MetaDataApplier(toolbox));
        myPrefs = toolbox.getPreferencesRegistry();

        bindEvent(DataElementsAddedEvent.class, this::handleDataElementsAddedEvent, myExecutor);
        bindEvent(DataElementsRemovedEvent.class, this::handleDataElementsRemovedEvent, myExecutor);
        addService(myRegistry.getPrefs().getListenerService(Preferences.ALL_KEY, this::handleModelChange));
        addService(myPrefs.getPreferences(StyleManagerController.class).getListenerService(Preferences.ALL_KEY,
                this::handleStyleChange));

        /* This is needed because handleStyleChange() doesn't handle when
         * 'Enable Custom Style' is checked. */
        myStyleRegistryListener = new VisualizationStyleRegistryChangeListener()
        {
            @Override
            public void visualizationStyleInstalled(Class<? extends VisualizationStyle> styleClass, Object source)
            {
            }

            @Override
            public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
            {
                myProcrastinatingExecutor.execute(() -> handleModelChange(evt.getDTIKey()));
            }

            @Override
            public void defaultStyleChanged(Class<? extends VisualizationSupport> mgsClass,
                    Class<? extends VisualizationStyle> styleClass, Object source)
            {
            }
        };
    }

    @Override
    public void open()
    {
        super.open();
        myMantleToolbox.getVisualizationStyleRegistry().addVisualizationStyleRegistryChangeListener(myStyleRegistryListener);
    }

    /**
     * Un-applies any actions to the data elements of the data type.
     *
     * @param ids the data element IDs
     * @param dataType the data type of the elements
     */
    private void clearActions(Collection<Long> ids, DataTypeInfo dataType)
    {
        if (myTypeKeysAndStyles.containsKey(dataType.getTypeKey()))
        {
            for (ActionApplier applier : myActionAppliers)
            {
                applier.clearActions(ids, dataType);
            }
        }
    }

    /**
     * Ensures that a filter evaluator exists or is created for the feature
     * action.
     *
     * @param featureAction the feature action
     */
    private void populateEvaluator(FeatureAction featureAction)
    {
        DataFilterEvaluator evaluator = featureAction.getEvaluator();
        if (evaluator == null)
        {
            evaluator = new DataFilterEvaluator(featureAction.getFilter(), myMantleToolbox.getDynamicEnumerationRegistry());
            featureAction.setEvaluator(evaluator);
        }
    }

    /**
     * Does everything. Figures out what actions need to be applied if any, and
     * applies them to the features.
     *
     * @param ids the data element IDs
     * @param dataType the data type
     */
    private void doActions(Collection<Long> ids, DataTypeInfo dataType)
    {
        long start = System.nanoTime();

        List<FeatureAction> featureActions = myRegistry.getEnabled(dataType.getTypeKey());
        if (!featureActions.isEmpty())
        {
            DataTypeStyleConfig style = myPrefs.getPreferences(StyleManagerController.class)
                    .getJAXBObject(StyleManagerConfig.class, "styleManagerConfig", new StyleManagerConfig())
                    .getDataTypeStyleByTypeKey(dataType.getTypeKey());
            myTypeKeysAndStyles.put(dataType.getTypeKey(), XMLUtilities.jaxbClone(style, DataTypeStyleConfig.class));

            Aggregator.process(ids, 100_000, idSubset -> applyActions(featureActions, idSubset, dataType));

            if (LOGGER.isDebugEnabled())
            {
                long delta = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - start);
                LOGGER.debug("Applied feature actions to " + ids.size() + " elements in " + delta + " μs");
            }
        }
    }

    /**
     * Applies the feature actions to the IDs.
     *
     * @param featureActions the feature actions
     * @param ids the IDs
     * @param dataType the data type
     */
    private void applyActions(Collection<? extends FeatureAction> featureActions, Collection<Long> ids, DataTypeInfo dataType)
    {
        Map<Collection<Action>, List<MapDataElement>> actionToElementsMap = mapActionToElements(ids, dataType, featureActions);

        for (Map.Entry<Collection<Action>, List<MapDataElement>> entry : actionToElementsMap.entrySet())
        {
            Collection<Action> actions = entry.getKey();
            List<MapDataElement> elements = entry.getValue();

            applyActions(actions, elements, dataType);
        }
    }

    /**
     * Applies the actions to the data elements.
     *
     * @param actions the actions
     * @param elements the data elements
     * @param dataType the data type
     */
    private void applyActions(Collection<? extends Action> actions, List<? extends MapDataElement> elements,
            DataTypeInfo dataType)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Applying actions " + actions + " to " + elements.size());
        }

        for (ActionApplier applier : myActionAppliers)
        {
            applier.applyActions(actions, elements, dataType);
        }
    }

    /**
     * Gets the actions that need to be applied to the data element.
     *
     * @param element the data element
     * @param featureActions collection of all possible actions
     * @return the passing actions
     */
    private Collection<FeatureAction> getPassingActions(DataElement element, Collection<? extends FeatureAction> featureActions)
    {
        Collection<FeatureAction> passingActions = New.list();
        for (FeatureAction featureAction : featureActions)
        {
            if (featureAction.getEvaluator().accepts(element))
            {
                passingActions.add(featureAction);
            }
        }
        return passingActions;
    }

    /**
     * Applies feature actions to new features.
     *
     * @param event the event
     */
    private void handleDataElementsAddedEvent(DataElementsAddedEvent event)
    {
        doActions(event.getAddedDataElementIds(), event.getType());
    }

    /**
     * Cleans up as necessary when features are removed.
     *
     * @param event the event
     */
    private void handleDataElementsRemovedEvent(DataElementsRemovedEvent event)
    {
        removeElements(event.getRemovedDataElementIds(), event.getType());
    }

    /**
     * Checks if there were any feature action groups that the data element
     * didn't satisfy, and creates a feature action that satisfies the data
     * element for any such groups.
     *
     * @param element the data element
     * @param passingFeatureActions the passing feature actions
     * @param groupsToSatisfy the groups that need to be satisfied
     * @param dataType the data type
     */
    private void handleGroupUnsatisfaction(MapDataElement element, Collection<FeatureAction> passingFeatureActions,
            Set<String> groupsToSatisfy, DataTypeInfo dataType)
    {
        if (!groupsToSatisfy.isEmpty())
        {
            Set<String> passedGroups = passingFeatureActions.stream().map(a -> a.getGroupName()).filter(g -> g != null)
                    .collect(Collectors.toSet());
            Collection<String> unsatisfiedGroups = CollectionUtilities.difference(groupsToSatisfy, passedGroups);
            if (!unsatisfiedGroups.isEmpty())
            {
                for (String group : unsatisfiedGroups)
                {
                    FeatureActionCreator actionCreator = myRegistry.getActionCreator(group);
                    FeatureAction featureAction = actionCreator.create(element);

                    populateEvaluator(featureAction);
                    // Sanity check to avoid creating new actions that won't be
                    // accepted, which can actually happen.
                    if (featureAction.getEvaluator().accepts(element))
                    {
                        myRegistry.add(dataType.getTypeKey(), Collections.singleton(featureAction), this);
                        passingFeatureActions.add(featureAction);

                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("Created new action: " + featureAction);
                        }
                    }
                }
            }
        }
    }

    /**
     * Applies feature actions to existing features.
     *
     * @param event the event
     */
    private void handleModelChange(PreferenceChangeEvent event)
    {
        if (event.getSource() != this)
        {
            myExecutor.execute(() -> handleModelChange(event.getKey()));
        }
    }

    /**
     * Applies feature actions to existing features.
     *
     * @param typeKey the data type key
     */
    private void handleModelChange(String typeKey)
    {
        DataTypeInfo dataType = myMantleToolbox.getDataTypeInfoFromKey(typeKey);
        if (dataType != null)
        {
            List<Long> ids = myMantleToolbox.getDataElementLookupUtils().getDataElementCacheIds(dataType);
            if (!ids.isEmpty())
            {
                Aggregator.process(ids, 100_000, idSubset -> clearActions(idSubset, dataType));
                myTypeKeysAndStyles.remove(dataType.getTypeKey());
                doActions(ids, dataType);
            }
        }
    }

    /**
     * Applies feature actions to existing features.
     *
     * @param event the event
     */
    private void handleStyleChange(PreferenceChangeEvent event)
    {
        try
        {
            Object value = event.getValueAsObject(null);
            if (value instanceof StyleManagerConfig)
            {
                StyleManagerConfig config = (StyleManagerConfig)value;
                for (Map.Entry<String, DataTypeStyleConfig> entry : myTypeKeysAndStyles.entrySet())
                {
                    String dataTypeKey = entry.getKey();
                    DataTypeStyleConfig oldConfig = entry.getValue();

                    DataTypeStyleConfig newConfig = config.getDataTypeStyleByTypeKey(dataTypeKey);
                    if (!Objects.equals(newConfig, oldConfig))
                    {
                        handleModelChange(dataTypeKey);
                    }
                }
            }
        }
        catch (JAXBException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Creates a map of a set of actions to the elements that apply to them.
     *
     * @param ids the data element IDs
     * @param dataType the data type
     * @param featureActions all the actions
     * @return the map
     */
    private Map<Collection<Action>, List<MapDataElement>> mapActionToElements(Collection<Long> ids, DataTypeInfo dataType,
            Collection<? extends FeatureAction> featureActions)
    {
        Map<Collection<Action>, List<MapDataElement>> actionToElementsMap = New.map();

        for (FeatureAction action : featureActions)
        {
            // Set the type key so that the evaluator will accept it
            action.getFilter().getSource().setTypeKey(dataType.getTypeKey());

            populateEvaluator(action);
        }

        Set<String> groupsToSatisfy = featureActions.stream().map(a -> a.getGroupName()).distinct()
                .filter(g -> myRegistry.getActionCreator(g) != null).collect(Collectors.toSet());

        Collection<DataElement> elements = FeatureActionUtilities.getDataElements(myMantleToolbox, ids, dataType);
        for (DataElement element : elements)
        {
            if (element instanceof MapDataElement)
            {
                MapDataElement mapElement = (MapDataElement)element;

                Collection<FeatureAction> passingFeatureActions = getPassingActions(mapElement, featureActions);

                handleGroupUnsatisfaction(mapElement, passingFeatureActions, groupsToSatisfy, dataType);

                Set<Action> passingActions = new LinkedHashSet<>();
                for (FeatureAction featureAction : passingFeatureActions)
                {
                    passingActions.addAll(featureAction.getActions());
                }

                if (!passingActions.isEmpty())
                {
                    actionToElementsMap.computeIfAbsent(passingActions, k -> New.list()).add(mapElement);
                }
            }
        }
        return actionToElementsMap;
    }

    /**
     * Removes the data elements of the data type.
     *
     * @param ids the data element IDs
     * @param dataType the data type of the elements
     */
    private void removeElements(Collection<Long> ids, DataTypeInfo dataType)
    {
        if (myTypeKeysAndStyles.containsKey(dataType.getTypeKey()))
        {
            for (ActionApplier applier : myActionAppliers)
            {
                applier.removeElements(ids, dataType);
            }
        }
    }
}
