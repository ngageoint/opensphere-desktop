package io.opensphere.controlpanels.layers.groupby;

import io.opensphere.core.Toolbox;

/**
 * The Class GroupByTitleTreeBuilder.
 */
public class GroupByActiveTitleTreeBuilder extends AbstractGroupByTitleTreeBuilder
{
    /**
     * Instantiates a new group by title tree builder.
     *
     * @param tb the {@link Toolbox}
     */
    public GroupByActiveTitleTreeBuilder(Toolbox tb)
    {
        super(tb, true, true);
    }
}
