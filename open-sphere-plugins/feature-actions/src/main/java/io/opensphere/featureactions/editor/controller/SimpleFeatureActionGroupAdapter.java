package io.opensphere.featureactions.editor.controller;

import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.model.FeatureAction;

/**
 * Keeps the group name of a {@link FeatureAction} in sync with the
 * {@link SimpleFeatureActionGroup} they belong to. Also manages
 * {@link FilterActionAdapter} and {@link StyleActionAdapter} as simple feature
 * actions are added or removed.
 */
public class SimpleFeatureActionGroupAdapter implements ListChangeListener<SimpleFeatureAction>
{
    /**
     * The currently active adapters.
     */
    private final Map<SimpleFeatureAction, Pair<FilterActionAdapter, StyleActionAdapter>> myAdapters = New.map();

    /**
     * The group we are keeping in sync.
     */
    private final SimpleFeatureActionGroup myGroup;

    /**
     * Just a generic string listener to update simple filter to an actual
     * filter.
     */
    private final ChangeListener<String> myNameListener = this::nameChanged;

    /**
     * Creates a new group adapter.
     *
     * @param group The group to adapt.
     */
    public SimpleFeatureActionGroupAdapter(SimpleFeatureActionGroup group)
    {
        myGroup = group;
        handleNew(group.getActions());
        myGroup.groupNameProperty().addListener(myNameListener);
        myGroup.getActions().addListener(this);
    }

    /**
     * Stops listening for changes.
     */
    public void close()
    {
        myGroup.groupNameProperty().removeListener(myNameListener);
        myGroup.getActions().removeListener(this);
        for (Pair<FilterActionAdapter, StyleActionAdapter> adapters : myAdapters.values())
        {
            adapters.getFirstObject().close();
            adapters.getSecondObject().close();
        }
    }

    @Override
    public void onChanged(Change<? extends SimpleFeatureAction> c)
    {
        while (c.next())
        {
            handleNew(c.getAddedSubList());

            for (SimpleFeatureAction removed : c.getRemoved())
            {
                removed.getFeatureAction().setGroupName(null);
                Pair<FilterActionAdapter, StyleActionAdapter> adapters = myAdapters.get(removed);
                if (adapters != null)
                {
                    adapters.getFirstObject().close();
                    adapters.getSecondObject().close();
                }
            }
        }
    }

    /**
     * Handles new feature actions and sets up the adapters.
     *
     * @param newActions The new actions.
     */
    private void handleNew(List<? extends SimpleFeatureAction> newActions)
    {
        for (SimpleFeatureAction added : newActions)
        {
            added.getFeatureAction().setGroupName(myGroup.getGroupName());
            FilterActionAdapter filterAdapter = new FilterActionAdapter(added);
            StyleActionAdapter styleAdapter = new StyleActionAdapter(added);
            myAdapters.put(added, new Pair<FilterActionAdapter, StyleActionAdapter>(filterAdapter, styleAdapter));
        }
    }

    /**
     * Listens for any string property changes from the simple filter.
     *
     * @param observable A string property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void nameChanged(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        for (SimpleFeatureAction action : myGroup.getActions())
        {
            action.getFeatureAction().setGroupName(newValue);
        }
    }
}
