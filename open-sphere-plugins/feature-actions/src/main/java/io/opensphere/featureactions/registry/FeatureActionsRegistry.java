package io.opensphere.featureactions.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.jcip.annotations.ThreadSafe;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.FeatureActions;

/** Registry for feature actions. */
@ThreadSafe
public class FeatureActionsRegistry
{
    /** Map from feature action group to optional creator. */
    private final Map<String, FeatureActionCreator> myGroupToCreatorMap = Collections.synchronizedMap(New.map());

    /**
     * The preferences where the actions will be saved.
     */
    private final Preferences myPrefs;

    /**
     * Constructs a new {@link FeatureActionsRegistry}.
     *
     * @param prefsRegistry The preferences where the actions will be saved.
     */
    public FeatureActionsRegistry(PreferencesRegistry prefsRegistry)
    {
        myPrefs = prefsRegistry.getPreferences(FeatureActionsRegistry.class);
        removeInvisibleActions();
    }

    /**
     * Adds feature actions for the layer key.
     *
     * @param layerKey the layer key (e.g. data type key)
     * @param actions the feature actions to add
     * @param source the source
     */
    public synchronized void add(String layerKey, Collection<? extends FeatureAction> actions, Object source)
    {
        FeatureActions featureActions = myPrefs.getJAXBObject(FeatureActions.class, layerKey, new FeatureActions());
        featureActions.getActions().addAll(actions);
        myPrefs.putJAXBObject(layerKey, featureActions, false, source);
    }

    /**
     * Removes all feature actions for the layer key and group.
     *
     * @param layerKey the layer key (e.g. data type key)
     * @param group the group to remove
     * @param source the source
     */
    public synchronized void removeGroup(String layerKey, String group, Object source)
    {
        FeatureActions featureActions = myPrefs.getJAXBObject(FeatureActions.class, layerKey, null);
        if (featureActions != null)
        {
            List<FeatureAction> layerActions = featureActions.getActions();
            boolean wasRemoved = layerActions.removeIf(a -> group.equals(a.getGroupName()));
            if (wasRemoved)
            {
                if (layerActions.isEmpty())
                {
                    myPrefs.remove(layerKey, source);
                }
                else
                {
                    myPrefs.putJAXBObject(layerKey, featureActions, false, source);
                }
            }
        }
    }

    /**
     * Removes feature actions for the layer key.
     *
     * @param layerKey the layer key (e.g. data type key)
     * @param actions the feature actions to remove
     * @param source the source
     */
    public synchronized void remove(String layerKey, Collection<? extends FeatureAction> actions, Object source)
    {
        FeatureActions featureActions = myPrefs.getJAXBObject(FeatureActions.class, layerKey, new FeatureActions());
        List<FeatureAction> layerActions = featureActions.getActions();
        if (layerActions != null)
        {
            Set<String> idsToRemove = New.set();
            for (FeatureAction action : actions)
            {
                idsToRemove.add(action.getId());
            }

            List<Integer> toRemove = New.list();
            int index = 0;
            for (FeatureAction action : layerActions)
            {
                if (idsToRemove.contains(action.getId()))
                {
                    toRemove.add(Integer.valueOf(index));
                }
                index++;
            }

            for (int i = toRemove.size() - 1; i >= 0; i--)
            {
                layerActions.remove(toRemove.get(i).intValue());
            }

            if (layerActions.isEmpty())
            {
                myPrefs.remove(layerKey, source);
            }
            else
            {
                myPrefs.putJAXBObject(layerKey, featureActions, false, source);
            }
        }
    }

    /**
     * Updates feature actions for the given layer.
     *
     * @param layerKey The layer to update feature actions for.
     * @param actions The actions to update.
     * @param source the source
     */
    public synchronized void update(String layerKey, Collection<? extends FeatureAction> actions, Object source)
    {
        FeatureActions featureActions = myPrefs.getJAXBObject(FeatureActions.class, layerKey, new FeatureActions());
        List<FeatureAction> layerActions = featureActions.getActions();
        if (layerActions != null)
        {
            Set<String> idsToRemove = New.set();
            for (FeatureAction action : actions)
            {
                idsToRemove.add(action.getId());
            }

            List<Integer> toRemove = New.list();
            int index = 0;
            for (FeatureAction action : layerActions)
            {
                if (idsToRemove.contains(action.getId()))
                {
                    toRemove.add(Integer.valueOf(index));
                }
                index++;
            }

            for (int i = toRemove.size() - 1; i >= 0; i--)
            {
                layerActions.remove(toRemove.get(i).intValue());
            }

            layerActions.addAll(actions);
            myPrefs.putJAXBObject(layerKey, featureActions, false, source);
        }
    }

    /**
     * Determines if the registry knows about the layer key.
     *
     * @param layerKey the layer key (e.g. data type key)
     * @return whether the registry know about the layer
     */
    public synchronized boolean hasLayer(String layerKey)
    {
        return myPrefs.getJAXBObject(FeatureActions.class, layerKey, null) != null;
    }

    /**
     * Gets feature actions for the layer key.
     *
     * @param layerKey the layer key (e.g. data type key)
     * @return the feature actions for the layer key
     */
    public synchronized List<FeatureAction> get(String layerKey)
    {
        return New.list(myPrefs.getJAXBObject(FeatureActions.class, layerKey, new FeatureActions()).getActions());
    }

    /**
     * Gets enabled feature actions for the layer key.
     *
     * @param layerKey the layer key (e.g. data type key)
     * @return the feature actions for the layer key
     */
    public List<FeatureAction> getEnabled(String layerKey)
    {
        List<FeatureAction> actions = get(layerKey);
        if (!actions.isEmpty())
        {
            actions = actions.stream().filter(fa -> fa.isEnabled()).collect(Collectors.toList());
        }
        return actions;
    }

    /**
     * Gets the prefs. Only use for listeners, don't abuse it.
     *
     * @return the prefs
     */
    public Preferences getPrefs()
    {
        return myPrefs;
    }

    /**
     * Sets the feature action creator for the group.
     *
     * @param group the group
     * @param creator the feature action creator
     */
    public void setActionCreator(String group, FeatureActionCreator creator)
    {
        myGroupToCreatorMap.put(group, creator);
    }

    /**
     * Gets the feature action creator for the group.
     *
     * @param group the group
     * @return the feature action creator
     */
    public FeatureActionCreator getActionCreator(String group)
    {
        return myGroupToCreatorMap.get(group);
    }

    /**
     * Removes invisible actions from the preferences/registry.
     */
    private void removeInvisibleActions()
    {
        for (String key : myPrefs.keys())
        {
            FeatureActions featureActions = myPrefs.getJAXBObject(FeatureActions.class, key, null);
            if (featureActions != null)
            {
                Collection<FeatureAction> invisibleActions = featureActions.getActions().stream().filter(a -> !a.isVisible())
                        .collect(Collectors.toSet());
                if (featureActions.getActions().removeAll(invisibleActions))
                {
                    myPrefs.putJAXBObject(key, featureActions, false, this);
                }
            }
        }
    }
}
