package io.opensphere.mantle.mp.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.MapAnnotationPointGroup;
import io.opensphere.mantle.mp.MapAnnotationPointRegistry;
import io.opensphere.mantle.mp.MapAnnotationRegistryPersistenceHelper;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;
import io.opensphere.mantle.mp.event.AbstractRootMapAnnotationPointRegistryEvent;
import io.opensphere.mantle.mp.event.impl.RootMapAnnotationPointGroupAddedEvent;
import io.opensphere.mantle.mp.event.impl.RootMapAnnotationPointGroupRemovedEvent;
import io.opensphere.mantle.mp.event.impl.RootMapAnnotationPointGroupStructureChangeEvent;

/**
 * The Class MapAnnotationPointRegistryImpl.
 */
@SuppressWarnings("PMD.GodClass")
public class MapAnnotationPointRegistryImpl
        implements MapAnnotationPointRegistry, EventListener<AbstractMapAnnotationPointGroupChangeEvent>
{
    /** The Constant string for the last saved directory preference. */
    public static final String LAST_SAVED_DIRECTORY_PREFERENCE = "LastSavedDirectoryPath";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MapAnnotationPointRegistryImpl.class);

    /** the event manager for firing events. */
    private final EventManager myEventManager;

    /** The data group info read/write lock. */
    private final ReadWriteLock myGroupsReadWriteLock = new ReentrantReadWriteLock();

    /** The my persist changes to preferences. */
    private boolean myPersistChangesToPreferences;

    /** The my persistence helper. */
    private MapAnnotationRegistryPersistenceHelper myPersistenceHelper;

    /** The data group info. */
    private final Set<MutableMapAnnotationPointGroup> myPointGroups;

    /** The my toolbox. */
    private final Toolbox myToolbox;

    /** The user default point. */
    private MapAnnotationPoint myUserDefaultPoint;

    /**
     * Instantiates a new MapAnnotationPointRegistryImpl.
     *
     * @param tb the toolbox
     * @param persistenceHelperClass the persistence helper class
     */
    public MapAnnotationPointRegistryImpl(Toolbox tb, String persistenceHelperClass)
    {
        myToolbox = tb;
        myPointGroups = new HashSet<>();
        myUserDefaultPoint = new DefaultMapAnnotationPoint();
        myEventManager = tb.getEventManager();
        myEventManager.subscribe(AbstractMapAnnotationPointGroupChangeEvent.class, this);
        try
        {
            Class<?> cl = Class.forName(persistenceHelperClass);
            if (cl != null)
            {
                Object o = cl.getDeclaredConstructor().newInstance();
                if (o instanceof MapAnnotationRegistryPersistenceHelper)
                {
                    myPersistenceHelper = (MapAnnotationRegistryPersistenceHelper)o;
                }
            }
        }
        catch (ReflectiveOperationException e)
        {
            myPersistenceHelper = null;
            LOGGER.error(e);
        }
    }

    @Override
    public final boolean addRootGroup(MutableMapAnnotationPointGroup group, Object source)
    {
        if (group == null)
        {
            return false;
        }

        if (!group.isRootNode())
        {
            throw new IllegalArgumentException(
                    "Only root MapAnnotationPointGroup may be added to the MapAnnotationPointRegistry");
        }

        boolean added = false;
        myGroupsReadWriteLock.writeLock().lock();
        try
        {
            added = myPointGroups.add(group);
        }
        finally
        {
            myGroupsReadWriteLock.writeLock().unlock();
        }
        if (added)
        {
            saveToPreferences();
            myEventManager.publishEvent(new RootMapAnnotationPointGroupAddedEvent(group, source));
        }
        return added;
    }

    @Override
    public void clearRegistry(Object source)
    {
        Set<MutableMapAnnotationPointGroup> groupSet = getGroupSet();
        for (MutableMapAnnotationPointGroup group : groupSet)
        {
            removeGroup(group, source);
        }
    }

    @Override
    public Set<MutableMapAnnotationPointGroup> createFilteredCopy(Predicate<? super MutableMapAnnotationPoint> pointFilter,
            Predicate<? super MutableMapAnnotationPointGroup> groupFilter)
    {
        Set<MutableMapAnnotationPointGroup> resultSet = New.set();
        myGroupsReadWriteLock.readLock().lock();
        try
        {
            for (MutableMapAnnotationPointGroup group : myPointGroups)
            {
                if (groupFilter == null || groupFilter.test(group))
                {
                    resultSet.add(group.createFilteredCopy(pointFilter, groupFilter));
                }
            }
        }
        finally
        {
            myGroupsReadWriteLock.readLock().unlock();
        }
        return resultSet;
    }

    @Override
    public List<MutableMapAnnotationPointGroup> createGroupList(Comparator<MutableMapAnnotationPointGroup> comparator,
            Predicate<? super MutableMapAnnotationPointGroup> nodeFilter)
    {
        List<MutableMapAnnotationPointGroup> dataGroups = new ArrayList<>();

        myGroupsReadWriteLock.readLock().lock();
        try
        {
            List<MutableMapAnnotationPointGroup> groupList = new ArrayList<>(myPointGroups);

            // Add the children that are all but filtered.
            for (MutableMapAnnotationPointGroup dgi : groupList)
            {
                Set<MutableMapAnnotationPointGroup> set = dgi.createGroupSet(nodeFilter);
                dataGroups.addAll(set);
            }

            // Sort the list of children into order.
            if (comparator == null)
            {
                Collections.sort(groupList, MapAnnotationPointGroup.NAME_COMPARATOR);
            }
            else
            {
                Collections.sort(groupList, comparator);
            }
        }
        finally
        {
            myGroupsReadWriteLock.readLock().unlock();
        }
        return dataGroups;
    }

    @Override
    public TreeNode createTreeNode()
    {
        return createTreeNode(null, null);
    }

    @Override
    public TreeNode createTreeNode(Comparator<? super MutableMapAnnotationPointGroup> comparator,
            Predicate<? super MutableMapAnnotationPointGroup> nodeFilter)
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        rootNode.setAllowsChildren(false);
        myGroupsReadWriteLock.readLock().lock();
        try
        {
            List<MutableMapAnnotationPointGroup> groupList = new ArrayList<>(myPointGroups);

            // Sort the list of children into order.
            if (comparator == null)
            {
                Collections.sort(groupList, MapAnnotationPointGroup.NAME_COMPARATOR);
            }
            else
            {
                Collections.sort(groupList, comparator);
            }

            // Add the children that are all but filtered.
            for (MutableMapAnnotationPointGroup group : groupList)
            {
                if (nodeFilter == null || nodeFilter.test(group))
                {
                    MutableTreeNode node = group.createTreeNode(comparator, nodeFilter);
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
            myGroupsReadWriteLock.readLock().unlock();
        }
        return rootNode;
    }

    @Override
    public TreeNode createTreeNodeWithChildrenInPrefferedOrder()
    {
        return createTreeNodeWithChildrenInPrefferedOrder(null);
    }

    @Override
    public TreeNode createTreeNodeWithChildrenInPrefferedOrder(Predicate<? super MutableMapAnnotationPointGroup> nodeFilter)
    {
        return createTreeNode((o1, o2) -> Integer.compare(o1.getPreferredOrder(), o2.getPreferredOrder()), nodeFilter);
    }

    @Override
    public void findGroup(Predicate<MapAnnotationPointGroup> filter, Collection<MapAnnotationPointGroup> results,
            boolean stopOnFirstFound)
    {
        myGroupsReadWriteLock.readLock().lock();
        try
        {
            findGroupInSet(myPointGroups, filter, results, stopOnFirstFound);
        }
        finally
        {
            myGroupsReadWriteLock.readLock().unlock();
        }
    }

    @Override
    public MapAnnotationPointGroup findGroupWithPoint(final MapAnnotationPoint point)
    {
        Set<MapAnnotationPointGroup> foundSet = New.set();
        findGroup(value -> value.hasPoint(point, false), foundSet, true);
        return foundSet.isEmpty() ? null : foundSet.iterator().next();
    }

    @Override
    public Set<MutableMapAnnotationPoint> findPoints(final Predicate<? super MutableMapAnnotationPoint> filter,
            final boolean stopOnFirstFound)
    {
        Set<MutableMapAnnotationPoint> resultSet = New.set();
        myGroupsReadWriteLock.readLock().lock();
        try
        {
            for (MutableMapAnnotationPointGroup group : myPointGroups)
            {
                resultSet.addAll(group.findPoints(filter, true, stopOnFirstFound));
                if (stopOnFirstFound && !resultSet.isEmpty())
                {
                    break;
                }
            }
        }
        finally
        {
            myGroupsReadWriteLock.readLock().unlock();
        }
        return resultSet.isEmpty() ? Collections.<MutableMapAnnotationPoint>emptySet() : resultSet;
    }

    @Override
    public Set<MutableMapAnnotationPointGroup> getGroupSet()
    {
        Set<MutableMapAnnotationPointGroup> groupSet = new HashSet<>();
        myGroupsReadWriteLock.readLock().lock();
        try
        {
            groupSet.addAll(myPointGroups);
        }
        finally
        {
            myGroupsReadWriteLock.readLock().unlock();
        }
        return groupSet;
    }

    @Override
    public List<? extends MapAnnotationPointGroup> getGroupsWithPoint(final MutableMapAnnotationPoint point)
    {
        Predicate<MapAnnotationPointGroup> filter = group -> group.hasPoint(point, true);

        List<MapAnnotationPointGroup> result = New.list();
        findGroup(filter, result, false);
        return result;
    }

    @Override
    public MutableMapAnnotationPointGroup getTopParent(MutableMapAnnotationPointGroup group)
    {
        return group == null ? null : group.getTopParent();
    }

    @Override
    public MapAnnotationPoint getUserDefaultPoint()
    {
        return myUserDefaultPoint;
    }

    @Override
    public boolean hasGroup(MutableMapAnnotationPointGroup group)
    {
        boolean hasIt = false;
        if (group != null)
        {
            MutableMapAnnotationPointGroup topParent = group.getTopParent();
            if (topParent != null)
            {
                myGroupsReadWriteLock.readLock().lock();
                try
                {
                    hasIt = myPointGroups.contains(topParent);
                }
                finally
                {
                    myGroupsReadWriteLock.readLock().unlock();
                }
            }
        }
        return hasIt;
    }

    /**
     * Initialize.
     */
    public void initialize()
    {
        if (myPersistenceHelper != null)
        {
            myPersistChangesToPreferences = false;
            myPersistenceHelper.loadFromPreferences(myToolbox, this);
        }
        myPersistChangesToPreferences = true;
        if (myPointGroups.isEmpty())
        {
            addRootGroup(new DefaultMapAnnotationPointGroup(myToolbox, "My Points"), this);
        }
    }

    @Override
    public MapAnnotationPointGroup loadFromXMLFile(Toolbox tb, File aFile) throws IOException
    {
        return myPersistenceHelper.loadFromFile(tb, aFile);
    }

    @Override
    public void notify(AbstractMapAnnotationPointGroupChangeEvent event)
    {
        // If the event isn't a root type ( our own type ) and
        // the event originated from a group who's root is managed
        // by this controller, then rebroadcast a simplified group structure
        // changed event.
        if (!(event instanceof AbstractRootMapAnnotationPointRegistryEvent))
        {
            MutableMapAnnotationPointGroup topParent = event.getGroup().getTopParent();
            boolean topParentIsOneOfMyRootNodes = false;
            myGroupsReadWriteLock.readLock().lock();
            try
            {
                topParentIsOneOfMyRootNodes = myPointGroups.contains(topParent);
            }
            finally
            {
                myGroupsReadWriteLock.readLock().unlock();
            }
            if (topParentIsOneOfMyRootNodes)
            {
                saveToPreferences();
                myEventManager
                        .publishEvent(new RootMapAnnotationPointGroupStructureChangeEvent(topParent, event, event.getSource()));
            }
        }
    }

    @Override
    public boolean removeGroup(MutableMapAnnotationPointGroup group, Object source)
    {
        boolean removed = false;
        boolean rootGroupWasRemoved = false;

        if (group != null)
        {
            myGroupsReadWriteLock.writeLock().lock();
            try
            {
                MutableMapAnnotationPointGroup topParent = group.getTopParent();
                if (myPointGroups.contains(topParent))
                {
                    if (Utilities.sameInstance(topParent, group))
                    {
                        rootGroupWasRemoved = myPointGroups.remove(group);
                    }
                    else
                    {
                        // We don't need to generate an event here
                        // because the removeChild call will eventually
                        // result in the same event we would create.
                        group.getParent().removeChild(group, source);
                    }
                }
            }
            finally
            {
                myGroupsReadWriteLock.writeLock().unlock();
            }
        }
        if (rootGroupWasRemoved)
        {
            saveToPreferences();
            myEventManager.publishEvent(new RootMapAnnotationPointGroupRemovedEvent(group, null, source));
        }

        return removed;
    }

    @Override
    public void saveToXMLFile(File aFile, MapAnnotationPointGroup group) throws IOException
    {
        myPersistenceHelper.saveToFile(aFile, group);
    }

    @Override
    public void setUserDefaultPoint(MapAnnotationPoint userDefaultPoint, Object source)
    {
        myUserDefaultPoint = userDefaultPoint != null ? userDefaultPoint : new DefaultMapAnnotationPoint();
        saveDefaultToPreferences();
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
     * @param filter the filter
     * @param results return collection of results
     * @param stopOnFirstFound the stop on first found
     */
    private void findGroupInNode(MapAnnotationPointGroup nodeToSearch, Predicate<? super MapAnnotationPointGroup> filter,
            Collection<MapAnnotationPointGroup> results, boolean stopOnFirstFound)
    {
        Utilities.checkNull(nodeToSearch, "setToSearch");
        Utilities.checkNull(filter, "filter");
        Utilities.checkNull(results, "resultSet");

        if (nodeToSearch.hasChildren())
        {
            for (MapAnnotationPointGroup group : nodeToSearch.getChildren())
            {
                if (filter.test(group))
                {
                    results.add(group);
                    if (stopOnFirstFound)
                    {
                        break;
                    }
                }
                findGroupInNode(group, filter, results, stopOnFirstFound);
                if (!results.isEmpty() && stopOnFirstFound)
                {
                    break;
                }
            }
        }
    }

    /**
     * Recursively searches the provided set to search an determines which
     * MapAnnotationPointGroup pass the provided filter, those passing are added
     * to the returned result set.
     *
     * @param setToSearch the set to search
     * @param filter the filter to use to select data group info.
     * @param results return collection of found groups that pass the filter.
     * @param stopOnFirstFound the stop on first found
     */
    private void findGroupInSet(Set<? extends MapAnnotationPointGroup> setToSearch, Predicate<MapAnnotationPointGroup> filter,
            Collection<MapAnnotationPointGroup> results, boolean stopOnFirstFound)
    {
        Utilities.checkNull(setToSearch, "setToSearch");
        Utilities.checkNull(filter, "filter");
        Utilities.checkNull(results, "results");

        for (MapAnnotationPointGroup group : setToSearch)
        {
            if (filter.test(group))
            {
                results.add(group);
                if (stopOnFirstFound)
                {
                    break;
                }
            }
            findGroupInNode(group, filter, results, stopOnFirstFound);
            if (!results.isEmpty() && stopOnFirstFound)
            {
                break;
            }
        }
    }

    /**
     * Save the user default point to preferences.
     */
    private void saveDefaultToPreferences()
    {
        if (myPersistChangesToPreferences)
        {
            if (myPersistenceHelper != null)
            {
                myPersistenceHelper.saveDefaultToPreferences(myToolbox, myUserDefaultPoint);
            }
            else
            {
                LOGGER.error("Failed to save default map annotation point to preferences because helper is not available.");
            }
        }
    }

    /**
     * Save to preferences.
     */
    private void saveToPreferences()
    {
        if (myPersistChangesToPreferences)
        {
            if (myPersistenceHelper != null)
            {
                // Create a copy of the group tree to send to persist so we can
                // unlock the internal set.
                Set<MapAnnotationPointGroup> groupSet = New.set();
                myGroupsReadWriteLock.readLock().lock();
                try
                {
                    if (!myPointGroups.isEmpty())
                    {
                        for (MapAnnotationPointGroup group : myPointGroups)
                        {
                            groupSet.add(new DefaultMapAnnotationPointGroup(myToolbox, group));
                        }
                    }
                }
                finally
                {
                    myGroupsReadWriteLock.readLock().unlock();
                }
                myPersistenceHelper.saveToPreferences(myToolbox, groupSet);
            }
            else
            {
                LOGGER.error("Failed to save map annotation point registry to preferences because helper is not available.");
            }
        }
    }
}
