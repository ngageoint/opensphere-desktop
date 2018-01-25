package io.opensphere.controlpanels.layers.base;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterRegistryListener;
import io.opensphere.core.datafilter.impl.DataFilterRegistryAdapter;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.tree.DirectionalTransferHandler;
import io.opensphere.core.util.swing.tree.OrderTreeEventController;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.event.AbstractRootDataGroupControllerEvent;
import io.opensphere.mantle.controller.event.impl.RootDataGroupAddedEvent;
import io.opensphere.mantle.controller.event.impl.RootDataGroupRemovedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.event.DataGroupInfoChildAddedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoChildRemovedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberAddedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberRemovedEvent;
import io.opensphere.mantle.data.event.DataTypeInfoTagsChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.DataGroupInfoGroupByUtility;
import io.opensphere.mantle.data.impl.DefaultDataGroupActivator;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;
import io.opensphere.mantle.data.impl.GroupByTreeBuilder;
import io.opensphere.mantle.data.impl.GroupKeywordUtilities;
import io.opensphere.mantle.data.impl.NodeUserObjectGenerator;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class AbstractDiscoveryDataLayerController.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractDiscoveryDataLayerController implements EventListener<AbstractRootDataGroupControllerEvent>
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(AbstractDiscoveryDataLayerController.class);

    /** The activation listener. */
    private final Runnable myActivationListener = this::handleActiveGroupsChanged;

    /** The Change executor service. */
    private final ScheduledExecutorService myChangeExecutorService = Executors.newScheduledThreadPool(1);

    /** The change support that handles my listeners. */
    private final ChangeSupport<DiscoveryDataLayerChangeListener> myChangeSupport = new WeakChangeSupport<>();

    /**
     * Asks the user yes no questions.
     */
    private final UserConfirmer myConfirmer;

    /** The Data filter registry listener. */
    private final DataFilterRegistryListener myDataFilterRegistryListener;

    /** The data group controller. */
    private final DataGroupController myDataGroupController;

    /** The Data groups changed executor. */
    private final ProcrastinatingExecutor myDataGroupsChangedExecutor = new ProcrastinatingExecutor(myChangeExecutorService, 300,
            500);

    /** The Data type info tags change event listener. */
    private final EventListener<DataTypeInfoTagsChangeEvent> myDataTypeInfoTagsChangeEventListener;

    /** The filtered tree. */
    private TreeNode myFilteredTree;

    /**
     * Activates/deactivates data groups.
     */
    private final GroupActivator myGroupActivator;

    /** The Group vis changed executor. */
    private final ProcrastinatingExecutor myGroupVisChangedExecutor = new ProcrastinatingExecutor(myChangeExecutorService, 300,
            500);

    /** The Repaint tree executor. */
    private final ProcrastinatingExecutor myRepaintTreeExecutor = new ProcrastinatingExecutor(myChangeExecutorService, 300, 500);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The tree needs rebuild. */
    private boolean myTreeNeedsRebuild = true;

    /** The unfiltered tree. */
    private TreeNode myUnfilteredTree;

    /** The Update labels executor. */
    private final ProcrastinatingExecutor myUpdateLabelsExecutor = new ProcrastinatingExecutor(myChangeExecutorService, 300, 500);

    /** The view filter. */
    private String myViewFilter;

    /**
     * Filter node.
     *
     * @param nodeToAddTo the node to add to
     * @param nodeToFilter the node to filter
     * @param searchPattern the search pattern
     * @return true, if successful
     */
    public static boolean filterNode(DefaultMutableTreeNode nodeToAddTo, TreeNode nodeToFilter, Pattern searchPattern)
    {
        boolean passedNode = false;
        if (nodeToFilter instanceof DefaultMutableTreeNode)
        {
            Object userObject = ((DefaultMutableTreeNode)nodeToFilter).getUserObject();
            DefaultMutableTreeNode node = userObject == null ? new DefaultMutableTreeNode()
                    : new DefaultMutableTreeNode(userObject);
            if (userObject instanceof GroupByNodeUserObject)
            {
                GroupByNodeUserObject uo = (GroupByNodeUserObject)userObject;
                passedNode = uo.matchesPattern(searchPattern);
            }

            boolean passedAny = false;
            if (nodeToFilter.getChildCount() >= 0)
            {
                for (Enumeration<?> e = nodeToFilter.children(); e.hasMoreElements();)
                {
                    TreeNode n = (TreeNode)e.nextElement();
                    if (filterNode(node, n, searchPattern))
                    {
                        passedAny = true;
                    }
                }
            }

            if (passedNode || passedAny)
            {
                passedNode = true;
                nodeToAddTo.add(node);
            }
        }
        return passedNode;
    }

    /**
     * Filter tree.
     *
     * @param treeToFilter the tree to filter
     * @param viewFilter the view filter
     * @return the tree node
     */
    public static TreeNode filterTree(TreeNode treeToFilter, String viewFilter)
    {
        Object userObject = ((DefaultMutableTreeNode)treeToFilter).getUserObject();
        DefaultMutableTreeNode fRootNode = userObject == null ? new DefaultMutableTreeNode()
                : new DefaultMutableTreeNode(userObject);

        Pattern p = GroupKeywordUtilities.getSearchPattern(viewFilter);
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Filter Tree With Regex [" + p.pattern() + "]");
        }

        if (treeToFilter.getChildCount() >= 0)
        {
            for (Enumeration<?> e = treeToFilter.children(); e.hasMoreElements();)
            {
                TreeNode n = (TreeNode)e.nextElement();
                filterNode(fRootNode, n, p);
            }
        }

        return fRootNode;
    }

    /**
     * Instantiates a new abstract discovery data layer controller.
     *
     * @param pBox the box
     * @param confirmer Asks the user yes no questions.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public AbstractDiscoveryDataLayerController(Toolbox pBox, UserConfirmer confirmer)
    {
        myToolbox = pBox;
        myConfirmer = confirmer;
        myToolbox.getEventManager().subscribe(AbstractRootDataGroupControllerEvent.class, this);
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController().addActivationListener(myActivationListener);
        myDataTypeInfoTagsChangeEventListener = createDataTypeTagsChangedEventListener();
        myToolbox.getEventManager().subscribe(DataTypeInfoTagsChangeEvent.class, myDataTypeInfoTagsChangeEventListener);
        myDataGroupController = MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController();
        myDataFilterRegistryListener = createDataFilterRegistryListener();
        myToolbox.getDataFilterRegistry().addListener(myDataFilterRegistryListener);
        myViewFilter = null;
        myGroupActivator = new GroupActivator(new DefaultDataGroupActivator(pBox.getEventManager()));
    }

    /**
     * Adds the {@link DiscoveryDataLayerChangeListener}.
     *
     * @param listener the listener
     */
    public void addListener(DiscoveryDataLayerChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Change the activation state for the groups.
     *
     * @param groups the groups whose state should be changed.
     * @param active when true the groups will be made active, when false the
     *            will be made inactive.
     */
    public void changeActivation(Collection<DataGroupInfo> groups, boolean active)
    {
        ThreadUtilities.runBackground(() -> groups.parallelStream().filter(g -> g.userActivationStateControl())
                .forEach(g -> myGroupActivator.activateDeactivateGroup(active, g, myConfirmer)));
    }

    /**
     * Change the activation state for the groups.
     *
     * @param groups the groups whose state should be changed.
     */
    public void deleteDataGroups(Collection<DataGroupInfo> groups)
    {
        // Filter out any groups that don't allow user activation/de-activation
        // control.
        for (DataGroupInfo dgi : groups)
        {
            if (dgi.userDeleteControl())
            {
                getDataGroupController().removeDataGroupInfo(dgi, this);
            }
        }
    }

    /**
     * Gets the data group controller.
     *
     * @return the data group controller
     */
    public DataGroupController getDataGroupController()
    {
        return myDataGroupController;
    }

    /**
     * Gets the drag and drop transfer handler.
     *
     * @return The drag and drop transfer handler.
     */
    public DirectionalTransferHandler getDragAndDropHandler()
    {
        return null;
    }

    /**
     * Gets the group by tree builder.
     *
     * @return the group by tree builder
     */
    public abstract GroupByTreeBuilder getGroupByTreeBuilder();

    /**
     * Gets the group tree with the given filter.
     *
     * @return the group tree
     */
    public TreeNode getGroupTree()
    {
        if (myTreeNeedsRebuild)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Rebuild Tree");
            }

            Set<DataGroupInfo> dgiSet = getDataGroupInfoSet();

//            Set<DataGroupInfo> dgiSet = getServerOnlyDataGroupInfoSet();

            GroupByTreeBuilder builder = getGroupByTreeBuilder();
            long start = System.nanoTime();
            myUnfilteredTree = DataGroupInfoGroupByUtility.createGroupByTree(builder, getNodeUserObjectGenerator(), dgiSet);
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(StringUtilities.formatTimingMessage("Built Tree In: ", System.nanoTime() - start));
            }
            myTreeNeedsRebuild = false;
        }
        if (StringUtils.isBlank(myViewFilter))
        {
            myFilteredTree = myUnfilteredTree;
        }
        else
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Filter Tree");
            }
            long start = System.nanoTime();
            myFilteredTree = filterTree(myUnfilteredTree, myViewFilter);
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(StringUtilities.formatTimingMessage("Filtered Tree In: ", System.nanoTime() - start));
            }
        }
        return myFilteredTree;
    }

    /**
     * Gets the node user object generator.
     *
     * @return the node user object generator
     */
    public abstract NodeUserObjectGenerator getNodeUserObjectGenerator();

//    /**
//     * Gets the available data groups.
//     *
//     * @return the available data groups
//     */
//    public TreeNode getGroupTree()
//    {
//        if (myTreeNeedsRebuild)
//        {
//            LOGGER.trace("Rebuild Tree");
//
//            Set<DataGroupInfo> dgiSet = getDataGroupInfoSet();
//
//            GroupByTreeBuilder builder = getGroupByTreeBuilder();
//            long start = System.nanoTime();
//            myUnfilteredTree = DataGroupInfoGroupByUtility.createGroupByTree(builder, getNodeUserObjectGenerator(), dgiSet);
//            LOGGER.trace(StringUtilities.formatTimingMessage("Built Tree In: ", System.nanoTime() - start));
//            myTreeNeedsRebuild = false;
//        }
//        if (StringUtils.isBlank(myViewFilter))
//        {
//            myFilteredTree = myUnfilteredTree;
//        }
//        else
//        {
//            LOGGER.trace("Filter Tree");
//            long start = System.nanoTime();
//            myFilteredTree = filterTree(myUnfilteredTree, myViewFilter);
//            LOGGER.trace(StringUtilities.formatTimingMessage("Filtered Tree In: ", System.nanoTime() - start));
//        }
//        return myFilteredTree;
//    }

    /**
     * Get the orderTreeEventController.
     *
     * @return the orderTreeEventController
     */
    public OrderTreeEventController getOrderTreeEventController()
    {
        return null;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Gets the view by type string.
     *
     * @return the view by type string
     */
    public abstract String getViewByTypeString();

    @Override
    public void notify(AbstractRootDataGroupControllerEvent event)
    {
        if (event instanceof RootDataGroupRemovedEvent || event instanceof RootDataGroupAddedEvent
                || event.getOriginEvent() instanceof DataGroupInfoChildAddedEvent
                || event.getOriginEvent() instanceof DataGroupInfoChildRemovedEvent
                || event.getOriginEvent() instanceof DataGroupInfoMemberAddedEvent
                || event.getOriginEvent() instanceof DataGroupInfoMemberRemovedEvent)
        {
            myTreeNeedsRebuild = true;
            notifyDataGroupsChanged();
        }
    }

    /**
     * Removes the {@link DiscoveryDataLayerChangeListener}.
     *
     * @param listener the listener
     */
    public void removeListener(DiscoveryDataLayerChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Sets the tree filter.
     *
     * @param filter the new tree filter
     */
    public void setTreeFilter(String filter)
    {
        myViewFilter = filter;
        notifyDataGroupsChanged();
    }

    /**
     * Sets the tree needs rebuild.
     *
     * @param rebuild the new tree needs rebuild
     */
    public void setTreeNeedsRebuild(boolean rebuild)
    {
        myTreeNeedsRebuild = rebuild;
    }

    /**
     * Sets the view by type from string.
     *
     * @param vbt the new view by type from string
     */
    public abstract void setViewByTypeFromString(String vbt);

    /**
     * Update group node user object labels.
     */
    public void updateGroupNodeUserObjectLabels()
    {
        if (myUnfilteredTree != null)
        {
            updateGroupNodeUserObjectLabels((DefaultMutableTreeNode)myUnfilteredTree);
        }
    }

    /**
     * Gets the set of data groups to display in the tree with the given filter.
     *
     * @return The set of data groups to display in tree.
     */
    protected Set<DataGroupInfo> getDataGroupInfoSet()
    {
        return MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController().getDataGroupInfoSet();
    }

    /**
     * Get the preferences change listener for showing layer type labels.
     *
     * @return The change listener.
     */
    protected PreferenceChangeListener getShowLayerTypeLabelsPreferencesChangeListener()
    {
        return new PreferenceChangeListener()
        {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt)
            {
                updateGroupNodeUserObjectLabels();
                notifyUpdateTreeLabelsRequest();
            }
        };
    }

    /**
     * Handle tags changed.
     *
     * @param event the event
     */
    protected abstract void handleTagsChanged(DataTypeInfoTagsChangeEvent event);

    /**
     * Notify data groups changed.
     */
    protected void notifyDataGroupsChanged()
    {
        myChangeSupport.notifyListeners(new Callback<DiscoveryDataLayerChangeListener>()
        {
            @Override
            public void notify(DiscoveryDataLayerChangeListener listener)
            {
                listener.dataGroupsChanged();
            }
        }, myDataGroupsChangedExecutor);
    }

    /**
     * Notify groups visibility changed.
     *
     * @param event the event
     */
    protected void notifyGroupsVisibilityChanged(final DataTypeVisibilityChangeEvent event)
    {
        myChangeSupport.notifyListeners(new Callback<DiscoveryDataLayerChangeListener>()
        {
            @Override
            public void notify(DiscoveryDataLayerChangeListener listener)
            {
                listener.dataGroupVisibilityChanged(event);
            }
        }, myGroupVisChangedExecutor);
    }

    /**
     * Notify data groups changed.
     */
    protected void notifyRepaintTreeRequest()
    {
        myChangeSupport.notifyListeners(new Callback<DiscoveryDataLayerChangeListener>()
        {
            @Override
            public void notify(DiscoveryDataLayerChangeListener listener)
            {
                listener.treeRepaintRequest();
            }
        }, myRepaintTreeExecutor);
    }

    /**
     * Notify data groups changed.
     */
    protected void notifyUpdateTreeLabelsRequest()
    {
        myChangeSupport.notifyListeners(new Callback<DiscoveryDataLayerChangeListener>()
        {
            @Override
            public void notify(DiscoveryDataLayerChangeListener listener)
            {
                listener.refreshTreeLabelRequest();
            }
        }, myUpdateLabelsExecutor);
    }

    /**
     * Update group node user object labels.
     *
     * @param node the node
     */
    protected void updateGroupNodeUserObjectLabels(DefaultMutableTreeNode node)
    {
        if (node != null)
        {
            if (node.getUserObject() instanceof GroupByNodeUserObject)
            {
                ((GroupByNodeUserObject)node.getUserObject()).generateLabel();
            }
            if (node.getChildCount() > 0)
            {
                for (int i = 0; i < node.getChildCount(); i++)
                {
                    updateGroupNodeUserObjectLabels((DefaultMutableTreeNode)node.getChildAt(i));
                }
            }
        }
    }

    /**
     * Creates the data filter registry listener.
     *
     * @return the data filter registry listener
     */
    private DataFilterRegistryListener createDataFilterRegistryListener()
    {
        DataFilterRegistryListener listener = new DataFilterRegistryAdapter()
        {
            @Override
            public void loadFilterAdded(String typeKey, DataFilter filter, Object source)
            {
                notifyUpdateTreeLabelsRequest();
            }

            @Override
            public void loadFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source)
            {
                notifyUpdateTreeLabelsRequest();
            }

            @Override
            public void viewFilterAdded(String typeKey, DataFilter filter, Object source)
            {
                notifyUpdateTreeLabelsRequest();
            }

            @Override
            public void viewFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source)
            {
                notifyUpdateTreeLabelsRequest();
            }
        };
        return listener;
    }

    /**
     * Creates the data type tags changed event listener.
     *
     * @return the event listener
     */
    private EventListener<DataTypeInfoTagsChangeEvent> createDataTypeTagsChangedEventListener()
    {
        EventListener<DataTypeInfoTagsChangeEvent> listener = new EventListener<DataTypeInfoTagsChangeEvent>()
        {
            @Override
            public void notify(DataTypeInfoTagsChangeEvent event)
            {
                handleTagsChanged(event);
            }
        };
        return listener;
    }

    /**
     * Handle active groups changed.
     */
    private void handleActiveGroupsChanged()
    {
        myTreeNeedsRebuild = true;
    }
}
