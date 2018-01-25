package io.opensphere.controlpanels.layers.groupby;

import io.opensphere.mantle.data.impl.AvailableGroupByTreeBuilder;

/**
 * Provides tree builders used for the Available data panel.
 */
public class AvailableTreeBuilderProvider extends BaseTreeBuilderProvider<AvailableGroupByTreeBuilder>
{
    @Override
    protected Class<AvailableGroupByTreeBuilder> getTreeBuilderType()
    {
        return AvailableGroupByTreeBuilder.class;
    }
}
