package io.opensphere.mantle.data.filter;

import java.awt.Color;

import io.opensphere.core.Toolbox;
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
public class FilterButtonTreeTableTableCellRenderer implements ButtonStateUpdater
{
    /** The data layer filter. */
    private final DataLayerFilter myFilter;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public FilterButtonTreeTableTableCellRenderer(Toolbox toolbox)
    {
        myFilter = new DataLayerFilter();
        myToolbox = toolbox;
    }

    @Override
    public void update(QuadStateIconButton button, TreeTableTreeNode node)
    {
        boolean visible = false;
        boolean activeFilter = false;
        if (node.getPayload().getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject userObject = (GroupByNodeUserObject)node.getPayload().getPayloadData();
            DataGroupInfo dataGroup = userObject.getDataGroupInfo();
            DataTypeInfo dataType = userObject.getActualDataTypeInfo();
            if (dataType != null)
            {
                visible = DataLayerFilter.DATA_TYPE_FILTERABLE.test(dataType);
                activeFilter = DataLayerFilter.hasActiveLoadFilter(myToolbox, dataType);
            }
            else if (dataGroup != null)
            {
                visible = myFilter.test(dataGroup);
                activeFilter = DataLayerFilter.hasActiveLoadFilter(myToolbox, dataGroup);
            }
        }
        button.setHidden(!visible);
        button.setMixInColor(activeFilter ? Color.GREEN : null);
    }
}
