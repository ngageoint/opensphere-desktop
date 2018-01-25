package io.opensphere.controlpanels.layers.activedata.controller;

import java.util.function.Predicate;

import io.opensphere.controlpanels.layers.groupby.PredicatedGroupByTreeBuilder;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.GroupByTreeBuilder;

/**
 * An {@link AvailableDataDataLayerController} that further restricts the
 * predicate sent to the tree builder.
 */
public class PredicatedAvailableDataDataLayerController extends AvailableDataDataLayerController
{
    /** The Filter. */
    private final Predicate<? super DataGroupInfo> myFilter;

    /**
     * Instantiates a new search available data layer controller.
     *
     * @param pBox the box
     * @param filter the list of verified servers
     */
    public PredicatedAvailableDataDataLayerController(Toolbox pBox, Predicate<? super DataGroupInfo> filter)
    {
        super(pBox, null);
        myFilter = filter;
    }

    @Override
    public GroupByTreeBuilder getGroupByTreeBuilder()
    {
        return new PredicatedGroupByTreeBuilder(super.getGroupByTreeBuilder(), myFilter);
    }
}
