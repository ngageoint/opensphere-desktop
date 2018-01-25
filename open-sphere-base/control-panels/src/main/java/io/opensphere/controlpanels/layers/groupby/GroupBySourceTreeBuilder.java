package io.opensphere.controlpanels.layers.groupby;

import io.opensphere.mantle.data.impl.DataGroupInfoGroupByUtility.TreeOptions;

/**
 * The Class GroupBySourceTreeBuilder.
 */
public class GroupBySourceTreeBuilder extends GroupByDefaultTreeBuilder
{
    @Override
    public TreeOptions getTreeOptions()
    {
        // Don't flatten the tree
        return new TreeOptions(false, false, false);
    }
}
