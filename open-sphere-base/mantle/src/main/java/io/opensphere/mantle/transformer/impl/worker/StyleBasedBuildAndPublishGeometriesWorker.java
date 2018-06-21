package io.opensphere.mantle.transformer.impl.worker;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.DefaultFeatureIndividualGeometryBuilderData;

/**
 * The Class BuildAndPublishGeometriesWorker.
 */
public class StyleBasedBuildAndPublishGeometriesWorker extends AbstractDataElementTransformerWorker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StyleBasedBuildAndPublishGeometriesWorker.class);

    /** The ids. */
    private final long[] myIds;

    /** The map data elements. */
    private final Collection<? extends MapDataElement> myMapDataElements;

    /** The Multi feature geometry build helper. */
    private final MultiFeatureGeometryBuildHelper myMultiFeatureGeometryBuildHelper;

    /** The Style data element transformer worker data provider. */
    private final StyleDataElementTransformerWorkerDataProvider myStyleDataElementTransformerWorkerDataProvider;

    /**
     * Instantiates a new builds the and publish geometries.
     *
     * @param provider the {@link StyleDataElementTransformerWorkerDataProvider}
     * @param ids the ids
     * @param mapDataElements the map data elements
     */
    public StyleBasedBuildAndPublishGeometriesWorker(StyleDataElementTransformerWorkerDataProvider provider, long[] ids,
            Collection<? extends MapDataElement> mapDataElements)
    {
        super(provider);
        myStyleDataElementTransformerWorkerDataProvider = provider;
        myIds = ids.clone();
        myMapDataElements = mapDataElements;
        myMultiFeatureGeometryBuildHelper = new MultiFeatureGeometryBuildHelper();
    }

    @Override
    public void run()
    {
        try
        {
            getProvider().getUpdateTaskActivity().registerUpdateInProgress(getProvider().getUpdateSource());
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                        "StyleBasedBuildAndPublishGeometriesWorker: Starting Geometry Add For " + myIds.length + " features");
            }
            long zero = System.nanoTime();
            long st = System.nanoTime();
            Set<Geometry> visGeometrySet = New.set(myIds.length);
            Set<Geometry> hiddenGeometrySet = New.set(myIds.length);
            VisualizationState defaultVSState = new VisualizationState(true);
            int index = 0;
            MapGeometrySupport mgs = null;
            VisualizationState vs = null;
            RenderPropertyPool rpPool = createPool();

            DefaultFeatureIndividualGeometryBuilderData builderData = new DefaultFeatureIndividualGeometryBuilderData();
            for (MapDataElement mde : myMapDataElements)
            {
                if (myIds[index] != DataElement.FILTERED)
                {
                    vs = mde.getVisualizationState() == null ? defaultVSState : mde.getVisualizationState();
                    mgs = mde.getMapGeometrySupport();
                    FeatureVisualizationStyle style = myStyleDataElementTransformerWorkerDataProvider.getStyle(mgs,
                            mde.getIdInCache());
                    if (style != null)
                    {
                        long mgsTypeId = myStyleDataElementTransformerWorkerDataProvider.getMGSTypeId(mgs);
                        long geomId = myStyleDataElementTransformerWorkerDataProvider.getCombinedId(mgsTypeId, myIds[index]);
                        builderData.set(myIds[index], geomId, getProvider().getDataType(), vs, mgs, mde.getMetaData());

                        if (builderData.getMGS() != null)
                        {
                            Set<Geometry> setToUse = vs.isVisible() ? visGeometrySet : hiddenGeometrySet;
                            createGeometries(rpPool, builderData, style, setToUse);
                        }
                    }
                }
                index++;
            }
            myMultiFeatureGeometryBuildHelper.createMultiFeatureGeometries(visGeometrySet, rpPool);
            myMultiFeatureGeometryBuildHelper.clear();

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("StyleBasedBuildAndPublishGeometriesWorker: RenderPropertyPoolSize: " + rpPool.size());
            }
            rpPool.clearPool();
            long ed = System.nanoTime();
            if (LOGGER.isDebugEnabled())
            {
                int total = visGeometrySet.size() + hiddenGeometrySet.size();
                LOGGER.debug(StringUtilities.formatTimingMessage(
                        "Built " + total + " geometries (" + visGeometrySet.size() + "/" + hiddenGeometrySet.size() + ") in ",
                        ed - st));
            }

            updateProviderGeometrySets(visGeometrySet, hiddenGeometrySet);

            publishGeometries(visGeometrySet);

            ed = System.nanoTime();
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(StringUtilities.formatTimingMessage("Overall Geometry Update Took: ", ed - zero));
            }
        }
        finally
        {
            getProvider().getUpdateTaskActivity().unregisterUpdateInProgress(getProvider().getUpdateSource());
        }
    }

    /**
     * Create the geometries.
     *
     * @param rpPool The pool of render properties.
     * @param builderData The geometry builder data.
     * @param style The visualization style.
     * @param setToUse The set to add the geometries to.
     */
    private void createGeometries(RenderPropertyPool rpPool, DefaultFeatureIndividualGeometryBuilderData builderData,
            FeatureVisualizationStyle style, Set<Geometry> setToUse)
    {
        if (style.getAppliesTo().isIndividualElements())
        {
            style.createIndividualGeometry(setToUse, builderData, rpPool);
        }
        else
        {
            myMultiFeatureGeometryBuildHelper.addBuilderData(style, builderData);
        }
        if (builderData.getMGS().hasChildren())
        {
            createGeometriesForChildren(setToUse, builderData, rpPool);
        }
    }

    /**
     * Creates the geometries for children.
     *
     * @param setToAddTo the set to add resultant geometries to.
     * @param bd the {@link DefaultFeatureIndividualGeometryBuilderData}
     * @param rpPool the {@link RenderPropertyPool}
     */
    private void createGeometriesForChildren(Set<Geometry> setToAddTo, DefaultFeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool rpPool)
    {
        for (MapGeometrySupport child : bd.getMGS().getChildren())
        {
            if (child != null)
            {
                FeatureVisualizationStyle style = myStyleDataElementTransformerWorkerDataProvider.getStyle(child,
                        bd.getElementId());
                long mgsTypeId = myStyleDataElementTransformerWorkerDataProvider.getMGSTypeId(child);
                long geomId = myStyleDataElementTransformerWorkerDataProvider.getCombinedId(mgsTypeId, bd.getElementId());
                bd.setGeomId(geomId);
                bd.setMGS(child);
                if (style != null)
                {
                    if (style.getAppliesTo().isIndividualElements())
                    {
                        style.createIndividualGeometry(setToAddTo, bd, rpPool);
                    }
                    else
                    {
                        myMultiFeatureGeometryBuildHelper.addBuilderData(style, bd);
                    }
                }
                if (child.hasChildren())
                {
                    createGeometriesForChildren(setToAddTo, bd, rpPool);
                }
            }
        }
    }

    /**
     * Update provider geometry sets.
     *
     * @param visGeometrySet the vis geometry set
     * @param hiddenGeometrySet the hidden geometry set
     */
    private void updateProviderGeometrySets(Set<Geometry> visGeometrySet, Set<Geometry> hiddenGeometrySet)
    {
        ReentrantLock lock = getProvider().getGeometrySetLock();
        lock.lock();
        try
        {
            getProvider().getIdSet().addAll(CollectionUtilities.listView(myIds));
            // Remove this to ensure we don't have filtered ids in the id
            // set.
            getProvider().getIdSet().remove(DataElement.FILTERED);
            getProvider().getGeometrySet().addAll(visGeometrySet);
            getProvider().getHiddenGeometrySet().addAll(hiddenGeometrySet);

            // Adjust hidden state of render properties to match data type.
            if (!getProvider().getDataType().isVisible())
            {
                AllVisibilityRenderPropertyUpdator visPropertyUpdator = new AllVisibilityRenderPropertyUpdator(getProvider(),
                        getProvider().getDataType().isVisible());
                visPropertyUpdator.run();
            }

            // Adjust opacity of all color render properties to match data
            // type.
            if (getProvider().getDataType().getBasicVisualizationInfo() != null
                    && getProvider().getDataType().getBasicVisualizationInfo().getDefaultTypeColor().getAlpha() != 255)
            {
                ColorAllRenderPropertyUpdator alphaUpdator = new ColorAllRenderPropertyUpdator(getProvider(),
                        getProvider().getDataType().getBasicVisualizationInfo().getDefaultTypeColor(), true);
                alphaUpdator.run();
            }
        }
        finally
        {
            lock.unlock();
        }
    }
}
