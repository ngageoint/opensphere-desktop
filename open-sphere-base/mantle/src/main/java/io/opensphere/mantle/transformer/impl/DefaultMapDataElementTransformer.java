package io.opensphere.mantle.transformer.impl;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.event.consolidated.AbstractConsolidatedDataElementChangeEvent;
import io.opensphere.mantle.data.geom.factory.impl.MapGeometrySupportGeometryFactory;
import io.opensphere.mantle.plugin.selection.SelectionCommand;
import io.opensphere.mantle.plugin.selection.SelectionCommandProcessor;
import io.opensphere.mantle.transformer.TransformerGeomRegistryUpdateTaskActivity;
import io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider;
import io.opensphere.mantle.transformer.impl.worker.DefaultBuildAndPublishGeometriesWorker;
import io.opensphere.mantle.transformer.impl.worker.RemoveDataElementsWorker;
import io.opensphere.mantle.transformer.impl.worker.ShutdownTransformerWorker;
import io.opensphere.mantle.transformer.util.GeometrySetUtil;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class DefaultMapDataElementTransformer.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultMapDataElementTransformer extends AbstractMapDataElementTransformer
        implements DataElementTransformerWorkerDataProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultMapDataElementTransformer.class);

    /** The publish changes to geometry registry. */
    static final boolean PUBLISH_CHANGES_TO_GEOMETRY_REGISTRY = true;

    /** The my geometry set. */
    private final Set<Geometry> myGeometrySet;

    /** The geometry set lock. */
    private final ReentrantLock myGeometrySetLock;

    /** The Geom reg update activity. */
    private final TransformerGeomRegistryUpdateTaskActivity myGeomRegUpdateActivity;

    /** The my geometry set. */
    private final Set<Geometry> myHiddenGeometrySet;

    /** The id set. */
    private final TLongSet myIdSet;

    /** The Last color change update number. */
    private long myLastColorChangeUpdateNumber;

    /** The map geometry support geometry factory. */
    private final MapGeometrySupportGeometryFactory myMapGeometrySupportGeometryFactory;

    /**
     * Instantiates a new default map data element transformer.
     *
     * @param aToolbox the a toolbox
     * @param dti the dti
     * @param source the source
     * @param category the category
     * @param activity the activity
     */
    public DefaultMapDataElementTransformer(Toolbox aToolbox, DataTypeInfo dti, String source, String category,
            TransformerGeomRegistryUpdateTaskActivity activity)
    {
        super(aToolbox, dti, source, category);
        myIdSet = new TLongHashSet();
        myGeomRegUpdateActivity = activity;
        myGeometrySetLock = new ReentrantLock();
        myGeometrySet = New.set();
        myHiddenGeometrySet = New.set();
        MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(aToolbox);
        myMapGeometrySupportGeometryFactory = new MapGeometrySupportGeometryFactory(mtb);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.AbstractMapDataElementTransformer#addMapDataElements(java.util.Collection,
     *      long[])
     */
    @Override
    public void addMapDataElements(Collection<? extends MapDataElement> dataElements, long[] ids)
    {
        executeIfNotShutdown(
                new DefaultBuildAndPublishGeometriesWorker(this, myMapGeometrySupportGeometryFactory, ids, dataElements));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.AbstractMapDataElementTransformer#createDataElementChangeListener()
     */
    @Override
    public EventListener<AbstractConsolidatedDataElementChangeEvent> createDataElementChangeListener()
    {
        return event -> handleElementChange(event);
    }

    /**
     * Processes a data element change event.
     *
     * @param event the event fired when the element changed.
     */
    protected void handleElementChange(AbstractConsolidatedDataElementChangeEvent event)
    {
        if (event.getDataTypeKeys().contains(getDataType().getTypeKey()))
        {
            executeIfNotShutdown(new ConsolidatedDataElementEventHandler(this, event, myMapGeometrySupportGeometryFactory));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.AbstractMapDataElementTransformer#createDataTypeChangeListener()
     */
    @Override
    public EventListener<AbstractDataTypeInfoChangeEvent> createDataTypeChangeListener()
    {
        return event -> handleDataTypeChange(event);
    }

    /**
     * Processes a data type change event.
     *
     * @param event The event fired when the datatype changed.
     */
    protected void handleDataTypeChange(AbstractDataTypeInfoChangeEvent event)
    {
        if (Utilities.sameInstance(getDataType(), event.getDataTypeInfo()))
        {
            executeIfNotShutdown(new DataTypeInfoChangeWorker(this, event, myMapGeometrySupportGeometryFactory));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.AbstractMapDataElementTransformer#createPurgeCommandProcessor()
     */
    @Override
    public SelectionCommandProcessor createPurgeCommandProcessor()
    {
        return (bounds, cmd) -> handlePurge(bounds, cmd);
    }

    /**
     * Handles a purge command, purging by region.
     *
     * @param bounds the geograhpic boundary of the purge region.
     * @param cmd the command with which to execute the purge operation.
     */
    protected void handlePurge(Collection<? extends PolygonGeometry> bounds, SelectionCommand cmd)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Purge Occured: " + cmd);
        }
        executeIfNotShutdown(
                new PurgeByRegionCommandWorker(this, JTSCoreGeometryUtilities.convertToJTSPolygonsAndSplit(bounds), cmd));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.AbstractMapDataElementTransformer#createSelectionCommandProcessor()
     */
    @Override
    public SelectionCommandProcessor createSelectionCommandProcessor()
    {
        return (bounds, cmd) -> handleSelection(bounds, cmd);
    }

    /**
     * Handles a selection operation.
     *
     * @param bounds the geographic bounds of the selection operation.
     * @param cmd the command with which to handle the selection operation.
     */
    protected void handleSelection(Collection<? extends PolygonGeometry> bounds, SelectionCommand cmd)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Selecton Occured: " + cmd);
        }
        executeIfNotShutdown(
                new SelectionCommandWorker(this, JTSCoreGeometryUtilities.convertToJTSPolygonsAndSplit(bounds), cmd));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.MapDataElementTransformer#getDataModelIdFromGeometryId(long)
     */
    @Override
    public long getDataModelIdFromGeometryId(long geomId)
    {
        return hasGeometryForDataModelId(geomId) ? geomId : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider#getDataModelIdFromGeometryIdBitMask()
     */
    @Override
    public long getDataModelIdFromGeometryIdBitMask()
    {
        return GeometrySetUtil.ALL_BITS_MASK;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.MapDataElementTransformer#getElementIdsForGeometryIds(long[])
     */
    @Override
    public TLongLongHashMap getElementIdsForGeometryIds(long[] geometryIds)
    {
        Utilities.checkNull(geometryIds, "geometryIds");
        if (geometryIds.length == 0)
        {
            throw new IllegalArgumentException("geometryIds list cannot be zero-length");
        }
        TLongLongHashMap resultMap = new TLongLongHashMap(geometryIds.length);

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

                // Check the input id list against our set and create the result
                // map. In this transformer the geom ids and element
                // ids are the same.
                for (long id : geometryIds)
                {
                    if (geomIdsSet.contains(id))
                    {
                        resultMap.put(id, id);
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

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider#getGeometrySet()
     */
    @Override
    public Set<Geometry> getGeometrySet()
    {
        return myGeometrySet;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider#getGeometrySetLock()
     */
    @Override
    public ReentrantLock getGeometrySetLock()
    {
        return myGeometrySetLock;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider#getHiddenGeometrySet()
     */
    @Override
    public Set<Geometry> getHiddenGeometrySet()
    {
        return myHiddenGeometrySet;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.AbstractMapDataElementTransformer#getIdSet()
     */
    @Override
    public TLongSet getIdSet()
    {
        return myIdSet;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider#getUpdateSource()
     */
    @Override
    public Object getUpdateSource()
    {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider#getUpdateTaskActivity()
     */
    @Override
    public TransformerGeomRegistryUpdateTaskActivity getUpdateTaskActivity()
    {
        return myGeomRegUpdateActivity;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.MapDataElementTransformer#hasGeometryForDataModelId(long)
     */
    @Override
    public boolean hasGeometryForDataModelId(long id)
    {
        return getIdSet().contains(id);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.MapDataElementTransformer#hasGeometryId(long)
     */
    @Override
    public boolean hasGeometryId(long geomId)
    {
        return hasGeometryForDataModelId(geomId);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider#isPublishUpdatesToGeometryRegistry()
     */
    @Override
    public boolean isPublishUpdatesToGeometryRegistry()
    {
        return PUBLISH_CHANGES_TO_GEOMETRY_REGISTRY;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.AbstractMapDataElementTransformer#removeMapDataElements(long[])
     */
    @Override
    public void removeMapDataElements(long[] ids)
    {
        executeIfNotShutdown(new RemoveDataElementsWorker(this, CollectionUtilities.listView(ids)));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.transformer.impl.AbstractMapDataElementTransformer#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();
        getExecutorService().execute(new ShutdownTransformerWorker(this));
        getExecutorService().shutdown();
    }

    /**
     * Execute if not shutdown.
     *
     * @param task the task
     */
    protected void executeIfNotShutdown(Runnable task)
    {
        if (!getExecutorService().isShutdown())
        {
            getExecutorService().execute(task);
        }
    }

    /**
     * Gets the value of the {@link #myLastColorChangeUpdateNumber} field.
     *
     * @return the value stored in the {@link #myLastColorChangeUpdateNumber}
     *         field.
     */
    public long getLastColorChangeUpdateNumber()
    {
        return myLastColorChangeUpdateNumber;
    }

    /**
     * Sets the value of the {@link #myLastColorChangeUpdateNumber} field.
     *
     * @param lastColorChangeUpdateNumber the value to store in the
     *            {@link #myLastColorChangeUpdateNumber} field.
     */
    public void setLastColorChangeUpdateNumber(long lastColorChangeUpdateNumber)
    {
        myLastColorChangeUpdateNumber = lastColorChangeUpdateNumber;
    }
}
