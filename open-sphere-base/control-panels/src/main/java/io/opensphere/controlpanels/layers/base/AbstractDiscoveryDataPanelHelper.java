package io.opensphere.controlpanels.layers.base;

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.core.util.swing.tree.ButtonModelPayload;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * The Class AbstractDiscoveryDataPanelHelper.
 */
public final class AbstractDiscoveryDataPanelHelper
{
    /**
     * Make a copy of the node and any children using a custom payload to use
     * along with the check box tree.
     *
     * @param node The node to copy.
     * @param parent the parent
     * @param changeListener the change listener
     * @return The copied node.
     */
    public static TreeTableTreeNode copyNode(DefaultMutableTreeNode node, TreeTableTreeNode parent, ActionListener changeListener)
    {
        GroupByNodeUserObject payloadData = (GroupByNodeUserObject)node.getUserObject();
        String aLabel = payloadData == null ? "Unknown" : payloadData.toString();
        String toolTip = "";
        if (payloadData != null)
        {
            toolTip = payloadData.getToolTip();
        }
        ButtonModelPayload bmp = new ButtonModelPayload(payloadData, aLabel, toolTip,
                payloadData != null && !payloadData.isCategoryOnly());
        if (payloadData != null && payloadData.isSelectable())
        {
            bmp.getButton().setSelected(payloadData.isSelected());
        }
        bmp.getButton().addActionListener(changeListener);

        TreeTableTreeNode copiedNode = new TreeTableTreeNode(parent, bmp);

        for (DefaultMutableTreeNode child : JTreeUtilities.getChildren(node))
        {
            TreeTableTreeNode childCopy = copyNode(child, copiedNode, changeListener);
            copiedNode.add(childCopy);
        }
        return copiedNode;
    }

    /**
     * Retrieves the data group from the provided node if it has one, or returns
     * null if not.
     *
     * @param node the node
     * @return the group from node
     */
    public static DataGroupInfo getGroupFromNode(TreeTableTreeNode node)
    {
        DataGroupInfo result = null;
        if (node.getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject uo = (GroupByNodeUserObject)node.getPayloadData();
            result = uo.getDataGroupInfo();
        }
        return result;
    }

    /**
     * Gets the groups from tree nodes.
     *
     * @param nodes the nodes
     * @return the groups from tree nodes
     */
    public static Set<DataGroupInfo> getGroupsFromTreeNodes(Collection<TreeTableTreeNode> nodes)
    {
        Set<DataGroupInfo> result = New.set();
        if (CollectionUtilities.hasContent(nodes))
        {
            for (TreeTableTreeNode node : nodes)
            {
                DataGroupInfo dgi = getGroupFromNode(node);
                if (dgi != null)
                {
                    result.add(dgi);
                }
            }
        }
        return result;
    }

    /**
     * Retrieves the data type from the provided node if it has one, or returns
     * null if not.
     *
     * @param node the node
     * @return the group from node
     */
    public static DataTypeInfo getTypeFromNode(TreeTableTreeNode node)
    {
        DataTypeInfo result = null;
        if (node.getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject uo = (GroupByNodeUserObject)node.getPayloadData();
            result = uo.getDataTypeInfo();
        }
        return result;
    }

    /**
     * Update check box tree state.
     *
     * @param node the node
     * @param controller the controller
     */
    public static void updateCheckBoxTreeLabels(TreeTableTreeNode node, DataGroupController controller)
    {
        ButtonModelPayload payload = node.getPayload();
        if (payload != null)
        {
            GroupByNodeUserObject payloadData = (GroupByNodeUserObject)payload.getPayloadData();
            if (payloadData != null)
            {
                if (payloadData.isSelectable())
                {
                    payload.setLabel(payloadData.getLabel());
                }
                else if (payloadData.isCategoryNode())
                {
                    payloadData.setVisibleCategoryCount(node.getChildCount());
                    payloadData.generateLabel();
                    payload.setLabel(payloadData.getLabel());
                }
            }
        }

        if (node.getChildCount() > 0)
        {
            for (TreeTableTreeNode child : node.getChildren())
            {
                updateCheckBoxTreeLabels(child, controller);
            }
        }
    }

    /**
     * Update check box tree state.
     *
     * @param node the node
     * @param changeListener the change listener
     */
    public static void updateCheckBoxTreeState(TreeTableTreeNode node, ActionListener changeListener)
    {
        ButtonModelPayload payload = node.getPayload();
        if (payload != null)
        {
            GroupByNodeUserObject payloadData = (GroupByNodeUserObject)payload.getPayloadData();
            if (payloadData != null && payloadData.isSelectable())
            {
                payload.getButton().removeActionListener(changeListener);
                payload.getButton().setSelected(payloadData.isSelected());
                payload.getButton().addActionListener(changeListener);
            }
        }

        if (node.getChildCount() > 0)
        {
            for (TreeTableTreeNode child : node.getChildren())
            {
                updateCheckBoxTreeState(child, changeListener);
            }
        }
    }

    /**
     * Prevent instantiation of this utility class.
     */
    private AbstractDiscoveryDataPanelHelper()
    {
    }
}
