package io.opensphere.mantle.controller.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;

import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.controller.DataGroupActivationManager;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.event.ActiveDataGroupSavedSetsChangedEvent;
import io.opensphere.mantle.controller.event.ActiveDataGroupSavedSetsChangedEvent.ChangeType;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.ActiveGroupEntry;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoActiveHistoryRecord;
import io.opensphere.mantle.data.DataGroupInfoActiveSet;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultActiveGroupEntry;
import io.opensphere.mantle.data.impl.DefaultDataGroupActivator;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.dgset.v1.JAXBDataGroupInfoActiveSet;
import io.opensphere.mantle.data.impl.dgset.v1.JAXBDataGroupInfoActiveSetConfig;

/**
 * The Class DataGroupActivationController.
 */
@SuppressWarnings("PMD.GodClass")
class DataGroupActivationController implements DataGroupActivationManager
{
    /** The Constant ACTIVE_SET_PREFERENCE_KEY. */
    private static final String ACTIVE_SET_PREFERENCE_KEY = "DataGroupActiveSetConfig";

    /** The Constant DEFAULT_ACTIVE_SET_PREFERENCE_KEY. */
    private static final String DEFAULT_ACTIVE_SET_PREFERENCE_KEY = "DefaultActiveSet";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataGroupActivationController.class);

    /** The Constant USER_ACTIVATED_SET_NAME. */
    private static final String USER_ACTIVATED_SET_NAME = "DataGroupController.UserActivatedSets";

    /** The activation change support. */
    private final WeakChangeSupport<Runnable> myActivationChangeSupport = WeakChangeSupport.create();

    /** The Constant ourEventExecutor. */
    private final ThreadPoolExecutor myActivationExecutor = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("DataGroupActivationCommandProcessor::Work"));

    /** The set of all active groups. */
    private final Set<DataGroupInfo> myActiveGroups = Collections.synchronizedSet(New.set());

    /** The active set config. */
    private JAXBDataGroupInfoActiveSetConfig myActiveSetConfig;

    /** The controller. */
    private final DataGroupControllerImpl myController;

    /** The data group activation listener. */
    private final ActivationListener myDataGroupActivationListener = new AbstractActivationListener()
    {
        @Override
        public void handleCommit(boolean active, DataGroupInfo dgi, io.opensphere.core.util.lang.PhasedTaskCanceller canceller)
        {
            handleActivationStateChange(active, dgi);
        }
    };

    /** The Default active layers. */
    private DataGroupInfoActiveSet myDefaultActiveLayers;

    /**
     * Accumulation of groups that have been activated since the last event was
     * fired.
     */
    @GuardedBy("this")
    private final Set<DataGroupInfo> myDeltaActivatedGroups = New.set();

    /**
     * Accumulation of groups that have been deactivated since the last event
     * was fired.
     */
    @GuardedBy("this")
    private final Set<DataGroupInfo> myDeltaDeactivatedGroups = New.set();

    /** Executor used to consolidate activation events. */
    private final Executor myEventExecutor = CommonTimer.createProcrastinatingExecutor(1000);

    /** The initial activation performed. */
    private final AtomicBoolean myInitialActivationPerformed = new AtomicBoolean(false);

    /** The life cycle event listener. */
    private EventListener<ApplicationLifecycleEvent> myLifeCycleEventListener;

    /**
     * Checks if is user activation state control.
     *
     * @param group the group
     * @return true, if is user activation state control
     */
    private static boolean isUserActivationStateControl(ActiveGroupEntry group)
    {
        return group != null && isUserActivationStateControl(group.getId());
    }

    /**
     * Checks if is user activation state control.
     *
     * @param id the id
     * @return true, if is user activation state control
     */
    private static boolean isUserActivationStateControl(String id)
    {
        DataGroupInfo dgi = DefaultDataGroupInfo.getKeyMap().getGroupForKey(id);
        return dgi == null || dgi.userActivationStateControl();
    }

    /**
     * Mark members in use.
     *
     * @param dgi the {@link DataGroupInfo}
     * @param inUse the in use
     * @param registrant the registrant
     */
    @SuppressWarnings("PMD.SimplifiedTernary")
    private static void markMembersInUse(DataGroupInfo dgi, boolean inUse, Object registrant)
    {
        if (dgi.hasMembers(false))
        {
            Consumer<? super DataTypeInfo> action = inUse ? dti -> dti.registerInUse(registrant, true)
                    : dti -> dti.unregisterInUse(registrant);
            dgi.getMembers(false).forEach(action);
        }
    }

    /**
     * Instantiates a new data group activation command processor.
     *
     * @param controller the controller
     */
    public DataGroupActivationController(DataGroupControllerImpl controller)
    {
        myController = controller;
        myActivationExecutor.allowCoreThreadTimeOut(true);

        // Initialize active set config.
        myActiveSetConfig = myController.getToolbox().getPreferencesRegistry().getPreferences(DataGroupController.class)
                .getJAXBObject(JAXBDataGroupInfoActiveSetConfig.class, ACTIVE_SET_PREFERENCE_KEY, null);
        if (myActiveSetConfig == null)
        {
            myActiveSetConfig = new JAXBDataGroupInfoActiveSetConfig();
        }

        myDefaultActiveLayers = myController.getToolbox().getPreferencesRegistry()
                .getPreferences(DataGroupActivationController.class)
                .getJAXBObject(JAXBDataGroupInfoActiveSet.class, DEFAULT_ACTIVE_SET_PREFERENCE_KEY, null);
        if (myDefaultActiveLayers == null)
        {
            myDefaultActiveLayers = new JAXBDataGroupInfoActiveSet();
        }

        /* If we don't have a last active set, try to copy the last active set
         * from our default set. */
        DataGroupInfoActiveSet initialActiveSet = myActiveSetConfig.getSetByName(USER_ACTIVATED_SET_NAME);
        if ((initialActiveSet == null || initialActiveSet.getGroupEntries().isEmpty())
                && CollectionUtilities.hasContent(myDefaultActiveLayers.getGroupEntries()))
        {
            JAXBDataGroupInfoActiveSet set = new JAXBDataGroupInfoActiveSet(USER_ACTIVATED_SET_NAME,
                    myDefaultActiveLayers.getGroupEntries());
            myActiveSetConfig.addSet(set);
        }

        final DataGroupInfoActiveSet initialSet = myActiveSetConfig.getSetByName(USER_ACTIVATED_SET_NAME);
        if (initialSet != null && !initialSet.getGroupEntries().isEmpty())
        {
            myLifeCycleEventListener = event ->
            {
                if (event.getStage() == ApplicationLifecycleEvent.Stage.PLUGINS_INITIALIZED)
                {
                    Thread t = new Thread(() ->
                    {
                        List<String> groupIds = initialSet.getGroupIds();
                        setGroupsActiveById(groupIds, true);
                        myInitialActivationPerformed.set(true);
                    });
                    t.start();
                }
            };
            myController.getToolbox().getEventManager().subscribe(ApplicationLifecycleEvent.class, myLifeCycleEventListener);
        }
    }

    @Override
    public void addActivationListener(Runnable activationListener)
    {
        myActivationChangeSupport.addListener(activationListener);
    }

    /**
     * Clean up group.
     *
     * @param dgi the dgi
     */
    public synchronized void cleanUpGroup(DataGroupInfo dgi)
    {
        updateUserActivatedSet(false, new DefaultActiveGroupEntry(dgi.getDisplayNameWithPostfixTopParentName(), dgi.getId()));
        markMembersInUse(dgi, false, myController);
        deregisterGroup(dgi);
    }

    /**
     * Deregister a data group.
     *
     * @param group The group.
     */
    public void deregisterGroup(DataGroupInfo group)
    {
        group.activationProperty().removeListener(myDataGroupActivationListener);
    }

    @Override
    public Service getActivationListenerService(Runnable listener)
    {
        return myActivationChangeSupport.getListenerService(listener);
    }

    /**
     * Gets the active group ids.
     *
     * @return the active group ids
     */
    public Set<String> getActiveGroupIds()
    {
        return myActiveGroups.stream().filter(g -> !g.activationProperty().isActivatingOrDeactivating()).map(g -> g.getId())
                .collect(Collectors.toSet());
    }

    /**
     * Gets the active groups.
     *
     * @return the active groups
     */
    public List<DataGroupInfo> getActiveGroups()
    {
        return New.list(myActiveGroups);
    }

    /**
     * Gets the active history list.
     *
     * @return the active history list
     */
    public List<DataGroupInfoActiveHistoryRecord> getActiveHistoryList()
    {
        return myActiveSetConfig.getActivityHistory();
    }

    /**
     * Gets the active set.
     *
     * @param setName the set name
     * @return the active set
     */
    public DataGroupInfoActiveSet getActiveSet(String setName)
    {
        return myActiveSetConfig.getSetByName(setName);
    }

    /**
     * Gets the active set names.
     *
     * @return the active set names
     */
    public List<String> getActiveSetNames()
    {
        List<String> names = New.list(myActiveSetConfig.getSetNames());
        names.remove(USER_ACTIVATED_SET_NAME);
        return names;
    }

    /**
     * Initial activation performed.
     *
     * @return true, if successful
     */
    public boolean initialActivationPerformed()
    {
        return myInitialActivationPerformed.get();
    }

    /**
     * Load active set.
     *
     * @param setName the set name
     * @param exclusive the exclusive
     * @return true, if successful
     */
    public boolean loadActiveSet(String setName, boolean exclusive)
    {
        return loadActiveSetInternal(myActiveSetConfig.getSetByName(setName), exclusive);
    }

    /**
     * Register a data group so that when it is activated or deactivated, events
     * will be fired, and the group will be properly added or removed from my
     * active group collection. This registers the group and its children.
     *
     * @param group The group.
     */
    public void registerGroup(DataGroupInfo group)
    {
        group.groupStream().forEach(g ->
        {
            g.activationProperty().removeListener(myDataGroupActivationListener);
            g.activationProperty().addListener(myDataGroupActivationListener);
        });
        sendActivationsToNecessaryGroups(group);
    }

    @Override
    public void removeActivationListener(Runnable activationListener)
    {
        myActivationChangeSupport.removeListener(activationListener);
    }

    /**
     * Removes the active set.
     *
     * @param setName the set name
     * @return true, if successful
     */
    public boolean removeActiveSet(String setName)
    {
        boolean removed = !myActiveSetConfig.removeSet(setName).isEmpty();
        if (removed)
        {
            saveActiveSetConfig();
            myController.getToolbox().getEventManager().publishEvent(
                    new ActiveDataGroupSavedSetsChangedEvent(setName, ActiveDataGroupSavedSetsChangedEvent.ChangeType.REMOVE));
        }
        return removed;
    }

    /**
     * Restore default set.
     *
     * @param exclusive the exclusive
     * @return true, if successful
     */
    public boolean restoreDefaultSet(boolean exclusive)
    {
        return loadActiveSetInternal(myDefaultActiveLayers, exclusive);
    }

    /**
     * Save active set.
     *
     * @param setName the set name
     */
    public void saveActiveSet(String setName)
    {
        saveActiveSet(setName, convertToEntrySet(filterOutNonUserActivateableGroupsById(getActiveGroupIds())));
    }

    /**
     * Save an active set with a specific set of group ids.
     *
     * Note: Overwrites any set with that same name.
     *
     * @param setName the name of the set.
     * @param groups the groups
     */
    public void saveActiveSet(String setName, Collection<? extends ActiveGroupEntry> groups)
    {
        Collection<? extends ActiveGroupEntry> filteredGroup = filterOutNonUserActivateableGroups(groups);
        JAXBDataGroupInfoActiveSet set = new JAXBDataGroupInfoActiveSet(setName, filteredGroup);
        Set<DataGroupInfoActiveSet> removed = myActiveSetConfig.removeSet(setName);
        myActiveSetConfig.addSet(set);
        saveActiveSetConfig();
        myController.getToolbox().getEventManager().publishEvent(new ActiveDataGroupSavedSetsChangedEvent(setName,
                removed.isEmpty() ? ActiveDataGroupSavedSetsChangedEvent.ChangeType.ADD : ChangeType.CHANGED));
    }

    /**
     * Sets the groups active by id.
     *
     * @param dgiKeyCollection the dgi key collection
     * @param active the active
     */
    public void setGroupsActiveById(Collection<String> dgiKeyCollection, boolean active)
    {
        Set<DataGroupInfo> dgiSet = New.set(DefaultDataGroupInfo.getKeyMap().getGroupsForKeys(dgiKeyCollection).values());
        try
        {
            new DefaultDataGroupActivator(myController.getToolbox().getEventManager()).setGroupsActive(dgiSet, active);
        }
        catch (InterruptedException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Gets the default active group ids.
     *
     * @return the default active group ids
     */
    protected Set<String> getDefaultActiveGroupIds()
    {
        Set<String> result = New.set();

        if (myDefaultActiveLayers != null)
        {
            result.addAll(myDefaultActiveLayers.getGroupIds());
        }
        return result;
    }

    /**
     * Gets the user activated set.
     *
     * @return the user activated set
     */
    protected Set<String> getUserActivatedGroupIds()
    {
        Set<String> result = New.set();
        DataGroupInfoActiveSet initialActiveSet = myActiveSetConfig.getSetByName(USER_ACTIVATED_SET_NAME);
        if (initialActiveSet != null)
        {
            result.addAll(initialActiveSet.getGroupIds());
        }
        return result;
    }

    /**
     * Gets the user deactivated set.
     *
     * @return the user deactivated set
     */
    protected Set<String> getUserDeactivatedGroupIds()
    {
        Set<String> historyIds = getActiveHistoryList().stream().map(rec -> rec.getId()).collect(Collectors.toSet());
        Set<String> userActivatedGroupIds = getUserActivatedGroupIds();
        return New.set(CollectionUtilities.difference(historyIds, userActivatedGroupIds));
    }

    /**
     * Convert to entry set.
     *
     * @param groupIds the group ids
     * @return the sets the
     */
    private Set<ActiveGroupEntry> convertToEntrySet(Collection<String> groupIds)
    {
        Set<ActiveGroupEntry> set = New.set();
        Map<String, DataGroupInfo> map = DefaultDataGroupInfo.getKeyMap().getGroupsForKeys(groupIds);
        for (Map.Entry<String, DataGroupInfo> entry : map.entrySet())
        {
            set.add(new DefaultActiveGroupEntry(entry.getValue().getDisplayNameWithPostfixTopParentName(), entry.getKey()));
        }
        return set;
    }

    /**
     * Filter out non user activate able groups.
     *
     * @param groups the groups
     * @return the collection
     */
    private Collection<? extends ActiveGroupEntry> filterOutNonUserActivateableGroups(
            Collection<? extends ActiveGroupEntry> groups)
    {
        List<ActiveGroupEntry> result = New.list();
        for (ActiveGroupEntry age : groups)
        {
            if (isUserActivationStateControl(age))
            {
                result.add(age);
            }
        }
        return result;
    }

    /**
     * Filter out non user activate able groups.
     *
     * @param groupIds the group ids
     * @return the collection
     */
    private Set<String> filterOutNonUserActivateableGroupsById(Collection<String> groupIds)
    {
        Set<String> result = New.set();
        for (String dgiId : groupIds)
        {
            if (isUserActivationStateControl(dgiId))
            {
                DataGroupInfo dgi = DefaultDataGroupInfo.getKeyMap().getGroupForKey(dgiId);
                if (dgi == null || !dgi.hasChildren())
                {
                    result.add(dgiId);
                }
            }
        }
        return result;
    }

    /**
     * Fire an event that signals the groups that have been activated or
     * deactivated since the last event.
     */
    private void fireActivationEvent()
    {
        Set<DataGroupInfo> activated;
        Set<DataGroupInfo> deactivated;
        synchronized (this)
        {
            activated = New.set(myDeltaActivatedGroups);
            myDeltaActivatedGroups.clear();
            deactivated = New.set(myDeltaDeactivatedGroups);
            myDeltaDeactivatedGroups.clear();
        }

        myController.getToolbox().getEventManager().publishEvent(new ActiveDataGroupsChangedEvent(this, activated, deactivated));
        myActivationChangeSupport.notifyListeners(r -> r.run());
    }

    /**
     * Gets the non user activate able groups by id.
     *
     * @param groupIds the group ids
     * @return the non user activate able groups by id
     */
    private Set<String> getNonUserActivateableGroupsById(Collection<String> groupIds)
    {
        Set<String> result = New.set();
        for (String dgiId : groupIds)
        {
            DataGroupInfo dgi = DefaultDataGroupInfo.getKeyMap().getGroupForKey(dgiId);
            if (dgi != null && !dgi.userActivationStateControl())
            {
                result.add(dgiId);
            }
        }
        return result;
    }

    /**
     * Handle a change to the activation state of a group.
     *
     * @param active {@code true} if the group is active.
     * @param dgi The data group info.
     */
    private void handleActivationStateChange(boolean active, DataGroupInfo dgi)
    {
        markMembersInUse(dgi, active, myController);
        synchronized (this)
        {
            if (active)
            {
                myActiveGroups.add(dgi);
                myDeltaActivatedGroups.add(dgi);
                myDeltaDeactivatedGroups.remove(dgi);
            }
            else
            {
                myActiveGroups.remove(dgi);
                myDeltaDeactivatedGroups.add(dgi);
                myDeltaActivatedGroups.remove(dgi);
            }
            updateUserActivatedSet(active,
                    new DefaultActiveGroupEntry(dgi.getDisplayNameWithPostfixTopParentName(), dgi.getId()));
            myEventExecutor.execute(DataGroupActivationController.this::fireActivationEvent);
        }
    }

    /**
     * Load active set.
     *
     * @param setToActivate the set to activate
     * @param exclusive the exclusive
     * @return true, if successful
     */
    private boolean loadActiveSetInternal(DataGroupInfoActiveSet setToActivate, boolean exclusive)
    {
        if (setToActivate != null)
        {
            Set<String> currentSet = getActiveGroupIds();

            /* Determine which groups are not user activate able by the user and
             * remove them from the current set so they are not de-activated by
             * a user load set. */
            Set<String> nonUserSet = getNonUserActivateableGroupsById(currentSet);
            currentSet.removeAll(nonUserSet);
            Set<String> currentSet2 = New.set(currentSet);
            Set<String> namedSet = New.set(setToActivate.getGroupIds());

            // Set to deactivate is those in the current set that are not in the
            // named set.
            currentSet.removeAll(namedSet);
            if (exclusive && !currentSet.isEmpty())
            {
                setGroupsActiveById(currentSet, false);
            }

            // Set to activate are those in the named set that are not in the
            // current set.
            namedSet.removeAll(currentSet2);
            if (!namedSet.isEmpty())
            {
                setGroupsActiveById(namedSet, true);
            }
            return true;
        }
        return false;
    }

    /**
     * Save config.
     */
    private void saveActiveSetConfig()
    {
        JAXBDataGroupInfoActiveSetConfig configToSave = new JAXBDataGroupInfoActiveSetConfig(myActiveSetConfig);
        configToSave.sortAndDeduplicateActivityHistory();
        myController.getToolbox().getPreferencesRegistry().getPreferences(DataGroupController.class)
                .putJAXBObject(ACTIVE_SET_PREFERENCE_KEY, configToSave, false, this);
    }

    /**
     * Send activations to necessary groups that are in the active set if they
     * are added to the controller after the plugins are initialized.
     *
     * @param group the group
     */
    private void sendActivationsToNecessaryGroups(DataGroupInfo group)
    {
        Set<String> activeGroupIds = getUserActivatedGroupIds();
        List<DataGroupInfo> toActivate = group.groupStream()
                .filter(g -> !g.activationProperty().isActivatingOrDeactivating() && activeGroupIds.contains(g.getId()))
                .collect(Collectors.toList());
        if (!toActivate.isEmpty())
        {
            if (LOGGER.isTraceEnabled())
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Sending Activations to Necessary Groups: \n");
                for (DataGroupInfo dgi : toActivate)
                {
                    sb.append("    ").append(dgi.getId()).append('\n');
                }
                LOGGER.trace(sb.toString());
            }
            try
            {
                new DefaultDataGroupActivator(myController.getToolbox().getEventManager()).setGroupsActive(toActivate, true);
            }
            catch (InterruptedException e)
            {
                LOGGER.error(e, e);
            }
        }
    }

    /**
     * Update user activated set.
     *
     * Get all the currently active ids, add all the old active ids, then add or
     * remove the new id as necessary.
     *
     * @param add true to add the id to the set, false to remove it.
     * @param group the group to add/remove.
     */
    private void updateUserActivatedSet(boolean add, ActiveGroupEntry group)
    {
        Set<ActiveGroupEntry> totalSet = New.set(convertToEntrySet(filterOutNonUserActivateableGroupsById(getActiveGroupIds())));
        Set<DataGroupInfoActiveSet> oldSetSet = myActiveSetConfig.removeSet(USER_ACTIVATED_SET_NAME);
        for (DataGroupInfoActiveSet set : oldSetSet)
        {
            for (ActiveGroupEntry entry : set.getGroupEntries())
            {
                if (isUserActivationStateControl(entry))
                {
                    totalSet.add(new DefaultActiveGroupEntry(entry));
                }
            }
        }
        if (add)
        {
            if (isUserActivationStateControl(group))
            {
                totalSet.add(group);
            }
        }
        else
        {
            totalSet.remove(group);
        }
        if (LOGGER.isTraceEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Updating User Activated Set To: \n");
            for (ActiveGroupEntry entry : totalSet)
            {
                sb.append("     [Name: ").append(entry.getName()).append("  Id: ").append(entry.getId()).append("]\n");
            }
            LOGGER.trace(sb.toString());
        }
        JAXBDataGroupInfoActiveSet set = new JAXBDataGroupInfoActiveSet(USER_ACTIVATED_SET_NAME, totalSet);
        myActiveSetConfig.addSet(set);
        saveActiveSetConfig();
    }
}
