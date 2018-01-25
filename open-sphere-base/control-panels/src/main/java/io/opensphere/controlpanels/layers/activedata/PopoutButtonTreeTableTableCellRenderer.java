package io.opensphere.controlpanels.layers.activedata;

import io.opensphere.core.util.swing.QuadStateIconButton;
import io.opensphere.core.util.swing.tree.ButtonStateUpdater;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * Renderer which returns the value of the table cell as the renderable
 * component.
 */
public class PopoutButtonTreeTableTableCellRenderer implements ButtonStateUpdater
{
    @Override
    public void update(QuadStateIconButton button, TreeTableTreeNode node)
    {
        boolean visible = false;
        if (node.getPayload().getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject uo = (GroupByNodeUserObject)node.getPayload().getPayloadData();
            DataGroupInfo dgi = uo.getDataGroupInfo();
            DataTypeInfo dti = uo.getDataTypeInfo();
            visible = uo.isCategoryNode() || dgi != null && dti == null;
        }
        button.setHidden(!visible);
    }
}
