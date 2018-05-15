package io.opensphere.mantle.transformer.impl.worker;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.factory.impl.MapGeometrySupportGeometryFactory;

/**
 * The Class DefaultBuildAndPublishGeometriesWorker.
 */
public class DefaultBuildAndPublishGeometriesWorker extends AbstractDataElementTransformerWorker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultBuildAndPublishGeometriesWorker.class);

    /** The Factory. */
    private final MapGeometrySupportGeometryFactory myFactory;

    /** The ids. */
    private final long[] myIds;

    /** The map data elements. */
    private final List<MapDataElement> myMapDataElements;

    /**
     * Instantiates a new builds the and publish geometries.
     *
     * @param provider the provider
     * @param factory the factory
     * @param ids the ids
     * @param mapDataElements the map data elements
     */
    public DefaultBuildAndPublishGeometriesWorker(DataElementTransformerWorkerDataProvider provider,
            MapGeometrySupportGeometryFactory factory, long[] ids, Collection<? extends MapDataElement> mapDataElements)
    {
        super(provider);
        myFactory = factory;
        myIds = ids.clone();
        myMapDataElements = New.linkedList(mapDataElements);
    }

    @Override
    public void run()
    {
        try
        {
            getProvider().getUpdateTaskActivity().registerUpdateInProgress(getProvider().getUpdateSource());
            long zero = System.nanoTime();
            Set<Geometry> visGeometrySet = New.set(myIds.length);
            Set<Geometry> hiddenGeometrySet = New.set(myIds.length);
            VisualizationState defaultVSState = new VisualizationState(true);
            int index = 0;
            Iterator<MapDataElement> mdeItr = myMapDataElements.iterator();
            MapDataElement mde = null;
            VisualizationState vs = null;
            RenderPropertyPool pool = createPool();
            while (mdeItr.hasNext())
            {
                mde = mdeItr.next();
                mdeItr.remove();
                if (myIds[index] != DataElement.FILTERED)
                {
                    vs = mde.getVisualizationState() == null ? defaultVSState : mde.getVisualizationState();

                    if (mde.getMapGeometrySupport() != null)
                    {
                        myFactory.createGeometries(vs.isVisible() ? visGeometrySet : hiddenGeometrySet,
                                mde.getMapGeometrySupport(), myIds[index], getProvider().getDataType(), vs, pool);
                    }
                }
                index++;
            }
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("AllRenderPropertyUpdator: RenderPropertyPoolSize: " + pool.size());
            }
            pool.clearPool();
            long end = System.nanoTime();
            int total = visGeometrySet.size() + hiddenGeometrySet.size();
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(StringUtilities.formatTimingMessage(
                        "Built " + total + " geometries (" + visGeometrySet.size() + "/" + hiddenGeometrySet.size() + ") in ",
                        end - zero));
            }

            getProvider().getGeometrySetLock().lock();
            try
            {
                getProvider().getIdSet().addAll(CollectionUtilities.listView(myIds));
                // Remove this to ensure we don't have filtered ids in the id
                // set.
                getProvider().getIdSet().remove(DataElement.FILTERED_AS_LONG.longValue());
                getProvider().getGeometrySet().addAll(visGeometrySet);
                getProvider().getHiddenGeometrySet().addAll(hiddenGeometrySet);

                adjustHiddenState();

                // Adjust opacity of all color render properties to match data
                // type.
                if (getProvider().getDataType().getBasicVisualizationInfo() != null
                        && getProvider().getDataType().getBasicVisualizationInfo().getDefaultTypeColor().getAlpha() != 255)
                {
                    ColorAllRenderPropertyUpdator opacityUpdator = new ColorAllRenderPropertyUpdator(getProvider(),
                            getProvider().getDataType().getBasicVisualizationInfo().getDefaultTypeColor(), true);
                    opacityUpdator.run();
                }
            }
            finally
            {
                getProvider().getGeometrySetLock().unlock();
            }

            publishGeometries(visGeometrySet);

            end = System.nanoTime();
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(StringUtilities.formatTimingMessage("Overall Geometry Update Took: ", end - zero));
            }
        }
        finally
        {
            getProvider().getUpdateTaskActivity().unregisterUpdateInProgress(getProvider().getUpdateSource());
        }
    }

    /**
     * Adjust hidden state of render properties to match data type.
     */
    private void adjustHiddenState()
    {
        if (!getProvider().getDataType().isVisible())
        {
            new AllVisibilityRenderPropertyUpdator(getProvider(), getProvider().getDataType().isVisible()).run();
        }
    }
}
