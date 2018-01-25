package io.opensphere.controlpanels.layers.groupby;

import io.opensphere.mantle.data.impl.ActiveGroupByTreeBuilder;

/**
 * Provides tree builders used for the active layer window.
 */
public class ActiveTreeBuilderProvider extends BaseTreeBuilderProvider<ActiveGroupByTreeBuilder>
{
    @Override
    protected Class<ActiveGroupByTreeBuilder> getTreeBuilderType()
    {
        return ActiveGroupByTreeBuilder.class;
    }
}
