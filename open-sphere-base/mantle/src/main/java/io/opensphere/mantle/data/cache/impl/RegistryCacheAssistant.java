package io.opensphere.mantle.data.cache.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultPropertyValueReceiver;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.CacheQuery;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.cache.Priority;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetaDataListViewProxy;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetadataManagerImpl;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.DynamicMetaDataList;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class RegistryCacheAssistant.
 */
@SuppressWarnings("PMD.GodClass")
public class RegistryCacheAssistant implements CacheAssistant
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RegistryCacheAssistant.class);

    /** The Constant MAX_REGISTRY_INSERT_BLOCK_SIZE. */
    private static final int MAX_REGISTRY_INSERT_BLOCK_SIZE = 50000;

    /** The Constant MAX_REGISTRY_REMOVE_BLOCK_SIZE. */
    private static final int MAX_REGISTRY_REMOVE_BLOCK_SIZE = 50000;

    /** The Constant ourDBExecutorService. */
    private static final ThreadPoolExecutor DB_EXECUTOR_SERVICE = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS,
            new PriorityBlockingQueue<>(10, new RunnablePriorityComparator()),
            new NamedThreadFactory("DataElementCache:DBWorker"));

    /** The data element cache. */
    private final DataElementCacheImpl myDataElementCache;

    /** The Dynamic column manager. */
    private final DynamicMetadataManagerImpl myDynamicColumnManager;

    /** The Dynamic enumeration registry. */
    private final DynamicEnumerationRegistry myDynamicEnumerationRegistry;

    /** The my toolbox. */
    private final Toolbox myToolbox;

    static
    {
        DB_EXECUTOR_SERVICE.allowCoreThreadTimeOut(true);
    }

    /**
     * Checks if is map data element.
     *
     * @param dti the dti
     * @return true, if is map data element
     */
    private static boolean isMapDataElement(DataTypeInfo dti)
    {
        return dti.getMapVisualizationInfo() != null && dti.getMapVisualizationInfo().getVisualizationType() != null
                && dti.getMapVisualizationInfo().getVisualizationType().isMapDataElementType();
    }

    /**
     * Retrieve.
     *
     * @param tb the tb
     * @param registryId the registry id
     * @param dti the dti
     * @param getOriginId the get origin id
     * @param getMetaData the get meta data
     * @param getMapGeometrySupport the get map geometry support
     * @return the retrieve result
     * @throws DataElementLookupException the data element lookup exception
     */
    private static RegistryCacheRetrieveResult retrieve(Toolbox tb, long registryId, DataTypeInfo dti, boolean getOriginId,
            boolean getMetaData, boolean getMapGeometrySupport) throws DataElementLookupException
    {
        List<RegistryCacheRetrieveResult> deList = new ArrayList<>(1);
        Retriever r = new Retriever(deList, tb, new long[] { registryId }, dti, getOriginId, getMetaData, getMapGeometrySupport);
        int num = r.retrieve();
        if (num >= 1)
        {
            return deList.get(0);
        }
        return null;
    }

    /**
     * Disk cache assistant.
     *
     * @param tb the tb
     * @param dec the dec
     * @param dynamicColumnManager the dynamic column manager
     * @param deReg the de reg
     */
    public RegistryCacheAssistant(Toolbox tb, DataElementCacheImpl dec, DynamicMetadataManagerImpl dynamicColumnManager,
            DynamicEnumerationRegistry deReg)
    {
        myToolbox = tb;
        myDataElementCache = dec;
        myDynamicColumnManager = dynamicColumnManager;
        myDynamicEnumerationRegistry = deReg;
        myToolbox.getDataRegistry().addClassProvider(DynamicMetaDataClassRegistry.getInstance());
    }

    @Override
    public void cacheElement(String source, String category, long id, DataTypeInfo type, CacheEntry ce)
    {
        DB_EXECUTOR_SERVICE.execute(new AddCacheEntryElementWorker(category, source, type, new TLongArrayList(new long[] { id }),
                Collections.singletonList(ce)));
    }

    @Override
    @SuppressWarnings("PMD.LooseCoupling")
    public void cacheElements(String source, String category, TLongList ids, DataTypeInfo type, LinkedList<CacheEntry> ceList)
    {
        TLongList mdeInsertIds = new TLongArrayList();
        List<CacheEntry> mdeInsertList = new LinkedList<>();

        TLongIterator idItr = ids.iterator();
        Iterator<CacheEntry> deItr = ceList.iterator();
        long curId;
        CacheEntry curEl = null;
        while (idItr.hasNext() && deItr.hasNext())
        {
            curId = idItr.next();
            curEl = deItr.next();
            deItr.remove();
            mdeInsertIds.add(curId);
            mdeInsertList.add(curEl);

            if (mdeInsertIds.size() >= MAX_REGISTRY_INSERT_BLOCK_SIZE)
            {
                DB_EXECUTOR_SERVICE.execute(new AddCacheEntryElementWorker(category, source, type, mdeInsertIds, mdeInsertList));
                mdeInsertIds = new TLongArrayList();
                mdeInsertList = new LinkedList<>();
            }
        }

        if (!mdeInsertIds.isEmpty())
        {
            DB_EXECUTOR_SERVICE.execute(new AddCacheEntryElementWorker(category, source, type, mdeInsertIds, mdeInsertList));
        }
    }

    @Override
    public void dataTypeRemoved(DataTypeInfo dti)
    {
        // intentionally blank
    }

    @Override
    public DirectAccessRetriever getDirectAccessRetriever(DataTypeInfo dti, Map<Long, CacheEntry> cacheRefMap,
            DynamicMetadataManagerImpl dcm)
    {
        return new RegistryDirectAccessRetriever(dti, cacheRefMap, dcm);
    }

    @Override
    public int getPreferredInsertBlockSize()
    {
        return MAX_REGISTRY_INSERT_BLOCK_SIZE;
    }

    @Override
    public void removeElement(long cacheId, CacheReference ref)
    {
        DB_EXECUTOR_SERVICE.execute(
                new RemoveElementsWorker(Collections.singletonList(Long.valueOf(cacheId)), Collections.singletonList(ref)));
    }

    @Override
    public void removeElements(List<Long> cacheIds, List<CacheReference> refs)
    {
        List<Long> idBatchToRemove = new LinkedList<>();
        List<CacheReference> refBatchToRemove = new LinkedList<>();
        Iterator<Long> idItr = cacheIds.iterator();
        Iterator<CacheReference> refItr = refs.iterator();
        while (idItr.hasNext())
        {
            idBatchToRemove.add(idItr.next());
            refBatchToRemove.add(refItr.next());

            if (idBatchToRemove.size() == MAX_REGISTRY_REMOVE_BLOCK_SIZE)
            {
                DB_EXECUTOR_SERVICE.execute(new RemoveElementsWorker(idBatchToRemove, refBatchToRemove));
                idBatchToRemove = new LinkedList<>();
                refBatchToRemove = new LinkedList<>();
            }
        }
        if (!idBatchToRemove.isEmpty())
        {
            DB_EXECUTOR_SERVICE.execute(new RemoveElementsWorker(idBatchToRemove, refBatchToRemove));
        }
    }

    @Override
    public void retrieveAndUpdateElementCacheEntries(CacheQuery query, List<Long> cacheIds, List<CacheEntry> entries,
            boolean updateEntries)
    {
        Future<?> future = DB_EXECUTOR_SERVICE.submit(new RetrieveAndUpdateWorker(query, cacheIds, entries, updateEntries));
        try
        {
            future.get();
        }
        catch (InterruptedException e)
        {
            LOGGER.error(e);
        }
        catch (ExecutionException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * The Class AddCacheEntryElementWorker.
     */
    private class AddCacheEntryElementWorker implements Priority
    {
        /** The my category. */
        private final String myCategory;

        /** The my dti. */
        private final DataTypeInfo myDti;

        /** The my element cache ids. */
        private final TLongList myElementCacheIds;

        /** The my data elements. */
        private final List<CacheEntry> myElements;

        /** The my source. */
        private final String mySource;

        /** The my use dynamic meta data. */
        private boolean myUseDynamicMetaData;

        /**
         * Instantiates a new adds the data element worker.
         *
         * @param category the category
         * @param source the source
         * @param dti the dti
         * @param elementIds the element ids
         * @param dataElements the data elements
         */
        public AddCacheEntryElementWorker(String category, String source, DataTypeInfo dti, TLongList elementIds,
                List<CacheEntry> dataElements)
        {
            mySource = source;
            myCategory = category;
            myElements = dataElements;
            myElementCacheIds = elementIds;
            myDti = dti;
            if (DynamicMetaDataClassRegistry.getInstance().canCompile()
                    && DynamicMetaDataClassRegistry.getInstance().getLatestDynamicClassForDataTypeKey(dti.getTypeKey()) != null)
            {
                myUseDynamicMetaData = true;
            }
        }

        /**
         * Creates the cache reference.
         *
         * @param regId the reg id
         * @param element the element
         * @return the registry cache reference
         */
        public RegistryCacheReference createCacheReference(long regId, CacheEntry element)
        {
            RegistryCacheReference ref = new RegistryCacheReference(regId);
            ref.setOriginIdCached(true);
            if (element.getLoadedElementData().getMetaData() != null)
            {
                ref.setMetaDataInfoCached(true);
            }
            if (element.getLoadedElementData().getMapGeometrySupport() != null)
            {
                ref.setMapGeometrySupportCached(true);
            }
            return ref;
        }

        /**
         * Creates the deposit.
         *
         * @param dynamicMetadata the dynamic metadata
         * @return the cache deposit
         */
        public CacheDeposit<CacheEntry> createDeposit(boolean dynamicMetadata)
        {
            final Collection<PropertyAccessor<? super CacheEntry, ?>> accessors = new ArrayList<>();
            accessors.add(new CacheEntryOriginIdSerializableAccessor());
            accessors.add(new CacheEntryMapGeometrySupportSerializableAccessor());
            if (dynamicMetadata)
            {
                accessors.add(new CacheEntryDynamicMetaDataListSerializableAccessor());
            }
            else
            {
                accessors.add(new CacheEntryPropertyListAccessor(getDti()));
            }
            CacheDeposit<CacheEntry> deposit = new DefaultCacheDeposit<>(
                    new DataModelCategory(getSource(), CacheEntry.class.getName(), getCategory()), accessors, getElements(), true,
                    CacheDeposit.SESSION_END, true);
            return deposit;
        }

        /**
         * Gets the category.
         *
         * @return the category
         */
        public String getCategory()
        {
            return myCategory;
        }

        /**
         * Gets the dti.
         *
         * @return the dti
         */
        public DataTypeInfo getDti()
        {
            return myDti;
        }

        /**
         * Gets the elements.
         *
         * @return the elements
         */
        public Collection<CacheEntry> getElements()
        {
            return myElements;
        }

        @Override
        public int getPriority()
        {
            return 2;
        }

        /**
         * Gets the source.
         *
         * @return the source
         */
        public String getSource()
        {
            return mySource;
        }

        @Override
        public void run()
        {
            long[] ids = null;
            long start = System.nanoTime();
            CacheDeposit<CacheEntry> deposit = createDeposit(myUseDynamicMetaData);

            // First create the records and reserve the ids.
            ids = myToolbox.getDataRegistry().addModels(deposit, RegistryCacheAssistant.this);

            // Now insert the components with an update.
            // Collection<? extends CacheModificationReport> cacheModReports =
            // myToolbox.getDataRegistry().updateModels(ids,
            // myElements, createUpdatePropertyAccessors(),
            // RegistryCacheAssistant.this, false);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                        StringUtilities.formatTimingMessage("Insert of Elements To Registry Took: ", System.nanoTime() - start)
                                + " for " + myElements.size() + " records");
            }

            // Build the cache references
            TLongObjectHashMap<CacheReference> idToCRMap = new TLongObjectHashMap<>();

            TLongIterator cacheIdItr = myElementCacheIds.iterator();
            long curCacheId;
            Iterator<CacheEntry> elemItr = myElements.iterator();
            CacheEntry currElem = null;
            for (int i = 0; i < ids.length; i++)
            {
                currElem = elemItr.next();
                curCacheId = cacheIdItr.next();
                idToCRMap.put(curCacheId, createCacheReference(ids[i], currElem));
            }
            myDataElementCache.cacheAssistantStoreComplete(idToCRMap);
        }
    }

    /**
     * The Class RegistryDirectAccessRetriever.
     */
    private class RegistryDirectAccessRetriever extends DefaultDirectAccessRetriever
    {
        /**
         * Instantiates a new registry direct access retriever.
         *
         * @param dti the dti
         * @param cacheRefMap the cache ref map
         * @param dcm the dcm
         */
        public RegistryDirectAccessRetriever(DataTypeInfo dti, Map<Long, CacheEntry> cacheRefMap, DynamicMetadataManagerImpl dcm)
        {
            super(dti, cacheRefMap, dcm);
        }

        @Override
        public MapGeometrySupport getMapGeometrySupport(long cacheId)
        {
            MapGeometrySupport mgs = null;
            CacheEntry ce = getCacheEntry(cacheId);
            if (ce != null)
            {
                mgs = extractMGSFromEntryIfAvailable(ce);
                if (mgs == null && ce.isMetaDataInfoCached())
                {
                    mgs = (MapGeometrySupport)retrieveValue(ce, RegistryCacheFetchType.MAP_GEOMETRY_SUPPORT);
                }
            }
            return mgs;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Object> getMetaData(long cacheId)
        {
            List<Object> result = null;
            CacheEntry ce = getCacheEntry(cacheId);
            if (ce != null)
            {
                result = extractMetaDataFromEntryIfAvailable(ce);
                result = result == null ? null : DynamicEnumDecoder.decode(myDynamicEnumerationRegistry, result);
                if (result == null && ce.isMetaDataInfoCached())
                {
                    result = (List<Object>)retrieveValue(ce, RegistryCacheFetchType.META_DATA);
                    result = result == null ? null : DynamicEnumDecoder.decode(myDynamicEnumerationRegistry, result);
                    result = new DynamicMetaDataListViewProxy(cacheId, result, getDynamicColumnCoordinator());
                }
            }
            return result;
        }

        @Override
        public Long getOriginId(long cacheId)
        {
            Long originId = null;
            CacheEntry ce = getCacheEntry(cacheId);
            if (ce != null)
            {
                originId = extractOriginIdFromEntryIfAvailable(ce);
                if (originId == null && ce.isOriginIdCached())
                {
                    originId = (Long)retrieveValue(ce, RegistryCacheFetchType.ORIGIN_ID);
                }
            }
            return originId;
        }

        /**
         * Retrieve value.
         *
         * @param ce the ce
         * @param fetchType the fetch type
         * @return the object
         */
        private Object retrieveValue(CacheEntry ce, RegistryCacheFetchType fetchType)
        {
            Object result = null;
            RetrieveOneRecordWorker worker = new RetrieveOneRecordWorker(getDataType(), ce, fetchType, false);
            Future<?> aFuture = DB_EXECUTOR_SERVICE.submit(worker);
            try
            {
                aFuture.get(50, TimeUnit.MILLISECONDS);
                result = worker.getResult();
            }
            catch (InterruptedException | ExecutionException e)
            {
                LOGGER.error(e);
            }
            catch (TimeoutException e)
            {
                LOGGER.error("Timed out trying to retrieve " + fetchType + " for item of type " + ce.getDataTypeKey());
            }

            return result;
        }
    }

    /**
     * The Class RemoveElementsWorker.
     */
    private class RemoveElementsWorker implements Priority
    {
        /** The my cache ids to remove. */
        private final List<Long> myCacheIdsToRemove;

        /** The my refs to remove. */
        private final List<CacheReference> myRefsToRemove;

        /**
         * Instantiates a new removes the elements worker.
         *
         * @param cacheIds the cache ids
         * @param refsToRemove the refs to remove
         */
        public RemoveElementsWorker(List<Long> cacheIds, List<CacheReference> refsToRemove)
        {
            myCacheIdsToRemove = cacheIds;
            myRefsToRemove = refsToRemove;
        }

        @Override
        public int getPriority()
        {
            return 3;
        }

        @Override
        public void run()
        {
            if (myRefsToRemove != null && !myRefsToRemove.isEmpty())
            {
                List<RegistryCacheReference> rcrList = new LinkedList<>();
                for (CacheReference cr : myRefsToRemove)
                {
                    if (cr instanceof RegistryCacheReference)
                    {
                        rcrList.add((RegistryCacheReference)cr);
                    }
                }

                long[] idsToRemove = new long[rcrList.size()];
                int index = 0;
                for (RegistryCacheReference rcr : rcrList)
                {
                    idsToRemove[index] = rcr.getRegistryId();
                    index++;
                }
                long start = System.nanoTime();
                myToolbox.getDataRegistry().removeModels(idsToRemove, RegistryCacheAssistant.this);
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(StringUtilities.formatTimingMessage("Remove of Elements From Registry Took: ",
                            System.nanoTime() - start) + " for " + idsToRemove.length + " records");
                }

                myDataElementCache.cacheAssistantRemoveComplete(myCacheIdsToRemove);
            }
        }
    }

    /**
     * The Class RetrieveAndUpdateWorker.
     */
    private class RetrieveAndUpdateWorker implements Priority
    {
        /** The my cache ids. */
        private final List<Long> myCacheIds;

        /** The my entries. */
        private final List<CacheEntry> myEntries;

        /** The my query. */
        private final CacheQuery myQuery;

        /** The my update entries. */
        private final boolean myUpdateEntries;

        /**
         * Instantiates a new retrieve and update worker.
         *
         * @param query the query
         * @param cacheIds the cache ids
         * @param entries the entries
         * @param updateEntries the update entries
         */
        public RetrieveAndUpdateWorker(CacheQuery query, List<Long> cacheIds, List<CacheEntry> entries, boolean updateEntries)
        {
            myUpdateEntries = updateEntries;
            myQuery = query;
            myCacheIds = cacheIds;
            myEntries = entries;
        }

        @Override
        public int getPriority()
        {
            return 0;
        }

        @Override
        public void run()
        {
            TLongLongHashMap regIdToCacheIdMap = new TLongLongHashMap();
            TLongObjectHashMap<CacheEntry> regIdToCacheEntryMap = new TLongObjectHashMap<>();
            Map<String, List<CacheEntry>> dtToEntryListMap = new HashMap<>();
            Map<String, List<Long>> dtToRegIdListListMap = new HashMap<>();

            buildMaps(regIdToCacheIdMap, regIdToCacheEntryMap, dtToEntryListMap, dtToRegIdListListMap);

            CacheEntryViewProxy proxy = new CacheEntryViewProxy(myDynamicColumnManager, myDynamicEnumerationRegistry);
            LoadedElementData led = new LoadedElementData();
            for (Map.Entry<String, List<Long>> entry : dtToRegIdListListMap.entrySet())
            {
                List<RegistryCacheRetrieveResult> results = new LinkedList<>();
                long[] regIds = CollectionUtilities.toLongArray(entry.getValue());
                MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(myToolbox);
                DataTypeInfo dti = mtb.getDataTypeController().getDataTypeInfoForType(entry.getKey());
                try
                {
                    long start = System.nanoTime();
                    Retriever r = new Retriever(results, myToolbox, regIds, dti, myQuery.isRetrieveOriginId(),
                            myQuery.isRetrieveMetaDataProvider(), myQuery.isRetrieveMapGeometrySupport());
                    r.retrieve();

                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace(StringUtilities.formatTimingMessage("Lookup of Elements From Registry Took: ",
                                System.nanoTime() - start) + " for " + regIds.length + " records of type " + dti.getDisplayName()
                                + " Found: " + results.size());
                    }
                }
                catch (DataElementLookupException e)
                {
                    LOGGER.error(e);
                }

                long currTime = System.currentTimeMillis();
                for (RegistryCacheRetrieveResult rr : results)
                {
                    CacheEntry ece = regIdToCacheEntryMap.get(rr.getRegId());
                    if (ece != null)
                    {
                        led.setAll(rr.getOriginId(), rr.getMapGeometrySupport(), rr.getMetaData());
                        proxy.setParts(regIdToCacheIdMap.get(rr.getRegId()), ece, led);
                        if (myQuery.acceptsInternal(proxy))
                        {
                            if (myUpdateEntries)
                            {
                                ece.setLastUsedTime(currTime);
                            }
                            myQuery.processInternal(regIdToCacheIdMap.get(rr.getRegId()), proxy);

                            if (myQuery.isComplete())
                            {
                                break;
                            }
                        }

                        if (myUpdateEntries)
                        {
                            updateCacheEntry(ece, rr);
                        }
                    }
                }

                if (myQuery.isComplete())
                {
                    break;
                }
            }
        }

        /**
         * Builds the maps to support the queries.
         *
         * @param regIdToCacheIdMap the reg id to cache id map
         * @param regIdToCacheEntryMap the reg id to cache entry map
         * @param dtToEntryListMap the dt to entry list map
         * @param dtToRegIdListListMap the dt to reg id list list map
         */
        private void buildMaps(TLongLongHashMap regIdToCacheIdMap, TLongObjectHashMap<CacheEntry> regIdToCacheEntryMap,
                Map<String, List<CacheEntry>> dtToEntryListMap, Map<String, List<Long>> dtToRegIdListListMap)
        {
            Iterator<Long> idItr = myCacheIds.iterator();
            Iterator<CacheEntry> entryItr = myEntries.iterator();
            Long curId = null;
            CacheEntry ce = null;
            while (idItr.hasNext())
            {
                curId = idItr.next();
                ce = entryItr.next();

                if (ce.getCacheReference() instanceof RegistryCacheReference)
                {
                    RegistryCacheReference rcr = (RegistryCacheReference)ce.getCacheReference();
                    regIdToCacheEntryMap.put(rcr.getRegistryId(), ce);
                    regIdToCacheIdMap.put(rcr.getRegistryId(), curId);
                    List<CacheEntry> eceList = dtToEntryListMap.get(ce.getDataTypeKey());
                    if (eceList == null)
                    {
                        eceList = new LinkedList<>();
                        dtToEntryListMap.put(ce.getDataTypeKey(), eceList);
                    }
                    List<Long> idList = dtToRegIdListListMap.get(ce.getDataTypeKey());
                    if (idList == null)
                    {
                        idList = new LinkedList<>();
                        dtToRegIdListListMap.put(ce.getDataTypeKey(), idList);
                    }
                    idList.add(rcr.getRegistryId());
                    eceList.add(ce);
                }
            }
        }

        /**
         * Update cache entry.
         *
         * @param ece the ece
         * @param rr the rr
         */
        private void updateCacheEntry(CacheEntry ece, RegistryCacheRetrieveResult rr)
        {
            LoadedElementData led = ece.getLoadedElementData();
            if (led == null)
            {
                led = new LoadedElementData();
                ece.setLoadedElementData(led);
            }

            if (rr.getOriginId() != null)
            {
                led.setOriginId(rr.getOriginId());
            }

            if (rr.getMapGeometrySupport() != null)
            {
                led.setMapGeometrySupport(rr.getMapGeometrySupport());
            }

            if (rr.getMetaData() != null)
            {
                led.setMetaData(rr.getMetaData());
            }
        }
    }

    /**
     * The Class RetrieveOneRecordWorker.
     */
    private class RetrieveOneRecordWorker implements Priority
    {
        /** The my dti. */
        private final DataTypeInfo myDTI;

        /** The my entry. */
        private final CacheEntry myEntry;

        /** The my fetch type. */
        private final RegistryCacheFetchType myFetchType;

        /** The my reg id. */
        private long myRegId;

        /** The my result. */
        private Object myResult;

        /** The my warm cache. */
        private final boolean myWarmCache;

        /**
         * Instantiates a new retrieve one record worker.
         *
         * @param dti the dti
         * @param ce the ce
         * @param fetchType the fetch type
         * @param warmCache the warm cache
         */
        public RetrieveOneRecordWorker(DataTypeInfo dti, CacheEntry ce, RegistryCacheFetchType fetchType, boolean warmCache)
        {
            myDTI = dti;
            myEntry = ce;
            myFetchType = fetchType;
            myWarmCache = warmCache;
            if (ce.getCacheReference() instanceof RegistryCacheReference)
            {
                myRegId = ((RegistryCacheReference)ce.getCacheReference()).getRegistryId();
            }
        }

        @Override
        public int getPriority()
        {
            return 0;
        }

        /**
         * Gets the result.
         *
         * @return the result
         */
        public Object getResult()
        {
            return myResult;
        }

        @Override
        public void run()
        {
            try
            {
                RegistryCacheRetrieveResult rr = RegistryCacheAssistant.retrieve(myToolbox, myRegId, myDTI, myFetchType == RegistryCacheFetchType.ORIGIN_ID,
                        myFetchType == RegistryCacheFetchType.META_DATA, myFetchType == RegistryCacheFetchType.MAP_GEOMETRY_SUPPORT);
                if (rr != null)
                {
                    LoadedElementData led = myEntry.getLoadedElementData();
                    if (myWarmCache && led == null)
                    {
                        led = new LoadedElementData();
                        myEntry.setLoadedElementData(led);
                        myEntry.setLastUsedTime(System.currentTimeMillis());
                    }
                    if (myFetchType == RegistryCacheFetchType.META_DATA)
                    {
                        if (myWarmCache)
                        {
                            led.setMetaData(rr.getMetaData());
                        }
                        myResult = rr.getMetaData();
                    }
                    else if (myFetchType == RegistryCacheFetchType.MAP_GEOMETRY_SUPPORT)
                    {
                        if (myWarmCache)
                        {
                            led.setMapGeometrySupport(rr.getMapGeometrySupport());
                        }
                        myResult = rr.getMapGeometrySupport();
                    }
                    else if (myFetchType == RegistryCacheFetchType.ORIGIN_ID)
                    {
                        if (myWarmCache)
                        {
                            led.setOriginId(rr.getOriginId());
                        }
                        myResult = rr.getOriginId();
                    }
                }
            }
            catch (DataElementLookupException e)
            {
                LOGGER.error(e);
            }
        }
    }

    /**
     * The Class Retriever.
     */
    private static class Retriever
    {
        /** The my data element ids. */
        private final long[] myDataElementIds;

        /** The my de list. */
        private final List<RegistryCacheRetrieveResult> myDeList;

        /** The dti. */
        private final DataTypeInfo myDti;

        /** The my get map geometry support. */
        private final boolean myGetMapGeometrySupport;

        /** The my get meta data. */
        private final boolean myGetMetaData;

        /** The my get origin id. */
        private final boolean myGetOriginId;

        /** The my is map data element. */
        private final boolean myIsMapDataElement;

        /** The my is using dynamic meta data. */
        private final boolean myIsUsingDynamicMetaData;

        /** The my md dyn key rx. */
        private DefaultPropertyValueReceiver<DynamicMetaDataList> myMdDynKeyRx;

        /** The my md key rx. */
        private DefaultPropertyValueReceiver<Object[]> myMdKeyRx;

        /** The my mgs rx. */
        private DefaultPropertyValueReceiver<MapGeometrySupport> myMgsRx;

        /** The my orig id rx. */
        private DefaultPropertyValueReceiver<Long> myOrigIdRx;

        /** The my property value receivers. */
        private Collection<PropertyValueReceiver<?>> myPropertyValueReceivers;

        /** The my tb. */
        private final Toolbox myTb;

        /**
         * Instantiates a new retriever.
         *
         * @param deList the de list
         * @param tb the tb
         * @param dataElementIds the data element ids
         * @param dti the dti
         * @param getOriginId the get origin id
         * @param getMetaData the get meta data
         * @param getMapGeometrySupport the get map geometry support
         * @throws DataElementLookupException the data element lookup exception
         */
        public Retriever(List<RegistryCacheRetrieveResult> deList, Toolbox tb, long[] dataElementIds, DataTypeInfo dti, boolean getOriginId,
                boolean getMetaData, boolean getMapGeometrySupport) throws DataElementLookupException
        {
            Utilities.checkNull(deList, "deList");
            Utilities.checkNull(tb, "tb");
            if (dti == null)
            {
                throw new DataElementLookupException("Could not determine DataType");
            }

            myDeList = deList;
            myDti = dti;
            myTb = tb;
            myGetMetaData = getMetaData;
            myGetOriginId = getOriginId;
            myGetMapGeometrySupport = getMapGeometrySupport;
            myDataElementIds = dataElementIds;
            myIsMapDataElement = isMapDataElement(dti);
            myIsUsingDynamicMetaData = DynamicMetaDataClassRegistry.getInstance().canCompile()
                    && DynamicMetaDataClassRegistry.getInstance().getLatestDynamicClassForDataTypeKey(dti.getTypeKey()) != null;

            createRetrievers();
        }

        /**
         * Retrieve.
         *
         * @return the int
         */
        public int retrieve()
        {
            DefaultQuery dq = new DefaultQuery(DataModelCategory.EMPTY, myPropertyValueReceivers);
            int num = myTb.getDataRegistry().performLocalQuery(myDataElementIds, dq);

            for (int i = 0; i < num; i++)
            {
                List<Object> metaData = null;
                DynamicMetaDataList dynMetaData = null;
                MapGeometrySupport mgs = null;
                Long originId = null;

                if (myMdKeyRx != null)
                {
                    metaData = Arrays.asList(myMdKeyRx.getValues().get(i));
                }

                if (myMdDynKeyRx != null)
                {
                    dynMetaData = myMdDynKeyRx.getValues().get(i);
                }

                if (myOrigIdRx != null)
                {
                    originId = myOrigIdRx.getValues().get(i);
                }

                if (myGetMapGeometrySupport && myIsMapDataElement)
                {
                    mgs = myMgsRx.getValues().get(i);
                }

                myDeList.add(new RegistryCacheRetrieveResult(myDataElementIds[i], originId, myIsUsingDynamicMetaData ? dynMetaData : metaData,
                        mgs));
            }
            return num;
        }

        /**
         * Creates the retrievers.
         */
        private void createRetrievers()
        {
            myPropertyValueReceivers = new ArrayList<>(3);

            if (myGetOriginId)
            {
                myOrigIdRx = new DefaultPropertyValueReceiver<>(DataElement.DATA_ELEMENT_ORIGIN_ID_PROPERTY_DESCRIPTOR);
                myPropertyValueReceivers.add(myOrigIdRx);
            }

            if (myGetMetaData)
            {
                if (myIsUsingDynamicMetaData)
                {
                    myMdDynKeyRx = new DefaultPropertyValueReceiver<>(
                            CacheEntryDynamicMetaDataListSerializableAccessor.PROPERTY_DESCRIPTOR);
                    myPropertyValueReceivers.add(myMdDynKeyRx);
                }
                else
                {
                    myMdKeyRx = new DefaultPropertyValueReceiver<>(myDti.getMetaDataInfo().getPropertyArrayDescriptor());
                    myPropertyValueReceivers.add(myMdKeyRx);
                }
            }

            if (myIsMapDataElement && myGetMapGeometrySupport)
            {
                myMgsRx = new DefaultPropertyValueReceiver<>(MapGeometrySupport.PROPERTY_DESCRIPTOR);
                myPropertyValueReceivers.add(myMgsRx);
            }
        }
    }
}
