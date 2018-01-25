package io.opensphere.mantle.transformer.impl;

import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.plugin.selection.SelectionCommand;
import io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class PurgeByRegionCommandWorker.
 */
public class PurgeByRegionCommandWorker extends PolygonRegionCommandWorker
{
    /**
     * Instantiates a new selection command worker.
     *
     * @param provider the provider
     * @param regions the regions
     * @param command the command
     */
    public PurgeByRegionCommandWorker(DataElementTransformerWorkerDataProvider provider, List<Polygon> regions,
            SelectionCommand command)
    {
        super(provider, regions, command, true);
    }

    @Override
    public void process()
    {
        // Don't purge anything from base.
        if (getProvider() != null && getProvider().getDataType() != null
                && getProvider().getDataType().getBasicVisualizationInfo() != null
                && getProvider().getDataType().getBasicVisualizationInfo().getLoadsTo().isAnalysisEnabled())
        {
            Set<Long> intersectingIdSet = getIntersectingIdSet();
            if (intersectingIdSet != null && !intersectingIdSet.isEmpty())
            {
                long[] idSet = CollectionUtilities.toLongArray(getIntersectingIdSet());
                MantleToolboxUtils.getMantleToolbox(getProvider().getToolbox()).getDataTypeController()
                        .removeDataElements(getProvider().getDataType(), idSet);
            }
        }
    }
}
