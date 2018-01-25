package io.opensphere.mantle.controller.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.concurrent.GuardedBy;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.event.AbstractRootDataGroupControllerEvent;
import io.opensphere.mantle.controller.event.impl.RootDataGroupAddedEvent;
import io.opensphere.mantle.controller.event.impl.RootDataGroupRemovedEvent;
import io.opensphere.mantle.controller.event.impl.RootDataGroupStructureChangeEvent;
import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.ActiveGroupEntry;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoActiveHistoryRecord;
import io.opensphere.mantle.data.DataGroupInfoActiveSet;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataGroupInfoChildRemovedEvent;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.util.TextViewDialog;

/**
 * The Class DataGroupControllerImpl.
 */
@SuppressWarnings("PMD.GodClass")
public class DataGroupControllerImpl implements DataGroupController, EventListener<AbstractDataGroupInfoChangeEvent>
{
    /** The activation controller. */
    private final DataGroupActivationController myActivationController;

    /** The data groups. */
    @GuardedBy("myDataGroupsLock")
    private final Set<DataGroupInfo> myDataGroups;

    /** The data groups read/write lock. */
    private final ReadWriteLock myDataGroupsLock = new ReentrantReadWriteLock();

    /** the event manager for firing events. */
    private final EventManager myEventManager;

    /**
     * Listener to be notified when a group is added.
     */
    private final Consumer<? super DataGroupInfo> myGroupAddedListener = this::handleGroupAdded;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new data group controller impl.
     *
     * @param tb the toolbox
     */
    public DataGroupControllerImpl(Toolbox tb)
    {
        myToolbox = tb;
        myActivationController = new DataGroupActivationController(this);
        myDataGroups = new HashSet<>();
        myEventManager = tb.getEventManager();
        myEventManager.subscribe(AbstractDataGroupInfoChangeEvent.class, this);

        EventQueueUtilities.runOnEDT(this::createAndInstallControllerPrintMenuItem);
    }

    @Override
    public void addActivationListener(Runnable activationListener)
    {
        myActivationController.addActivationListener(activationListener);
    }

    @Override
    public boolean addRootDataGroupInfo(DataGroupInfo dgi, Object source)
    {
        if (dgi == null)
        {
            return false;
        }

        if (!dgi.isRootNode())
        {
            throw new IllegalArgumentException("Only root DataGroupInfo may be added to the DataGroupController");
        }

        boolean added = false;
        myDataGroupsLock.writeLock().lock();
        try
        {
            added = myDataGroups.add(dgi);
        }
        finally
        {
            myDataGroupsLock.writeLock().unlock();
        }
        if (added)
        {
            /* Only add the listener to the root group, since children will
             * notify their parents. */
            dgi.getChildAddedChangeSupport().addListener(myGroupAddedListener);
            myGroupAddedListener.accept(dgi);
            myEventManager.publishEvent(new RootDataGroupAddedEvent(dgi, source));
        }
        return added;
    }

    @Override
    public void cleanUpGroup(DataGroupInfo dgi)
    {
        myActivationController.cleanUpGroup(dgi);
    }

    @Override
    public List<DataGroupInfo> createGroupList(Comparator<DataGroupInfo> comparator, Predicate<? super DataGroupInfo> nodeFilter)
    {
        List<DataGroupInfo> dataGroups = new ArrayList<>();

        myDataGroupsLock.readLock().lock();
        try
        {
            List<DataGroupInfo> dgiList = new ArrayList<>(myDataGroups);

            // Add the children that ain't filtered.
            for (DataGroupInfo dgi : dgiList)
            {
                Set<DataGroupInfo> set = dgi.createGroupSet(nodeFilter);
                dataGroups.addAll(set);
            }

            // Sort the list of children into order.
            Collections.sort(dgiList, comparator == null ? DataGroupInfo.DISPLAY_NAME_COMPARATOR : comparator);
        }
        finally
        {
            myDataGroupsLock.readLock().unlock();
        }
        return dataGroups;
    }

    @Override
    public TreeNode createTreeNode()
    {
        return createTreeNode(null, null);
    }

    @Override
    public TreeNode createTreeNode(Comparator<? super DataGroupInfo> comparator, Predicate<? super DataGroupInfo> nodeFilter)
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        rootNode.setAllowsChildren(false);
        myDataGroupsLock.readLock().lock();
        try
        {
            List<DataGroupInfo> dgiList = CollectionUtilities.sort(myDataGroups,
                    comparator == null ? DataGroupInfo.DISPLAY_NAME_COMPARATOR : comparator);

            // Add the children that are all but filtered.
            for (DataGroupInfo dgi : dgiList)
            {
                if (nodeFilter == null || nodeFilter.test(dgi))
                {
                    MutableTreeNode node = dgi.createTreeNode(comparator, nodeFilter);
                    if (!rootNode.getAllowsChildren())
                    {
                        rootNode.setAllowsChildren(true);
                    }
                    rootNode.add(node);
                }
            }
        }
        finally
        {
            myDataGroupsLock.readLock().unlock();
        }
        return rootNode;
    }

    @Override
    public Set<DataGroupInfo> findActiveDataGroupInfo(Predicate<? super DataGroupInfo> dgiFilter, boolean stopOnFirstFound)
    {
        Set<DataGroupInfo> resultSet = New.set();
        findDataGroupInfoIn(getActiveGroups(), dgiFilter, resultSet, stopOnFirstFound);
        return resultSet;
    }

    @Override
    public Collection<DataGroupInfo> findDataGroupInfo(Predicate<? super DataGroupInfo> dgiFilter,
            Collection<DataGroupInfo> results, boolean stopOnFirstFound)
    {
        myDataGroupsLock.readLock().lock();
        try
        {
            findDataGroupInfoIn(myDataGroups, dgiFilter, results, stopOnFirstFound);
        }
        finally
        {
            myDataGroupsLock.readLock().unlock();
        }
        return results;
    }

    @Override
    public DataTypeInfo findMemberById(final String dtiId)
    {
        Utilities.checkNull(dtiId, "dtiId");
        Set<DataTypeInfo> foundSet = findMembers(value -> value != null && Objects.equals(value.getTypeKey(), dtiId), true);
        return foundSet == null || foundSet.isEmpty() ? null : foundSet.iterator().next();
    }

    @Override
    public Set<DataTypeInfo> findMembers(Predicate<? super DataTypeInfo> dtiFilter, boolean stopOnFirstFound)
    {
        Set<DataTypeInfo> resultSet = New.set();
        myDataGroupsLock.readLock().lock();
        try
        {
            for (DataGroupInfo dgi : myDataGroups)
            {
                resultSet.addAll(dgi.findMembers(dtiFilter, true, stopOnFirstFound));
                if (stopOnFirstFound && !resultSet.isEmpty())
                {
                    break;
                }
            }
        }
        finally
        {
            myDataGroupsLock.readLock().unlock();
        }
        return resultSet.isEmpty() ? Collections.<DataTypeInfo>emptySet() : resultSet;
    }

    @Override
    public List<DataGroupInfo> getActiveGroups()
    {
        return myActivationController.getActiveGroups();
    }

    @Override
    public List<DataGroupInfoActiveHistoryRecord> getActiveHistoryList()
    {
        return myActivationController.getActiveHistoryList();
    }

    @Override
    public Set<DataTypeInfo> getActiveMembers(boolean recurseChildren)
    {
        Set<DataTypeInfo> resultSet = New.set();
        for (DataGroupInfo dgi : getActiveGroups())
        {
            resultSet.addAll(dgi.getMembers(recurseChildren));
        }
        return resultSet == null ? Collections.<DataTypeInfo>emptySet() : resultSet;
    }

    @Override
    public DataGroupInfoActiveSet getActiveSet(String setName)
    {
        return myActivationController.getActiveSet(setName);
    }

    @Override
    public List<String> getActiveSetNames()
    {
        return myActivationController.getActiveSetNames();
    }

    @Override
    public DataGroupInfo getDataGroupInfo(String key)
    {
        DataGroupInfo found = null;
        myDataGroupsLock.readLock().lock();
        try
        {
            for (DataGroupInfo dgi : myDataGroups)
            {
                found = dgi.getGroupById(key);
                if (found != null)
                {
                    break;
                }
            }
        }
        finally
        {
            myDataGroupsLock.readLock().unlock();
        }
        return found;
    }

    @Override
    public Set<DataGroupInfo> getDataGroupInfoSet()
    {
        Set<DataGroupInfo> dgiSet = new HashSet<>();
        myDataGroupsLock.readLock().lock();
        try
        {
            dgiSet.addAll(myDataGroups);
        }
        finally
        {
            myDataGroupsLock.readLock().unlock();
        }
        return dgiSet;
    }

    @Override
    public List<? extends DataGroupInfo> getDataGroupInfosWithDti(final DataTypeInfo dti)
    {
        List<DataGroupInfo> result = New.list();
        findDataGroupInfo(dgi -> dgi.hasMember(dti, true), result, false);
        return result;
    }

    @Override
    public Collection<? extends String> getQueryableDataTypeKeys()
    {
        Collection<String> typeKeys = New.collection();
        for (DataGroupInfo group : getActiveGroups())
        {
            if (group.hasMembers(false))
            {
                for (DataTypeInfo info : group.getMembers(false))
                {
                    if (info.isVisible() && info.isQueryable())
                    {
                        typeKeys.add(info.getTypeKey());
                    }
                }
            }
        }

        return typeKeys;
    }

    @Override
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public DataGroupInfo getTopParent(DataGroupInfo dgi)
    {
        return dgi == null ? null : dgi.getTopParent();
    }

    @Override
    public DataGroupInfo getTopParent(String dgiKey)
    {
        DataGroupInfo topParent = null;
        myDataGroupsLock.readLock().lock();
        try
        {
            for (DataGroupInfo dgi : myDataGroups)
            {
                if (dgi.getGroupById(dgiKey) != null)
                {
                    topParent = dgi;
                    break;
                }
            }
        }
        finally
        {
            myDataGroupsLock.readLock().unlock();
        }
        return topParent;
    }

    @Override
    public Set<String> getUserDeactivatedGroupIds()
    {
        return myActivationController.getUserDeactivatedGroupIds();
    }

    @Override
    public boolean hasDataGroupInfo(DataGroupInfo dgi)
    {
        boolean hasIt = false;
        if (dgi != null)
        {
            DataGroupInfo topParent = dgi.getTopParent();
            if (topParent != null)
            {
                myDataGroupsLock.readLock().lock();
                try
                {
                    hasIt = myDataGroups.contains(topParent);
                }
                finally
                {
                    myDataGroupsLock.readLock().unlock();
                }
            }
        }
        return hasIt;
    }

    @Override
    public boolean hasDataGroupInfo(String key)
    {
        return getDataGroupInfo(key) != null;
    }

    @Override
    public boolean isTypeActive(DataTypeInfo dti)
    {
        boolean active = false;
        if (dti != null)
        {
            active = dti.isInUseBy(this);
        }
        return active;
    }

    @Override
    public boolean loadActiveSet(String setName, boolean exclusive)
    {
        return myActivationController.loadActiveSet(setName, true);
    }

    @Override
    public void notify(AbstractDataGroupInfoChangeEvent event)
    {
        // If the event isn't a root type ( our own type ) and
        // the event originated from a group who's root is managed
        // by this controller, then rebroadcast a simplified group structure
        // changed event.
        if (!(event instanceof AbstractRootDataGroupControllerEvent))
        {
            DataGroupInfo topParent = event.getGroup().getTopParent();
            boolean topParentIsOneOfMyRootNodes = false;
            myDataGroupsLock.readLock().lock();
            try
            {
                topParentIsOneOfMyRootNodes = myDataGroups.contains(topParent);
            }
            finally
            {
                myDataGroupsLock.readLock().unlock();
            }
            if (topParentIsOneOfMyRootNodes)
            {
                myEventManager.publishEvent(new RootDataGroupStructureChangeEvent(topParent, event, event.getSource()));
                if (event instanceof DataGroupInfoChildRemovedEvent)
                {
                    myActivationController.deregisterGroup(event.getGroup());
                }
            }
        }
    }

    @Override
    public void removeActivationListener(Runnable activationListener)
    {
        myActivationController.removeActivationListener(activationListener);
    }

    @Override
    public boolean removeActiveSet(String setName)
    {
        return myActivationController.removeActiveSet(setName);
    }

    @Override
    public boolean removeDataGroupInfo(DataGroupInfo dgi, Object source)
    {
        boolean removed = false;

        if (dgi != null)
        {
            boolean rootGroupWasRemoved = false;
            myDataGroupsLock.writeLock().lock();
            try
            {
                DataGroupInfo topParent = dgi.getTopParent();
                if (myDataGroups.contains(topParent))
                {
                    if (Utilities.sameInstance(topParent, dgi))
                    {
                        rootGroupWasRemoved = myDataGroups.remove(dgi);
                    }
                    else
                    {
                        // We don't need to generate an event here
                        // because the removeChild call will eventually
                        // result in the same event we would create.
                        if (dgi.getAssistant().canDeleteGroup(dgi))
                        {
                            dgi.getAssistant().deleteGroup(dgi, source);
                        }
                        else
                        {
                            dgi.getParent().removeChild(dgi, source);
                        }
                    }
                }
            }
            finally
            {
                myDataGroupsLock.writeLock().unlock();
            }
            if (rootGroupWasRemoved)
            {
                dgi.groupStream().forEach(g -> myActivationController.deregisterGroup(g));
                myEventManager.publishEvent(new RootDataGroupRemovedEvent(dgi, null, source));
            }
        }

        return removed;
    }

    @Override
    public boolean removeDataGroupInfo(String key, Object source)
    {
        DataGroupInfo dgi = DefaultDataGroupInfo.getKeyMap().getGroupForKey(key);
        return removeDataGroupInfo(dgi, source);
    }

    @Override
    public boolean restoreDefaultActiveSet(boolean exclusive)
    {
        return myActivationController.restoreDefaultSet(exclusive);
    }

    @Override
    public void saveActiveSet(String setName)
    {
        myActivationController.saveActiveSet(setName);
    }

    @Override
    public void saveActiveSet(String name, Collection<? extends ActiveGroupEntry> groups)
    {
        myActivationController.saveActiveSet(name, groups);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DataGroupController:\n" + "\nActive Groups: Count:");

        List<DataGroupInfo> activeDGIList = CollectionUtilities.sort(getActiveGroups(),
                DataGroupInfo.LONG_DISPLAY_NAME_COMPARATOR);
        sb.append(activeDGIList.size()).append('\n');
        for (DataGroupInfo dgi : activeDGIList)
        {
            sb.append("     ").append(dgi.getLongDisplayName()).append('\n');
        }

        myDataGroupsLock.readLock().lock();
        try
        {
            sb.append("\nRoot Data Group Tree\n\n");
            List<DataGroupInfo> dgiList = CollectionUtilities.sort(myDataGroups, DataGroupInfo.LONG_DISPLAY_NAME_COMPARATOR);
            for (DataGroupInfo dgi : dgiList)
            {
                sb.append(dgi.toString()).append('\n');
            }
        }
        finally
        {
            myDataGroupsLock.readLock().unlock();
        }

        return sb.toString();
    }

    /**
     * Creates and installs a data type summary menu item.
     */
    private void createAndInstallControllerPrintMenuItem()
    {
        JMenuItem deCacheSummaryMI = new JMenuItem("DataGroupController - Print Summary");
        deCacheSummaryMI.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TextViewDialog dvd = new TextViewDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                        "DataGroupController Summary", DataGroupControllerImpl.this.toString(), false,
                        myToolbox.getPreferencesRegistry());
                dvd.setLocationRelativeTo(myToolbox.getUIRegistry().getMainFrameProvider().get());
                dvd.setVisible(true);
            }
        });
        myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU)
                .add(deCacheSummaryMI);
    }

    /**
     * Recursively searches the provided collection to search and determine
     * which DataGroupInfo pass the provided filter, those passing are added to
     * the returned result set.
     *
     * @param toSearch the groups to search
     * @param dgiFilter the dgi filter to use to select data group info.
     * @param results return collection of found groups that pass the filter.
     * @param stopOnFirstFound the stop on first found
     */
    private void findDataGroupInfoIn(Collection<DataGroupInfo> toSearch, Predicate<? super DataGroupInfo> dgiFilter,
            Collection<DataGroupInfo> results, boolean stopOnFirstFound)
    {
        Utilities.checkNull(toSearch, "toSearch");
        Utilities.checkNull(dgiFilter, "dgiFilter");
        Utilities.checkNull(results, "results");

        for (DataGroupInfo dgi : toSearch)
        {
            if (dgiFilter.test(dgi))
            {
                results.add(dgi);
                if (stopOnFirstFound)
                {
                    break;
                }
            }
            findDataGroupInfoInNode(dgi, dgiFilter, results, stopOnFirstFound);
            if (!results.isEmpty() && stopOnFirstFound)
            {
                break;
            }
        }
    }

    /**
     * Recursively searches the children of the node to search to see if any of
     * the children or their children pass the filter criteria. Any
     * DataGroupInfo found to pass will be added to the result set.
     *
     * Note: nodeToSearch is not checked to see if it passes the filter
     * criteria, only children if it has any.
     *
     * @param nodeToSearch the node to search
     * @param dgiFilter the dgi filter
     * @param results return collection of results
     * @param stopOnFirstFound the stop on first found
     */
    private void findDataGroupInfoInNode(DataGroupInfo nodeToSearch, Predicate<? super DataGroupInfo> dgiFilter,
            Collection<DataGroupInfo> results, boolean stopOnFirstFound)
    {
        Utilities.checkNull(nodeToSearch, "setToSearch");
        Utilities.checkNull(dgiFilter, "dgiFilter");
        Utilities.checkNull(results, "resultSet");

        if (nodeToSearch.hasChildren())
        {
            for (DataGroupInfo dgi : nodeToSearch.getChildren())
            {
                if (dgiFilter.test(dgi))
                {
                    results.add(dgi);
                    if (stopOnFirstFound)
                    {
                        break;
                    }
                }
                findDataGroupInfoInNode(dgi, dgiFilter, results, stopOnFirstFound);
                if (!results.isEmpty() && stopOnFirstFound)
                {
                    break;
                }
            }
        }
    }

    /**
     * Handle a group added to the controller.
     *
     * @param group The group.
     */
    private void handleGroupAdded(DataGroupInfo group)
    {
        myActivationController.registerGroup(group);
    }
}
