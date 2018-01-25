package io.opensphere.controlpanels.layers.base;

import io.opensphere.core.util.swing.QuadStateIconButton;
import io.opensphere.core.util.swing.tree.ButtonStateUpdater;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * Renderer which returns the value of the table cell as the renderable
 * component.
 */
public class SettingsButtonTreeTableTableCellRenderer implements ButtonStateUpdater
{
    @Override
    public void update(QuadStateIconButton button, TreeTableTreeNode node)
    {
        boolean visible = false;
        if (node.getPayload().getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject uo = (GroupByNodeUserObject)node.getPayload().getPayloadData();
            DataGroupInfo dgi = uo.getDataGroupInfo();
            visible = dgi != null && dgi.hasMembers(false) && dgi.hasDetails();
        }
        button.setHidden(!visible);
    }
}
