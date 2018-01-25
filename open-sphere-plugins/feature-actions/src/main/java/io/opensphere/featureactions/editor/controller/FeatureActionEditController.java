package io.opensphere.featureactions.editor.controller;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;

/**
 * Converts a list of {@link FeatureAction} to a single
 * {@link SimpleFeatureAction} organized in their respective groups.
 */
public class FeatureActionEditController
{
    /**
     * Used to save or delete models.
     */
    private final FeatureActionsRegistry myActionRegistry;

    /**
     * Keeps the saved model and the editor model in sync.
     */
    private final SimpleFeatureActionsAdapter myAdapter;

    /**
     * Defaults new feature actions values to some educated guesses, helping the
     * user create feature actions faster.
     */
    private final FeatureActionsDefaulter myDefaulter;

    /**
     * The id of the layer we are editing feature actions for.
     */
    private final String myLayerId;

    /**
     * The model used by the simple editor.
     */
    private final SimpleFeatureActions myModel;

    /**
     * Constructs a new edit controller.
     *
     * @param actionRegistry Used to save or delete feature actions.
     * @param layerId The id of the layer we are editing feature actions for.
     */
    public FeatureActionEditController(FeatureActionsRegistry actionRegistry, String layerId)
    {
        myActionRegistry = actionRegistry;
        myLayerId = layerId;
        myModel = provideSimple();
        myAdapter = new SimpleFeatureActionsAdapter(myModel);
        myDefaulter = new FeatureActionsDefaulter(myModel);
    }

    /**
     * Saves all of the changes to the registry.
     */
    public void applyChanges()
    {
        Set<String> currentIds = New.set();
        List<FeatureAction> actions = New.list();
        for (SimpleFeatureActionGroup group : myModel.getFeatureGroups())
        {
            for (SimpleFeatureAction action : group.getActions())
            {
                actions.add(action.getFeatureAction());
                currentIds.add(action.getFeatureAction().getId());
            }
        }

        List<FeatureAction> existing = myActionRegistry.get(myLayerId);
        List<FeatureAction> deleted = New.list();
        for (FeatureAction exist : existing)
        {
            if (!currentIds.contains(exist.getId()))
            {
                deleted.add(exist);
            }
        }

        if (!deleted.isEmpty())
        {
            myActionRegistry.remove(myLayerId, deleted, this);
        }

        if (!actions.isEmpty())
        {
            myActionRegistry.update(myLayerId, actions, this);
        }
    }

    /**
     * Stops listening to changes.
     */
    public void close()
    {
        myAdapter.close();
        myDefaulter.close();
    }

    /**
     * Gets the model the editor should edit.
     *
     * @return The editors model.
     */
    public SimpleFeatureActions getModel()
    {
        return myModel;
    }

    /**
     * Converts a list of {@link FeatureAction} to a single
     * {@link SimpleFeatureAction} organized in their respective groups.
     *
     * @return The actions organized by their groups.
     */
    private SimpleFeatureActions provideSimple()
    {
        SimpleFeatureActions actions = new SimpleFeatureActions(myLayerId);

        Map<String, List<SimpleFeatureAction>> grouped = New.insertionOrderMap();

        for (FeatureAction action : myActionRegistry.get(myLayerId))
        {
            if (action.isVisible())
            {
                grouped.computeIfAbsent(action.getGroupName(), k -> New.list()).add(new SimpleFeatureAction(
                        XMLUtilities.jaxbClone(action, FeatureAction.class)));
            }
        }

        for (Entry<String, List<SimpleFeatureAction>> entry : grouped.entrySet())
        {
            SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
            group.setGroupName(entry.getKey());
            group.getActions().addAll(entry.getValue());
            actions.getFeatureGroups().add(group);
        }

        return actions;
    }
}
