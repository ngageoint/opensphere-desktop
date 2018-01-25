package io.opensphere.featureactions.editor.controller;

import java.util.List;
import java.util.Map;

import javafx.collections.ListChangeListener;

import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;

/**
 * Manages the {@link SimpleFeatureActionGroupAdapter} when groups are added or
 * removed.
 */
public class SimpleFeatureActionsAdapter implements ListChangeListener<SimpleFeatureActionGroup>
{
    /**
     * All the actions defined for a given layer.
     */
    private final SimpleFeatureActions myActions;

    /**
     * The collection of group adapters for each group.
     */
    private final Map<SimpleFeatureActionGroup, SimpleFeatureActionGroupAdapter> myAdapters = New.map();

    /**
     * Constructs a new {@link SimpleFeatureActionsAdapter}.
     *
     * @param actions All the actions defined for a given layer.
     */
    public SimpleFeatureActionsAdapter(SimpleFeatureActions actions)
    {
        myActions = actions;
        handleNew(myActions.getFeatureGroups());
        myActions.getFeatureGroups().addListener(this);
    }

    /**
     * Stops listening to changes.
     */
    public void close()
    {
        myActions.getFeatureGroups().removeListener(this);
        for (SimpleFeatureActionGroupAdapter adapter : myAdapters.values())
        {
            adapter.close();
        }
    }

    @Override
    public void onChanged(Change<? extends SimpleFeatureActionGroup> c)
    {
        while (c.next())
        {
            handleNew(c.getAddedSubList());

            for (SimpleFeatureActionGroup group : c.getRemoved())
            {
                SimpleFeatureActionGroupAdapter adapter = myAdapters.remove(group);
                if (adapter != null)
                {
                    adapter.close();
                }
            }
        }
    }

    /**
     * Handles the adding of groups to the model. Constructs a new adapter for
     * each new group.
     *
     * @param added The list of groups to create adapters for.
     */
    private void handleNew(List<? extends SimpleFeatureActionGroup> added)
    {
        for (SimpleFeatureActionGroup group : added)
        {
            myAdapters.put(group, new SimpleFeatureActionGroupAdapter(group));
        }
    }
}
