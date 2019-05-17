package io.opensphere.controlpanels.layers.base;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.controlpanels.layers.activedata.tree.TreeTransferHandler;
import io.opensphere.controlpanels.layers.tagmanager.TagUtility;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ActionContext;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconStyle;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GhostTextField;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.HorizontalSpacerForGridbag;
import io.opensphere.core.util.swing.IconToggleButton;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.core.util.swing.tree.ListCheckBoxTree;
import io.opensphere.core.util.swing.tree.TreeDecorator;
import io.opensphere.core.util.swing.tree.TreeTableTreeCellRenderer;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.CategoryContextKey;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * The Class AbstractDiscoveryDataPanel.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractDiscoveryDataPanel extends AbstractHUDPanel
        implements DiscoveryDataLayerChangeListener, TreeSelectionListener, UserConfirmer
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(AbstractDiscoveryDataPanel.class);

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The my tree check box change listener. */
    private final transient ActionListener myCheckBoxChangeListener = e -> checkBoxActionPerformed(e);

    /** If context menus should be shown for the tree. */
    private final boolean myContextMenusEnabled;

    /** The Data layer select panel. */
    private transient AbstractHUDPanel myDataLayerPanel;

    /** The data panel. */
    private transient AbstractHUDPanel myDataPanel;

    /** The expand contract tree button. */
    private transient IconToggleButton myExpandContractTreeButton;

    /** The change executor. */
    private final ProcrastinatingExecutor myRebuildExecutor = new ProcrastinatingExecutor("AddDataPanel::Rebuild", 300);

    /** The root node for the layer selection tree. */
    private transient TreeTableTreeNode myRootNode;

    /** The filter search text field. */
    private transient GhostTextField mySearchTextField;

    /** The selection mode for the tree. */
    private final int mySelectionMode;

    /** The toolbox. */
    private final transient Toolbox myToolbox;

    /** The tree which contains the set of selectable layers. */
    private transient ListCheckBoxTree myTree;

    /** The Tree expansion listener. */
    private final transient TreeExpansionListener myTreeExpansionListener = new TreeExpansionListener()
    {
        @Override
        public void treeCollapsed(TreeExpansionEvent event)
        {
            getExpansionHelper().removeExpansionPath(getController().getViewByTypeString(), event.getPath());
        }

        @Override
        public void treeExpanded(TreeExpansionEvent event)
        {
            getExpansionHelper().addExpansionPath(getController().getViewByTypeString(), event.getPath());
        }
    };

    /** The Tree scroll pane. */
    private transient JScrollPane myTreeScrollPane;

    /** The change executor. */
    private final ProcrastinatingExecutor myUpdateCheckBoxExecutor = new ProcrastinatingExecutor("AddDataPanel::Update", 300,
            500);

    /** If checkboxes should be shown for the tree. */
    private final boolean myUseCheckboxes;

    /**
     * Instantiates a new abstract discovery data panel.
     *
     * @param tb the toolbox
     */
    public AbstractDiscoveryDataPanel(Toolbox tb)
    {
        this(tb, true, true, TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }

    /**
     * Instantiates a new abstract discovery data panel.
     *
     * @param tb the toolbox
     * @param contextMenusEnabled If context menus should be shown for the tree.
     * @param useCheckboxes If checkboxes should be shown for the tree.
     * @param selectionMode The selection mode for the tree.
     */
    public AbstractDiscoveryDataPanel(Toolbox tb, boolean contextMenusEnabled, boolean useCheckboxes, int selectionMode)
    {
        super(tb.getPreferencesRegistry());
        myToolbox = tb;
        myContextMenusEnabled = contextMenusEnabled;
        myUseCheckboxes = useCheckboxes;
        mySelectionMode = selectionMode;
    }

    @Override
    public boolean askUser(String question, String title)
    {
        return JOptionPane.YES_OPTION == EventQueueUtilities
                .happyOnEdt(
                        () -> Integer.valueOf(JOptionPane.showConfirmDialog(this, question, title, JOptionPane.YES_NO_OPTION)))
                .intValue();
    }

    /**
     * Gets the data group for event.
     *
     * @param e the e
     * @return the data group for event
     */
    public DataGroupInfo getDataGroupForEvent(ActionEvent e)
    {
        DataGroupInfo dataGroup = null;
        if (e.getSource() instanceof TreeTableTreeNode)
        {
            TreeTableTreeNode node = (TreeTableTreeNode)e.getSource();
            dataGroup = LayerUtilities.getDataGroup(node);
        }
        return dataGroup;
    }

    /**
     * Gets the data type for event.
     *
     * @param e the e
     * @return the data type for event
     */
    public DataTypeInfo getDataTypeForEvent(ActionEvent e)
    {
        DataTypeInfo dataType = null;
        if (e.getSource() instanceof TreeTableTreeNode)
        {
            TreeTableTreeNode node = (TreeTableTreeNode)e.getSource();
            dataType = LayerUtilities.getUserObject(node).getActualDataTypeInfo();
        }
        return dataType;
    }

    /**
     * Initialize is panel. Derived types will have differing display elements.
     */
    public void initGuiElements()
    {
        setLayout(new BorderLayout());

        createSearchFilterTextField();
        createExpandContractTreeButton();
        initializeTree();
        createTreeScrollPane();
        createDataLayerPanel();
        createDataPanel();

        add(myDataPanel, BorderLayout.CENTER);

        rebuildTreeNow();
    }

    /**
     * Rebuild tree.
     */
    public void rebuildTree()
    {
        myRebuildExecutor.execute(() -> EventQueueUtilities.runOnEDT(this::rebuildTreeNow));
    }

    /** Select all leaves in the tree. */
    public void selectAllLeaves()
    {
        TreeTableTreeNode root = (TreeTableTreeNode)getTree().getModel().getRoot();
        Collection<TreeTableTreeNode> leaves = New.collection();
        root.getAllLeaves(leaves);

        TreePath[] paths = new TreePath[leaves.size()];
        int index = 0;
        for (TreeTableTreeNode leaf : leaves)
        {
            paths[index++] = new TreePath(leaf.getPath());
        }
        getTree().setSelectionPaths(paths);
    }

    @Override
    public void setBackground(Color color)
    {
        super.setBackground(color);

        if (mySearchTextField != null)
        {
            setSearchTextFieldBackgroundColor();
        }
    }

    /**
     * Get if my tree has any nodes.
     *
     * @return {@code true} if the tree has nodes.
     */
    public boolean treeHasNodes()
    {
        return getRootNode().getChildCount() > 0;
    }

    @Override
    public void treeRepaintRequest()
    {
        EventQueueUtilities.runOnEDT(() -> myTree.repaint());
    }

    /**
     * Adjust search filter.
     */
    private void adjustSearchFilter()
    {
        Quantify.collectMetric("mist3d.layer-manager.search");
        getController().setTreeFilter(mySearchTextField.getText());
    }

    /**
     * Any can be deactivated.
     *
     * @param dgis the dgi set
     * @return true if any of the data groups can be deactivated.
     */
    protected boolean anyCanBeActivated(Collection<DataGroupInfo> dgis)
    {
        for (DataGroupInfo dgi : dgis)
        {
            // TODO also check to see if it is NOT active
            if (dgi.userActivationStateControl())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Any can be deactivated.
     *
     * @param dgis the dgi set
     * @return true if any of the data groups can be deactivated.
     */
    protected boolean anyCanBeDeactivated(Collection<DataGroupInfo> dgis)
    {
        for (DataGroupInfo dgi : dgis)
        {
            // TODO also check to see if it is active
            if (dgi.userActivationStateControl())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check box action performed.
     *
     * @param e the ActionEvent
     */
    protected abstract void checkBoxActionPerformed(ActionEvent e);

    /**
     * Gets the data layer select panel.
     */
    private void createDataLayerPanel()
    {
        myDataLayerPanel = new AbstractHUDPanel();
        myDataLayerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        myDataLayerPanel.setOpaque(false);
        myDataLayerPanel.setLayout(new BorderLayout());
        myDataLayerPanel.setSize(new Dimension(215, 338));
        myDataLayerPanel.add(myTreeScrollPane, BorderLayout.CENTER);
    }

    /**
     * Gets the setup panel.
     */
    private void createDataPanel()
    {
        myDataPanel = new AbstractHUDPanel();
        myDataPanel.setOpaque(false);
        myDataPanel.setLayout(new GridBagLayout());
        myDataPanel.setName(getDataPanelTitle());

        JSplitPane splitPane = null;
        AbstractHUDPanel leftPanel = null;
        AbstractHUDPanel rightPanel = null;
        if (getTopRightPanel() != null && getDetailsComponent() != null)
        {
            leftPanel = new AbstractHUDPanel(new BorderLayout());
            leftPanel.setOpaque(false);

            rightPanel = new AbstractHUDPanel(new BorderLayout());
            rightPanel.setOpaque(false);

            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
            splitPane.setOpaque(false);
            splitPane.setOneTouchExpandable(true);
            splitPane.setDividerSize(8);
            splitPane.setResizeWeight(0.5);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        addLeftSideComponents(leftPanel, gbc);
        addRightSideComponents(rightPanel, gbc);

        if (splitPane != null)
        {
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 3;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            myDataPanel.add(splitPane, gbc);
        }
    }

    /**
     * Create the top panel.
     *
     * @return The top panel.
     */
    private JPanel createTopPanel()
    {
        GridBagPanel topPanel = new GridBagPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(1, 2, 2, 0));
        topPanel.fillHorizontal();

        GridBagPanel groupByPanel = new GridBagPanel();
        groupByPanel.add(new JLabel("Group By:"));
        groupByPanel.fillHorizontal().setInsets(0, 2, 0, 0).add(getViewByComboBox());
        topPanel.addRow(groupByPanel);

        topPanel.setInsets(1, 0, 0, 0).addRow(createSearchPanel());

        if (getTopButtonPanel() != null)
        {
            topPanel.setInsets(2, 0, 0, 0).addRow(getTopButtonPanel());
        }

        return topPanel;
    }

    /**
     * Create the search panel.
     *
     * @return The search panel.
     */
    protected GridBagPanel createSearchPanel()
    {
        GridBagPanel searchPanel = new GridBagPanel();
        searchPanel.add(getExpandContractTreeButton());
        searchPanel.setInsets(0, 2, 0, 0);
        searchPanel.fillHorizontal().add(getSearchFilterTextField());
        searchPanel.fillNone();
        return searchPanel;
    }

    /**
     * This method initializes treeScrollPane.
     */
    private void createTreeScrollPane()
    {
        myTreeScrollPane = getJScrollPane(myTree);

        // Disable the horizontal scrollbar for the tree to avoid resizing loop.
        myTreeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        myTreeScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        // HACK resize the parent in order to fix the tree bounds
        myTreeScrollPane.getVerticalScrollBar().addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent e)
            {
                resizeParent(-1);
            }

            @Override
            public void componentShown(ComponentEvent e)
            {
                resizeParent(1);
            }

            /**
             * Gets the relevant ancestor and applies the given delta to the
             * width.
             *
             * @param dWidth the width delta
             */
            private void resizeParent(int dWidth)
            {
                Component comp = ComponentUtilities.getFirstParent(myTreeScrollPane,
                        value -> value instanceof JInternalFrame || value instanceof JSplitPane);

                if (comp instanceof JInternalFrame)
                {
                    comp.setSize(comp.getWidth() + dWidth, comp.getHeight());
                }
                else if (comp instanceof JSplitPane)
                {
                    JSplitPane splitPane = (JSplitPane)comp;
                    splitPane.setDividerLocation(splitPane.getDividerLocation() + dWidth);
                }
            }
        });
    }

    /**
     * Recursively search the tree and try to find the node that has the
     * provided group.
     *
     * @param node the node
     * @param selector the selector
     * @return the tree table tree node
     */
    private TreeTableTreeNode findNodeWithGroup(TreeTableTreeNode node, final Predicate<GroupByNodeUserObject> selector)
    {
        TreeTableTreeNode found = null;
        if (node.getPayload() != null && node.getPayload().getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject uo = (GroupByNodeUserObject)node.getPayload().getPayloadData();
            if (selector.test(uo))
            {
                found = node;
            }
        }
        if (found == null && node.getChildCount() > 0)
        {
            for (TreeTableTreeNode child : node.getChildren())
            {
                found = findNodeWithGroup(child, selector);
                if (found != null)
                {
                    break;
                }
            }
        }
        return found;
    }

    /**
     * Handle group focus request.
     *
     * @param selector the selector
     * @param notFoundAlternative the not found alternative
     */
    protected void focusOnNode(final Predicate<GroupByNodeUserObject> selector, final Runnable notFoundAlternative)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                TreeTableTreeNode foundNode = null;
                if (myTree.getModel().getRoot() instanceof TreeTableTreeNode)
                {
                    foundNode = findNodeWithGroup((TreeTableTreeNode)myTree.getModel().getRoot(), selector);
                }

                if (foundNode != null)
                {
                    final TreeTableTreeNode fFoundNode = foundNode;
                    myTree.expandPath(JTreeUtilities.buildPathToNode(fFoundNode));

                    // TODO this does not preserve the selected row
                    // between tree types.
                    JComponent treeComp = myTree;
                    final TreePath path = JTreeUtilities.buildPathToNode(fFoundNode);

                    ListCheckBoxTree jt = (ListCheckBoxTree)treeComp;
                    int row = jt.getRowForPath(path);
                    if (row != -1)
                    {
                        jt.forceSelection(path);
                        jt.scrollPathToVisible(path);
                    }
                }
                else
                {
                    if (notFoundAlternative != null)
                    {
                        notFoundAlternative.run();
                    }
                }
            }
        });
    }

    /**
     * Get the context id for the context menu. Derived types should use their
     * own context.
     *
     * @return The context id.
     */
    protected abstract String getActionContextId();

    /**
     * Gets the menu item for activation or deactivate.
     *
     * @param dgis the data groups which will be acted on by the menu item.
     * @param activate When true this menu is for activation, when false it is
     *            for de-activation.
     * @return the deactivate menu item
     */
    protected JMenuItem getActivationMenuItem(final Collection<DataGroupInfo> dgis, final boolean activate)
    {
        JMenuItem menuItem = new JMenuItem(activate ? "Activate" : "Deactivate");
        menuItem.addActionListener(e -> getController().changeActivation(dgis, activate));
        return menuItem;
    }

    /**
     * Gets the controller.
     *
     * @return the controller
     */
    protected abstract AbstractDiscoveryDataLayerController getController();

    /**
     * Gets the data panel title.
     *
     * @return the data panel title
     */
    protected abstract String getDataPanelTitle();

    /**
     * Gets the detail component.
     *
     * @return the detail component
     */
    protected JComponent getDetailsComponent()
    {
        return null;
    }

    /**
     * Gets the expand contract node menu item.
     *
     * @return the expand contract node menu item
     */
    protected JMenuItem getExpandContractNodeMenuItem()
    {
        final TreePath mouseOverPath = myTree.getMouseOverPath();
        if (mouseOverPath == null)
        {
            return null;
        }

        TreeTableTreeNode mouseOverNode = (TreeTableTreeNode)mouseOverPath.getLastPathComponent();
        if (mouseOverNode.isLeaf())
        {
            return null;
        }

        boolean isExpanded = myTree.isExpanded(mouseOverPath);
        JMenuItem menuItem = new JMenuItem(isExpanded ? "Collapse All" : "Expand All");
        menuItem.addActionListener(
                e -> JTreeUtilities.expandOrCollapsePath(myTree, mouseOverPath, !myTree.isExpanded(mouseOverPath)));
        return menuItem;
    }

    /**
     * Gets the expand contract tree button.
     *
     * @return the expand contract tree button
     */
    protected IconToggleButton getExpandContractTreeButton()
    {
        return myExpandContractTreeButton;
    }

    /**
     * Gets the expansion helper.
     *
     * @return the expansion helper
     */
    protected abstract DiscoveryTreeExpansionHelper getExpansionHelper();

    /**
     * Gets the lower panel.
     *
     * @return the lower panel
     */
    protected JPanel getLowerPanel()
    {
        return null;
    }

    /**
     * Gets the manage tags menu item.
     *
     * @param dgi the data group which will be acted on by the menu item.
     * @return the manage tags menu item
     */
    protected JMenuItem getManageTagsMenuItem(final DataGroupInfo dgi)
    {
        JMenuItem menuItem = new JMenuItem("Manage Tags");
        menuItem.addActionListener(e -> TagUtility.showTagManagerForGroup(myToolbox, this, dgi, this));
        return menuItem;
    }

    /**
     * Gets the root node.
     *
     * @return the root node
     */
    protected final TreeTableTreeNode getRootNode()
    {
        return myTree == null || myTree.getModel() == null ? null : (TreeTableTreeNode)myTree.getModel().getRoot();
    }

    /**
     * Gets the topic filter text field.
     *
     * @return the topic filter text field
     */
    protected GhostTextField getSearchFilterTextField()
    {
        return mySearchTextField;
    }

    /**
     * Gets the selected nodes.
     *
     * @return the selected nodes
     */
    protected List<TreeTableTreeNode> getSelectedNodes()
    {
        List<TreeTableTreeNode> selectedRows = New.list();
        TreePath[] paths = myTree.getSelectionPaths();
        if (paths != null)
        {
            for (TreePath path : paths)
            {
                Object lpc = path.getLastPathComponent();
                if (lpc instanceof TreeTableTreeNode)
                {
                    selectedRows.add((TreeTableTreeNode)lpc);
                }
            }
        }
        return selectedRows;
    }

    /**
     * Gets the show layer details menu item.
     *
     * @param dgi the data group which will be acted on by the menu item.
     * @return the show layer details menu item
     */
    protected abstract JMenuItem getShowLayerDetailsMenuItem(DataGroupInfo dgi);

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    protected Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Gets the top button panel.
     *
     * @return the top button panel
     */
    protected JComponent getTopButtonPanel()
    {
        return null;
    }

    /**
     * Gets the top right panel.
     *
     * @return the top right panel
     */
    protected JPanel getTopRightPanel()
    {
        return null;
    }

    /**
     * Gets the tree.
     *
     * @return the tree
     */
    protected ListCheckBoxTree getTree()
    {
        return myTree;
    }

    /**
     * Gets the tree button builders.
     *
     * @return the tree button builders
     */
    protected abstract DataTreeButtonProvisioner getTreeButtonProvisioner();

    /**
     * Gets the tree table tree cell renderer.
     *
     * @return the tree table tree cell renderer
     */
    protected abstract TreeTableTreeCellRenderer getTreeTableTreeCellRenderer();

    /**
     * Gets the tree transfer handler.
     *
     * @return the tree transfer handler
     */
    protected abstract TreeTransferHandler getTreeTransferHandler();

    /**
     * Gets the view by combo box.
     *
     * @return the view by combo box
     */
    protected abstract Component getViewByComboBox();

    /**
     * Initializes the tree.
     */
    private void initializeTree()
    {
        assert SwingUtilities.isEventDispatchThread();

        if (getController().getDragAndDropHandler() != null)
        {
            getTreeTransferHandler().addTransferHandler(getController().getDragAndDropHandler());
        }

        myTree = new ListCheckBoxTree(getTreeTransferHandler());
        myTree.setSelectionWarnThreshold(Integer.valueOf(Constants.SELECT_WARN_THRESHOLD));
        myTree.setToggleClickCount(3);
        TreeTableTreeCellRenderer renderer = getTreeTableTreeCellRenderer();
        if (myContextMenusEnabled)
        {
            myTree.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent mouseEvent)
                {
                    if (mouseEvent.getButton() == MouseEvent.BUTTON3)
                    {
                        TreePath path = myTree
                                .getPathForRow(myTree.getClosestRowForLocation(mouseEvent.getX(), mouseEvent.getY()));
                        if (path != null)
                        {
                            showContextMenu((TreeTableTreeNode)path.getLastPathComponent(), mouseEvent.getComponent(),
                                    mouseEvent.getPoint());
                        }
                    }
                }
            });
        }
        myTree.setCheckBoxEnabled(myUseCheckboxes);
        TreeDecorator.decorate(myTree, renderer.isUseOnOffIcons(), myUseCheckboxes);
        myTree.getSelectionModel().setSelectionMode(mySelectionMode);

        myTree.setCellRenderer(renderer);
        myTree.getSelectionModel().addTreeSelectionListener(this);
    }

    /**
     * Gets the expand contract tree button initial selection state.
     *
     * @return the expand contract tree button initial selection state
     */
    protected boolean isExpandContractTreeButtonInitialSelectionState()
    {
        return false;
    }

    /**
     * Rebuild tree complete.
     *
     * @param lastSelectedNodes the previously selected nodes
     */
    protected void rebuildTreeComplete(List<TreeTableTreeNode> lastSelectedNodes)
    {
    }

    /**
     * Rebuilds the tree now on the current thread.
     */
    protected void rebuildTreeNow()
    {
        assert SwingUtilities.isEventDispatchThread();

        List<TreeTableTreeNode> previousNodes = getSelectedNodes();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Rebuilding " + getClass().getSimpleName() + " Tree");
        }

        myTree.removeTreeExpansionListener(myTreeExpansionListener);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode)getController().getGroupTree();
        myRootNode = AbstractDiscoveryDataPanelHelper.copyNode(root, null, myCheckBoxChangeListener);
        AbstractDiscoveryDataPanelHelper.updateCheckBoxTreeLabels(myRootNode, getController().getDataGroupController());

        myTree.setIgnoreSelectionEvents(true);
        ((DefaultTreeModel)myTree.getModel()).setRoot(myRootNode);
        myTree.setIgnoreSelectionEvents(false);
        myTree.updateCheckboxState();

        if (!StringUtils.isBlank(mySearchTextField.getText()))
        {
            JTreeUtilities.expandOrCollapseAll(myTree, true);
        }
        else
        {
            getExpansionHelper().restoreExpansionPaths(getController().getViewByTypeString(), myTree);
        }
        myTree.addTreeExpansionListener(myTreeExpansionListener);

        /* Only set the viewport view if it's not already set to this component.
         * Setting it again causes problems with drag-n-drop events. */
        if (myTree.getParent() != myTreeScrollPane.getViewport())
        {
            myTreeScrollPane.setViewportView(myTree);
        }
        rebuildTreeComplete(previousNodes);
    }

    /**
     * Repaint tree.
     */
    protected void repaintTree()
    {
        if (myTree != null)
        {
            myTree.repaint();
        }
    }

    /**
     * Set the background colors for components that have their own background
     * colors.
     */
    private void setSearchTextFieldBackgroundColor()
    {
        mySearchTextField
                .setBackground(ColorUtilities.opacitizeColor(mySearchTextField.getBackground(), getBackgroundColor().getAlpha()));
    }

    /**
     * Update tree checkbox state.
     */
    protected void updateCheckboxState()
    {
        myUpdateCheckBoxExecutor.execute(() ->
        {
            EventQueueUtilities.runOnEDT(() ->
            {
                TreeTableTreeNode node = getRootNode();
                if (node != null)
                {
                    AbstractDiscoveryDataPanelHelper.updateCheckBoxTreeState(node, myCheckBoxChangeListener);
                    myTree.updateCheckboxState();
                }
                myTree.repaint();
            });
        });
    }

    /**
     * Update tree checkbox state.
     */
    protected void updateLabelState()
    {
        myUpdateCheckBoxExecutor.execute(() ->
        {
            EventQueueUtilities.runOnEDT(() ->
            {
                TreeTableTreeNode node = getRootNode();
                if (node != null)
                {
                    AbstractDiscoveryDataPanelHelper.updateCheckBoxTreeLabels(node, getController().getDataGroupController());
                }
                myTree.repaint();
            });
        });
    }

    /**
     * Adds the left side components.
     *
     * @param leftPanel the left panel
     * @param gbc the gbc
     */
    private void addLeftSideComponents(AbstractHUDPanel leftPanel, GridBagConstraints gbc)
    {
        gbc.gridx = 0;
        gbc.gridy = 0;
        if (leftPanel == null)
        {
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
        }
        myDataPanel.add(createTopPanel(), gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        if (leftPanel != null)
        {
            leftPanel.add(myDataLayerPanel, BorderLayout.CENTER);
        }
        else
        {
            myDataPanel.add(myDataLayerPanel, gbc);
        }

        if (getLowerPanel() != null)
        {
            gbc.gridy++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0;
            myDataPanel.add(getLowerPanel(), gbc);
        }
    }

    /**
     * Adds the right side components.
     *
     * @param rightPanel the right panel
     * @param gbc the gbc
     */
    private void addRightSideComponents(AbstractHUDPanel rightPanel, GridBagConstraints gbc)
    {
        if (getTopRightPanel() != null)
        {
            gbc.gridx = 1;
            gbc.gridy = 0;

            HorizontalSpacerForGridbag hs = new HorizontalSpacerForGridbag(1, 0);
            myDataPanel.add(hs, hs.getGbConst());

            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.weighty = 0;
            myDataPanel.add(getTopRightPanel(), gbc);
        }

        if (getDetailsComponent() != null)
        {
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.weightx = 0;
            gbc.weighty = 1;
            if (rightPanel != null)
            {
                rightPanel.add(getDetailsComponent(), BorderLayout.CENTER);
            }
            else
            {
                myDataPanel.add(getDetailsComponent(), gbc);
            }
        }
    }

    /**
     * create the expand contract tree button.
     */
    private void createExpandContractTreeButton()
    {
        myExpandContractTreeButton = new IconToggleButton();
        myExpandContractTreeButton.setSelected(isExpandContractTreeButtonInitialSelectionState());
        myExpandContractTreeButton
                .setSelectedIcon(IconUtil.getColorizedIcon(IconType.COLLAPSE, IconStyle.NORMAL, Color.LIGHT_GRAY, 16));
        myExpandContractTreeButton.setIcon(IconUtil.getColorizedIcon(IconType.EXPAND, IconStyle.NORMAL, Color.LIGHT_GRAY, 16));
        myExpandContractTreeButton.setContentAreaFilled(false);
        myExpandContractTreeButton.setToolTipText("Expand/Contract Layer List");
        myExpandContractTreeButton.addActionListener(e ->
        {
            Quantify.collectMetric("mist3d.layer-manager.expand-contract");
            JTreeUtilities.expandOrCollapseAll(myTree, myExpandContractTreeButton.isSelected());
        });
    }

    /**
     * create the topic filter text field.
     */
    private void createSearchFilterTextField()
    {
        mySearchTextField = new GhostTextField("Enter keyword to search");
        setSearchTextFieldBackgroundColor();
        mySearchTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                adjustSearchFilter();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                adjustSearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                adjustSearchFilter();
            }
        });
    }

    /**
     * Show the context menu for acting on the given node in the tree.
     *
     * @param node The node for which the menu is desired.
     * @param component The component the mouse is over.
     * @param point The mouse position.
     */
    private void showContextMenu(TreeTableTreeNode node, Component component, Point point)
    {
        // if the node the mouse is over is one of the selected do the multi
        // select otherwise do single for the mouse over node
        List<TreeTableTreeNode> selectedNodes = getSelectedNodes();
        boolean menuShown = false;
        if (selectedNodes.size() > 1 && selectedNodes.contains(node))
        {
            Collection<DataGroupInfo> dgis = New.collection(selectedNodes.size());
            Collection<DataGroupInfo> actualGroups = New.collection(selectedNodes.size());
            Collection<DataTypeInfo> actualTypes = New.collection(selectedNodes.size());
            for (TreeTableTreeNode selNode : selectedNodes)
            {
                DataGroupInfo group = AbstractDiscoveryDataPanelHelper.getGroupFromNode(selNode);
                DataTypeInfo type = AbstractDiscoveryDataPanelHelper.getTypeFromNode(selNode);

                if (group != null)
                {
                    dgis.add(group);
                }

                if (type != null)
                {
                    actualTypes.add(type);
                }
                else if (group != null)
                {
                    actualGroups.add(group);
                }
            }

            if (!dgis.isEmpty())
            {
                MultiDataGroupContextKey key = new MultiDataGroupContextKey(dgis, actualGroups, actualTypes);
                ActionContext<MultiDataGroupContextKey> actionContex = getToolbox().getUIRegistry().getContextActionManager()
                        .getActionContext(getActionContextId(), MultiDataGroupContextKey.class);
                actionContex.doAction(key, component, point.x, point.y, null);
                menuShown = true;
            }
        }
        else
        {
            DataGroupInfo dgi = AbstractDiscoveryDataPanelHelper.getGroupFromNode(node);
            if (dgi == null)
            {
                String category = node.getPayload().getButton().getText();
                CategoryContextKey key = new CategoryContextKey(category);
                ActionContext<CategoryContextKey> actionContext = getToolbox().getUIRegistry().getContextActionManager()
                        .getActionContext(getActionContextId(), CategoryContextKey.class);
                actionContext.doAction(key, component, point.x, point.y, null);
                menuShown = true;
            }
            else
            {
                DataTypeInfo dti = AbstractDiscoveryDataPanelHelper.getTypeFromNode(node);
                DataGroupContextKey key = new DataGroupContextKey(dgi, dti);
                ActionContext<DataGroupContextKey> actionContext = getToolbox().getUIRegistry().getContextActionManager()
                        .getActionContext(getActionContextId(), DataGroupContextKey.class);
                actionContext.doAction(key, component, point.x, point.y, null);
                menuShown = true;
            }
        }

        // Show the context menu for tree only operations
        if (!menuShown)
        {
            JMenuItem expand = getExpandContractNodeMenuItem();
            if (expand != null)
            {
                JPopupMenu menu = new JPopupMenu();
                menu.add(expand);
                menu.show(myTree, point.x, point.y);
            }
        }
    }
}
