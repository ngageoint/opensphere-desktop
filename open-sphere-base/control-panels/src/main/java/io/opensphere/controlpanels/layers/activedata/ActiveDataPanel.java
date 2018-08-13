package io.opensphere.controlpanels.layers.activedata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.layers.activedata.controller.ActiveDataDataLayerController;
import io.opensphere.controlpanels.layers.activedata.tabletree.ActiveDataTreeTableTreeCellRenderer;
import io.opensphere.controlpanels.layers.activedata.tree.TreeTransferHandler;
import io.opensphere.controlpanels.layers.activedata.zorder.ZOrderTreeTransferHandler;
import io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel;
import io.opensphere.controlpanels.layers.base.DataTreeButtonProvisioner;
import io.opensphere.controlpanels.layers.base.DiscoveryTreeExpansionHelper;
import io.opensphere.controlpanels.layers.base.LayerUtilities;
import io.opensphere.controlpanels.layers.importdata.ImportButtonPanel;
import io.opensphere.controlpanels.layers.layerdetail.LayerDetailPanel;
import io.opensphere.controlpanels.layers.layerdetail.LayerDetailsCoordinator;
import io.opensphere.controlpanels.layers.layerpopout.ActiveDataViewCreator;
import io.opensphere.controlpanels.layers.layerpopout.LayerPopperOuter;
import io.opensphere.controlpanels.layers.layersets.LayerSetButtonPanel;
import io.opensphere.controlpanels.layers.util.LoadsToUtilities;
import io.opensphere.controlpanels.util.ShowFilterDialogEvent;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.IconToggleButton;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.core.util.swing.tree.ButtonModelPayloadJCheckBox;
import io.opensphere.core.util.swing.tree.TreeTableTreeCellRenderer;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.event.DataTypeInfoFocusEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.PlayState;
import io.opensphere.mantle.data.StreamingSupport;
import io.opensphere.mantle.data.TypeFocusEvent;
import io.opensphere.mantle.data.TypeFocusEvent.FocusType;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;
import io.opensphere.mantle.layers.event.LayerSelectedEvent;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The whole big-ass panel that's in the Active tab of the Layers HUD.
 */
@SuppressWarnings("PMD.GodClass")
public final class ActiveDataPanel extends AbstractDiscoveryDataPanel implements ActiveDataViewCreator
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ActiveDataPanel.class);

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The activation listener. */
    private final transient Runnable myActivationListener = this::rebuildTree;

    /** The active data tree table tree cell renderer. */
    private final transient ActiveDataTreeTableTreeCellRenderer myActiveDataTreeTableTreeCellRenderer;

    /** The provider for context menus for a single data group. */
    private final transient ContextMenuProvider<DataGroupContextKey> myContextMenuProvider = new ContextMenuProvider<DataGroupContextKey>()
    {
        @Override
        public List<? extends Component> getMenuItems(String contextId, DataGroupContextKey key)
        {
            List<Component> menuItems = New.list();
            DataGroupInfo dgi = key.getDataGroup();
            DataTypeInfo dataType = key.getDataType();

            JMenuItem expandCollapse = getExpandContractNodeMenuItem();
            if (expandCollapse != null)
            {
                menuItems.add(expandCollapse);
            }
            Collection<DataGroupInfo> dgis = Collections.singleton(dgi);
            if (anyCanBeDeactivated(dgis))
            {
                menuItems.add(getActivationMenuItem(dgis, false));
            }
            if (dgi.hasDetails())
            {
                menuItems.add(getShowLayerDetailsMenuItem(dgi));
            }
            if (dgi.isTaggable())
            {
                menuItems.add(getManageTagsMenuItem(dgi));
            }
            if (LoadsToUtilities.allowTimelineSelection(dataType))
            {
                if (LoadsToUtilities.isTimelineEnabled(dataType))
                {
                    menuItems.add(SwingUtilities.newMenuItem("Remove from Timeline",
                            e -> LoadsToUtilities.setIncludeInTimeline(dataType, false)));
                }
                else
                {
                    menuItems.add(SwingUtilities.newMenuItem("Add to Timeline",
                            e -> LoadsToUtilities.setIncludeInTimeline(dataType, true)));
                }
            }
            if (LoadsToUtilities.allowAnalyzeSelection(dataType))
            {
                if (LoadsToUtilities.isAnalyzeEnabled(dataType))
                {
                    menuItems.add(SwingUtilities.newMenuItem("Remove from Analyze Window",
                            e -> LoadsToUtilities.setIncludeInAnalyze(dataType, false)));
                }
                else
                {
                    menuItems.add(SwingUtilities.newMenuItem("Add to Analyze Window",
                            e -> LoadsToUtilities.setIncludeInAnalyze(dataType, true)));
                }
            }

            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 0;
        }
    };

    /** The active data layer controller. */
    private final transient ActiveDataDataLayerController myDataLayerController;

    /** The deactivate layer button. */
    private transient IconButton myDeactivateLayerButton;

    /**
     * The delete layer button.
     */
    private transient IconButton myDeleteLayerButton;

    /** The pause streaming layers button. */
    private transient IconButton myPauseLayersButton;

    /** The stop all streaming layers button. */
    private transient IconButton myStopLayersButton;

    /** The play all streaming layers button. */
    private transient IconButton myPlayLayersButton;

    /** The DTI focus change listener. */
    private final transient EventListener<DataTypeInfoFocusEvent> myDTIFocusChangeListener;

    /** The Expansion helper. */
    private final transient DiscoveryTreeExpansionHelper myExpansionHelper;

    /**
     * Provides export menu items for layers.
     */
    private transient ExportersMenuProvider myExportersMenuProvider;

    /** The lower panel. */
    private transient ActiveLayerControlPanel myLayerControlPanel;

    /** The my layer details coordinator. */
    private final transient LayerDetailsCoordinator myLayerDetailsCoordinator;

    /** The my loads to changed listener. */
    private final transient EventListener<DataTypeInfoLoadsToChangeEvent> myLoadsToChangedListener;

    /** The lower panel. */
    private transient JPanel myLowerPanel;

    /** The provider for context menus for a collection of data groups. */
    private final transient ContextMenuProvider<MultiDataGroupContextKey> myMultiGroupMenuProvider = new ContextMenuProvider<MultiDataGroupContextKey>()
    {
        @Override
        public List<? extends Component> getMenuItems(String contextId, MultiDataGroupContextKey key)
        {
            List<Component> menuItems = New.list();
            Collection<DataGroupInfo> dgis = key.getDataGroups();
            if (anyCanBeDeactivated(dgis))
            {
                menuItems.add(getActivationMenuItem(dgis, false));
            }
            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 0;
        }
    };

    /** Listener for changes to the order of items displayed in this panel. */
    private final transient OrderChangeListener myOrderChangeListener = event -> handleOrderChange();

    /**
     * Create a new window for the selected layer.
     */
    private transient LayerPopperOuter myPopperOuter;

    /** The show hide features button. */
    private transient IconToggleButton myShowHideFeaturesButton;

    /** The show hide tiles button. */
    private transient IconToggleButton myShowHideTilesButton;

    /**
     * Provides export menu items for layers.
     */
    private transient ExportersSingleMenuProvider mySingleExportersMenuProvider;

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /** The lower button panel. */
    private transient GridBagPanel myTopButtonPanel;

    /**
     * The tree transfer handler.
     */
    private final transient TreeTransferHandler myTransferHandler;

    /** The tree button builders. */
    private final transient ActiveTreeButtonBuilders myTreeButtonBuilders;

    /** The my type visibility change listener. */
    private final transient EventListener<DataTypeVisibilityChangeEvent> myTypeVisibilityChangeListener;

    /**
     * Instantiates a new timeline panel.
     *
     * @param tb the {@link Toolbox}
     * @param coordinator the coordinator
     */
    public ActiveDataPanel(Toolbox tb, LayerDetailsCoordinator coordinator)
    {
        this(tb, coordinator, null, null);
    }

    /**
     * Instantiates a new timeline panel.
     *
     * @param toolbox the {@link Toolbox}
     * @param coordinator the coordinator
     * @param controller The controller to use, or null if the default
     *            controller is acceptable.
     * @param expansionHelper The expansion helper to use to save node expansion
     *            states.
     */
    private ActiveDataPanel(Toolbox toolbox, LayerDetailsCoordinator coordinator, ActiveDataDataLayerController controller,
            DiscoveryTreeExpansionHelper expansionHelper)
    {
        super(toolbox);
        myToolbox = toolbox;
        myLayerDetailsCoordinator = coordinator;
        if (controller == null)
        {
            myDataLayerController = new ActiveDataDataLayerController(toolbox, this);
            ContextActionManager contextManager = toolbox.getUIRegistry().getContextActionManager();
            contextManager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                    myContextMenuProvider);
            contextManager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, MultiDataGroupContextKey.class,
                    myMultiGroupMenuProvider);
            myExportersMenuProvider = new ExportersMenuProvider(myToolbox);
            mySingleExportersMenuProvider = new ExportersSingleMenuProvider(myExportersMenuProvider);
            contextManager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                    mySingleExportersMenuProvider);
            contextManager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, MultiDataGroupContextKey.class,
                    myExportersMenuProvider);
        }
        else
        {
            myDataLayerController = controller;
        }

        myLoadsToChangedListener = event -> repaintTree();
        toolbox.getEventManager().subscribe(DataTypeInfoLoadsToChangeEvent.class, myLoadsToChangedListener);
        myTypeVisibilityChangeListener = event -> repaintTree();
        toolbox.getEventManager().subscribe(DataTypeVisibilityChangeEvent.class, myTypeVisibilityChangeListener);
        myDTIFocusChangeListener = this::handleDataTypeInfoFocusEvent;
        toolbox.getEventManager().subscribe(DataTypeInfoFocusEvent.class, myDTIFocusChangeListener);

        myDataLayerController.addListener(this);

        myTreeButtonBuilders = new ActiveTreeButtonBuilders(toolbox, this::handleButtonClicked);
        myActiveDataTreeTableTreeCellRenderer = new ActiveDataTreeTableTreeCellRenderer(
                MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController());
        myActiveDataTreeTableTreeCellRenderer.setButtonBuilders(getTreeButtonProvisioner().getButtonBuilders());

        if (expansionHelper == null)
        {
            myExpansionHelper = new DiscoveryTreeExpansionHelper(toolbox.getPreferencesRegistry().getPreferences(getClass()),
                    DiscoveryTreeExpansionHelper.Mode.STORE_CONTRACTIONS);
        }
        else
        {
            myExpansionHelper = expansionHelper;
        }

        myExpansionHelper.loadFromPreferences();

        myTransferHandler = new TreeTransferHandler(
                new ZOrderTreeTransferHandler(toolbox, getController().getOrderTreeEventController()));

        toolbox.getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, DefaultOrderCategory.FEATURE_CATEGORY)
                .addParticipantChangeListener(myOrderChangeListener);
        toolbox.getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY, DefaultOrderCategory.EARTH_ELEVATION_CATEGORY)
                .addParticipantChangeListener(myOrderChangeListener);
        toolbox.getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY)
                .addParticipantChangeListener(myOrderChangeListener);
        toolbox.getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, DefaultOrderCategory.IMAGE_DATA_CATEGORY)
                .addParticipantChangeListener(myOrderChangeListener);
        toolbox.getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY)
                .addParticipantChangeListener(myOrderChangeListener);
        toolbox.getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_MY_PLACES_LAYER_FAMILY, DefaultOrderCategory.MY_PLACES_CATEGORY)
                .addParticipantChangeListener(myOrderChangeListener);

        getController().getOrderTreeEventController().setAllowDrag(true);

        MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController().addActivationListener(myActivationListener);
    }

    @Override
    public Container createActiveDataView(ActiveDataDataLayerController controller)
    {
        DiscoveryTreeExpansionHelper expansionHelper = new DiscoveryTreeExpansionHelper(
                getToolbox().getPreferencesRegistry().getPreferences(controller.getClass()),
                DiscoveryTreeExpansionHelper.Mode.STORE_CONTRACTIONS);
        ActiveDataPanel newPanel = new ActiveDataPanel(getToolbox(), myLayerDetailsCoordinator, controller, expansionHelper);
        newPanel.initGuiElements();

        return newPanel;
    }

    @Override
    public void dataGroupsChanged()
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Data Groups Changed: Request Tree Rebuild");
        }
        rebuildTree();
    }

    @Override
    public void dataGroupVisibilityChanged(DataTypeVisibilityChangeEvent event)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Data Group Visibility Changed: Update Check Box State");
        }
        updateCheckboxState();
    }

    @Override
    public ActiveDataDataLayerController getController()
    {
        return myDataLayerController;
    }

    @Override
    public TreeTableTreeCellRenderer getTreeTableTreeCellRenderer()
    {
        return myActiveDataTreeTableTreeCellRenderer;
    }

    @Override
    public TreeTransferHandler getTreeTransferHandler()
    {
        return myTransferHandler;
    }

    @Override
    public void initGuiElements()
    {
        myLayerControlPanel = new ActiveLayerControlPanel(getController().getToolbox(), myLayerDetailsCoordinator);
        myLayerControlPanel.setVisible(false);

        LayerSetButtonPanel bookmarkSplitButton = new LayerSetButtonPanel(getToolbox());
        Toolbox tb = myDataLayerController.getToolbox();
        ImportButtonPanel importButton = new ImportButtonPanel(tb.getUIRegistry().getContextActionManager(), tb.getEventManager(),
                null);

        myTopButtonPanel = new GridBagPanel();
        myTopButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        myTopButtonPanel.add(getShowHideTilesButton());
        myTopButtonPanel.setInsets(0, 4, 0, 0);
        myTopButtonPanel.add(getShowHideFeaturesButton());
        myTopButtonPanel.add(getDeactivateLayerButton());
        myTopButtonPanel.add(getDeleteLayerButton());
        myTopButtonPanel.add(bookmarkSplitButton.getSplitButton());
        myTopButtonPanel.add(getPlayLayersButton());
        myTopButtonPanel.add(getPauseLayersButton());
        myTopButtonPanel.add(getStopLayersButton());

        myTopButtonPanel.fillHorizontalSpace().fillNone();
        myTopButtonPanel.add(importButton.getSplitButton());

        myLowerPanel = new JPanel(new BorderLayout());
        myLowerPanel.setBackground(new Color(0, 0, 0, 0));
        myLowerPanel.add(myLayerControlPanel, BorderLayout.CENTER);

        super.initGuiElements();
        getSearchFilterTextField().setGhostText("Search active layers");
        getTree().addMouseListener(getController().getMouseListener());
        getTree().setHoverListener(this::handleHoverChange);
    }

    @Override
    public void refreshTreeLabelRequest()
    {
        updateLabelState();
    }

    @Override
    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);
        if (aFlag && myPopperOuter == null)
        {
            myPopperOuter = new LayerPopperOuter(getToolbox(), this);
        }
    }

    @Override
    public void treeRepaintRequest()
    {
        EventQueueUtilities.runOnEDT(() -> getTree().repaint());
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        TreePath[] selectedPaths = getTree().getSelectionPaths();
        if (selectedPaths != null)
        {
            Collection<GroupByNodeUserObject> userObjects = LayerUtilities.userObjectsFromTreePaths(selectedPaths);

            Collection<DataGroupInfo> dataGroups = New.list();
            Collection<DataTypeInfo> dataTypes = New.list();
            Collection<DataGroupInfo> actualDataGroups = New.list();
            Collection<DataTypeInfo> actualDataTypes = New.list();
            for (GroupByNodeUserObject uo : userObjects)
            {
                if (uo.getDataTypeInfo() != null && !dataTypes.contains(uo.getDataTypeInfo()))
                {
                    dataTypes.add(uo.getDataTypeInfo());
                }
                if (uo.getDataGroupInfo() != null && !dataGroups.contains(uo.getDataGroupInfo()))
                {
                    dataGroups.add(uo.getDataGroupInfo());
                }

                if (uo.getActualDataTypeInfo() != null && !actualDataTypes.contains(uo.getActualDataTypeInfo()))
                {
                    actualDataTypes.add(uo.getActualDataTypeInfo());
                }
                if (uo.getDataGroupInfo() != null && !actualDataGroups.contains(uo.getDataGroupInfo()))
                {
                    actualDataGroups.add(uo.getDataGroupInfo());
                }
            }

            // Make the selected paths visible
            for (TreePath tp : selectedPaths)
            {
                getTree().scrollPathToVisible(tp);
            }

            // Tell people about the selected data types
            for (DataTypeInfo dataType : dataTypes)
            {
                getController().setSelected(dataType);
            }
            getToolbox().getEventManager().publishEvent(new DataTypeInfoFocusEvent(dataTypes, this));

            // Tell the active layer control panel what was selected
            myLayerControlPanel.delaySetSelected(new LayerSelectedEvent(true, actualDataGroups, actualDataTypes));

            myDeactivateLayerButton.setEnabled(dataGroups.stream().anyMatch(dataGroup -> dataGroup.userActivationStateControl()));
            myDeleteLayerButton.setEnabled(dataGroups.stream().anyMatch(dataGroup -> dataGroup.userDeleteControl()));
            getController().getDragAndDropHandler().getController()
                    .setAllowDrag(dataGroups.stream().anyMatch(dataGroup -> dataGroup.isDragAndDrop()));
        }
        else
        {
            myDeactivateLayerButton.setEnabled(false);
            myDeleteLayerButton.setEnabled(false);
            getController().getDragAndDropHandler().getController().setAllowDrag(false);
            myLayerControlPanel.delaySetSelected(new LayerSelectedEvent(true, null, null));
            getController().setSelected(null);
            getToolbox().getEventManager().publishEvent(new DataTypeInfoFocusEvent(Collections.emptySet(), this));
        }
    }

    @Override
    protected void checkBoxActionPerformed(ActionEvent e)
    {
        if (e.getSource() instanceof ButtonModelPayloadJCheckBox)
        {
            ButtonModelPayloadJCheckBox cb = (ButtonModelPayloadJCheckBox)e.getSource();
            Object payload = cb.getPayloadData();
            if (payload instanceof GroupByNodeUserObject)
            {
                GroupByNodeUserObject uo = (GroupByNodeUserObject)payload;
                if (uo.getDataTypeInfo() != null)
                {
                    getController().toggleTypeVisibility(cb.isSelected(), uo.getDataTypeInfo());
                }
                else if (uo.getDataGroupInfo() != null)
                {
                    getController().toggleGroupVisibility(cb.isSelected(), uo.getDataGroupInfo());
                }
            }
        }
    }

    @Override
    protected String getActionContextId()
    {
        return DataGroupInfo.ACTIVE_DATA_CONTEXT;
    }

    @Override
    protected String getDataPanelTitle()
    {
        return "Active";
    }

    @Override
    protected DiscoveryTreeExpansionHelper getExpansionHelper()
    {
        return myExpansionHelper;
    }

    @Override
    protected JPanel getLowerPanel()
    {
        return myLowerPanel;
    }

    @Override
    protected JMenuItem getShowLayerDetailsMenuItem(final DataGroupInfo dgi)
    {
        JMenuItem menuItem = new JMenuItem("Layer Details");
        menuItem.addActionListener(e -> myLayerDetailsCoordinator.showLayerDetailsForGroup(dgi, null));
        return menuItem;
    }

    @Override
    protected JPanel getTopButtonPanel()
    {
        return myTopButtonPanel;
    }

    @Override
    protected DataTreeButtonProvisioner getTreeButtonProvisioner()
    {
        return myTreeButtonBuilders;
    }

    @Override
    protected Component getViewByComboBox()
    {
        final JComboBox<String> viewBy = new JComboBox<>(myDataLayerController.getViewByTypes());
        int selectedIndex = 0;
        String viewByString = getController().getViewByTypeString().toLowerCase();
        for (int i = 0; i < viewBy.getItemCount(); i++)
        {
            if (viewByString.equals(viewBy.getItemAt(i).toLowerCase()))
            {
                selectedIndex = i;
                break;
            }
        }
        viewBy.setSelectedIndex(selectedIndex);
        viewBy.addActionListener(e ->
        {
            String activeType = viewBy.getSelectedItem().toString();
            Quantify.collectMetric("mist3d.layer-manager.group-by." + activeType);

            myLayerControlPanel.setSelected(new LayerSelectedEvent(true, null, null));
            getController().setSelected(null);
            getController().setViewByTypeFromString(activeType);
        });
        return viewBy;
    }

    @Override
    protected boolean isExpandContractTreeButtonInitialSelectionState()
    {
        return true;
    }

    @Override
    protected void rebuildTreeComplete(List<TreeTableTreeNode> lastSelectedNodes)
    {
        if (lastSelectedNodes != null && lastSelectedNodes.size() == 1)
        {
            TreeTableTreeNode node = lastSelectedNodes.get(0);
            final GroupByNodeUserObject userObject = LayerUtilities.getUserObject(node);
            focusOnNode(new Predicate<GroupByNodeUserObject>()
            {
                @Override
                public boolean test(GroupByNodeUserObject value)
                {
                    String dgiId = null;
                    String dtiId = null;
                    if (userObject != null)
                    {
                        dgiId = userObject.getDataGroupInfo() == null ? null : userObject.getDataGroupInfo().getId();
                        dtiId = userObject.getDataTypeInfo() == null ? null : userObject.getDataTypeInfo().getTypeKey();
                    }

                    String vDgiId = value.getDataGroupInfo() == null ? null : value.getDataGroupInfo().getId();
                    String vDtiId = value.getDataTypeInfo() == null ? null : value.getDataTypeInfo().getTypeKey();

                    boolean equals = EqualsHelper.equals(dgiId, vDgiId, dtiId, vDtiId);
                    return equals;
                }
            }, new Runnable()
            {
                @Override
                public void run()
                {
                    EventQueueUtilities.runOnEDT(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            myLayerControlPanel.setSelected(new LayerSelectedEvent(true, null, null));
                            getController().setSelected(null);
                        }
                    });
                }
            });
        }
    }

    /**
     * Deactivate selected layers.
     */
    private void deactivateSelectedLayers()
    {
        Quantify.collectMetric("mist3d.layer-manager.button-row.de-activate-selected-layers");
        List<TreeTableTreeNode> nodes = getSelectedNodes();
        if (nodes != null && !nodes.isEmpty())
        {
            Set<DataGroupInfo> groupsToDeactivate = New.set();
            for (TreeTableTreeNode node : nodes)
            {
                LayerUtilities.recursivelyAddAllDataGroupsToSet(groupsToDeactivate, node,
                        g -> g != null && g.userActivationStateControl());
            }
            if (!groupsToDeactivate.isEmpty())
            {
                myDataLayerController.changeActivation(groupsToDeactivate, false);
            }
        }
    }

    /**
     * Deletes selected layers.
     */
    private void deleteSelectedLayers()
    {
        Quantify.collectMetric("mist3d.layer-manager.button-row.delete-selected-my-places");
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected layers?",
                "Delete Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.OK_OPTION)
        {
            List<TreeTableTreeNode> nodes = getSelectedNodes();
            if (nodes != null && !nodes.isEmpty())
            {
                Set<DataGroupInfo> groupsToDelete = New.set();
                for (TreeTableTreeNode node : nodes)
                {
                    LayerUtilities.recursivelyAddJustDataGroups(groupsToDelete, node, g -> g != null && g.userDeleteControl());
                }

                Map<DataTypeInfo, DataGroupInfo> typesToDelete = New.map();
                for (TreeTableTreeNode node : nodes)
                {
                    LayerUtilities.recursivelyAddAllDataTypesToMap(typesToDelete, node, g -> g != null && g.userDeleteControl());
                }

                if (!groupsToDelete.isEmpty())
                {
                    myDataLayerController.deleteDataGroups(groupsToDelete);
                }

                if (!typesToDelete.isEmpty())
                {
                    for (Map.Entry<DataTypeInfo, DataGroupInfo> entry : typesToDelete.entrySet())
                    {
                        myDataLayerController.deleteDataTypeOrGroup(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    /**
     * Recursively search the tree and try to find the node that has the
     * provided group.
     *
     * @param selector the selector
     * @return the tree table tree node
     */
    private Set<TreeTableTreeNode> findNodes(final Predicate<GroupByNodeUserObject> selector)
    {
        Set<TreeTableTreeNode> foundSet = New.set();
        if (getRootNode() != null)
        {
            LayerUtilities.findNodes(foundSet, getRootNode(), selector);
        }
        return foundSet;
    }

    /**
     * Gets the deactivate button.
     *
     * @return the deactivate button
     */
    private IconButton getDeactivateLayerButton()
    {
        if (myDeactivateLayerButton == null)
        {
            myDeactivateLayerButton = new IconButton(IconUtil.IconType.CLOSE);
            myDeactivateLayerButton.setToolTipText("De-activate the currently selected layer(s).");
            myDeactivateLayerButton.setEnabled(false);
            myDeactivateLayerButton.addActionListener(e -> deactivateSelectedLayers());
        }
        return myDeactivateLayerButton;
    }

    /**
     * Gets the Play layer button.
     *
     * @return the play streaming layer button
     */
    private IconButton getPlayLayersButton()
    {
        if (myPlayLayersButton == null)
        {
            myPlayLayersButton = new IconButton(IconUtil.IconType.PLAY, Color.GREEN);
            myPlayLayersButton.setToolTipText("Stream all available layers.");
            myPlayLayersButton.setEnabled(true);
            myPlayLayersButton.setActionCommand(IconUtil.IconType.PLAY.toString());
            myPlayLayersButton.addActionListener(e ->
            {
                Quantify.collectMetric("mist3d.layer-manager.button-row.stream-all.play");
                controlStreamingLayers(e);
            });
        }
        return myPlayLayersButton;
    }

    /**
     * Gets the Pause layer button.
     *
     * @return the pause streaming layer button
     */
    private IconButton getPauseLayersButton()
    {
        if (myPauseLayersButton == null)
        {
            myPauseLayersButton = new IconButton(IconUtil.IconType.PAUSE, Color.YELLOW);
            myPauseLayersButton.setToolTipText("Pause all streaming layers.");
            myPauseLayersButton.setEnabled(true);
            myPauseLayersButton.setActionCommand(IconUtil.IconType.PAUSE.toString());
            myPauseLayersButton.addActionListener(e ->
            {
                Quantify.collectMetric("mist3d.layer-manager.button-row.stream-all.pause");
                controlStreamingLayers(e);
            });
        }
        return myPauseLayersButton;
    }

    /**
     * Gets the stop layer button.
     *
     * @return the stop streaming layer button
     */
    private IconButton getStopLayersButton()
    {
        if (myStopLayersButton == null)
        {
            myStopLayersButton = new IconButton(IconUtil.IconType.STOP, Color.RED);
            myStopLayersButton.setToolTipText("Stop all streaming layers.");
            myStopLayersButton.setEnabled(true);
            myStopLayersButton.setActionCommand(IconUtil.IconType.STOP.toString());
            myStopLayersButton.addActionListener(e ->
            {
                Quantify.collectMetric("mist3d.layer-manager.button-row.stream-all.stop");
                controlStreamingLayers(e);
            });
        }
        return myStopLayersButton;
    }

    /**
     * Gets the delete layer button.
     *
     * @return the delete layer button
     */
    private IconButton getDeleteLayerButton()
    {
        if (myDeleteLayerButton == null)
        {
            myDeleteLayerButton = new IconButton(IconUtil.IconType.DELETE);
            myDeleteLayerButton.setToolTipText("Delete the currently selected My Places.");
            myDeleteLayerButton.setEnabled(false);
            myDeleteLayerButton.addActionListener(e -> deleteSelectedLayers());
        }
        return myDeleteLayerButton;
    }

    /**
     * Gets the show hide features button.
     *
     * @return the show hide features button
     */
    private IconToggleButton getShowHideFeaturesButton()
    {
        if (myShowHideFeaturesButton == null)
        {
            myShowHideFeaturesButton = new IconToggleButton();
            myShowHideFeaturesButton.setToolTipText("Toggles the visibility of all feature layers.");
            myShowHideFeaturesButton.setSelected(true);
            IconUtil.setIcons(myShowHideFeaturesButton, "/images/features.png", IconUtil.DEFAULT_ICON_FOREGROUND,
                    IconUtil.ICON_SELECTION_FOREGROUND);
            myShowHideFeaturesButton.addActionListener(e -> toggleFeatureVisibility(myShowHideFeaturesButton.isSelected()));
        }
        return myShowHideFeaturesButton;
    }

    /**
     * Gets the show hide tiles button.
     *
     * @return the show hide tiles button
     */
    private IconToggleButton getShowHideTilesButton()
    {
        if (myShowHideTilesButton == null)
        {
            myShowHideTilesButton = new IconToggleButton();
            myShowHideTilesButton.setToolTipText("Toggles the visibility of all data tile layers.");
            myShowHideTilesButton.setSelected(true);
            IconUtil.setIcons(myShowHideTilesButton, "/images/tiles.png", IconUtil.DEFAULT_ICON_FOREGROUND,
                    IconUtil.ICON_SELECTION_FOREGROUND);
            myShowHideTilesButton.addActionListener(e -> toggleTileVisibility(myShowHideTilesButton.isSelected()));
        }
        return myShowHideTilesButton;
    }

    /**
     * Handles a button click event.
     *
     * @param e the event
     */
    private void handleButtonClicked(ActionEvent e)
    {
        String command = e.getActionCommand();

        // Stuff that requires a data group gets handled here
        DataGroupInfo dataGroupInfo = getDataGroupForEvent(e);
        if (dataGroupInfo != null)
        {
            if (DataTreeButtonProvisioner.GEAR_BUTTON.equals(command))
            {
                Quantify.collectMetric("mist3d.layer-manager.tree.layer-settings");
                myLayerDetailsCoordinator.showLayerDetailsForGroup(dataGroupInfo, LayerDetailPanel.SETTINGS_TAB);
            }
            else if (DataTreeButtonProvisioner.FILTER_BUTTON.equals(command))
            {
                Quantify.collectMetric("mist3d.layer-manager.tree.filters");
                myToolbox.getEventManager().publishEvent(new ShowFilterDialogEvent(dataGroupInfo.getId()));
            }
            else if (DataTreeButtonProvisioner.REMOVE_BUTTON.equals(command))
            {
                Quantify.collectMetric("mist3d.layer-manager.tree.remove");
                DataTypeInfo dataType = getDataTypeForEvent(e);
                if (dataType != null)
                {
                    DataTypeInfoFocusEvent event = new DataTypeInfoFocusEvent(dataType, this, FocusType.HOVER_LOST);
                    myToolbox.getEventManager().publishEvent(event);
                }
                myDataLayerController.changeActivation(Collections.singleton(dataGroupInfo), false);
            }
        }

        // Everything else gets handled here
        TreeTableTreeNode node = (TreeTableTreeNode)e.getSource();
        if (DataTreeButtonProvisioner.PLAY_BUTTON.equals(command) || DataTreeButtonProvisioner.PAUSE_BUTTON.equals(command)
                || DataTreeButtonProvisioner.STOP_BUTTON.equals(command))
        {
            Quantify.collectConditionalMetric("mist3d.layer-manager.tree.play",
                    DataTreeButtonProvisioner.PLAY_BUTTON.equals(command));
            Quantify.collectConditionalMetric("mist3d.layer-manager.tree.pause",
                    DataTreeButtonProvisioner.PAUSE_BUTTON.equals(command));
            Quantify.collectConditionalMetric("mist3d.layer-manager.tree.stop",
                    DataTreeButtonProvisioner.STOP_BUTTON.equals(command));

            StreamingSupport streamingSupport = StreamingSupportUpdater.getStreamingSupport(node);
            if (streamingSupport != null)
            {
                PlayState playState = DataTreeButtonProvisioner.PAUSE_BUTTON.equals(command) ? PlayState.PAUSE
                        : DataTreeButtonProvisioner.STOP_BUTTON.equals(command) ? PlayState.STOP : PlayState.FORWARD;
                streamingSupport.getPlayState().set(playState);
            }
        }
        else if (DataTreeButtonProvisioner.PLAYCLOCK_BUTTON.equals(command))
        {
            Quantify.collectMetric("mist3d.layer-manager.tree.playclock");
            StreamingSupport streamingSupport = StreamingSupportUpdater.getStreamingSupport(node);
            if (streamingSupport != null)
            {
                streamingSupport.setSynchronized(Boolean.TRUE);
                streamingSupport.getPlayState().set(PlayState.FORWARD);
            }
        }
        else if (DataTreeButtonProvisioner.DELETE_BUTTON.equals(command))
        {
            Quantify.collectMetric("mist3d.layer-manager.tree.delete");
            GroupByNodeUserObject userObject = LayerUtilities.getUserObject(node);
            if (userObject != null)
            {
                DataTypeInfo dataTypeInfo = userObject.getDataTypeInfo();
                DataGroupInfo uoDataGroupInfo = userObject.getDataGroupInfo();

                String name = uoDataGroupInfo.getDisplayName();
                if (dataTypeInfo != null)
                {
                    name = dataTypeInfo.getDisplayName();
                }

                int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + name + "?",
                        "Delete Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.OK_OPTION)
                {
                    myDataLayerController.deleteDataTypeOrGroup(dataTypeInfo, uoDataGroupInfo);
                }
            }
        }
        else if (DataTreeButtonProvisioner.POPOUT_BUTTON.equals(command))
        {
            Quantify.collectMetric("mist3d.layer-manager.tree.popout");
            if (myPopperOuter == null)
            {
                myPopperOuter = new LayerPopperOuter(getToolbox(), this);
            }
            myPopperOuter.popoutLayers(node, getParent());
        }
    }

    /**
     * Handles a DataTypeInfoFocusEvent.
     *
     * @param event the event
     */
    private void handleDataTypeInfoFocusEvent(DataTypeInfoFocusEvent event)
    {
        if (!event.getSource().getClass().equals(ActiveDataPanel.class) && !event.getTypes().isEmpty())
        {
            final Collection<? extends DataTypeInfo> dtis = event.getTypes();
            final Predicate<GroupByNodeUserObject> filter = value -> value.getDataTypeInfo() != null
                    && dtis.stream().anyMatch(t -> t.getTypeKey().equals(value.getDataTypeInfo().getTypeKey()));
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    Set<TreeTableTreeNode> foundSet = findNodes(filter);
                    if (!foundSet.isEmpty())
                    {
                        if (event.getFocusType() == TypeFocusEvent.FocusType.CLICK)
                        {
                            final TreePath tp = JTreeUtilities.buildPathToNode(foundSet.iterator().next());
                            getTree().scrollPathToVisible(tp);
                            int row = getTree().getRowForPath(tp);
                            if (row != -1)
                            {
                                getTree().setSelectionInterval(row, row);
                            }
                        }
                        else if (event.getFocusType() == TypeFocusEvent.FocusType.HOVER_GAINED)
                        {
                            List<TreePath> paths = foundSet.stream().map(n -> JTreeUtilities.buildPathToNode(n))
                                    .collect(Collectors.toList());
                            getTree().setHoverPaths(paths);
                        }
                        else
                        {
                            getTree().setHoverPaths(Collections.emptySet());
                        }
                    }
                }
            });
        }
    }

    /**
     * Handles a change in the hovered path of the tree.
     *
     * @param path the tree path
     * @param isHoverGained true for hover gained, false for hover lost
     */
    private void handleHoverChange(TreePath path, Boolean isHoverGained)
    {
        GroupByNodeUserObject userObject = LayerUtilities.userObjectFromTreePath(path);
        if (userObject != null && userObject.getActualDataTypeInfo() != null)
        {
            TypeFocusEvent.FocusType focusType = isHoverGained.booleanValue() ? TypeFocusEvent.FocusType.HOVER_GAINED
                    : TypeFocusEvent.FocusType.HOVER_LOST;
            DataTypeInfoFocusEvent event = new DataTypeInfoFocusEvent(userObject.getActualDataTypeInfo(), this, focusType);
            myToolbox.getEventManager().publishEvent(event);
        }
    }

    /**
     * Handles an order change.
     */
    private void handleOrderChange()
    {
        myDataLayerController.setTreeNeedsRebuild(true);
        rebuildTree();
    }

    /**
     * Streaming control for all streamable layers loaded in panel.
     *
     * @param e the button event.
     */
    private void controlStreamingLayers(ActionEvent e)
    {
        String command = e.getActionCommand();

        PlayState playState = IconUtil.IconType.PAUSE.toString().equals(command) ? PlayState.PAUSE
                : IconUtil.IconType.STOP.toString().equals(command) ? PlayState.STOP : PlayState.FORWARD;

        DataGroupController controller = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController();
        Collection<DataTypeInfo> dataTypes = controller.findActiveMembers(
                t -> t.getStreamingSupport().isStreamingEnabled() && t.getStreamingSupport().getPlayState().get() != playState);
        for (DataTypeInfo dataType : dataTypes)
        {
            EventQueueUtilities.runOnEDT(() -> dataType.getStreamingSupport().getPlayState().set(playState));
        }
    }

    /**
     * Toggle feature visibility.
     *
     * @param visible the visible
     */
    private void toggleFeatureVisibility(final boolean visible)
    {
        Quantify.collectEnableDisableMetric("mist3d.layer-manager.button-row.toggle-feature-visibility", visible);

        findNodes(new Predicate<GroupByNodeUserObject>()
        {
            @Override
            public boolean test(GroupByNodeUserObject value)
            {
                DataGroupInfo dgi = value.getDataGroupInfo();
                DataTypeInfo dti = value.getDataTypeInfo();
                if (dti != null)
                {
                    if (dti.getMapVisualizationInfo() != null && dti.getMapVisualizationInfo().usesMapDataElements())
                    {
                        dti.setVisible(visible, null);
                    }
                }
                else if (dgi != null && dgi.hasMembers(false))
                {
                    for (DataTypeInfo type : dgi.getMembers(false))
                    {
                        if (type.getMapVisualizationInfo() != null && type.getMapVisualizationInfo().usesMapDataElements())
                        {
                            type.setVisible(visible, null);
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * Toggle feature visibility.
     *
     * @param visible the tiles visible
     */
    private void toggleTileVisibility(final boolean visible)
    {
        Quantify.collectEnableDisableMetric("mist3d.layer-manager.button-row.toggle-tile-visibility", visible);
        findNodes(new Predicate<GroupByNodeUserObject>()
        {
            @Override
            public boolean test(GroupByNodeUserObject value)
            {
                DataGroupInfo dgi = value.getDataGroupInfo();
                DataTypeInfo dti = value.getDataTypeInfo();
                if (dti != null)
                {
                    if (dti.getMapVisualizationInfo() != null && dti.getBasicVisualizationInfo() != null
                            && dti.getMapVisualizationInfo().isImageTileType()
                            && !LoadsTo.BASE.equals(dti.getBasicVisualizationInfo().getLoadsTo()))
                    {
                        dti.setVisible(visible, null);
                    }
                }
                else if (dgi != null && dgi.hasMembers(false))
                {
                    for (DataTypeInfo type : dgi.getMembers(false))
                    {
                        if (type.getMapVisualizationInfo() != null && type.getBasicVisualizationInfo() != null
                                && type.getMapVisualizationInfo().isImageTileType()
                                && !LoadsTo.BASE.equals(type.getBasicVisualizationInfo().getLoadsTo()))
                        {
                            type.setVisible(visible, null);
                        }
                    }
                }
                return false;
            }
        });
    }
}
