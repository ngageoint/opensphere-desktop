package io.opensphere.mantle.transformer.impl.worker;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.DefaultFeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.transformer.util.GeometrySetUtil;

/**
 * The Class StyleBasedUpdateGeometriesWorker.
 */
public class StyleBasedUpdateGeometriesWorker extends AbstractDataElementTransformerWorker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StyleBasedUpdateGeometriesWorker.class);

    /** The id set. */
    private final List<Long> myIdsOfInterest;

    /** The Multi feature geometry build helper. */
    private final MultiFeatureGeometryBuildHelper myMultiFeatureGeometryBuildHelper;

    /** The Style data element transformer worker data provider. */
    private final StyleDataElementTransformerWorkerDataProvider myStyleDataElementTransformerWorkerDataProvider;

    /**
     * Instantiates a new update geometries.
     *
     * @param provider the provider
     * @param idSet the id set
     */
    public StyleBasedUpdateGeometriesWorker(StyleDataElementTransformerWorkerDataProvider provider, List<Long> idSet)
    {
        super(provider);
        myStyleDataElementTransformerWorkerDataProvider = provider;
        myIdsOfInterest = idSet;
        myMultiFeatureGeometryBuildHelper = new MultiFeatureGeometryBuildHelper();
    }

    @Override
    public void run()
    {
        if (myIdsOfInterest != null && !myIdsOfInterest.isEmpty())
        {
            try
            {
                getProvider().getUpdateTaskActivity().registerUpdateInProgress(getProvider().getUpdateSource());

                boolean requiresMetaData = requiresMetaData();

                IterableElementTransfomerDataRetriever dataRetriever = new IterableElementTransfomerDataRetriever(
                        getProvider().getToolbox(), getProvider().getDataType(), myIdsOfInterest, true, true, requiresMetaData,
                        true);

                dataRetriever.retrieveData();

                Set<Long> oldVisIds = New.set(myIdsOfInterest.size());
                Set<Geometry> oldVisibleGeomSet = GeometrySetUtil.findGeometrySetWithIds(getProvider().getGeometrySet(),
                        getProvider().getGeometrySetLock(), myIdsOfInterest, oldVisIds,
                        getProvider().getDataModelIdFromGeometryIdBitMask());

                Set<Long> oldHiddenIds = New.set(myIdsOfInterest.size());
                Set<Geometry> oldHiddenGeomSet = GeometrySetUtil.findGeometrySetWithIds(getProvider().getHiddenGeometrySet(),
                        getProvider().getGeometrySetLock(), myIdsOfInterest, oldHiddenIds,
                        getProvider().getDataModelIdFromGeometryIdBitMask());

                Set<Geometry> newVisibleGeomSet = New.set(oldVisibleGeomSet.size());
                Set<Geometry> newHiddenGeomSet = New.set(oldHiddenGeomSet.size());

                Iterator<ElementData> edItr = dataRetriever.iterator();

                RenderPropertyPool rpp = createPool();
                ElementData ed = null;
                FeatureVisualizationStyle style = null;
                DefaultFeatureIndividualGeometryBuilderData builderData = new DefaultFeatureIndividualGeometryBuilderData();
                while (edItr.hasNext())
                {
                    ed = edItr.next();
                    if (ed.found())
                    {
                        MapGeometrySupport mgs = ed.getMapGeometrySupport();
                        style = myStyleDataElementTransformerWorkerDataProvider.getStyle(mgs, ed.getID().longValue());
                        long mgsTypeId = myStyleDataElementTransformerWorkerDataProvider.getMGSTypeId(mgs);
                        long geomId = myStyleDataElementTransformerWorkerDataProvider.getCombinedId(mgsTypeId,
                                ed.getID().longValue());
                        builderData.set(ed.getID().longValue(), geomId, getProvider().getDataType(), ed.getVisualizationState(),
                                mgs, requiresMetaData ? ed.getMetaDataProvider() : null);

                        Set<Geometry> setToUse = ed.getVisualizationState().isVisible() ? newVisibleGeomSet : newHiddenGeomSet;
                        if (style != null)
                        {
                            if (style.getAppliesTo().isIndividualElements())
                            {
                                style.createIndividualGeometry(setToUse, builderData, rpp);
                            }
                            else
                            {
                                myMultiFeatureGeometryBuildHelper.addBuilderData(style, builderData);
                            }
                        }
                        if (mgs.hasChildren())
                        {
                            generateGeometriesForChildren(setToUse, mgs, builderData, rpp);
                        }
                    }
                    else
                    {
                        if (LOGGER.isTraceEnabled())
                        {
                            LOGGER.trace(
                                    "Skipping element " + ed.getID() + " Because nothing was retrieved for it from the cache.");
                        }
                    }
                }
                myMultiFeatureGeometryBuildHelper.createMultiFeatureGeometries(newVisibleGeomSet, rpp);
                myMultiFeatureGeometryBuildHelper.clear();

                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("RenderPropertyPoolSize: " + rpp.size());
                }
                rpp.clearPool();

                postCreationProcess(oldVisibleGeomSet, oldHiddenGeomSet, newVisibleGeomSet, newHiddenGeomSet);

                if (getProvider().isPublishUpdatesToGeometryRegistry())
                {
                    publishToProvider(oldVisibleGeomSet, newVisibleGeomSet);
                }
            }
            finally
            {
                getProvider().getUpdateTaskActivity().unregisterUpdateInProgress(getProvider().getUpdateSource());
            }
        }
    }

    /**
     * Gets whether meta data is required.
     *
     * @return whether meta data is required
     */
    protected boolean requiresMetaData()
    {
        return myStyleDataElementTransformerWorkerDataProvider.stylesRequireMetaData();
    }

    /**
     * Generate geometries for children.
     *
     * @param setToUse the set to use
     * @param parent the parent MapGeometrySupport for which to process the
     *            children.
     * @param bd the {@link DefaultFeatureIndividualGeometryBuilderData}
     * @param rpp the {@link RenderPropertyPool}
     */
    private void generateGeometriesForChildren(Set<Geometry> setToUse, MapGeometrySupport parent,
            DefaultFeatureIndividualGeometryBuilderData bd, RenderPropertyPool rpp)
    {
        FeatureVisualizationStyle style = null;
        for (MapGeometrySupport child : parent.getChildren())
        {
            if (child != null)
            {
                style = myStyleDataElementTransformerWorkerDataProvider.getStyle(child, bd.getElementId());
                if (style != null)
                {
                    long mgsTypeId = myStyleDataElementTransformerWorkerDataProvider.getMGSTypeId(child);
                    long geomId = myStyleDataElementTransformerWorkerDataProvider.getCombinedId(mgsTypeId, bd.getElementId());
                    bd.setMGS(child);
                    bd.setGeomId(geomId);
                    if (style.getAppliesTo().isIndividualElements())
                    {
                        style.createIndividualGeometry(setToUse, bd, rpp);
                    }
                    else
                    {
                        myMultiFeatureGeometryBuildHelper.addBuilderData(style, bd);
                    }
                }
                if (child.hasChildren())
                {
                    generateGeometriesForChildren(setToUse, child, bd, rpp);
                }
            }
        }
    }

    /**
     * Post creation process.
     *
     * @param oldVisibleGeomSet the old visible geom set
     * @param oldHiddenGeomSet the old hidden geom set
     * @param newVisibleGeomSet the new visible geom set
     * @param newHiddenGeomSet the new hidden geom set
     */
    private void postCreationProcess(Set<Geometry> oldVisibleGeomSet, Set<Geometry> oldHiddenGeomSet,
            Set<Geometry> newVisibleGeomSet, Set<Geometry> newHiddenGeomSet)
    {
        long start;
        long end;
        start = System.nanoTime();
        ReentrantLock gsLock = getProvider().getGeometrySetLock();
        gsLock.lock();
        try
        {
            getProvider().getGeometrySet().removeAll(oldVisibleGeomSet);
            getProvider().getGeometrySet().addAll(newVisibleGeomSet);

            getProvider().getHiddenGeometrySet().removeAll(oldHiddenGeomSet);
            getProvider().getHiddenGeometrySet().addAll(newHiddenGeomSet);

            // Adjust hidden state of render properties to match data type.
            if (!getProvider().getDataType().isVisible())
            {
                AllVisibilityRenderPropertyUpdator visPropUpdate = new AllVisibilityRenderPropertyUpdator(getProvider(),
                        getProvider().getDataType().isVisible());
                visPropUpdate.run();
            }

            // Adjust opacity of all color render properties to match data type.
            if (getProvider().getDataType().getBasicVisualizationInfo() != null
                    && getProvider().getDataType().getBasicVisualizationInfo().getDefaultTypeColor().getAlpha() != 255)
            {
                ColorAllRenderPropertyUpdator alphaUpdate = new ColorAllRenderPropertyUpdator(getProvider(),
                        getProvider().getDataType().getBasicVisualizationInfo().getDefaultTypeColor(), true);
                alphaUpdate.run();
            }
        }
        finally
        {
            gsLock.unlock();
        }
        end = System.nanoTime();
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.formatTimingMessage("Post Geom Creation Set Maintance took ", end - start));
        }
    }

    /**
     * Publish to provider.
     *
     * @param oldVisibleGeomSet the old visible geom set
     * @param newVisibleGeomSet the new visible geom set
     */
    private void publishToProvider(Set<Geometry> oldVisibleGeomSet, Set<Geometry> newVisibleGeomSet)
    {
        long start = System.nanoTime();
        getProvider().getUpdateTaskActivity().registerUpdateInProgress(getProvider().getUpdateSource());
        getProvider().getToolbox().getGeometryRegistry().receiveObjects(getProvider().getUpdateSource(), newVisibleGeomSet,
                oldVisibleGeomSet);
        getProvider().getUpdateTaskActivity().unregisterUpdateInProgress(getProvider().getUpdateSource());
        if (LOGGER.isTraceEnabled())
        {
            long end = System.nanoTime();
            LOGGER.trace(StringUtilities.formatTimingMessage(
                    "Added/Remove Geometries to registry[" + newVisibleGeomSet.size() + "/" + oldVisibleGeomSet.size() + "] in ",
                    end - start));
        }
    }
}
