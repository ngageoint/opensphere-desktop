package io.opensphere.controlpanels.layers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import io.opensphere.controlpanels.layers.activedata.controller.AvailableDataDataLayerController;
import io.opensphere.controlpanels.layers.activedata.tree.TreeTransferHandler;
import io.opensphere.controlpanels.layers.availabledata.AvailableDataTreeTableTreeCellRenderer;
import io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataPanel;
import io.opensphere.controlpanels.layers.base.DataTreeButtonProvisioner;
import io.opensphere.controlpanels.layers.base.DiscoveryTreeExpansionHelper;
import io.opensphere.controlpanels.layers.base.LayerUtilities;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.core.util.swing.tree.ListCheckBoxTree;
import io.opensphere.core.util.swing.tree.TreeTableTreeCellRenderer;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * The Class SearchAvailableLayersPanel. This available layers tree has no check
 * boxes. It acts as a search only tree with single selection.
 */
@SuppressWarnings("PMD.GodClass")
public class SearchAvailableLayersPanel extends AbstractDiscoveryDataPanel implements ActionListener
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The data layer controller. */
    private final transient AvailableDataDataLayerController myDataLayerController;

    /**
     * Predicate used to determine which data type to return within a selected
     * data group.
     */
    private final transient Predicate<? super DataTypeInfo> myDataTypePredicate;

    /** The Expansion helper. */
    private final transient DiscoveryTreeExpansionHelper myExpansionHelper;

    /** The Selected data group info. */
    private transient volatile Collection<? extends DataGroupInfo> mySelectedDataGroupInfos = Collections.emptySet();

    /** The Selected data type info. */
    private transient volatile Collection<? extends DataTypeInfo> mySelectedDataTypeInfos = Collections.emptySet();

    /** The tree table tree cell renderer. */
    private final transient AvailableDataTreeTableTreeCellRenderer myTreeTableTreeCellRenderer;

    /**
     * Instantiates a new search available layers panel.
     *
     * @param tb the toolbox
     * @param controller the controller
     * @param selectionMode The selection mode for the tree.
     * @param dataTypePredicate Predicate used to determine which data type to
     *            return within a selected data group.
     */
    public SearchAvailableLayersPanel(Toolbox tb, AvailableDataDataLayerController controller, int selectionMode,
            Predicate<? super DataTypeInfo> dataTypePredicate)
    {
        super(tb, false, false, selectionMode);
        myDataTypePredicate = dataTypePredicate;

        if (controller == null)
        {
            myDataLayerController = new AvailableDataDataLayerController(tb, this);
        }
        else
        {
            myDataLayerController = controller;
        }

        myDataLayerController.addListener(this);
        myTreeTableTreeCellRenderer = new AvailableDataTreeTableTreeCellRenderer(getToolbox());
        myExpansionHelper = new DiscoveryTreeExpansionHelper(getToolbox().getPreferencesRegistry().getPreferences(getClass()),
                DiscoveryTreeExpansionHelper.Mode.STORE_EXPANSIONS);
        myExpansionHelper.loadFromPreferences();
        if (getLowerPanel() != null)
        {
            add(getLowerPanel(), BorderLayout.SOUTH);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
    }

    @Override
    public void dataGroupsChanged()
    {
        rebuildTree();
    }

    @Override
    public void dataGroupVisibilityChanged(DataTypeVisibilityChangeEvent event)
    {
    }

    /**
     * Gets the selected data group. This method is thread-safe.
     *
     * @return the selected data group
     */
    public Collection<? extends DataGroupInfo> getSelectedDataGroups()
    {
        return mySelectedDataGroupInfos;
    }

    /**
     * Gets the selected data type. This method is thread-safe.
     *
     * @return the selected data type
     */
    public Collection<? extends DataTypeInfo> getSelectedDataTypes()
    {
        return mySelectedDataTypeInfos;
    }

    @Override
    public void initGuiElements()
    {
        super.initGuiElements();
        getTree().setPaintHoverRow(false);
    }

    @Override
    public void refreshTreeLabelRequest()
    {
    }

    /**
     * Selects all leaf nodes.
     */
    public void selectAll()
    {
        setSelectionPaths(new Predicate<TreeNode>()
        {
            @Override
            public boolean test(TreeNode node)
            {
                return node.isLeaf();
            }
        });
    }

    /**
     * Sets the selected tree paths to the groups that pass the filter.
     *
     * @param dataGroupPredicate The filter.
     */
    public void setSelectedDataGroups(Predicate<DataGroupInfo> dataGroupPredicate)
    {
        Predicate<TreeNode> nodeHasKey = new Predicate<TreeNode>()
        {
            @Override
            public boolean test(TreeNode node)
            {
                if (node.isLeaf() && node instanceof TreeTableTreeNode)
                {
                    DataGroupInfo dataGroup = LayerUtilities.getDataGroup((TreeTableTreeNode)node);
                    return dataGroupPredicate.test(dataGroup);
                }
                else
                {
                    return false;
                }
            }
        };

        setSelectionPaths(nodeHasKey);
    }

    /**
     * Sets the selected tree paths to the groups that have a data type with one
     * of the given type keys.
     *
     * @param typeKeys the type keys
     */
    public void setSelectedTypeKeys(final Collection<String> typeKeys)
    {
        if (!typeKeys.isEmpty())
        {
            setSelectedDataGroups(dataGroup -> !dataGroup
                    .findMembers((Predicate<DataTypeInfo>)dataType -> typeKeys.contains(dataType.getTypeKey()), false, true)
                    .isEmpty());
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        Collection<DataGroupInfo> selectedDataGroupInfos = New.collection();
        Collection<DataTypeInfo> selectedDataTypeInfos = New.collection();

        JTree layerTree = getTree();
        TreePath[] treePaths = layerTree.getSelectionPaths();
        if (treePaths != null)
        {
            for (TreePath treePath : treePaths)
            {
                TreeTableTreeNode treeNode = (TreeTableTreeNode)treePath.getLastPathComponent();
                GroupByNodeUserObject groupByNode = (GroupByNodeUserObject)treeNode.getPayload().getPayloadData();
                if (groupByNode != null && groupByNode.getDataGroupInfo() != null)
                {
                    DataGroupInfo dgi = groupByNode.getDataGroupInfo();
                    selectedDataGroupInfos.add(dgi);
                    Collection<DataTypeInfo> dtis = dgi.getMembers(false);
                    if (!dtis.isEmpty())
                    {
                        for (DataTypeInfo dti : dtis)
                        {
                            if (myDataTypePredicate == null || myDataTypePredicate.test(dti))
                            {
                                selectedDataTypeInfos.add(dti);
                            }
                        }
                    }
                }
            }
        }
        mySelectedDataGroupInfos = New.unmodifiableCollection(selectedDataGroupInfos);
        mySelectedDataTypeInfos = New.unmodifiableCollection(selectedDataTypeInfos);
    }

    @Override
    protected void checkBoxActionPerformed(ActionEvent e)
    {
    }

    @Override
    protected String getActionContextId()
    {
        return null;
    }

    @Override
    protected AvailableDataDataLayerController getController()
    {
        return myDataLayerController;
    }

    @Override
    protected String getDataPanelTitle()
    {
        return null;
    }

    @Override
    protected DiscoveryTreeExpansionHelper getExpansionHelper()
    {
        return myExpansionHelper;
    }

    @Override
    protected JMenuItem getShowLayerDetailsMenuItem(DataGroupInfo dgi)
    {
        return null;
    }

    @Override
    protected DataTreeButtonProvisioner getTreeButtonProvisioner()
    {
        return null;
    }

    @Override
    protected TreeTableTreeCellRenderer getTreeTableTreeCellRenderer()
    {
        return myTreeTableTreeCellRenderer;
    }

    @Override
    protected TreeTransferHandler getTreeTransferHandler()
    {
        return null;
    }

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
        viewBy.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getExpandContractTreeButton().setSelected(false);
                getController().setViewByTypeFromString(viewBy.getSelectedItem().toString());
            }
        });
        return viewBy;
    }

    @Override
    protected void rebuildTreeComplete(List<TreeTableTreeNode> lastSelectedNodes)
    {
        if (CollectionUtilities.hasContent(lastSelectedNodes))
        {
            for (TreeTableTreeNode node : lastSelectedNodes)
            {
                GroupByNodeUserObject uo = null;
                if (node.getPayloadData() instanceof GroupByNodeUserObject)
                {
                    uo = (GroupByNodeUserObject)node.getPayloadData();
                }
                final GroupByNodeUserObject fUO = uo;
                focusOnNode(new Predicate<GroupByNodeUserObject>()
                {
                    @Override
                    public boolean test(GroupByNodeUserObject value)
                    {
                        String dgiId = null;
                        String dtiId = null;
                        if (fUO != null)
                        {
                            dgiId = fUO.getDataGroupInfo() == null ? null : fUO.getDataGroupInfo().getId();
                            dtiId = fUO.getDataTypeInfo() == null ? null : fUO.getDataTypeInfo().getTypeKey();
                        }

                        String vDgiId = value.getDataGroupInfo() == null ? null : value.getDataGroupInfo().getId();
                        String vDtiId = value.getDataTypeInfo() == null ? null : value.getDataTypeInfo().getTypeKey();

                        return EqualsHelper.equals(dgiId, vDgiId, dtiId, vDtiId);
                    }
                }, null);
            }
        }
    }

    /**
     * Sets the selection paths to the nodes that pass the filter.
     *
     * @param filter the filter
     */
    private void setSelectionPaths(Predicate<? super TreeNode> filter)
    {
        List<TreePath> selectedPaths = ListCheckBoxTree
                .mapTreePathsFromNodes(JTreeUtilities.flatten((TreeNode)getTree().getModel().getRoot(), filter));
        getTree().setSelectionPaths(selectedPaths.toArray(new TreePath[selectedPaths.size()]));
    }
}
