package io.opensphere.mantle.transformer.impl;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import com.vividsolutions.jts.geom.Polygon;

import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.event.consolidated.AbstractConsolidatedDataElementChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementColorChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementHighlightChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementSelectionChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementVisibilityChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.plugin.selection.SelectionCommand;
import io.opensphere.mantle.transformer.TransformerGeomRegistryUpdateTaskActivity;
import io.opensphere.mantle.transformer.impl.worker.AllVisibilityRenderPropertyUpdator;
import io.opensphere.mantle.transformer.impl.worker.ColorAllRenderPropertyUpdator;
import io.opensphere.mantle.transformer.impl.worker.PublishUnpublishGeometrySetWorker;
import io.opensphere.mantle.transformer.impl.worker.RemoveDataElementsWorker;
import io.opensphere.mantle.transformer.impl.worker.ShutdownTransformerWorker;
import io.opensphere.mantle.transformer.impl.worker.StyleBasedBuildAndPublishGeometriesWorker;
import io.opensphere.mantle.transformer.impl.worker.StyleBasedDeriveColorUpdateGeometriesWorker;
import io.opensphere.mantle.transformer.impl.worker.StyleBasedUpdateGeometriesWorker;
import io.opensphere.mantle.transformer.impl.worker.StyleDataElementTransformerWorkerDataProvider;
import io.opensphere.mantle.transformer.util.GeometrySetUtil;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.bitmanip.BitMaskGenerator;

/**
 * The Class StyleMGSTypeMDETransformer.
 */
@SuppressWarnings("PMD.GodClass")
public class StyleTransformerGeometryProcessor implements StyleDataElementTransformerWorkerDataProvider
{
    /** The data model ID bit mask. */
    private static final long DATA_MODEL_ID_MASK = BitMaskGenerator.createLongRangedBitMask(0, 39);

    /** The number of bytes to shift the geometry type mask. */
    private static final long GEOMETRY_TYPE_SHIFT_BY_BITS = 40;

    /** The my data type info. */
    private final DataTypeInfo myDataTypeInfo;

    /** The my geometry set. */
    private final Set<Geometry> myGeometrySet;

    /** The geometry set lock. */
    private final ReentrantLock myGeometrySetLock;

    /** The my geometry set. */
    private final Set<Geometry> myHiddenGeometrySet;

    /** The id set. */
    private final TLongSet myIdSet;

    /** The Last color change update number. */
    private long myLastColorChangeUpdateNumber;

    /** The Master transformer. */
    private final StyleMapDataElementTransformer myMasterTransformer;

    /** The Style manager. */
    private final StyleTransformerStyleManager myStyleManager;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new style mgs type mde transformer.
     *
     * @param master the master
     * @param tb the tb
     * @param dti the dti
     * @param es the es
     */
    public StyleTransformerGeometryProcessor(StyleMapDataElementTransformer master, Toolbox tb, DataTypeInfo dti,
            ExecutorService es)
    {
        myMasterTransformer = master;
        myToolbox = tb;
        myDataTypeInfo = dti;
        myIdSet = new TLongHashSet();
        myGeometrySetLock = new ReentrantLock();
        myGeometrySet = New.set();
        myHiddenGeometrySet = New.set();
        myStyleManager = new StyleTransformerStyleManager(myToolbox, myDataTypeInfo, this);
    }

    @Override
    public boolean anyStyleAppliesToAllElements()
    {
        return myStyleManager.anyStyleAppliesToAllElements();
    }

    @Override
    public long getCombinedId(long geometryTypeId, long dataModelId)
    {
        long upperShifted = geometryTypeId << GEOMETRY_TYPE_SHIFT_BY_BITS;
        return upperShifted ^ dataModelId;
    }

    @Override
    public long getDataModelIdFromGeometryId(long geomId)
    {
        return geomId & DATA_MODEL_ID_MASK;
    }

    @Override
    public long getDataModelIdFromGeometryIdBitMask()
    {
        return DATA_MODEL_ID_MASK;
    }

    @Override
    public DataTypeInfo getDataType()
    {
        return myDataTypeInfo;
    }

    @Override
    public TLongLongHashMap getElementIdsForGeometryIds(long[] geometryIds)
    {
        Utilities.checkNull(geometryIds, "geometryIds");
        if (geometryIds.length == 0)
        {
            throw new IllegalArgumentException("geometryIds list cannot be zero-length");
        }
        TLongLongHashMap resultMap = new TLongLongHashMap(geometryIds.length);
        myGeometrySetLock.lock();
        try

        {
            int count = myGeometrySet.size() + myHiddenGeometrySet.size();
            if (count > 0)
            {
                // Build up a set of the ids for all geometries in our
                // transformer.
                TLongSet geomIdsSet = new TLongHashSet(count);
                for (Geometry geom : myGeometrySet)
                {
                    geomIdsSet.add(geom.getDataModelId());
                }
                for (Geometry geom : myHiddenGeometrySet)
                {
                    geomIdsSet.add(geom.getDataModelId());
                }

                // Now check our geomid set to against each of the input
                // ids and if that id is one of our geom ids, put an entry
                // in the result map to provide the cache id.
                for (long geomId : geometryIds)
                {
                    if (geomIdsSet.contains(geomId))
                    {
                        resultMap.put(geomId, getDataModelIdFromGeometryId(geomId));
                    }
                }
            }
        }
        finally
        {
            myGeometrySetLock.unlock();
        }
        return resultMap;
    }

    @Override
    public Set<Geometry> getGeometrySet()
    {
        return myGeometrySet;
    }

    @Override
    public ReentrantLock getGeometrySetLock()
    {
        return myGeometrySetLock;
    }

    @Override
    public Set<Geometry> getHiddenGeometrySet()
    {
        return myHiddenGeometrySet;
    }

    @Override
    public TLongSet getIdSet()
    {
        return myIdSet;
    }

    @Override
    public long getMGSTypeId(MapGeometrySupport mgs)
    {
        return myStyleManager.getMGSTypeIdForMGS(mgs);
    }

    @Override
    public long getMGSTypeIdFromGeometryId(long geomId)
    {
        return geomId >> GEOMETRY_TYPE_SHIFT_BY_BITS;
    }

    @Override
    public FeatureVisualizationStyle getStyle(long geometryTypeId, long elementId)
    {
        return myStyleManager.getStyleByMGSTypeId(geometryTypeId, elementId);
    }

    @Override
    public FeatureVisualizationStyle getStyle(MapGeometrySupport mgs, long elementId)
    {
        return myStyleManager.getStyle(mgs, elementId);
    }

    @Override
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public Object getUpdateSource()
    {
        return myMasterTransformer;
    }

    @Override
    public TransformerGeomRegistryUpdateTaskActivity getUpdateTaskActivity()
    {
        return myMasterTransformer.getUpdateTaskActivity();
    }

    /**
     * Handle consolidated data element change event.
     *
     * @param event the event
     */
    public void handleConsolidatedDataElementChangeEvent(AbstractConsolidatedDataElementChangeEvent event)
    {
        executeIfNotShutdown(new DataElementEventHandler(event));
    }

    /**
     * Handle data type info change event.
     *
     * @param event the event
     */
    public void handleDataTypeInfoChangeEvent(AbstractDataTypeInfoChangeEvent event)
    {
        executeIfNotShutdown(new DTIChangeWorker(event));
    }

    /**
     * Checks for geometry data model id.
     *
     * @param id the id
     * @return true, if successful
     */
    public boolean hasGeometryDataModelId(long id)
    {
        return myIdSet.contains(Long.valueOf(id));
    }

    @Override
    public boolean hasGeometryId(long geomId)
    {
        myGeometrySetLock.lock();
        try
        {
            return myGeometrySet.stream().anyMatch(g -> g.getDataModelId() == geomId)
                    || myHiddenGeometrySet.stream().anyMatch(g -> g.getDataModelId() == geomId);
        }
        finally
        {
            myGeometrySetLock.unlock();
        }
    }

    @Override
    public boolean isPublishUpdatesToGeometryRegistry()
    {
        return myMasterTransformer.isPublishUpdatesToGeometryRegistry();
    }

    /**
     * Publish un-publish geometries.
     *
     * @param publish the publish
     */
    public void publishUnpublishGeometries(boolean publish)
    {
        executeIfNotShutdown(new PublishUnpublishGeometrySetWorker(this, myGeometrySet, publish));
    }

    /**
     * Purge occurred.
     *
     * @param bounds the bounds
     * @param regions the regions
     * @param cmd the cmd
     */
    public void purgeOccurred(Collection<? extends PolygonGeometry> bounds, List<Polygon> regions, SelectionCommand cmd)
    {
        executeIfNotShutdown(
                new PurgeByRegionCommandWorker(this, JTSCoreGeometryUtilities.convertToJTSPolygonsAndSplit(bounds), cmd));
    }

    /**
     * Rebuild geometries from style change.
     */
    public void rebuildGeometriesFromStyleChange()
    {
        StyleBasedUpdateGeometriesWorker worker = new StyleBasedUpdateGeometriesWorker(this, getIdsAsList());
        executeIfNotShutdown(worker);
    }

    /**
     * Selection occurred.
     *
     * @param bounds the bounds
     * @param cmd the command
     */
    public void selectionOccurred(Collection<? extends PolygonGeometry> bounds, SelectionCommand cmd)
    {
        executeIfNotShutdown(
                new SelectionCommandWorker(this, JTSCoreGeometryUtilities.convertToJTSPolygonsAndSplit(bounds), cmd));
    }

    /**
     * Removes all geometries from the geometry registry for which this
     * transformer is responsible.
     */
    public void shutdown()
    {
        myStyleManager.shutdown();
        myMasterTransformer.getExecutorService().execute(new ShutdownTransformerWorker(this));
        myMasterTransformer.getExecutorService().shutdown();
    }

    @Override
    public boolean stylesRequireMetaData()
    {
        return myStyleManager.stylesRequireMetaData();
    }

    /**
     * Gets the executor service.
     *
     * @return the executor service
     */
    public ExecutorService getExecutor()
    {
        return myMasterTransformer.getExecutorService();
    }

    /**
     * Sets the style for the individual data element IDs, overriding the layer
     * style.
     *
     * @param elementIds the data element IDs
     * @param style the style
     */
    public void setOverrideStyle(Collection<Long> elementIds, FeatureVisualizationStyle style)
    {
        myStyleManager.setOverrideStyle(elementIds, style);
    }

    /**
     * Removes the overridden style for the data element IDs.
     *
     * @param elementIds the data element IDs
     */
    public void removeOverrideStyle(Collection<Long> elementIds)
    {
        myStyleManager.removeOverrideStyle(elementIds);
    }

    /**
     * Gets the list of overridden data element IDs.
     *
     * @return the data element IDs.
     */
    public List<Long> getOverriddenIds()
    {
        return myStyleManager.getOverriddenIds();
    }

    /**
     * Adds the map data elements.
     *
     * @param dataElements the data elements
     * @param ids the ids
     */
    void addMapDataElements(Collection<? extends MapDataElement> dataElements, long[] ids)
    {
        executeIfNotShutdown(new StyleBasedBuildAndPublishGeometriesWorker(this, ids, dataElements));
    }

    /**
     * Removes the map data elements.
     *
     * @param ids the ids
     */
    void removeMapDataElements(long[] ids)
    {
        executeIfNotShutdown(new RemoveDataElementsWorker(this, CollectionUtilities.listView(ids)));
    }

    /**
     * Execute if not shutdown.
     *
     * @param task the task
     */
    private void executeIfNotShutdown(Runnable task)
    {
        if (!myMasterTransformer.getExecutorService().isShutdown())
        {
            myMasterTransformer.getExecutorService().execute(task);
        }
    }

    /**
     * The Class ConsolidatedDataElementEventHandler.
     */
    protected class DataElementEventHandler implements Runnable
    {
        /** The event. */
        private final AbstractConsolidatedDataElementChangeEvent myEvt;

        /**
         * Instantiates a new consolidated data element event handler.
         *
         * @param evt the evt
         */
        public DataElementEventHandler(AbstractConsolidatedDataElementChangeEvent evt)
        {
            myEvt = evt;
        }

        @Override
        public void run()
        {
            if (!(myEvt instanceof ConsolidatedDataElementHighlightChangeEvent))
            {
                boolean hasMultiElementStyleType = myStyleManager.anyStyleAppliesToAllElements();
                boolean anyStyleAlwaysRequiresGeometryRebuild = myStyleManager.anyStyleAlwaysRequiresFullGeometryRebuild();

                List<Long> idsOfInterest = CollectionUtilities.intersectionAsList(getIdsAsSet(), myEvt.getRegistryIds());
                if (!idsOfInterest.isEmpty() && !hasMultiElementStyleType && !anyStyleAlwaysRequiresGeometryRebuild)
                {
                    if (myEvt instanceof ConsolidatedDataElementVisibilityChangeEvent)
                    {
                        processVisibilityChangeEvent((ConsolidatedDataElementVisibilityChangeEvent)myEvt, idsOfInterest);
                    }
                    else if (myEvt instanceof ConsolidatedDataElementSelectionChangeEvent)
                    {
                        // If this is a selection change and a style is
                        // selection sensitive, we need to update the geometries
                        // rather than just deriving new ones.
                        if (myStyleManager.hasSelectionSensitiveStyle())
                        {
                            StyleBasedUpdateGeometriesWorker worker = new StyleBasedUpdateGeometriesWorker(
                                    StyleTransformerGeometryProcessor.this, idsOfInterest);
                            worker.run();
                        }
                        else
                        {
                            Color c = myDataTypeInfo.getBasicVisualizationInfo() == null ? Color.white
                                    : myDataTypeInfo.getBasicVisualizationInfo().getTypeColor();
                            StyleBasedDeriveColorUpdateGeometriesWorker worker = new StyleBasedDeriveColorUpdateGeometriesWorker(
                                    StyleTransformerGeometryProcessor.this, idsOfInterest, c, false);
                            worker.run();
                        }
                    }
                    else if (myEvt instanceof ConsolidatedDataElementColorChangeEvent)
                    {
                        ConsolidatedDataElementColorChangeEvent colorEvent = (ConsolidatedDataElementColorChangeEvent)myEvt;
                        if (!Utilities.sameInstance(myEvt.getSource(), myMasterTransformer) && !colorEvent.isExternalOnly())
                        {
                            Color c = myDataTypeInfo.getBasicVisualizationInfo() == null ? Color.white
                                    : myDataTypeInfo.getBasicVisualizationInfo().getTypeColor();
                            StyleBasedDeriveColorUpdateGeometriesWorker worker = new StyleBasedDeriveColorUpdateGeometriesWorker(
                                    StyleTransformerGeometryProcessor.this, idsOfInterest, c, false);
                            worker.run();
                        }
                    }
                    else
                    {
                        StyleBasedUpdateGeometriesWorker update = new StyleBasedUpdateGeometriesWorker(
                                StyleTransformerGeometryProcessor.this, idsOfInterest);
                        update.run();
                    }
                }
                if (hasMultiElementStyleType || anyStyleAlwaysRequiresGeometryRebuild)
                {
                    StyleBasedUpdateGeometriesWorker worker = new StyleBasedUpdateGeometriesWorker(
                            StyleTransformerGeometryProcessor.this,
                            anyStyleAlwaysRequiresGeometryRebuild ? idsOfInterest : getIdsAsList());
                    executeIfNotShutdown(worker);
                }
            }
        }

        /**
         * Process visibility change.
         *
         * @param vEvent the v event
         * @param idsOfInterest the ids of interest
         */
        private void processVisibilityChangeEvent(ConsolidatedDataElementVisibilityChangeEvent vEvent, List<Long> idsOfInterest)
        {
            long[] idsOfInterestArray = CollectionUtilities.toLongArray(idsOfInterest);
            TLongHashSet toBeVisibleIdSet = new TLongHashSet(vEvent.getVisibleIdSet().size());
            toBeVisibleIdSet.addAll(vEvent.getVisibleIdSet().toArray());
            toBeVisibleIdSet.retainAll(idsOfInterestArray);
            if (!toBeVisibleIdSet.isEmpty())
            {
                Set<Geometry> found = GeometrySetUtil.findGeometrySetWithIds(myHiddenGeometrySet, myGeometrySetLock,
                        CollectionUtilities.listView(toBeVisibleIdSet.toArray()), DATA_MODEL_ID_MASK);
                if (!found.isEmpty())
                {
                    myGeometrySetLock.lock();
                    try
                    {
                        myGeometrySet.addAll(found);
                        myHiddenGeometrySet.removeAll(found);
                    }
                    finally
                    {
                        myGeometrySetLock.unlock();
                    }

                    if (myMasterTransformer.isPublishUpdatesToGeometryRegistry())
                    {
                        PublishUnpublishGeometrySetWorker worker = new PublishUnpublishGeometrySetWorker(
                                StyleTransformerGeometryProcessor.this, found, true);
                        worker.run();
                    }
                }
            }

            TLongHashSet toBeHiddenIdSet = new TLongHashSet(vEvent.getInvisibleIdSet().size());
            toBeHiddenIdSet.addAll(vEvent.getInvisibleIdSet().toArray());
            toBeHiddenIdSet.retainAll(idsOfInterestArray);
            if (!toBeHiddenIdSet.isEmpty())
            {
                Set<Geometry> found = GeometrySetUtil.findGeometrySetWithIds(myGeometrySet, myGeometrySetLock,
                        CollectionUtilities.listView(toBeHiddenIdSet.toArray()), DATA_MODEL_ID_MASK);
                if (!found.isEmpty())
                {
                    myGeometrySetLock.lock();
                    try
                    {
                        myGeometrySet.removeAll(found);
                        myHiddenGeometrySet.addAll(found);
                    }
                    finally
                    {
                        myGeometrySetLock.unlock();
                    }

                    if (myMasterTransformer.isPublishUpdatesToGeometryRegistry())
                    {
                        PublishUnpublishGeometrySetWorker worker = new PublishUnpublishGeometrySetWorker(
                                StyleTransformerGeometryProcessor.this, found, false);
                        worker.run();
                    }
                }
            }
        }
    }

    /**
     * The Class DataTypeInfoChangeWorker.
     */
    protected class DTIChangeWorker implements Runnable
    {
        /** The event. */
        private final AbstractDataTypeInfoChangeEvent myEvt;

        /**
         * Instantiates a new data type info change worker.
         *
         * @param event the event
         */
        public DTIChangeWorker(AbstractDataTypeInfoChangeEvent event)
        {
            myEvt = event;
        }

        @Override
        public void run()
        {
            boolean hasMultiElementStyleType = myStyleManager.anyStyleAppliesToAllElements();
            boolean anyStyleAlwaysRequiresGeometryRebuild = myStyleManager.anyStyleAlwaysRequiresFullGeometryRebuild();
            boolean rebuildAll = false;
            switch (myEvt.getType())
            {
                case LIFT_CHANGED:
                case Z_ORDER_CHANGED:
                case LOADS_TO_CHANGED:
                    // Rebuild all geometries.
                    rebuildAll = true;
                    break;
                case VISIBILITY_CHANGED:
                    if (hasMultiElementStyleType || anyStyleAlwaysRequiresGeometryRebuild)
                    {
                        rebuildAll = true;
                    }
                    else
                    {
                        DataTypeVisibilityChangeEvent visEvt = (DataTypeVisibilityChangeEvent)myEvt;
                        AllVisibilityRenderPropertyUpdator worker = new AllVisibilityRenderPropertyUpdator(
                                StyleTransformerGeometryProcessor.this, visEvt.isVisible());
                        executeIfNotShutdown(worker);
                    }
                    break;
                case METADATA_SPECIAL_KEY_CHANGED:
                    // TODO: This needs work. Need to use those special
                    // metadata properties to alter our MapGeometrySupport and
                    // then rebuild. all geometries.
                    rebuildAll = true;
                    break;
                case TYPE_COLOR_CHANGED:
                    DataTypeInfoColorChangeEvent colorEvent = (DataTypeInfoColorChangeEvent)myEvt;
                    if (colorEvent.getUpdateNumber() > myLastColorChangeUpdateNumber)
                    {
                        myLastColorChangeUpdateNumber = colorEvent.getUpdateNumber();
                        List<Long> ids = getIdsAsList();
                        if (hasMultiElementStyleType || anyStyleAlwaysRequiresGeometryRebuild)
                        {
                            rebuildAll = true;
                            MantleToolboxUtils.getDataElementUpdateUtils(getToolbox()).setDataElementsColor(colorEvent.getColor(),
                                    ids, getDataType().getTypeKey(), myMasterTransformer);
                        }
                        else
                        {
                            // If this is a data type opacity change only then
                            // just alter the existing render property colors.
                            // Otherwise derive new geometries with the color
                            // changes.
                            if (colorEvent.isOpacityChangeOnly())
                            {
                                ColorAllRenderPropertyUpdator updator = new ColorAllRenderPropertyUpdator(
                                        StyleTransformerGeometryProcessor.this, colorEvent.getColor(), true);

                                executeIfNotShutdown(updator);
                                MantleToolboxUtils.getDataElementUpdateUtils(getToolbox()).setDataElementsOpacity(
                                        colorEvent.getColor().getAlpha(), ids, getDataType().getTypeKey(),
                                        myMasterTransformer);
                            }
                            else
                            {
                                // Update all render properties colors to match
                                // the new color.
                                StyleBasedDeriveColorUpdateGeometriesWorker aWorker = new StyleBasedDeriveColorUpdateGeometriesWorker(
                                        StyleTransformerGeometryProcessor.this, ids, colorEvent.getColor(),
                                        false);
                                executeIfNotShutdown(aWorker);
                                MantleToolboxUtils.getDataElementUpdateUtils(getToolbox()).setDataElementsColor(
                                        colorEvent.getColor(), ids, getDataType().getTypeKey(),
                                        myMasterTransformer);
                            }
                        }
                    }
                    break;
                case REBUILD_GEOMETRY_REQUEST:
                    rebuildAll = true;
                    break;
                default:
                    break;
            }
            if (rebuildAll)
            {
                StyleBasedUpdateGeometriesWorker worker = new StyleBasedUpdateGeometriesWorker(
                        StyleTransformerGeometryProcessor.this, getIdsAsList());
                executeIfNotShutdown(worker);
            }
        }
    }
}
