package io.opensphere.controlpanels.layers.availabledata;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

import io.opensphere.controlpanels.layers.activedata.controller.AvailableDataDataLayerController;
import io.opensphere.controlpanels.layers.activedata.tree.TreeTransferHandler;
import io.opensphere.controlpanels.layers.activedata.zorder.ZOrderTreeTransferHandler;
import io.opensphere.controlpanels.layers.availabledata.detail.DetailPanelManager;
import io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel;
import io.opensphere.controlpanels.layers.base.DataTreeButtonProvisioner;
import io.opensphere.controlpanels.layers.base.DiscoveryDataLayerChangeListener;
import io.opensphere.controlpanels.layers.base.DiscoveryTreeExpansionHelper;
import io.opensphere.controlpanels.layers.event.AvailableGroupSelectionEvent;
import io.opensphere.controlpanels.layers.importdata.ImportDataPanel;
import io.opensphere.controlpanels.layers.layerdetail.LayerDetailPanel;
import io.opensphere.controlpanels.layers.layerdetail.LayerDetailsCoordinator;
import io.opensphere.controlpanels.util.ShowFilterDialogEvent;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.tree.ButtonModelPayloadJCheckBox;
import io.opensphere.core.util.swing.tree.TreeTableTreeCellRenderer;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.event.ServerManagerDialogChangeEvent;
import io.opensphere.mantle.data.event.ServerManagerDialogChangeEvent.EventType;
import io.opensphere.mantle.data.impl.DefaultGroupInfoTreeNodeData;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;
import io.opensphere.mantle.util.MantleToolboxUtils;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/** The AddDataPanel. */
@SuppressWarnings("PMD.GodClass")
public final class AvailableDataPanel extends AbstractDiscoveryDataPanel implements ActionListener
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The activation listener. */
    private final transient Runnable myActivationListener = this::handleDataGroupActivationChange;

    /** The provider for context menus for a collection of data groups. */
    private final transient ContextMenuProvider<DataGroupContextKey> myContextMenuProvider = new ContextMenuProvider<DataGroupContextKey>()
    {
        @Override
        public List<? extends Component> getMenuItems(String contextId, DataGroupContextKey key)
        {
            List<Component> menuItems = New.list();
            DataGroupInfo dgi = key.getDataGroup();

            JMenuItem expandCollapse = getExpandContractNodeMenuItem();
            if (expandCollapse != null)
            {
                menuItems.add(expandCollapse);
            }
            Collection<DataGroupInfo> dgis = Collections.singleton(dgi);
            if (anyCanBeActivated(dgis))
            {
                menuItems.add(getActivationMenuItem(dgis, true));
            }
            if (anyCanBeDeactivated(dgis))
            {
                menuItems.add(getActivationMenuItem(dgis, false));
            }
            if (anyCanBeRemoved(dgis))
            {
                menuItems.add(getRemoveLayerMenuItem(dgis));
            }
            if (dgi.hasDetails())
            {
                menuItems.add(getShowLayerDetailsMenuItem(dgi));
            }
            if (dgi.isTaggable())
            {
                menuItems.add(getManageTagsMenuItem(dgi));
            }

            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 0;
        }
    };

    /** The available data layer controller. */
    private final transient AvailableDataDataLayerController myDataLayerController;

    /**
     * The panel on which detail views are rendered.
     */
    private transient JFXPanel myDetailsPanel;

    /**
     * The root pane of the detail scene.
     */
    private transient BorderPane myDetailRootPane;

    /**
     * The manager used to render details.
     */
    private final transient DetailPanelManager myDetailPanelManager;

    /** The Expansion helper. */
    private final transient DiscoveryTreeExpansionHelper myExpansionHelper;

    /** The group focus request listener. */
    private final transient EventListener<AvailableGroupSelectionEvent> myFocusListener = event ->
    {
        DataGroupInfo eventDataGroup = event.getDataGroupInfo();
        if (eventDataGroup == null)
        {
            return;
        }

        focusOnNode(value -> value.getDataGroupInfo() != null && value.getDataGroupInfo().getId().equals(eventDataGroup.getId()),
                null);
    };

    /** The my layer details coordinator. */
    private final transient LayerDetailsCoordinator myLayerDetailsCoordinator;

    /** The provider for context menus for a single data group. */
    private final transient ContextMenuProvider<MultiDataGroupContextKey> myMultiGroupMenuProvider = new ContextMenuProvider<MultiDataGroupContextKey>()
    {
        @Override
        public List<? extends Component> getMenuItems(String contextId, MultiDataGroupContextKey key)
        {
            List<Component> menuItems = New.list();
            Collection<DataGroupInfo> dgis = key.getDataGroups();
            if (anyCanBeActivated(dgis))
            {
                menuItems.add(getActivationMenuItem(dgis, true));
            }
            if (anyCanBeDeactivated(dgis))
            {
                menuItems.add(getActivationMenuItem(dgis, false));
            }
            if (anyCanBeRemoved(dgis))
            {
                menuItems.add(getRemoveLayerMenuItem(dgis));
            }
            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 0;
        }
    };

    /**
     * The Server manager button.
     */
    private JButton myServerManagerButton;

    /** The Server manager visibility change listener. */
    private final transient EventListener<ServerManagerDialogChangeEvent> myDialogListener = event -> updateStatus(event);

    /**
     * Updates the icons if a Server Status Event was sent.
     *
     * @param event the event to process.
     */
    protected void updateStatus(ServerManagerDialogChangeEvent event)
    {
        if (event.getEventType().equals(EventType.SERVER_STATUS_UPDATE) && event.getStatus() != null)
        {
            EventQueueUtilities.invokeLater(() -> IconUtil.setIcons(myServerManagerButton, IconType.STORAGE, event.getStatus()));
        }
    }

    /**
     * The Toolbox.
     */
    private final transient Toolbox myToolbox;

    /**
     * The Top right panel.
     */
    private GridBagPanel myTopRightPanel;

    /**
     * The tree transfer handler.
     */
    private final transient TreeTransferHandler myTransferHandler;

    /**
     * The builder used to add additional buttons to the right side of the tree
     * row when the user hovers the mouse over the entry.
     */
    private final transient AvailableDataTreeButtonBuilders myTreeButtonBuilders;

    /**
     * Refreshes the tree structure under a given data group info.
     */
    private final transient RefreshTreeController myTreeRefresher;

    /**
     * The tree table tree cell renderer.
     */
    private final transient AvailableDataTreeTableTreeCellRenderer myTreeTableTreeCellRenderer;

    /**
     * Instantiates a new time line panel.
     *
     * @param tb the {@link Toolbox}
     * @param coordinator the coordinator
     */
    public AvailableDataPanel(Toolbox tb, LayerDetailsCoordinator coordinator)
    {
        super(tb);
        myToolbox = tb;
        myLayerDetailsCoordinator = coordinator;
        myDetailPanelManager = new DetailPanelManager(myToolbox);

        tb.getEventManager().subscribe(AvailableGroupSelectionEvent.class, myFocusListener);
        tb.getEventManager().subscribe(ServerManagerDialogChangeEvent.class, myDialogListener);

        ContextActionManager contextManager = getToolbox().getUIRegistry().getContextActionManager();
        contextManager.registerContextMenuItemProvider(DataGroupInfo.MANAGE_DATA_CONTEXT, DataGroupContextKey.class,
                myContextMenuProvider);
        contextManager.registerContextMenuItemProvider(DataGroupInfo.MANAGE_DATA_CONTEXT, MultiDataGroupContextKey.class,
                myMultiGroupMenuProvider);

        myDataLayerController = new AvailableDataDataLayerController(getToolbox(), this);
        myDataLayerController.addListener(this);

        myTreeButtonBuilders = new AvailableDataTreeButtonBuilders(getToolbox(), this);
        myTreeTableTreeCellRenderer = new AvailableDataTreeTableTreeCellRenderer(getToolbox());
        myTreeTableTreeCellRenderer.setButtonBuilders(getTreeButtonProvisioner().getButtonBuilders());

        myTransferHandler = new TreeTransferHandler(
                new ZOrderTreeTransferHandler(myToolbox, getController().getOrderTreeEventController()));

        myExpansionHelper = new DiscoveryTreeExpansionHelper(getToolbox().getPreferencesRegistry().getPreferences(getClass()),
                DiscoveryTreeExpansionHelper.Mode.STORE_EXPANSIONS);
        myExpansionHelper.loadFromPreferences();

        myTreeRefresher = new RefreshTreeController();

        MantleToolboxUtils.getMantleToolbox(tb).getDataGroupController().addActivationListener(myActivationListener);
    }

    /** Ignore because this isn't really a HUD panel. */
    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
    }

    /** Ignore because this isn't really a HUD panel. */
    @Override
    public void doLayout()
    {
        layout();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        DataGroupInfo dataGroupInfo = getDataGroupForEvent(e);
        if (dataGroupInfo != null)
        {
            if (DataTreeButtonProvisioner.GEAR_BUTTON.equals(e.getActionCommand()))
            {
                myLayerDetailsCoordinator.showLayerDetailsForGroup(dataGroupInfo, LayerDetailPanel.SETTINGS_TAB);
            }
            else if (DataTreeButtonProvisioner.FILTER_BUTTON.equals(e.getActionCommand()))
            {
                myToolbox.getEventManager().publishEvent(new ShowFilterDialogEvent(dataGroupInfo.getId()));
            }
            else if (DataTreeButtonProvisioner.REFRESH_BUTTON.equals(e.getActionCommand()))
            {
                myTreeRefresher.refresh(dataGroupInfo);
            }
            else if (DataTreeButtonProvisioner.REMOVE_BUTTON.equals(e.getActionCommand()))
            {
                myDataLayerController.removeDataGroup(dataGroupInfo);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.DiscoveryDataLayerChangeListener#dataGroupsChanged()
     */
    @Override
    public void dataGroupsChanged()
    {
        rebuildTree();
    }

    /**
     * {@inheritDoc}
     *
     * @see DiscoveryDataLayerChangeListener#dataGroupVisibilityChanged(DataTypeVisibilityChangeEvent)
     */
    @Override
    public void dataGroupVisibilityChanged(DataTypeVisibilityChangeEvent event)
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getController()
     */
    @Override
    public AvailableDataDataLayerController getController()
    {
        return myDataLayerController;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getTreeTableTreeCellRenderer()
     */
    @Override
    public TreeTableTreeCellRenderer getTreeTableTreeCellRenderer()
    {
        return myTreeTableTreeCellRenderer;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getTreeTransferHandler()
     */
    @Override
    public TreeTransferHandler getTreeTransferHandler()
    {
        return myTransferHandler;
    }

    /**
     * Initializes the set of components needed for JavaFX / Swing integration.
     */
    protected void initFx()
    {
        myDetailRootPane = new BorderPane();
        Scene scene = new Scene(myDetailRootPane, Color.TRANSPARENT);
        FXUtilities.addDesktopStyle(scene);
        myDetailsPanel.setScene(scene);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#initGuiElements()
     */
    @Override
    public void initGuiElements()
    {
        myDetailsPanel = new JFXPanel();
        myDetailsPanel.setPreferredSize(new Dimension(300, 600));
        Platform.runLater(this::initFx);

        createServerManagerButton();

        myTopRightPanel = new GridBagPanel();
        myTopRightPanel.setGridx(0).setGridy(0).setInsets(0, 0, 0, 10).add(myServerManagerButton);
        ImportDataPanel importDataPanel = new ImportDataPanel(getToolbox());
        myTopRightPanel.setGridx(1).setGridy(0).setInsets(0, 0, 0, 0).add(importDataPanel);

        super.initGuiElements();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.DiscoveryDataLayerChangeListener#refreshTreeLabelRequest()
     */
    @Override
    public void refreshTreeLabelRequest()
    {
        updateLabelState();
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        TreePath treePath = getTree().getSelectionPath();
        if (treePath != null)
        {
            TreeTableTreeNode treeNode = (TreeTableTreeNode)treePath.getLastPathComponent();
            GroupByNodeUserObject groupByNode = (GroupByNodeUserObject)treeNode.getPayload().getPayloadData();
            Platform.runLater(() -> replaceDetailPanel(groupByNode.getDataGroupInfo()));
        }
        else
        {
            Platform.runLater(() -> replaceDetailPanel(null));
        }
    }

    /**
     * Replaces the detail panel with the type corresponding with the supplied
     * data group.
     *
     * @param pDataGroup the data group for which to get the detail panel.
     */
    protected void replaceDetailPanel(DataGroupInfo pDataGroup)
    {
        myDetailRootPane.setCenter(myDetailPanelManager.getDetailPanel(pDataGroup));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#checkBoxActionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    protected void checkBoxActionPerformed(ActionEvent e)
    {
        if (e.getSource() instanceof ButtonModelPayloadJCheckBox)
        {
            ButtonModelPayloadJCheckBox cb = (ButtonModelPayloadJCheckBox)e.getSource();
            Object payloadData = cb.getPayloadData();

            if (payloadData instanceof DefaultGroupInfoTreeNodeData)
            {
                DefaultGroupInfoTreeNodeData node = (DefaultGroupInfoTreeNodeData)payloadData;
                getController().changeActivation(New.set(node.getDataGroupInfo()), cb.isSelected());
            }
            else if (payloadData instanceof GroupByNodeUserObject)
            {
                GroupByNodeUserObject uo = (GroupByNodeUserObject)payloadData;
                if (uo.getDataGroupInfo() != null)
                {
                    DataGroupInfo dgi = uo.getDataGroupInfo();
                    if (dgi.isTriggeringSupported())
                    {
                        getController().trigger(this, New.set(dgi));
                    }

                    if (dgi.isActivationSupported())
                    {
                        getController().changeActivation(New.set(dgi), cb.isSelected());
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getActionContextId()
     */
    @Override
    protected String getActionContextId()
    {
        return DataGroupInfo.MANAGE_DATA_CONTEXT;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getDataPanelTitle()
     */
    @Override
    protected String getDataPanelTitle()
    {
        return "Add Data";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getDetailsComponent()
     */
    @Override
    protected JComponent getDetailsComponent()
    {
        return myDetailsPanel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getExpansionHelper()
     */
    @Override
    protected DiscoveryTreeExpansionHelper getExpansionHelper()
    {
        return myExpansionHelper;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getShowLayerDetailsMenuItem(io.opensphere.mantle.data.DataGroupInfo)
     */
    @Override
    protected JMenuItem getShowLayerDetailsMenuItem(final DataGroupInfo dgi)
    {
        JMenuItem menuItem = new JMenuItem("Layer Details");
        menuItem.addActionListener(e -> myLayerDetailsCoordinator.showLayerDetailsForGroup(dgi, null));
        return menuItem;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getTopRightPanel()
     */
    @Override
    protected JPanel getTopRightPanel()
    {
        return myTopRightPanel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getTreeButtonProvisioner()
     */
    @Override
    protected DataTreeButtonProvisioner getTreeButtonProvisioner()
    {
        return myTreeButtonBuilders;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel#getViewByComboBox()
     */
    @Override
    protected Component getViewByComboBox()
    {
        final JComboBox<String> viewBy = new JComboBox<>(myDataLayerController.getViewTypes());
        Dimension dim = new Dimension(200, 24);
        viewBy.setPreferredSize(dim);
        viewBy.setMinimumSize(dim);
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
            getExpandContractTreeButton().setSelected(false);
            getController().setViewByTypeFromString(viewBy.getSelectedItem().toString());
        });
        return viewBy;
    }

    /**
     * Tests to determine if any of the supplied data groups can be deleted.
     *
     * @param dgiSet the data groups to test for removal.
     * @return true, if one or more of the data groups can be removed.
     */
    protected boolean anyCanBeRemoved(Collection<DataGroupInfo> dgiSet)
    {
        for (DataGroupInfo dgi : dgiSet)
        {
            if (dgi.getAssistant() != null && dgi.getAssistant().canDeleteGroup(dgi))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the server manager button.
     */
    protected void createServerManagerButton()
    {
        myServerManagerButton = new IconButton();
        myServerManagerButton.setToolTipText("Manage Servers");
        myServerManagerButton.addActionListener(e -> getToolbox().getEventManager()
                .publishEvent(new ServerManagerDialogChangeEvent(this, EventType.VISIBILITY_CHANGE)));
        getToolbox().getEventManager().publishEvent(new ServerManagerDialogChangeEvent(this, EventType.SERVER_STATUS_REQUEST));
    }

    /**
     * Gets the removes the layer menu item.
     *
     * @param dgis the data groups which will be acted on by the menu item.
     * @return the removes the layer menu item
     */
    protected JMenuItem getRemoveLayerMenuItem(final Collection<DataGroupInfo> dgis)
    {
        JMenuItem menuItem = new JMenuItem("Remove Layer(s)");
        menuItem.addActionListener(e -> myDataLayerController.removeDataGroups(dgis));
        return menuItem;
    }

    /**
     * Handle data group activation change.
     */
    protected void handleDataGroupActivationChange()
    {
        EventQueue.invokeLater(() ->
        {
            updateCheckboxState();
            if (myDataLayerController.getViewByTypeString().equals("Active"))
            {
                myDataLayerController.forceRefresh();
            }
        });
    }
}
