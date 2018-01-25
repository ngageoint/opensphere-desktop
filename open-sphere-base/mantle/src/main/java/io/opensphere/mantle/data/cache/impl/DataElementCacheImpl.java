package io.opensphere.mantle.data.cache.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.MemoryManager.MemoryListener;
import io.opensphere.core.MemoryManager.Status;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.metrics.impl.DefaultNumberMetricsProvider;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.core.util.rangeset.RangedLongSetFactory;
import io.opensphere.mantle.controller.event.impl.DataElementsAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataElementsRemovedEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.CacheConfiguration;
import io.opensphere.mantle.data.cache.CacheDataTypeQuery;
import io.opensphere.mantle.data.cache.CacheIdQuery;
import io.opensphere.mantle.data.cache.CacheQuery;
import io.opensphere.mantle.data.cache.CacheStoreType;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.dynmeta.DynamicDataElementMetadataManager;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataDataTypeController;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetadataManagerImpl;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.mdfilter.MetaDataFilter;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;
import io.opensphere.mantle.util.taskactivity.UseCounterUpdateTaskActivity;

/**
 * The Class DataElementCache.
 */
@SuppressWarnings("PMD.GodClass")
public class DataElementCacheImpl implements DataElementCache
{
    /** The Constant IDS_STRING. */
    private static final String IDS_STRING = "ids";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataElementCacheImpl.class);

    /** The Constant ourEventExecutor. */
    private static final Executor EVENT_EXECUTOR = new ProcrastinatingExecutor("DataElementCacheImpl:CacheCleaner", 500, 1000);

    /** The Constant myIdCounter. */
    private static final AtomicLong ID_COUNTER = new AtomicLong(0);

    /** The all id set. */
    private final RangedLongSet myAllIdSet;

    /** The cache assistant. */
    private CacheAssistant myCacheAssistant;

    /** The my cache configuration. */
    private final CacheConfiguration myCacheConfiguration;

    /** The Cache load filter manager. */
    private final CacheLoadFilterManager myCacheLoadFilterManager;

    /** The my cache ref map. */
    private final Map<Long, CacheEntry> myCacheRefMap;

    /** The data type to id map. */
    private final DataTypeToIdMap myDataTypeToIdMap;

    /** The Dynamic enumeration registry. */
    private final DynamicEnumerationRegistry myDynamicEnumerationRegistry;

    /** The Dynamic column manager. */
    private final DynamicMetadataManagerImpl myDynamicMetadataManager;

    /** The Feature count metrics provider. */
    private final DefaultNumberMetricsProvider myFeatureCountMetricsProvider;

    /** The Insert task activity. */
    private final UseCounterUpdateTaskActivity myInsertTaskActivity;

    /** The Memory listener. */
    private final MemoryListener myMemoryListener;

    /** The my toolbox. */
    private final Toolbox myToolbox;

    /** The Type count metrics provider. */
    private final DefaultNumberMetricsProvider myTypeCountMetricsProvider;

    /** The Use dynamic classes. */
    private final boolean myUseDynamicClasses;

    /**
     * Instantiates a new data element cache.
     *
     * @param tb the toolbox with which to interact with the rest of the
     *            application.
     * @param cacheConfiguration the cache configuration
     * @param dcm the dcm
     * @param deReg the de reg
     */
    public DataElementCacheImpl(Toolbox tb, CacheConfiguration cacheConfiguration, DynamicMetadataManagerImpl dcm,
            DynamicEnumerationRegistry deReg)
    {
        Utilities.checkNull(tb, "tb");
        Utilities.checkNull(cacheConfiguration, "cacheConfiguration");

        myToolbox = tb;
        myDynamicMetadataManager = dcm;
        myDynamicEnumerationRegistry = deReg;
        myDataTypeToIdMap = new DataTypeToIdMap();
        myCacheRefMap = New.concurrentMap();
        myAllIdSet = RangedLongSetFactory.newSet();
        myCacheConfiguration = cacheConfiguration;
        myUseDynamicClasses = myCacheConfiguration.isUseDynamicClassStorageForDataElements();
        myTypeCountMetricsProvider = new DefaultNumberMetricsProvider(0, "Features", "Cache", "Data Types");
        myFeatureCountMetricsProvider = new DefaultNumberMetricsProvider(0, "Features", "Cache", "Features");
        myToolbox.getMetricsRegistry().addMetricsProvider(myTypeCountMetricsProvider);
        myToolbox.getMetricsRegistry().addMetricsProvider(myFeatureCountMetricsProvider);

        // Initialize the dynamic compiler here.
        DynamicMetaDataClassRegistry.getInstance();

        if (myCacheConfiguration.getCacheStoreType() == CacheStoreType.DISK)
        {
            myCacheAssistant = new DiskCacheAssistant(myToolbox, this, myDynamicMetadataManager, myDynamicEnumerationRegistry,
                    myCacheConfiguration.getDiskCacheLocation(), myCacheConfiguration.isUseDiskEncryption());
        }
        else if (myCacheConfiguration.getCacheStoreType() == CacheStoreType.REGISTRY)
        {
            myCacheAssistant = new RegistryCacheAssistant(tb, this, myDynamicMetadataManager, myDynamicEnumerationRegistry);
        }
        myCacheLoadFilterManager = new CacheLoadFilterManager(deReg);
        myToolbox.getDataFilterRegistry().addListener(myCacheLoadFilterManager);
        myInsertTaskActivity = new UseCounterUpdateTaskActivity("Processing Features");
        myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(myInsertTaskActivity);
        myMemoryListener = (oldStatus, newStatus) -> invokeConditionalCacheClean(oldStatus, newStatus);
        myToolbox.getSystemToolbox().getMemoryManager().addMemoryListener(myMemoryListener);
    }

    /**
     * Checks if is map data element type.
     *
     * @param dti the layer for which to test the map element type.
     * @return true, if is map data element type
     */
    private static boolean isMapDataElmentType(DataTypeInfo dti)
    {
        return dti != null && dti.getMapVisualizationInfo() != null
                && dti.getMapVisualizationInfo().getVisualizationType().isMapDataElementType();
    }

    /**
     * Adds the data type.
     *
     * @param dti the data type to add to the cache.
     */
    public void addDataType(DataTypeInfo dti)
    {
        Utilities.checkNull(dti, "dti");
        myDataTypeToIdMap.addDataType(dti);
        myDynamicMetadataManager.addDataType(dti);
        myTypeCountMetricsProvider.setValue(myDataTypeToIdMap.typeCount());
        if (DynamicMetaDataClassRegistry.getInstance().canCompile())
        {
            DynamicMetaDataClassRegistry.getInstance().addOrAdjustDataType(dti);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getAllElementIdsAsArray()
     */
    @Override
    public long[] getAllElementIdsAsArray()
    {
        return CollectionUtilities.toLongArray(myCacheRefMap.keySet());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getAllElementIdsAsList()
     */
    @Override
    public List<Long> getAllElementIdsAsList()
    {
        return CollectionUtilities.listView(getAllElementIdsAsArray());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getAllElementIdsAsRangedLongSet()
     */
    @Override
    public RangedLongSet getAllElementIdsAsRangedLongSet()
    {
        return RangedLongSetFactory.immutableSet(RangedLongSetFactory.newSet(myAllIdSet));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getDataTypeInfoKey(long)
     */
    @Override
    public String getDataTypeInfoKey(long id)
    {
        String ts = null;
        CacheEntry ece = myCacheRefMap.get(id);
        if (ece != null)
        {
            ts = ece.getDataTypeKey();
        }
        return ts;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getDataTypeInfoKeys(java.util.List)
     */
    @Override
    public List<String> getDataTypeInfoKeys(List<Long> ids)
    {
        Utilities.checkNull(ids, IDS_STRING);
        if (ids.isEmpty())
        {
            return Collections.<String>emptyList();
        }

        List<String> tsList = New.list(ids.size());
        CacheEntry ece = null;
        for (Long id : ids)
        {
            ece = myCacheRefMap.get(id);
            if (ece != null)
            {
                tsList.add(ece.getDataTypeKey());
            }
            else
            {
                tsList.add(null);
            }
        }
        return tsList;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getDataTypeInfoKeys(long[])
     */
    @Override
    public List<String> getDataTypeInfoKeys(long[] ids)
    {
        return getDataTypeInfoKeys(CollectionUtilities.listView(ids));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getDataTypeInfoKeySet(java.util.List)
     */
    @Override
    public Set<String> getDataTypeInfoKeySet(List<Long> ids)
    {
        Utilities.checkNull(ids, IDS_STRING);
        if (ids.isEmpty())
        {
            return Collections.<String>emptySet();
        }

        Set<String> dtiKeySet = New.set();
        CacheEntry ece = null;
        for (Long id : ids)
        {
            ece = myCacheRefMap.get(id);
            if (ece != null)
            {
                dtiKeySet.add(ece.getDataTypeKey());
            }
        }
        return dtiKeySet;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getDirectAccessRetriever(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public DirectAccessRetriever getDirectAccessRetriever(DataTypeInfo type)
    {
        Utilities.checkNull(type, "type");
        if (myCacheAssistant != null)
        {
            return myCacheAssistant.getDirectAccessRetriever(type, myCacheRefMap, myDynamicMetadataManager);
        }
        return new DefaultDirectAccessRetriever(type, myCacheRefMap, myDynamicMetadataManager);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getDynamicColumnManager()
     */
    @Override
    public DynamicDataElementMetadataManager getDynamicColumnManager()
    {
        return myDynamicMetadataManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getElementCountForType(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public int getElementCountForType(DataTypeInfo type)
    {
        return type == null ? 0 : myDataTypeToIdMap.getElementCountForType(type);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getElementIdsForTypeAsArray(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public long[] getElementIdsForTypeAsArray(DataTypeInfo type)
    {
        return type == null ? new long[0] : myDataTypeToIdMap.getIdsForTypeAsArray(type);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getElementIdsForTypeAsList(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public List<Long> getElementIdsForTypeAsList(DataTypeInfo type)
    {
        return type == null ? Collections.<Long>emptyList() : myDataTypeToIdMap.getIdsForTypeAsList(type);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getElementIdsForTypeAsRangedLongSet(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public RangedLongSet getElementIdsForTypeAsRangedLongSet(DataTypeInfo type)
    {
        return type == null ? RangedLongSetFactory.emptyImmutableRangedLongSet()
                : myDataTypeToIdMap.getIdsForTypeAsRangedLongSet(type);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getPreferredInsertBlockSize()
     */
    @Override
    public int getPreferredInsertBlockSize()
    {
        int preferredInsertBlockSize = -1;

        if (myCacheAssistant != null)
        {
            preferredInsertBlockSize = myCacheAssistant.getPreferredInsertBlockSize();
        }

        if (preferredInsertBlockSize == -1)
        {
            preferredInsertBlockSize = 50000;
        }
        if (preferredInsertBlockSize > 100000)
        {
            preferredInsertBlockSize = 100000;
        }

        return preferredInsertBlockSize;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getTimeSpan(long)
     */
    @Override
    public TimeSpan getTimeSpan(long id)
    {
        TimeSpan ts = null;
        CacheEntry ece = myCacheRefMap.get(id);
        if (ece != null)
        {
            ts = ece.getTime();
        }
        return ts;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getTimeSpans(java.util.Collection)
     */
    @Override
    public List<TimeSpan> getTimeSpans(Collection<? extends Long> ids)
    {
        Utilities.checkNull(ids, IDS_STRING);
        if (ids.isEmpty())
        {
            return Collections.<TimeSpan>emptyList();
        }

        List<TimeSpan> tsList = New.list(ids.size());
        CacheEntry ece = null;
        for (Long id : ids)
        {
            ece = myCacheRefMap.get(id);
            if (ece != null)
            {
                tsList.add(ece.getTime());
            }
            else
            {
                tsList.add(null);
            }
        }
        return tsList;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getTimeSpans(long[])
     */
    @Override
    public List<TimeSpan> getTimeSpans(long[] ids)
    {
        return getTimeSpans(CollectionUtilities.listView(ids));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getTypesWithElements()
     */
    @Override
    public Set<DataTypeInfo> getTypesWithElements()
    {
        return myDataTypeToIdMap.getTypesWithElements();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getVisualizationState(long)
     */
    @Override
    public VisualizationState getVisualizationState(long id)
    {
        VisualizationState ts = null;
        CacheEntry ece = myCacheRefMap.get(id);
        if (ece != null)
        {
            ts = ece.getVisState();
        }
        return ts;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getVisualizationStates(java.util.List)
     */
    @Override
    public List<VisualizationState> getVisualizationStates(List<Long> ids)
    {
        Utilities.checkNull(ids, IDS_STRING);
        if (ids.isEmpty())
        {
            return Collections.<VisualizationState>emptyList();
        }

        List<VisualizationState> tsList = New.list(ids.size());
        CacheEntry ece = null;
        for (Long id : ids)
        {
            ece = myCacheRefMap.get(id);
            if (ece != null)
            {
                tsList.add(ece.getVisState());
            }
            else
            {
                tsList.add(null);
            }
        }
        return tsList;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#getVisualizationStates(long[])
     */
    @Override
    public List<VisualizationState> getVisualizationStates(long[] ids)
    {
        return getVisualizationStates(CollectionUtilities.listView(ids));
    }

    /**
     * Inserts a new category into the cache.
     *
     * @param category the category
     * @param source the source
     * @param elements the elements
     * @return the long[]
     * @throws CacheException the cache exception
     */
    @SuppressWarnings("PMD.LooseCoupling")
    public long[] insert(String category, String source, Collection<? extends DataElement> elements) throws CacheException
    {
        long[] resultIds = null;
        getUseCounterUpdateTaskActivity().incrementUseCounter();
        try
        {
            long start = System.nanoTime();
            resultIds = insertInternal(category, source, elements);
            int numResults = resultIds == null ? 0 : resultIds.length;
            if (resultIds != null && resultIds.length == elements.size())
            {
                int index = 0;
                for (DataElement element : elements)
                {
                    element.setIdInCache(resultIds[index]);
                    index++;
                }
            }
            else
            {
                LOGGER.warn("Cache Ids size does not match elements size.");
            }
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(StringUtilities.formatTimingMessage("Inserted " + numResults + " elements into cache in ",
                        System.nanoTime() - start));
            }
        }
        finally
        {
            getUseCounterUpdateTaskActivity().decrementUseCounter();
        }
        return resultIds;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#query(io.opensphere.mantle.data.cache.CacheDataTypeQuery)
     */
    @Override
    public void query(CacheDataTypeQuery dtQuery)
    {
        Utilities.checkNull(dtQuery, "dtQuery");
        RangedLongSet queryIds = RangedLongSetFactory.newSet();
        if (dtQuery.getDataTypeKeyFilters() != null)
        {
            for (String dtiKey : dtQuery.getDataTypeKeyFilters())
            {
                DataTypeInfo dti = getDTIFromKey(dtiKey);
                if (dti != null)
                {
                    myDataTypeToIdMap.addIdsForTypeToRangedLongSet(dti, queryIds);
                }
            }
        }
        processQuery(queryIds, dtQuery, !currentMemoryStatusIsWarnOrCritical());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#query(io.opensphere.mantle.data.cache.CacheIdQuery)
     */
    @Override
    public void query(CacheIdQuery idQuery)
    {
        Utilities.checkNull(idQuery, "idQuery");
        if (idQuery.getFilterIds() == null || idQuery.getFilterIds().isEmpty())
        {
            throw new IllegalArgumentException("Ids for query cannot be null or empty");
        }

        processQuery(RangedLongSetFactory.newSet(idQuery.getFilterIds()), idQuery, !currentMemoryStatusIsWarnOrCritical());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.DataElementCache#query(io.opensphere.mantle.data.cache.CacheQuery)
     */
    @Override
    public void query(CacheQuery query)
    {
        Utilities.checkNull(query, "query");
        RangedLongSet queryIds = RangedLongSetFactory.newSet(myAllIdSet);
        processQuery(queryIds, query, !currentMemoryStatusIsWarnOrCritical());
    }

    /**
     * Removes the Identified IDs from the cache.
     *
     * @param ids the IDs to remove from the cache.
     * @return the number removed
     */
    public int remove(Collection<Long> ids)
    {
        long start = System.nanoTime();
        Utilities.checkNull(ids, IDS_STRING);
        int removedCount = 0;
        Map<String, DataTypeInfo> typeKeyToTypeMap = New.map();
        Map<String, List<Long>> typeToIdList = New.map();
        CacheEntry ref = null;
        processIdsForRemove(ids, typeKeyToTypeMap, typeToIdList);

        // Remove the ids from the data type sets and clean up the dynamic
        // metadata if any exists for those cache ids.
        for (Map.Entry<String, List<Long>> entry : typeToIdList.entrySet())
        {
            myDataTypeToIdMap.removeIdsFromTypeSet(typeKeyToTypeMap.get(entry.getKey()), entry.getValue());

            DynamicMetadataDataTypeController dynMetaCtror = myDynamicMetadataManager.getController(entry.getKey());
            if (dynMetaCtror != null)
            {
                dynMetaCtror.clearValues(entry.getValue(), this);
            }
        }
        myAllIdSet.remove(ids);
        List<Long> idsToRemoveFromStore = New.linkedList();
        List<CacheReference> refsToRemoveFromStore = New.linkedList();
        for (Long id : ids)
        {
            synchronized (myCacheRefMap)
            {
                ref = myCacheRefMap.remove(id);
            }
            if (ref != null)
            {
                removedCount++;
                if (ref.getCacheReference() != null)
                {
                    idsToRemoveFromStore.add(id);
                    refsToRemoveFromStore.add(ref.getCacheReference());
                }
            }
        }
        myFeatureCountMetricsProvider.setValue(myCacheRefMap.size());
        if (myCacheConfiguration.isRemoveFromStoreOnRemove() && myCacheAssistant != null)
        {
            myCacheAssistant.removeElements(idsToRemoveFromStore, refsToRemoveFromStore);
        }

        // Fire remove events.
        for (Map.Entry<String, List<Long>> entry : typeToIdList.entrySet())
        {
            DataTypeInfo dti = typeKeyToTypeMap.get(entry.getKey());
            myToolbox.getEventManager()
                    .publishEvent(new DataElementsRemovedEvent(dti, entry.getValue(), isMapDataElmentType(dti), this));
        }

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.formatTimingMessage("Removed " + removedCount + " elements from cache in ",
                    System.nanoTime() - start));
        }
        return removedCount;
    }

    /**
     * Removes the data type.
     *
     * @param dti the dti
     */
    public void removeDataType(DataTypeInfo dti)
    {
        if (dti != null)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Remove Data Type Started At: " + System.currentTimeMillis());
            }
            remove(myDataTypeToIdMap.getIdsForTypeAsRangedLongSet(dti));
            myDataTypeToIdMap.removeDataType(dti);
            myDynamicMetadataManager.removeDataType(dti);
            myTypeCountMetricsProvider.setValue(myDataTypeToIdMap.typeCount());
            if (myCacheAssistant != null)
            {
                myCacheAssistant.dataTypeRemoved(dti);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        // @formatter:off
        sb.append("Data Element Cache Summary\n"
                + "   Cache Assistant Class: ").append(myCacheAssistant.getClass().getName()).append("\n"
                + "   ").append(myCacheConfiguration.toString().replace("\n", "\n  "));
        sb.append("====================================================================\n"
                + "  Total Cached Data Elements: ").append(myAllIdSet.size()).append("\n"
                + "   ").append(myDataTypeToIdMap.toString().replace("\n", "\n   "));
        sb.append("\n  ====================================================================\n");
        // @formatter:on
        return sb.toString();
    }

    /**
     * Cache assistant remove complete.
     *
     * @param removedCacheIds the removed cache ids
     */
    protected void cacheAssistantRemoveComplete(List<Long> removedCacheIds)
    {
        Utilities.checkNull(removedCacheIds, "removedCacheIds");
        for (Long id : removedCacheIds)
        {
            CacheEntry ref = myCacheRefMap.get(id);
            if (ref != null)
            {
                ref.setCacheReference(null);
            }
        }
    }

    /**
     * Cache assistant store complete.
     *
     * @param idToCRMap the id to cr map
     */
    protected void cacheAssistantStoreComplete(TLongObjectHashMap<CacheReference> idToCRMap)
    {
        Utilities.checkNull(idToCRMap, "idToLEDMap");

        idToCRMap.forEachEntry((key, value) -> updateCacheReference(key, value));

        EVENT_EXECUTOR.execute(() -> cleanCache(0));
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Store Complte for " + idToCRMap.size() + " elements at " + System.currentTimeMillis());
        }
        if (myToolbox.getSystemToolbox().getMemoryManager().getMemoryStatus() == Status.CRITICAL)
        {
            cleanCache(0);
        }
    }

    /**
     * Updates the cache reference for the supplied key.
     *
     * @param key the key for which to update the cache reference.
     * @param value the reference to which to update the cache.
     * @return true if the reference was updated.
     */
    protected boolean updateCacheReference(long key, CacheReference value)
    {
        CacheEntry ref = myCacheRefMap.get(key);
        if (ref != null)
        {
            ref.setCacheReference(value);
        }
        return true;
    }

    /**
     * Actually clean the cache.
     *
     * @param reserveCount The reserve count.
     * @param maxPool The maximum pool size.
     */
    final void doCleanCache(int reserveCount, int maxPool)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Starting Cache Cleaner: Total Entries: " + myCacheRefMap.size() + " Max Pool: " + maxPool);
        }
        int maxToClean = myCacheRefMap.size() + reserveCount - maxPool;
        int cleanedCount = 0;
        long start = System.nanoTime();

        List<CacheEntryLUWrapper> removeCandidates = New.list(maxToClean);
        int inMemory = 0;
        int alreadyCached = 0;
        int notCached = 0;

        CleanScanProcedure procedure = new CleanScanProcedure(removeCandidates);
        List<CacheEntry> entryList = New.list(myCacheRefMap.values());
        for (CacheEntry entry : entryList)
        {
            if (entry != null && !procedure.execute(entry))
            {
                break;
            }
        }
        inMemory = procedure.getInMemory();
        alreadyCached = procedure.getAlreadyCached();
        notCached = procedure.getNotCached();

        int numToClean = maxToClean - alreadyCached;
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Need to clean max of " + maxToClean + " found " + alreadyCached + " already cached, need " + numToClean
                    + " more, total in mem " + inMemory);
        }

        if (numToClean > 0)
        {
            // Sort by LRU.
            Collections.sort(removeCandidates);

            for (CacheEntryLUWrapper ce : removeCandidates)
            {
                ce.getEntry().setLoadedElementData(null);
                cleanedCount++;
                if (cleanedCount >= numToClean)
                {
                    break;
                }
            }
        }
        removeCandidates.clear();

        if (LOGGER.isTraceEnabled())
        {
            long end = System.nanoTime();

            int numNotInMemory = alreadyCached + cleanedCount;
            int numInMemory = notCached + inMemory - cleanedCount;

            // @formatter:off
            LOGGER.trace(StringUtilities
                    .formatTimingMessage("Clean complete.\nCleaned " + cleanedCount + " in ", end - start)
                    + " Total Count: "
                    + myCacheRefMap.size()
                    + " Max Pool: "
                    + maxPool
                    + " NumInMem: "
                    + numInMemory
                    + " NotInMem: "
                    + numNotInMemory);
            // @formatter:on
        }
    }

    /**
     * Get the dynamic maximum pool size.
     *
     * @param memStatus The memory status.
     * @return The number of elements allowed.
     */
    final int getMaxPool(Status memStatus)
    {
        int maxPool = myCacheConfiguration.getInMemoryPoolSize();
        if (memStatus == Status.CRITICAL)
        {
            maxPool = 100000;
        }
        else if (memStatus == Status.WARNING)
        {
            maxPool = maxPool / 2 < 100000 ? 100000 : maxPool / 2;
        }
        return maxPool;
    }

    /**
     * Clean cache if necessary.
     *
     * @param reserveCount the reserveCount
     */
    protected final void cleanCache(int reserveCount)
    {
        if (myCacheConfiguration.isUnlimited())
        {
            return;
        }

        int maxPool = getMaxPool(myToolbox.getSystemToolbox().getMemoryManager().getMemoryStatus());

        if (myCacheRefMap.size() + reserveCount > maxPool)
        {
            doCleanCache(reserveCount, maxPool);
        }
        else
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("No Clean Required only " + myCacheRefMap.size() + " entries in memory.  Preferred is : " + maxPool);
            }
        }
    }

    /**
     * Cleans the cache if the new status is {@link Status#CRITICAL}, or if the
     * old status is {@link Status#WARNING} and the new status is
     * {@link Status#NOMINAL}.
     *
     * @param oldStatus the previous status of the cache.
     * @param newStatus the new status of the cache.
     */
    protected final void invokeConditionalCacheClean(Status oldStatus, Status newStatus)
    {
        if (newStatus == Status.CRITICAL || newStatus == Status.WARNING && oldStatus == Status.NOMINAL)
        {
            EVENT_EXECUTOR.execute(() -> cleanCache(0));
        }
    }

    /**
     * Current memory status is warn or critical.
     *
     * @return true, if successful
     */
    protected boolean currentMemoryStatusIsWarnOrCritical()
    {
        Status status = myToolbox.getSystemToolbox().getMemoryManager().getMemoryStatus();
        return status == Status.WARNING || status == Status.CRITICAL;
    }

    /**
     * Gets the dTI from key.
     *
     * @param dtiKey the dti key
     * @return the dTI from key
     */
    protected DataTypeInfo getDTIFromKey(String dtiKey)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController().getDataTypeInfoForType(dtiKey);
    }

    /**
     * Gets the use counter update task activity.
     *
     * @return the use counter update task activity
     */
    protected UseCounterUpdateTaskActivity getUseCounterUpdateTaskActivity()
    {
        return myInsertTaskActivity;
    }

    /**
     * Insert the supplied category into the cache.
     *
     * @param category the category
     * @param source the source
     * @param elements the elements
     * @return the long[]
     * @throws CacheException the cache exception
     */
    @SuppressWarnings("PMD.LooseCoupling")
    protected long[] insertInternal(String category, String source, Collection<? extends DataElement> elements)
        throws CacheException
    {
        long[] resultIds = null;
        Utilities.checkNull(elements, "elements");
        if (!elements.isEmpty())
        {
            int size = myCacheRefMap.size();
            if (!myCacheConfiguration.isUnlimited() && size + elements.size() >= myCacheConfiguration.getMaxInMemory())
            {
                throw new CacheException("Insert will exceed max capacity; could not insert elements.");
            }
            resultIds = reserveIdsAsArray(elements.size());
            cleanCache(resultIds.length);
            Map<String, DataTypeInfo> typeKeyToTypeMap = New.map();
            Map<String, TLongList> typeToIdList = New.map();
            Map<String, LinkedList<CacheEntry>> typeToCeList = New.map();

            int index = 0;
            for (DataElement de : elements)
            {
                MetaDataFilter mdf = myCacheLoadFilterManager.getFilter(de.getDataTypeInfo(), true);
                if (mdf != null && !mdf.accepts(de))
                {
                    resultIds[index] = DataElement.FILTERED;
                }
                else
                {
                    CacheEntry ce = new CacheEntry(myDynamicEnumerationRegistry, de, myUseDynamicClasses);
                    synchronized (myCacheRefMap)
                    {
                        myCacheRefMap.put(resultIds[index], ce);
                    }

                    TLongList idList = typeToIdList.get(de.getDataTypeInfo().getTypeKey());
                    if (idList == null)
                    {
                        idList = new TLongArrayList();
                        typeToIdList.put(de.getDataTypeInfo().getTypeKey(), idList);
                        typeKeyToTypeMap.put(de.getDataTypeInfo().getTypeKey(), de.getDataTypeInfo());
                    }

                    LinkedList<CacheEntry> ceList = typeToCeList.get(de.getDataTypeInfo().getTypeKey());
                    if (ceList == null)
                    {
                        ceList = new LinkedList<>();
                        typeToCeList.put(de.getDataTypeInfo().getTypeKey(), ceList);
                        typeKeyToTypeMap.put(de.getDataTypeInfo().getTypeKey(), de.getDataTypeInfo());
                    }

                    ceList.add(ce);
                    idList.add(resultIds[index]);
                }
                index++;
            }
            myAllIdSet.addAll(resultIds);
            myAllIdSet.remove(DataElement.FILTERED);
            myFeatureCountMetricsProvider.setValue(myCacheRefMap.size());

            for (Map.Entry<String, TLongList> entry : typeToIdList.entrySet())
            {
                DataTypeInfo dti = typeKeyToTypeMap.get(entry.getKey());
                myDataTypeToIdMap.addIdsToTypeSet(dti, entry.getValue());
                myToolbox.getEventManager().publishEvent(
                        new DataElementsAddedEvent(dti, entry.getValue().toArray(), isMapDataElmentType(dti), this));

                // Start the far caching process.
                if (myCacheAssistant != null)
                {
                    myCacheAssistant.cacheElements(category, source, entry.getValue(), dti, typeToCeList.get(entry.getKey()));
                }
            }
        }
        return resultIds;
    }

    /**
     * Process IDs for remove.
     *
     * @param ids the IDs
     * @param typeKeyToTypeMap the type key to type map
     * @param typeToIdList the type to id list
     */
    protected void processIdsForRemove(Collection<Long> ids, Map<String, DataTypeInfo> typeKeyToTypeMap,
            Map<String, List<Long>> typeToIdList)
    {
        CacheEntry ref;
        for (Long id : ids)
        {
            ref = myCacheRefMap.get(id);
            if (ref != null)
            {
                DataTypeInfo dti = getDTIFromKey(ref.getDataTypeKey());
                if (dti != null)
                {
                    List<Long> typeIdList = typeToIdList.get(dti.getTypeKey());
                    if (typeIdList == null)
                    {
                        typeIdList = New.linkedList();
                        typeToIdList.put(dti.getTypeKey(), typeIdList);
                        typeKeyToTypeMap.put(dti.getTypeKey(), dti);
                    }
                    typeIdList.add(id);
                }
            }
        }
    }

    /**
     * Process query.
     *
     * @param queryIds the query IDs
     * @param query the query
     * @param warmCache the warm cache
     */
    protected void processQuery(final RangedLongSet queryIds, final CacheQuery query, final boolean warmCache)
    {
        processQueryDirect(queryIds, query, warmCache);
    }

    /**
     * Process query directly, rather than going through the executor queue.
     *
     * @param queryIds the query IDs
     * @param query the query
     * @param warmCache the warm cache
     */
    protected void processQueryDirect(RangedLongSet queryIds, CacheQuery query, boolean warmCache)
    {
        long start = System.nanoTime();
        Iterator<Long> idItr = queryIds.iterator();
        List<Long> retrieveIdList = New.linkedList();
        List<CacheEntry> retrieveList = New.linkedList();

        Long currentId = null;
        CacheEntry rec = null;
        int numInMem = 0;
        CacheEntryViewProxy cevProxy = new CacheEntryViewProxy(myDynamicMetadataManager, myDynamicEnumerationRegistry);
        while (idItr.hasNext() && !query.isComplete())
        {
            currentId = idItr.next();
            rec = myCacheRefMap.get(currentId);
            if (rec == null)
            {
                query.notFound(currentId);
            }
            else
            {
                cevProxy.setParts(currentId, rec, rec.getLoadedElementData());
                if (query.intersectsTimesOfInterest(cevProxy))
                {
                    if (query.needsRetrieve(cevProxy))
                    {
                        retrieveIdList.add(currentId);
                        retrieveList.add(rec);
                    }
                    else
                    {
                        numInMem++;
                        if (query.acceptsInternal(cevProxy))
                        {
                            rec.setLastUsedTime(System.currentTimeMillis());
                            query.processInternal(currentId, cevProxy);
                        }
                    }
                }
            }
        }

        retrieveAndUpdate(query, warmCache, retrieveIdList, retrieveList);

        // Finalize the query.
        query.finalizeQueryInternal();

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.formatTimingMessage(
                    "Processed Cache Query for " + queryIds.size() + " elements[" + numInMem + "] in ",
                    System.nanoTime() - start));
        }
    }

    /**
     * Reserve IDs as array.
     *
     * @param numToReserve the number of IDs to reserve
     * @return a long[] array of IDs reserved within the cache.
     */
    protected long[] reserveIdsAsArray(int numToReserve)
    {
        if (numToReserve <= 0)
        {
            throw new IllegalArgumentException("Num to reserve cannot be <= 0");
        }
        long[] result = new long[numToReserve];
        for (int i = 0; i < numToReserve; i++)
        {
            result[i] = ID_COUNTER.incrementAndGet();
        }
        return result;
    }

    /**
     * Retrieve and update the cache entries.
     *
     * @param query The query.
     * @param warmCache If the cache should be warmed.
     * @param retrieveIdList The IDs.
     * @param retrieveList The elements.
     */
    protected void retrieveAndUpdate(CacheQuery query, boolean warmCache, List<Long> retrieveIdList,
            List<CacheEntry> retrieveList)
    {
        if (!query.isComplete() && !retrieveList.isEmpty() && myCacheAssistant != null)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Query Requires Retrieve of " + retrieveIdList.size() + " LED");
            }
            if (warmCache)
            {
                cleanCache(retrieveIdList.size());
            }
            myCacheAssistant.retrieveAndUpdateElementCacheEntries(query, retrieveIdList, retrieveList, warmCache);
            if (warmCache)
            {
                EVENT_EXECUTOR.execute(() -> cleanCache(0));
            }
        }
    }
}
