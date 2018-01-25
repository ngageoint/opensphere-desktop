package io.opensphere.controlpanels.layers.availabledata;

import io.opensphere.core.util.swing.QuadStateIconButton;
import io.opensphere.core.util.swing.tree.ButtonStateUpdater;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.RefreshableDataGroupInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * Shows or hides the refresh button depending on if the node selected is
 * refreshable or not.
 */
public class RefreshTreeCellRenderer implements ButtonStateUpdater
{
    @Override
    public void update(QuadStateIconButton button, TreeTableTreeNode node)
    {
        boolean visible = false;
        if (node.getPayload() != null && node.getPayload().getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject uo = (GroupByNodeUserObject)node.getPayload().getPayloadData();
            DataGroupInfo dgi = uo.getDataGroupInfo();
            visible = dgi instanceof RefreshableDataGroupInfo;
        }
        button.setHidden(!visible);
    }
}
