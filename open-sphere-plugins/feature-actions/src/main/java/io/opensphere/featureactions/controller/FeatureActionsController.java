package io.opensphere.featureactions.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.registry.FeatureActionCreator;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.event.impl.DataElementsAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataElementsRemovedEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.mdfilter.impl.DataFilterEvaluator;
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
     * Creates a filter evaluator for the feature action.
     *
     * @param featureAction the feature action
     * @return the pair of the action and evaluator
     */
    private Pair<FeatureAction, DataFilterEvaluator> createEvaluator(FeatureAction featureAction)
    {
        DataFilterEvaluator evaluator = featureAction.getEvaluator();
        if (evaluator == null)
        {
            evaluator = new DataFilterEvaluator(featureAction.getFilter(), myMantleToolbox.getDynamicEnumerationRegistry());
            featureAction.setEvaluator(evaluator);
        }
        return new Pair<>(featureAction, evaluator);
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

            Map<Collection<Action>, List<MapDataElement>> actionToElementsMap = mapActionToElements(ids, dataType,
                    featureActions);

            for (Map.Entry<Collection<Action>, List<MapDataElement>> entry : actionToElementsMap.entrySet())
            {
                Collection<Action> actions = entry.getKey();
                List<MapDataElement> elements = entry.getValue();

                applyActions(actions, elements, dataType);
            }

            if (LOGGER.isDebugEnabled())
            {
                long delta = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - start);
                LOGGER.debug("Applied feature actions to " + ids.size() + " elements in " + delta + " μs");
            }
        }
    }

    /**
     * Gets the actions that need to be applied to the data element.
     *
     * @param element the data element
     * @param actionsAndEvaluators collection of all possible actions and their
     *            filter evaluators
     * @return the passing actions
     */
    private Collection<FeatureAction> getPassingActions(DataElement element,
            Collection<? extends Pair<FeatureAction, DataFilterEvaluator>> actionsAndEvaluators)
    {
        Collection<FeatureAction> featureActions = New.list();
        for (Pair<FeatureAction, DataFilterEvaluator> pair : actionsAndEvaluators)
        {
            FeatureAction featureAction = pair.getFirstObject();
            DataFilterEvaluator evaluator = pair.getSecondObject();
            if (evaluator.accepts(element))
            {
                featureActions.add(featureAction);
            }
        }
        return featureActions;
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
     * @param actionsAndEvaluators collection of all possible actions and their
     *            filter evaluators
     */
    private void handleGroupUnsatisfaction(MapDataElement element, Collection<FeatureAction> passingFeatureActions,
            Set<String> groupsToSatisfy, DataTypeInfo dataType,
            Collection<? super Pair<FeatureAction, DataFilterEvaluator>> actionsAndEvaluators)
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

                    Pair<FeatureAction, DataFilterEvaluator> evaluator = createEvaluator(featureAction);
                    // Sanity check to avoid creating new actions that won't be
                    // accepted, which can actually happen.
                    if (evaluator.getSecondObject().accepts(element))
                    {
                        myRegistry.add(dataType.getTypeKey(), Collections.singleton(featureAction), this);
                        passingFeatureActions.add(featureAction);
                        actionsAndEvaluators.add(evaluator);

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
                clearActions(ids, dataType);
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
                for (Entry<String, DataTypeStyleConfig> entry : myTypeKeysAndStyles.entrySet())
                {
                    DataTypeStyleConfig newConfig = config.getDataTypeStyleByTypeKey(entry.getKey());
                    if (!EqualsHelper.equals(newConfig, entry.getValue()))
                    {
                        handleModelChange(entry.getKey());
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
            Collection<FeatureAction> featureActions)
    {
        Map<Collection<Action>, List<MapDataElement>> actionToElementsMap = New.map();

        // Set the type key so that the evaluator will accept it
        for (FeatureAction action : featureActions)
        {
            action.getFilter().getSource().setTypeKey(dataType.getTypeKey());
        }

        Set<String> groupsToSatisfy = featureActions.stream().map(a -> a.getGroupName()).distinct()
                .filter(g -> myRegistry.getActionCreator(g) != null).collect(Collectors.toSet());

        List<Pair<FeatureAction, DataFilterEvaluator>> actionsAndEvaluators = StreamUtilities.map(featureActions,
                this::createEvaluator);
        Collection<MapDataElement> elements = FeatureActionUtilities.getDataElements(myMantleToolbox, ids, dataType);
        for (MapDataElement element : elements)
        {
            Collection<FeatureAction> passingFeatureActions = getPassingActions(element, actionsAndEvaluators);

            handleGroupUnsatisfaction(element, passingFeatureActions, groupsToSatisfy, dataType, actionsAndEvaluators);

            Set<Action> passingActions = new LinkedHashSet<>();
            for (FeatureAction featureAction : passingFeatureActions)
            {
                passingActions.addAll(featureAction.getActions());
            }

            if (!passingActions.isEmpty())
            {
                actionToElementsMap.computeIfAbsent(passingActions, k -> New.list()).add(element);
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
