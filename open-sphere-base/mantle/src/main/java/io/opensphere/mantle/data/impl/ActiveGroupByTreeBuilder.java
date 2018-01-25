package io.opensphere.mantle.data.impl;

import io.opensphere.core.Toolbox;

/**
 * A {@link GroupByTreeBuilder} that builds the layer tree for the active
 * layer's panel.
 */
public interface ActiveGroupByTreeBuilder extends GroupByTreeBuilder
{
    /**
     * Initializes the group by tree builder.
     *
     * @param toolbox The system toolbox.
     */
    void initializeForActive(Toolbox toolbox);
}
