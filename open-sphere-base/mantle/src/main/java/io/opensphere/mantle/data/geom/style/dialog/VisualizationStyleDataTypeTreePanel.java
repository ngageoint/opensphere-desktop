package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.common.collapsablepanel.CollapsiblePanel;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.controller.event.AbstractRootDataGroupControllerEvent;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.InterpolatedTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.dialog.DataTypeNodeUserObject.NodeListener;
import io.opensphere.mantle.data.geom.style.dialog.DataTypeNodeUserObject.NodeType;
import io.opensphere.mantle.data.geom.style.dialog.ShowTypeVisualizationStyleEvent.StyleAction;
import io.opensphere.mantle.data.geom.style.impl.VisualizationStyleRegistryChangeAdapter;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class VisualizationStyleDataTypeTreePanel.
 */
@SuppressWarnings("PMD.GodClass")
public class VisualizationStyleDataTypeTreePanel extends JPanel implements NodeListener
{
    /**
     * The {@link Logger} instance used to capture output.
     */
    private static final Logger LOG = Logger.getLogger(VisualizationStyleDataTypeTreePanel.class);

    /**
     * The scheduled executor service for executing rebuilds of the
     * visualization trees.
     */
    private static final ScheduledExecutorService EXECUTOR = ProcrastinatingExecutor.protect(new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("VisualizationStyleTree"), SuppressableRejectedExecutionHandler.getInstance()));

    /** The Constant NODE_HEIGHT_PIXELS. */
    static final int NODE_HEIGHT_PIXELS = 25;

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Active data groups changed listener. */
    private final transient EventListener<ActiveDataGroupsChangedEvent> myActiveDataGroupsChangedListener;

    /** The Collapse icon. */
    private ImageIcon myCollapseIcon;

    /** The Data group controller listener. */
    @SuppressWarnings("PMD.SingularField")
    private final transient EventListener<AbstractRootDataGroupControllerEvent> myDataGroupControllerListener;

    /** The Expand icon. */
    private ImageIcon myExpandIcon;

    /** The type of data this panel will hold. */
    private final VisualizationStyleGroup myGroupType;

    /** The Node key to node obj map. */
    private final transient Map<String, DataTypeNodeUserObject> myNodeKeyToNodeObjMap;

    /** The node obj to leaf node panel map. */
    private final transient Map<DataTypeNodeUserObject, LeafNodePanel> myNodeObjToLeafNodePanelMap;

    /**
     * A procrastinating executor for rebuilds of the visualization trees. Since
     * there is an update for each added layer at startup, this prevents the
     * tree from being potentially rebuilt dozens of times and causing the
     * application from becoming unresponsive.
     */
    private final transient ProcrastinatingExecutor myRebuildExecutor = new ProcrastinatingExecutor(EXECUTOR, 500);

    /** The Registry change listener. */
    private transient VisualizationStyleRegistryChangeAdapter myRegistryStyleChangeListener;

    /** The Scroll pane. */
    @SuppressWarnings("PMD.SingularField")
    private final JScrollPane myScrollPane;

    /** The Style data type tree listener. */
    private final transient StyleDataTypeTreeListener myStyleDataTypeTreeListener;

    /** The Style manager controller. */
    private final transient StyleManagerController myStyleManagerController;

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /** The Feature label group. */
    private final transient SelectableLabelGroup myTreeNodeLabelGroup;

    /** The Feature tree panel. */
    @SuppressWarnings("PMD.SingularField")
    private final JPanel myTreePanel;

    /** The Feature tree panel. */
    @SuppressWarnings("PMD.SingularField")
    private final JPanel myTypePanel;

    /**
     * Instantiates a new visualization style data type tree panel.
     *
     * @param tb the {@link Toolbox}
     * @param styleManagerController the style manager controller
     * @param listener the {@link StyleDataTypeTreeListener}
     * @param groupType the type of data this panel is configured to contain.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public VisualizationStyleDataTypeTreePanel(Toolbox tb, StyleManagerController styleManagerController,
            StyleDataTypeTreeListener listener, VisualizationStyleGroup groupType)
    {
        super();
        myStyleManagerController = styleManagerController;
        myGroupType = groupType;
        myStyleDataTypeTreeListener = listener;
        myToolbox = tb;
        myTypePanel = new JPanel(new BorderLayout());
        myTreeNodeLabelGroup = new SelectableLabelGroup();
        myNodeKeyToNodeObjMap = New.map();
        myNodeObjToLeafNodePanelMap = New.map();

        try
        {
            myExpandIcon = new ImageIcon(ImageIO.read(VisualizationStyleDataTypeTreePanel.class.getResource("/images/down.png")));
        }
        catch (IOException e)
        {
            myExpandIcon = null;
        }
        try
        {
            myCollapseIcon = new ImageIcon(
                    ImageIO.read(VisualizationStyleDataTypeTreePanel.class.getResource("/images/right.png")));
        }
        catch (IOException e)
        {
            myCollapseIcon = null;
        }

        myTreePanel = new JPanel();
        myTreePanel.setLayout(new BoxLayout(myTreePanel, BoxLayout.Y_AXIS));
        myScrollPane = new JScrollPane(myTreePanel);
        myTypePanel.add(myScrollPane, BorderLayout.CENTER);

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Data Types");
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize() + 4));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);
        add(myTypePanel, BorderLayout.CENTER);

        myDataGroupControllerListener = event -> handleDataGroupInfoChangeEvent(event);
        tb.getEventManager().subscribe(AbstractRootDataGroupControllerEvent.class, myDataGroupControllerListener);

        myActiveDataGroupsChangedListener = event -> handleActiveDataGroupsChangedEvent(event);
        tb.getEventManager().subscribe(ActiveDataGroupsChangedEvent.class, myActiveDataGroupsChangedListener);

        MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleRegistry()
                .addVisualizationStyleRegistryChangeListener(getStyleRegistryChangeListener());

        rebuildFromModel();
    }

    /**
     * {@inheritDoc}
     *
     * @see DataTypeNodeUserObject.NodeListener#nodeCheckStateChanged(DataTypeNodeUserObject,
     *      boolean)
     */
    @Override
    public void nodeCheckStateChanged(DataTypeNodeUserObject node, boolean checked)
    {
        fireDataTypeCheckStateChanged(node);
    }

    /**
     * {@inheritDoc}
     *
     * @see DataTypeNodeUserObject.NodeListener#nodeSelectStateChanged(DataTypeNodeUserObject,
     *      boolean)
     */
    @Override
    public void nodeSelectStateChanged(DataTypeNodeUserObject node, boolean selected)
    {
        DataTypeNodeUserObject selNode = getSelectedNode();
        if (selNode == null)
        {
            fireNoDataTypeSelected();
        }
        else
        {
            fireDataTypeSelected(selNode);
        }
    }

    /**
     * Switch to data type.
     *
     * @param event the event
     * @return true, if successful
     */
    public boolean switchToDataType(ShowTypeVisualizationStyleEvent event)
    {
        synchronized (myNodeKeyToNodeObjMap)
        {
            for (Map.Entry<String, DataTypeNodeUserObject> entry : myNodeKeyToNodeObjMap.entrySet())
            {
                if (entry.getValue().getDataTypeInfo() != null
                        && event.getType().getTypeKey().equals(entry.getValue().getDataTypeInfo().getTypeKey()))
                {
                    LeafNodePanel lnp = myNodeObjToLeafNodePanelMap.get(entry.getValue());
                    if (lnp != null)
                    {
                        if (event.getStyleAction() == StyleAction.SHOW_ONLY
                                || event.getStyleAction() == StyleAction.ACTIVATE_IF_INACTIVE)
                        {
                            if (!lnp.getSelectableLabel().isSelected())
                            {
                                lnp.getSelectableLabel().setSelected(true, true);
                            }
                            // For case StyleAction.DEACTIVATE_IF_ACTIVE
                            if (event.getStyleAction() == StyleAction.ACTIVATE_IF_INACTIVE && !lnp.getCheckBox().isSelected())
                            {
                                lnp.getCheckBox().doClick();
                            }
                        }
                        else
                        {
                            // For case StyleAction.DEACTIVATE_IF_ACTIVE
                            if (lnp.getCheckBox().isSelected())
                            {
                                lnp.getCheckBox().doClick();
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Fire node select state changed.
     *
     * @param node the node
     */
    protected void fireDataTypeCheckStateChanged(final DataTypeNodeUserObject node)
    {
        if (myStyleManagerController != null)
        {
            EventQueueUtilities.runOnEDT(() -> myStyleManagerController.setUseCustomStyleForDataType(node.getNodeKey(),
                    node.isChecked(), VisualizationStyleDataTypeTreePanel.this));
        }
    }

    /**
     * Fire node select state changed.
     *
     * @param node the node
     */
    protected void fireDataTypeSelected(final DataTypeNodeUserObject node)
    {
        if (myStyleDataTypeTreeListener != null)
        {
            EventQueueUtilities.runOnEDT(() -> myStyleDataTypeTreeListener.dataTypeSelected(node));
        }
    }

    /**
     * Fire node select state changed.
     *
     */
    protected void fireForceRebuild()
    {
        if (myStyleDataTypeTreeListener != null)
        {
            EventQueueUtilities.runOnEDT(() -> myStyleDataTypeTreeListener.forceRebuild());
        }
    }

    /**
     * Fire no data type selected.
     */
    protected void fireNoDataTypeSelected()
    {
        if (myStyleDataTypeTreeListener != null)
        {
            EventQueueUtilities.runOnEDT(() -> myStyleDataTypeTreeListener.noDataTypeSelected());
        }
    }

    /**
     * Builds the tree model.
     *
     * @param dgiList the list of {@link DataGroupInfo}
     * @param lbGrp the {@link SelectableLabelGroup} for the tree
     * @param nodeKeyToNodeMap the node key to node map
     * @param lastSelectedNodeKey the last selected node key
     * @param styleGroup the type of data to store in the tree.
     * @return the panel with the mock Component tree. {@link SelectableLabel}
     */
    private JPanel buildTree(List<DataGroupInfo> dgiList, SelectableLabelGroup lbGrp,
            Map<String, DataTypeNodeUserObject> nodeKeyToNodeMap, String lastSelectedNodeKey, VisualizationStyleGroup styleGroup)
    {
        JPanel rootPanel = new JPanel(new BorderLayout());
        BoxLayout bl = new BoxLayout(rootPanel, BoxLayout.Y_AXIS);
        rootPanel.setLayout(bl);

        // Build the default Node.
        rootPanel.add(Box.createVerticalStrut(3));

        myNodeObjToLeafNodePanelMap.clear();

        NodeType rootNodeType;
        switch (styleGroup)
        {
            case FEATURES:
                rootNodeType = NodeType.DEFAULT_ROOT_FEATURE;
                break;
            case TILES:
                rootNodeType = NodeType.DEFAULT_ROOT_TILE;
                break;
            case HEATMAPS:
                rootNodeType = NodeType.DEFAULT_ROOT_HEATMAP;
                break;
            default:
                // fail fast:
                throw new UnsupportedOperationException("Unrecognized visualization style group: " + styleGroup.name());
        }

        DataTypeNodeUserObject aNode = new DataTypeNodeUserObject("Default Styles", rootNodeType, this);
        if (aNode.getNodeKey().equals(lastSelectedNodeKey))
        {
            aNode.setSelectedNoEvent(true);
        }
        nodeKeyToNodeMap.put(aNode.getNodeKey(), aNode);
        LeafNodePanel lnp = new LeafNodePanel(lbGrp, aNode, false);
        rootPanel.add(lnp);
        myNodeObjToLeafNodePanelMap.put(aNode, lnp);
        rootPanel.add(Box.createVerticalStrut(3));

        // Now go through each of the DataGroupInfo and build a CollapsablePanel
        // to represent its folder. Fill each panel with our LeafNodePanels for
        // each leaf node.
        Map<DataGroupInfo, List<DataTypeNodeUserObject>> topParentToLeafNodeMap = determineGroupsAndNodes(dgiList,
                lastSelectedNodeKey, styleGroup);

        List<DataGroupInfo> dgiKeyList = New.list(topParentToLeafNodeMap.keySet());
        Collections.sort(dgiKeyList, (o1, o2) -> o1.getDisplayName().compareTo(o2.getDisplayName()));
        Map<DataGroupInfo, CollapsiblePanel> topParentToNodeMap = New.map();
        for (DataGroupInfo topNode : dgiKeyList)
        {
            List<DataTypeNodeUserObject> nodeList = topParentToLeafNodeMap.get(topNode);
            if (!nodeList.isEmpty())
            {
                Collections.sort(nodeList, (o1, o2) -> o1.getDisplayName().compareTo(o2.getDisplayName()));
                CollapsiblePanel topParentNode = topParentToNodeMap.get(topNode);
                if (topParentNode == null)
                {
                    JPanel subPanel = new JPanel();
                    subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
                    topParentNode = new CollapsiblePanel(topNode.getDisplayName(), subPanel, 10);
                    topParentNode.setExpandedIcon(myExpandIcon);
                    topParentNode.setCollapsedIcon(myCollapseIcon);
                    rootPanel.add(topParentNode);
                    topParentToNodeMap.put(topNode, topParentNode);
                }

                // Build up the sub-labels.
                int numAdded = 0;
                for (DataTypeNodeUserObject node : nodeList)
                {
                    nodeKeyToNodeMap.put(node.getNodeKey(), node);
                    numAdded += createAndAddLeafPanel(styleGroup, lbGrp, node, topParentNode);
                }
                // Size the collapsible panels so it looks right in its parent
                // panel.
                topParentNode.setMaximumSize(new Dimension(1000, numAdded * NODE_HEIGHT_PIXELS + NODE_HEIGHT_PIXELS));
            }
        }

        rootPanel.add(new JPanel());

        return rootPanel;
    }

    /**
     * Process data type info to create a leaf node for use in the panel.
     *
     * @param groupType the group type for which the leaf panel will be created.
     * @param labelGroup the group to which the label will be assigned.
     * @param node the node to embed into the panel.
     * @param topParentNode the parent node to which the leaf will be added.
     * @return the number of items added to the parentNode
     */
    private int createAndAddLeafPanel(VisualizationStyleGroup groupType, SelectableLabelGroup labelGroup,
            DataTypeNodeUserObject node, CollapsiblePanel topParentNode)
    {
        LeafNodePanel leafNodePanel = new LeafNodePanel(labelGroup, node, true);
        myNodeObjToLeafNodePanelMap.put(node, leafNodePanel);
        ((JPanel)topParentNode.getComponent()).add(leafNodePanel);
        return 1;
    }

    /**
     * Organizes the supplied {@link List} of data groups, only selecting items
     * that correspond with the supplied {@link VisualizationStyleGroup} for
     * inclusion into the generated dictionary.
     *
     * @param dgiList the list of data groups to organize.
     * @param lastSelectedNodeKey the last selected node key
     * @param groupType the type of data to be organized into the dictionary.
     * @return the map
     */
    private Map<DataGroupInfo, List<DataTypeNodeUserObject>> determineGroupsAndNodes(List<DataGroupInfo> dgiList,
            String lastSelectedNodeKey, VisualizationStyleGroup groupType)
    {
        DataTypeNodeUserObject aNode;
        Map<DataGroupInfo, List<DataTypeNodeUserObject>> topParentToLeafNodeMap = New.map();
        if (dgiList != null && !dgiList.isEmpty())
        {
            for (DataGroupInfo dgi : dgiList)
            {
                DataGroupInfo topParent = dgi.getTopParent();
                List<DataTypeNodeUserObject> nodeList = topParentToLeafNodeMap.get(topParent);
                if (nodeList == null)
                {
                    nodeList = New.list();
                    topParentToLeafNodeMap.put(topParent, nodeList);
                }

                for (DataTypeInfo dti : dgi.getMembers(false))
                {
                    String name = dti.getDisplayName();
                    if (dti.getMapVisualizationInfo() != null)
                    {
                        MapVisualizationType type = dti.getMapVisualizationInfo().getVisualizationType();
                        boolean add = false;
                        NodeType nodeType = null;
                        switch (groupType)
                        {
                            case FEATURES:
                                if (type.isMapDataElementType())
                                {
                                    add = true;
                                    nodeType = NodeType.FEATURE_TYPE_LEAF;
                                }
                                break;
                            case TILES:
                                if (type.isImageTileType() || type.isImageType())
                                {
                                    add = true;
                                    nodeType = NodeType.TILE_TYPE_LEAF;
                                }
                                break;
                            case HEATMAPS:
                                if (type.isHeatmapType())
                                {
                                    add = true;
                                    nodeType = NodeType.HEATMAP_TYPE_LEAF;
                                }
                                break;
                            default:
                                // note the failure:
                                LOG.info("Unable to set selected style group type " + groupType);
                                add = false;

                        }
                        if (add)
                        {
                            aNode = new DataTypeNodeUserObject(name, nodeType, dgi, dti, this);
                            if (Objects.equals(aNode.getNodeKey(), lastSelectedNodeKey))
                            {
                                aNode.setSelectedNoEvent(true);
                            }
                            if (myStyleManagerController.isTypeUsingCustom(aNode.getNodeKey()))
                            {
                                aNode.setChecked(true, false);
                            }
                            nodeList.add(aNode);
                        }
                    }
                }
            }
        }
        return topParentToLeafNodeMap;
    }

    /**
     * Gets the selected node.
     *
     * @return the selected {@link DataTypeNodeUserObject}
     */
    private DataTypeNodeUserObject getSelectedNode()
    {
        synchronized (myNodeKeyToNodeObjMap)
        {
            return myNodeKeyToNodeObjMap.entrySet().stream().filter(e -> e.getValue().isSelected()).findFirst()
                    .map(e -> e.getValue()).orElse(null);
        }
    }

    /**
     * Gets the selected node key.
     *
     * @return the selected node key
     */
    private String getSelectedNodeKey()
    {
        DataTypeNodeUserObject node = getSelectedNode();
        return node == null ? null : node.getNodeKey();
    }

    /**
     * Gets the style registry change listener.
     *
     * @return the style registry change listener
     */
    private VisualizationStyleRegistryChangeAdapter getStyleRegistryChangeListener()
    {
        if (myRegistryStyleChangeListener == null)
        {
            myRegistryStyleChangeListener = new VisualizationStyleRegistryChangeAdapter()
            {
                @Override
                public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
                {
                    handleVisualizationStyleDatatypeChangeEvent(evt);
                }
            };
        }
        return myRegistryStyleChangeListener;
    }

    /**
     * Handle data group info change event.
     *
     * @param event the event
     */
    private void handleActiveDataGroupsChangedEvent(ActiveDataGroupsChangedEvent event)
    {
        rebuildFromModel();
    }

    /**
     * Handle data group info change event.
     *
     * @param event the event
     */
    private void handleDataGroupInfoChangeEvent(AbstractRootDataGroupControllerEvent event)
    {
        rebuildFromModel();
    }

    /**
     * Handle visualization style datatype change event.
     *
     * @param evt the evt
     */
    private void handleVisualizationStyleDatatypeChangeEvent(final VisualizationStyleDatatypeChangeEvent evt)
    {
        if (!Utilities.sameInstance(evt.getSource(), this))
        {
            boolean isFeatureType = FeatureVisualizationStyle.class.isAssignableFrom(evt.getNewStyle().getClass());
            boolean isHeatmapType = InterpolatedTileVisualizationStyle.class.isAssignableFrom(evt.getNewStyle().getClass());
            if (isFeatureType && myGroupType == VisualizationStyleGroup.FEATURES
                    || isHeatmapType && myGroupType == VisualizationStyleGroup.HEATMAPS)
            {
                rebuildFromModel(true);
            }
        }
    }

    /**
     * Rebuild from model.
     */
    private void rebuildFromModel()
    {
        rebuildFromModel(false);
    }

    /**
     * Rebuild from model.
     *
     * @param forceRebuildOfEditor a flag used to force reselect last selected
     *            item after a rebuild is complete.
     */
    private void rebuildFromModel(final boolean forceRebuildOfEditor)
    {
        EventQueueUtilities.runOnEDT(() -> rebuildFromModelImpl(forceRebuildOfEditor));
    }

    /**
     * Rebuilds the model.
     *
     * @param forceRebuildOfEditor a flag used to force reselect last selected
     *            item after a rebuild is complete.
     */
    private void rebuildFromModelImpl(final boolean forceRebuildOfEditor)
    {
        synchronized (myNodeKeyToNodeObjMap)
        {
            String oldSelectedNodeKey = getSelectedNodeKey();
            myNodeKeyToNodeObjMap.clear();
            myTreeNodeLabelGroup.removeAllLabels();
            rebuildFromModelInternal(myTreePanel, myTreeNodeLabelGroup, myNodeKeyToNodeObjMap, oldSelectedNodeKey, myGroupType);
            myScrollPane.revalidate();

            if (forceRebuildOfEditor)
            {
                fireForceRebuild();
            }
        }
    }

    /**
     * Rebuild from model internal (assumes working in AWT event thread).
     * Actually does all the work to rebuild the selection trees.
     *
     * @param treePanel the tree panel to be rebuild.
     * @param lbGrp the {@link SelectableLabelGroup} for the labels in the tree.
     * @param nodeKeyToNodeMap the node key to node map
     * @param lastSelectedNodeKey the last selected node key
     * @param styleGroup the type of data to be contained by the tree.
     */
    private void rebuildFromModelInternal(final JPanel treePanel, final SelectableLabelGroup lbGrp,
            final Map<String, DataTypeNodeUserObject> nodeKeyToNodeMap, final String lastSelectedNodeKey,
            final VisualizationStyleGroup styleGroup)
    {
        myRebuildExecutor.execute(() -> EventQueueUtilities
                .runOnEDT(() -> rebuildTreePanel(treePanel, lbGrp, nodeKeyToNodeMap, lastSelectedNodeKey, styleGroup)));
    }

    /**
     * Rebuilds the tree on the panel.
     *
     * @param pTreePanel the panel to rebuild.
     * @param pLabelGroup the {@link SelectableLabelGroup} for the tree used to
     *            organized labels.
     * @param pNodeKeyToNodeMap a dictionary of node objects, using the node's
     *            key as the hash key for fast lookups.
     * @param pLastSelectedNodeKey the key that was selected on before the
     *            rebuild was initiated.
     * @param pStyleGroup the type of data to be contained by the tree.
     */
    private void rebuildTreePanel(final JPanel pTreePanel, final SelectableLabelGroup pLabelGroup,
            final Map<String, DataTypeNodeUserObject> pNodeKeyToNodeMap, final String pLastSelectedNodeKey,
            final VisualizationStyleGroup pStyleGroup)
    {
        pTreePanel.removeAll();

        List<DataGroupInfo> dgiList = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController()
                .createGroupList(null, new VisualizationStyleDataGroupTreeFilter(myToolbox));
        pTreePanel.add(buildTree(dgiList, pLabelGroup, pNodeKeyToNodeMap, pLastSelectedNodeKey, pStyleGroup));
        pTreePanel.revalidate();
        pTreePanel.repaint();
    }
}
