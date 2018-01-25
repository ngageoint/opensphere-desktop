package io.opensphere.mantle.data.impl;

import io.opensphere.core.Toolbox;

/**
 * A {@link GroupByTreeBuilder} that builds the layer tree for the available
 * layer's panel.
 */
public interface AvailableGroupByTreeBuilder extends GroupByTreeBuilder
{
    /**
     * Initializes the group by tree builder.
     *
     * @param toolbox The system toolbox.
     */
    void initializeForAvailable(Toolbox toolbox);
}
