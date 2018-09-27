package io.opensphere.mantle.transformer.impl;

import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.plugin.selection.SelectionCommand;
import io.opensphere.mantle.plugin.selection.SelectionCommandFactory;
import io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class SelectionCommandWorker.
 */
public class SelectionCommandWorker extends PolygonRegionCommandWorker
{
    /**
     * Instantiates a new selection command worker.
     *
     * @param provider the provider
     * @param regions the regions
     * @param command the command
     */
    public SelectionCommandWorker(DataElementTransformerWorkerDataProvider provider, List<Polygon> regions,
            SelectionCommand command)
    {
        super(provider, regions, command, true);
    }

    @Override
    public void process()
    {
        List<Long> idsToChange = New.list(getProvider().getGeometrySet().size());
        SelectionCommand command = getCommand();

        if (command.equals(SelectionCommandFactory.SELECT))
        {
            idsToChange.addAll(getIntersectingIdSet());
            MantleToolboxUtils.getDataElementUpdateUtils(getProvider().getToolbox()).setDataElementsSelectionState(
                    getIntersectingIdSet(), idsToChange, getProvider().getDataType().getTypeKey(),
                    getProvider().getUpdateSource());
        }
        else if (command.equals(SelectionCommandFactory.SELECT_EXCLUSIVE))
        {
            idsToChange.addAll(getIntersectingIdSet());
            idsToChange.addAll(getNonIntersectingIdSet());
            MantleToolboxUtils.getDataElementUpdateUtils(getProvider().getToolbox()).setDataElementsSelectionState(
                    getIntersectingIdSet(), idsToChange, getProvider().getDataType().getTypeKey(),
                    getProvider().getUpdateSource());
        }
        else if (command.equals(SelectionCommandFactory.DESELECT))
        {
            idsToChange.addAll(getIntersectingIdSet());
            MantleToolboxUtils.getDataElementUpdateUtils(getProvider().getToolbox()).setDataElementsSelectionState(
                    Collections.<Long>emptySet(), idsToChange, getProvider().getDataType().getTypeKey(),
                    getProvider().getUpdateSource());

        }
    }
}
