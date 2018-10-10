package io.opensphere.core.data;

import java.io.NotSerializableException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.cache.Cache;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.CacheRemovalListener;
import io.opensphere.core.cache.ClassProvider;
import io.opensphere.core.cache.DefaultCacheModificationListener;
import io.opensphere.core.cache.PropertyValueMap;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.IntervalPropertyAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcherUtilities;
import io.opensphere.core.cache.mem.MemoryCache;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.PropertyValueIdReceiver;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ImpossibleException;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Registry for data models. Data models are produced by {@link Envoy}s and used
 * by {@link Transformer}s, as well as other plug-in components.
 */
@SuppressWarnings("PMD.GodClass")
public class DataRegistryImpl implements DataRegistry
{
    /** Failure message. */
    private static final String CACHE_FAILURE_MSG = "Failed to retrieve objects from the cache: ";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataRegistryImpl.class);

    /** The data cache. */
    private final Cache myCache;

    /**
     * The collection of class providers the cache can use when deserializing
     * objects that are not found by the system class loader.
     */
    private final List<ClassProvider> myClassProviders = Collections.synchronizedList(New.<ClassProvider>list());

    /** The providers used for queries. */
    private final List<CachingDataRegistryDataProvider> myDataProviders = new CopyOnWriteArrayList<>();

    /** An executor for background tasks. */
    private final ExecutorService myExecutor;

    /** The listener manager. */
    private final DataRegistryListenerManager myListenerManager = new DataRegistryListenerManager();

    /** The query manager. */
    private final QueryManager myQueryManager = new QueryManager(mt -> performQuery(mt, false, false));

    /**
     * Construct a data registry.
     *
     * @param executor The executor for background tasks.
     * @param cache The cache to use.
     */
    public DataRegistryImpl(ExecutorService executor, Cache cache)
    {
        myExecutor = executor;
        myCache = new MemoryCache(cache);
        myCache.setClassProvider(className ->
        {
            Class<?> theClass = null;

            synchronized (myClassProviders)
            {
                for (ClassProvider provider : myClassProviders)
                {
                    theClass = provider.getClass(className);
                    if (theClass != null)
                    {
                        break;
                    }
                }
            }

            return theClass;
        });
    }

    @Override
    public <T> void addChangeListener(DataRegistryListener<T> listener, DataModelCategory dataModelCategory,
            PropertyDescriptor<T> propertyDescriptor)
    {
        myListenerManager.addChangeListener(listener, dataModelCategory, propertyDescriptor);
    }

    @Override
    public void addClassProvider(ClassProvider provider)
    {
        myClassProviders.add(provider);
    }

    @Override
    public void addDataProvider(DataRegistryDataProvider dataProvider, ThreadPoolExecutor executor)
    {
        myDataProviders.add(new CachingDataRegistryDataProvider(dataProvider, executor, myCache));
    }

    @Override
    public <T> long[] addModels(final CacheDeposit<T> insert)
    {
        return addModels(insert, (Object)null);
    }

    @Override
    public <T> long[] addModels(final CacheDeposit<T> insert, Object source)
    {
        Utilities.checkNull(insert, "insert");
        DataModelCategory category = insert.getCategory();
        Utilities.checkNull(category, "insert.getCategory()");
        Utilities.checkNull(insert.getAccessors(), "insert.getAccessors()");
        Utilities.checkNull(insert.getInput(), "insert.getInput()");

        if (!insert.isNew())
        {
            throw new IllegalArgumentException("Update passed to addModels(). Please use updateModels() instead.");
        }

        Utilities.checkNull(category.getSource(), "insert.getCategory().getSource()");
        Utilities.checkNull(category.getFamily(), "insert.getCategory().getFamily()");
        Utilities.checkNull(category.getCategory(), "insert.getCategory().getCategory()");

        DefaultCacheModificationListener listener = new DefaultCacheModificationListener();
        doAddOrUpdate(insert, source, listener);
        if (listener.getReports().isEmpty())
        {
            return new long[0];
        }
        return listener.getReports().iterator().next().getIds();
    }

    @Override
    public void close()
    {
        myCache.close();
    }

    @Override
    public DataModelCategory[] getDataModelCategories(long[] ids)
    {
        DataModelCategory[] result;
        try
        {
            result = myCache.getDataModelCategories(ids);
        }
        catch (CacheException e)
        {
            LOGGER.error("Failed to get data model categories: " + e, e);
            result = null;
        }
        return result;
    }

    @Override
    public Set<DataModelCategory> getDataModelCategories(long[] ids, boolean source, boolean family, boolean category)
    {
        Set<DataModelCategory> result;
        try
        {
            result = New.set(myCache.getDataModelCategoriesByModelId(ids, source, family, category));
        }
        catch (CacheException e)
        {
            LOGGER.error("Failed to get data model categories: " + e, e);
            result = null;
        }
        return result;
    }

    @Override
    public long[] getPersistedSizes(long[] ids, PropertyDescriptor<?> propertyDescriptor)
    {
        long[] result;
        try
        {
            result = myCache.getValueSizes(ids, propertyDescriptor);
        }
        catch (CacheException e)
        {
            LOGGER.error("Failed to get persisted sizes: " + e, e);
            result = null;
        }
        return result;
    }

    @Override
    public boolean hasThreadCapacity(DataModelCategory dmc)
    {
        for (Iterator<CachingDataRegistryDataProvider> iter = getDataProviderIterator(dmc); iter.hasNext();)
        {
            CachingDataRegistryDataProvider provider = iter.next();
            if (provider.getExecutorSaturation() < 1.)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int performLocalQuery(long[] ids, Query query)
    {
        final DefaultQueryTracker tracker = new DefaultQueryTracker(query, true);

        int result = 0;
        try
        {
            for (int index = query.getStartIndex(); index - query.getStartIndex() < query.getLimit(); index += query
                    .getBatchSize())
            {
                int lastIndex;
                lastIndex = Math.min(ids.length, index + query.getBatchSize());
                lastIndex = Math.min(lastIndex, query.getStartIndex() + query.getLimit());
                int count = doValueQuery(tracker, index, Arrays.copyOfRange(ids, index, lastIndex));
                result += count;

                if (count < query.getBatchSize())
                {
                    break;
                }
            }
        }
        catch (RuntimeException | CacheException e)
        {
            LOGGER.error(CACHE_FAILURE_MSG + e, e);
        }
        return result;
    }

    @Override
    public long[] performLocalQuery(Query query)
    {
        QueryTracker tracker = startQuery(query, true, true);
        tracker.logException();
        return tracker.getIds();
    }

    @Override
    public QueryTracker performQuery(Query query)
    {
        QueryTracker tracker = startQuery(query, false, true);
        tracker.awaitCompletion();
        return tracker;
    }

    @Override
    public void removeChangeListener(DataRegistryListener<?> listener)
    {
        myListenerManager.removeChangeListener(listener);
    }

    @Override
    public void removeClassProvider(ClassProvider provider)
    {
        myClassProviders.remove(provider);
    }

    @Override
    public void removeDataProvider(DataRegistryDataProvider dataProvider)
    {
        Collection<CachingDataRegistryDataProvider> toBeRemoved = New.collection();
        for (CachingDataRegistryDataProvider dp : myDataProviders)
        {
            if (Utilities.sameInstance(dataProvider, dp.getDataProvider()))
            {
                toBeRemoved.add(dp);
            }
        }
        myDataProviders.removeAll(toBeRemoved);
    }

    @Override
    public long[] removeModels(DataModelCategory category, boolean returnIds)
    {
        return removeModels(category, returnIds, null);
    }

    @Override
    public long[] removeModels(DataModelCategory category, boolean returnIds, final Object source)
    {
        long[] ids;
        try
        {
            if (category == null
                    || category.getCategory() == null && category.getFamily() == null && category.getSource() == null)
            {
                ids = returnIds ? myCache.getIds(category, null, null, 0, Integer.MAX_VALUE) : null;

                myCache.clear();
                myListenerManager.notifyAllRemoved(source);
            }
            else
            {
                final TLongArrayList idList = returnIds ? new TLongArrayList() : null;
                myCache.clear(category, returnIds, new CacheRemovalListener()
                {
                    @Override
                    public void valuesRemoved(DataModelCategory dmc, long[] idsForGroup)
                    {
                        if (idList != null)
                        {
                            idList.add(idsForGroup);
                        }
                        myListenerManager.notifyRemoves(dmc, idsForGroup, source);
                    }

                    @Override
                    public <T> void valuesRemoved(DataModelCategory dmc, long[] idsForGroup, PropertyDescriptor<T> desc,
                            Iterable<? extends T> values)
                    {
                        myListenerManager.notifyRemoves(dmc, idsForGroup, desc, values, source);
                    }
                });
                ids = idList == null ? null : idList.toArray();
            }
        }
        catch (CacheException e)
        {
            LOGGER.error(CACHE_FAILURE_MSG + e, e);
            ids = returnIds ? new long[0] : null;
        }
        catch (NotSerializableException e)
        {
            throw new ImpossibleException(e);
        }
        return ids;
    }

    @Override
    public void removeModels(long[] ids)
    {
        removeModels(ids, null);
    }

    @Override
    public void removeModels(long[] ids, final boolean waitForListeners, final Object source)
    {
        try
        {
            DataModelCategory[] dataModelCategories = myCache.getDataModelCategories(ids);

            // Construct a map of categories to lists of ids. These are
            // necessary for notifying listeners.
            Map<DataModelCategory, TLongArrayList> map = New.map();
            for (int index = 0; index < ids.length; ++index)
            {
                CollectionUtilities.multiMapAdd(map, dataModelCategories[index], ids[index]);
            }

            final Collection<CountDownLatch> latches = New.collection(map.size() * 2);

            long t0 = System.nanoTime();
            if (myListenerManager.isWantingRemovedObjects())
            {
                for (Map.Entry<DataModelCategory, TLongArrayList> entry : map.entrySet())
                {
                    final DataModelCategory dataModelCategory = entry.getKey();
                    long[] idsForGroup = entry.getValue().toArray();
                    myCache.clear(idsForGroup, new CacheRemovalListener()
                    {
                        @Override
                        public void valuesRemoved(DataModelCategory dmc, long[] idsRemoved)
                        {
                        }

                        @Override
                        public <T> void valuesRemoved(DataModelCategory dmc, long[] idsRemoved, PropertyDescriptor<T> desc,
                                Iterable<? extends T> values)
                        {
                            CountDownLatch latch = myListenerManager.notifyRemoves(dataModelCategory, idsRemoved, desc, values,
                                    source);
                            if (waitForListeners)
                            {
                                latches.add(latch);
                            }
                        }
                    });
                }
            }
            else
            {
                myCache.clear(ids);
            }
            if (LOGGER.isDebugEnabled())
            {
                long et = System.nanoTime() - t0;
                LOGGER.debug(StringUtilities.formatTimingMessage("Time to remove " + ids.length + " ids from cache: ", et));
            }

            // Notify listeners.
            for (Map.Entry<DataModelCategory, TLongArrayList> entry : map.entrySet())
            {
                DataModelCategory dataModelCategory = entry.getKey();
                long[] idsForGroup = entry.getValue().toArray();
                CountDownLatch latch = myListenerManager.notifyRemoves(dataModelCategory, idsForGroup, source);
                if (waitForListeners)
                {
                    latches.add(latch);
                }
            }

            for (CountDownLatch latch : latches)
            {
                waitForLatch(latch);
            }
        }
        catch (CacheException e)
        {
            LOGGER.error(CACHE_FAILURE_MSG + e, e);
        }
    }

    @Override
    public void removeModels(long[] ids, final Object source)
    {
        removeModels(ids, false, source);
    }

    /**
     * Set the number of bytes that can be used by my in-memory cache.
     *
     * @param bytes The bytes.
     */
    public void setInMemoryCacheSizeBytes(long bytes)
    {
        try
        {
            myCache.setInMemorySizeBytes(bytes);
        }
        catch (CacheException e)
        {
            LOGGER.warn("Failed to set cache size to " + bytes + " bytes: " + e, e);
        }
    }

    @Override
    public QueryTracker submitLocalQuery(Query query)
    {
        return startQuery(query, true, false);
    }

    @Override
    public QueryTracker submitQuery(Query query)
    {
        return startQuery(query, false, false);
    }

    @Override
    public <T> void updateModels(CacheDeposit<T> insert, CacheModificationListener listener)
    {
        updateModels(insert, null, listener);
    }

    @Override
    public <T> void updateModels(CacheDeposit<T> insert, Object source, CacheModificationListener listener)
    {
        Utilities.checkNull(insert, "insert");
        Utilities.checkNull(insert.getCategory(), "insert.getCategory()");
        Utilities.checkNull(insert.getAccessors(), "insert.getAccessors()");
        Iterable<? extends T> input = insert.getInput();
        Utilities.checkNull(input, "insert.getInput()");

        if (insert.isNew())
        {
            throw new IllegalArgumentException("Add passed to updateModels(). Please use addModels() instead.");
        }

        doAddOrUpdate(insert, source, listener);
    }

    @Override
    public <T> void updateModels(long[] ids, Collection<? extends T> input,
            Collection<? extends PropertyAccessor<? super T, ?>> accessors, CacheModificationListener listener)
    {
        updateModels(ids, input, accessors, null, false, listener);
    }

    @Override
    public <T> void updateModels(final long[] ids, final Collection<? extends T> input,
            final Collection<? extends PropertyAccessor<? super T, ?>> accessors, final Object source, boolean returnEarly,
            final CacheModificationListener listener)
    {
        Utilities.checkNull(ids, "ids");
        Utilities.checkNull(accessors, "accessors");

        if (input.size() != 1 && ids.length != input.size())
        {
            throw new IllegalArgumentException(
                    "Either the input collection must be a singleton or must match the size of the id array.");
        }
        try
        {
            long t0 = System.nanoTime();
            myCache.updateValues(ids, input, accessors, returnEarly ? myExecutor : null, cmr ->
            {
                myListenerManager.notifyAddsOrUpdates(cmr, ids, input, cmr.filterAccessors(accessors),
                        DataRegistryListenerManager.ChangeType.UPDATE, source);
                if (listener != null)
                {
                    listener.cacheModified(cmr);
                }
            });
            if (LOGGER.isDebugEnabled())
            {
                long t1 = System.nanoTime();
                LOGGER.debug(StringUtilities.formatTimingMessage("Time to update " + ids.length + " values in cache: ", t1 - t0));
            }
        }
        catch (CacheException e)
        {
            LOGGER.error("Failed to cache data: " + e, e);
        }
        catch (NotSerializableException e)
        {
            LOGGER.error("Data was not serializable for input object [" + input + "]: " + e, e);
        }
    }

    /**
     * Deliver property values to property value receivers.
     *
     * @param <T> The type of the property values.
     * @param ids The requested ids.
     * @param startIndex The index of the first property value.
     * @param resultMap Map of property descriptions to property values.
     * @param failedIndices List of failed indices. These are indices into the
     *            property value lists.
     * @param receiver The object that will receive the values.
     */
    protected <T> void deliverValuesToReceiver(long[] ids, int startIndex, PropertyValueMap resultMap,
            TIntArrayList failedIndices, PropertyValueReceiver<T> receiver)
    {
        PropertyDescriptor<T> propertyDescriptor = receiver.getPropertyDescriptor();
        List<T> values = resultMap.getResultList(propertyDescriptor);

        // This is safe because the values in the result map must be the
        // same type as specified by the property descriptor from the
        // receiver.
        @SuppressWarnings("unchecked")
        PropertyValueReceiver<Object> cast = (PropertyValueReceiver<Object>)receiver;

        // Only send values to the receivers that are not in the failed
        // indices.
        int fromIndex = 0;
        int toIndex;
        for (TIntIterator iter = failedIndices.iterator(); iter.hasNext();)
        {
            toIndex = iter.next();
            if (fromIndex < toIndex)
            {
                if (cast instanceof PropertyValueIdReceiver)
                {
                    long[] batch = new long[toIndex - fromIndex];
                    System.arraycopy(ids, startIndex + fromIndex, batch, 0, batch.length);
                    ((PropertyValueIdReceiver<Object>)cast).receive(batch, startIndex + fromIndex,
                            values.subList(fromIndex, toIndex));
                }
                else
                {
                    cast.receive(values.subList(fromIndex, toIndex));
                }
            }
            fromIndex = toIndex + 1;
        }

        toIndex = values.size();
        if (fromIndex < toIndex)
        {
            if (fromIndex > 0)
            {
                values = values.subList(fromIndex, toIndex);
            }
            if (cast instanceof PropertyValueIdReceiver)
            {
                long[] batch;
                if (startIndex + fromIndex == 0)
                {
                    batch = ids;
                }
                else
                {
                    batch = new long[toIndex - fromIndex];
                    System.arraycopy(ids, startIndex + fromIndex, batch, 0, batch.length);
                }
                ((PropertyValueIdReceiver<Object>)cast).receive(batch, startIndex + fromIndex, values);
            }
            else
            {
                cast.receive(values);
            }
        }
    }

    /**
     * Perform an add or update on the registry. Check the integrity of the
     * insert, update the persistent and in-memory caches, and update
     * subscribers.
     *
     * @param <T> The type of input objects.
     * @param insert The insert object.
     * @param source The originator of the change.
     * @param listener Optional listener for cache modification reports.
     */
    protected <T> void doAddOrUpdate(final CacheDeposit<T> insert, final Object source, final CacheModificationListener listener)
    {
        Collection<String> propertyKeys = New.set(insert.getAccessors().size());
        boolean foundGeometryAccessor = false;
        boolean foundIntervalAccessor = false;
        for (PropertyAccessor<? super T, ?> accessor : insert.getAccessors())
        {
            if (accessor instanceof GeometryAccessor)
            {
                if (foundGeometryAccessor)
                {
                    throw new IllegalArgumentException("Cannot have more than one geometry accessor.");
                }
                foundGeometryAccessor = true;
                foundIntervalAccessor = true;
            }
            else if (accessor instanceof IntervalPropertyAccessor)
            {
                foundIntervalAccessor = true;
            }
            if (!propertyKeys.add(accessor.getPropertyDescriptor().getPropertyName()))
            {
                throw new IllegalArgumentException(
                        "Cannot have more than one accessor for the same property name. Found more than once accessor for property ["
                                + accessor.getPropertyDescriptor().getPropertyName() + "]");
            }
        }
        propertyKeys = null;

        if (!foundIntervalAccessor && !CollectionUtilities.hasContent(insert.getInput()))
        {
            return;
        }

        try
        {
            long t0 = System.nanoTime();

            final DataRegistryListenerManager.ChangeType changeType = insert.isNew() ? DataRegistryListenerManager.ChangeType.ADD
                    : DataRegistryListenerManager.ChangeType.UPDATE;
            long[] ids = myCache.put(insert, cmr ->
            {
                myListenerManager.notifyAddsOrUpdates(cmr, cmr.getIds(), insert.getInput(),
                        cmr.filterAccessors(insert.getAccessors()), changeType, source);
                if (listener != null)
                {
                    listener.cacheModified(cmr);
                }
            });

            if (LOGGER.isDebugEnabled())
            {
                long t1 = System.nanoTime();
                LOGGER.debug(
                        StringUtilities.formatTimingMessage("Time to add " + ids.length + " models to the cache: ", t1 - t0));
            }
        }
        catch (CacheException e)
        {
            LOGGER.error("Failed to cache data: " + e, e);
        }
        catch (NotSerializableException e)
        {
            LOGGER.error("Data of category [" + insert.getCategory() + "] was not serializable: " + e, e);
        }
    }

    /**
     * Perform a query to get the property values for some element ids.
     *
     * @param tracker The query tracker.
     * @param index The start index.
     * @param ids The array of numeric ids.
     * @return The number of ids that were matched.
     * @throws CacheException If there's a database error.
     */
    protected int doValueQuery(MutableQueryTracker tracker, int index, long[] ids) throws CacheException
    {
        int count = 0;
        long t0 = System.nanoTime();
        Collection<? extends PropertyValueReceiver<?>> receivers = tracker.getQuery().getPropertyValueReceivers();
        if (CollectionUtilities.hasContent(receivers))
        {
            count += retrievePropertyValues(tracker, index, ids, receivers);
            if (LOGGER.isDebugEnabled())
            {
                long t1 = System.nanoTime();
                LOGGER.debug(StringUtilities.formatTimingMessage("Time to retrieve " + receivers.size() * ids.length
                        + " property values for category " + tracker.getQuery().getDataModelCategory() + " from cache: ",
                        t1 - t0));
            }
        }

        return count;
    }

    /**
     * Worker method that performs a query, interacting with a
     * {@link QueryTracker}.
     *
     * @param tracker The query tracker.
     * @param cacheOnly Flag indicating if only the cache should be queried.
     * @param synchronous Flag indicating if the query should be done on the
     *            current thread only.
     */
    protected void performQuery(final MultiQueryTracker tracker, boolean cacheOnly, boolean synchronous)
    {
        // Capture the unsatisfied intervals to be handled in this method. We
        // don't want unsatisfied intervals to be added in between determining
        // the cache satisfactions and determining the data provider
        // satisfactions, because that could result in making a query against
        // the data providers without checking the cache first.
        List<IntervalPropertyValueSet> unsatisfied = tracker.getUnsatisfied();

        final Collection<? extends MutableQueryTracker> cacheTrackers;
        try
        {
            cacheTrackers = determineCacheSatisfactions(tracker, unsatisfied);
        }
        catch (CacheException e)
        {
            if (!tracker.isCancelled())
            {
                LOGGER.error(CACHE_FAILURE_MSG + e, e);
                tracker.setQueryStatus(QueryStatus.FAILED, e);
            }
            return;
        }
        catch (RuntimeException e)
        {
            LOGGER.error(CACHE_FAILURE_MSG + e, e);
            tracker.setQueryStatus(QueryStatus.FAILED, e);
            return;
        }
        catch (NotSerializableException e)
        {
            LOGGER.error("Query parameter was not serializable: " + e, e);
            tracker.setQueryStatus(QueryStatus.FAILED, e);
            return;
        }

        // If this is a cache-only query or the query is fully satisfied by the
        // cache, don't bother with the data providers.
        final Collection<Pair<CachingDataRegistryDataProvider, MutableQueryTracker>> dataProviders;
        if (cacheOnly || unsatisfied.isEmpty())
        {
            dataProviders = null;
        }
        else
        {
            validateQuery(tracker.getQuery());

            try
            {
                dataProviders = determineDataProviderSatisfactions(tracker, unsatisfied);
            }
            catch (RuntimeException e)
            {
                LOGGER.error("Failed to determine data provider satisfactions: " + e, e);
                tracker.setQueryStatus(QueryStatus.FAILED, e);
                return;
            }
        }

        if (tracker.isCancelled())
        {
            return;
        }

        if (!unsatisfied.isEmpty())
        {
            String msg = "No data provider found for query [" + tracker.getQuery() + "]";
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(msg);
            }
            tracker.setQueryStatus(QueryStatus.FAILED, cacheOnly ? null : new CacheException(msg));
            return;
        }

        // Wait to start the queries until all the sub-trackers are created so
        // the multi-query tracker knows when it's done.
        runQueries(cacheTrackers, dataProviders, synchronous, tracker.isIntervalQuery());
    }

    /**
     * Determine if the cache can satisfy any of a query. If the query has
     * multiple unsatisfied regions, multiple trackers may be returned.
     *
     * @param tracker The query tracker.
     * @param unsatisfied Input/output list of intervals to be satisfied. These
     *            should be a subset of the unsatisfied intervals in the query
     *            tracker. The intervals satisfied by created cache trackers
     *            will be removed from this list.
     * @return The sub-query trackers.
     * @throws CacheException If there's a problem accessing the cache.
     * @throws NotSerializableException If one of the parameter values is not
     *             serializable.
     */
    private Collection<? extends MutableQueryTracker> determineCacheSatisfactions(MultiQueryTracker tracker,
            List<IntervalPropertyValueSet> unsatisfied)
        throws NotSerializableException, CacheException
    {
        Collection<MutableQueryTracker> cacheTrackers;
        if (tracker.isIntervalQuery())
        {
            cacheTrackers = New.list();
            for (IntervalPropertyValueSet interval : New.collection(unsatisfied))
            {
                Collection<Collection<? extends IntervalPropertyMatcher<?>>> groupMatchers = PropertyMatcherUtilities
                        .getGroupMatchers(interval);
                for (Collection<? extends IntervalPropertyMatcher<?>> params : groupMatchers)
                {
                    Collection<? extends Satisfaction> satisfactions = myCache
                            .getIntervalSatisfactions(tracker.getQuery().getDataModelCategory(), params);

                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace(StringUtilities.concat("Cache satisfactions for ", tracker, " params ", params, " are ",
                                satisfactions));
                    }
                    if (!satisfactions.isEmpty())
                    {
                        MutableQueryTracker subTracker = tracker.createSubTracker(true, satisfactions, params);
                        if (subTracker != null)
                        {
                            cacheTrackers.add(subTracker);
                        }

                        // Remove the satisfactions from the unsatisfied list
                        // even if the tracker was not added, because this means
                        // that a tracker was added by a different thread for
                        // the same satisfaction, and it still means we should
                        // not try to satisfy it.
                        for (Satisfaction satisfaction : satisfactions)
                        {
                            IntervalPropertyValueSet.subtract(unsatisfied, satisfaction.getIntervalPropertyValueSet());
                        }
                    }
                }
            }
        }
        else if (!unsatisfied.isEmpty())
        {
            long[] ids = myCache.getIds(tracker.getQuery().getDataModelCategory(), tracker.getParameters(),
                    tracker.getQuery().getOrderSpecifiers(), tracker.getQuery().getStartIndex(),
                    Math.min(tracker.getQuery().getLimit(), tracker.getQuery().getBatchSize()));
            MutableQueryTracker subTracker;
            if (ids.length == 0)
            {
                subTracker = null;
            }
            else
            {
                IdSatisfaction satisfaction = new IdSatisfaction(ids);
                subTracker = tracker.createSubTracker(true, Collections.singleton(satisfaction));

                IntervalPropertyValueSet.subtract(unsatisfied, satisfaction.getIntervalPropertyValueSet());
            }
            if (subTracker == null)
            {
                cacheTrackers = Collections.emptyList();
            }
            else
            {
                cacheTrackers = Collections.singleton(subTracker);
            }
        }
        else
        {
            cacheTrackers = Collections.emptyList();
        }
        return cacheTrackers;
    }

    /**
     * Determine what data providers can satisfy the current query.
     *
     * @param tracker The query tracker.
     * @param unsatisfied Input/output list of unsatisfied intervals.
     * @return A collection of pairs of data providers and sub-query trackers.
     */
    @SuppressWarnings("PMD.SimplifiedTernary")
    private Collection<Pair<CachingDataRegistryDataProvider, MutableQueryTracker>> determineDataProviderSatisfactions(
            MultiQueryTracker tracker, List<IntervalPropertyValueSet> unsatisfied)
    {
        Collection<Pair<CachingDataRegistryDataProvider, MutableQueryTracker>> dataProviders = New.collection();

        for (CachingDataRegistryDataProviderIterator dataProviderIter = getDataProviderIterator(
                tracker.getQuery().getDataModelCategory()); !unsatisfied.isEmpty() && dataProviderIter.hasNext();)
        {
            final CachingDataRegistryDataProvider dp = dataProviderIter.next();

            if (tracker.isCancelled())
            {
                break;
            }

            if (tracker.isIntervalQuery())
            {
                // Get the satisfactions from the data provider.
                Collection<? extends Satisfaction> satisfactions = dp.getSatisfaction(tracker.getQuery().getDataModelCategory(),
                        unsatisfied);

                MutableQueryTracker subTracker = CollectionUtilities.hasContent(satisfactions)
                        ? tracker.createSubTracker(false, satisfactions) : null;
                if (subTracker != null)
                {
                    dataProviders.add(Pair.create(dp, subTracker));
                }

                for (Satisfaction satisfaction : satisfactions)
                {
                    IntervalPropertyValueSet.subtract(unsatisfied, satisfaction.getIntervalPropertyValueSet());
                }
            }
            else
            {
                MutableQueryTracker subTracker = tracker.createSubTracker(false,
                        SingleSatisfaction.generateSatisfactions(unsatisfied));
                if (subTracker != null)
                {
                    dataProviders.add(Pair.create(dp, subTracker));
                }

                unsatisfied.clear();
            }
        }

        return dataProviders;
    }

    /**
     * Get an iterator over the data providers that are available for a category
     * of data.
     *
     * @param dataModelCategory The category of data.
     * @return The iterator.
     */
    private CachingDataRegistryDataProviderIterator getDataProviderIterator(final DataModelCategory dataModelCategory)
    {
        return new CachingDataRegistryDataProviderIterator(dataModelCategory, myDataProviders.iterator());
    }

    /**
     * Perform a query against the cache.
     *
     * @param tracker The query tracker.
     */
    private void performCacheQuery(MutableQueryTracker tracker)
    {
        long[] result = new long[0];
        try
        {
            for (int index = tracker.getQuery().getStartIndex(); index - tracker.getQuery().getStartIndex() < tracker.getQuery()
                    .getLimit(); index += tracker.getQuery().getBatchSize())
            {
                if (tracker.isCancelled())
                {
                    return;
                }

                long t0 = System.nanoTime();
                long[] ids;
                Satisfaction firstSatisfaction = CollectionUtilities.getItem(tracker.getSatisfactions(), 0);
                if (firstSatisfaction instanceof IdSatisfaction)
                {
                    ids = ((IdSatisfaction)firstSatisfaction).getIds();
                }
                else
                {
                    ids = myCache.getIds(tracker.getSatisfactions(), tracker.getParameters(),
                            tracker.getQuery().getOrderSpecifiers(), index,
                            Math.min(tracker.getQuery().getLimit(), tracker.getQuery().getBatchSize()));
                }
                long t1 = System.nanoTime();
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(StringUtilities.formatTimingMessage("Time to retrieve " + ids.length + " ids for category "
                            + tracker.getQuery().getDataModelCategory() + " from cache: ", t1 - t0));
                }

                if (ids.length > 0)
                {
                    result = Utilities.concatenate(result, ids);
                    tracker.addIds(result);
                    doValueQuery(tracker, index, ids);
                }
                if (ids.length < tracker.getQuery().getBatchSize())
                {
                    break;
                }
            }
            tracker.setQueryStatus(QueryStatus.SUCCESS, (Throwable)null);
        }
        catch (CacheException | RuntimeException e)
        {
            LOGGER.error(CACHE_FAILURE_MSG + e, e);
            tracker.setQueryStatus(QueryStatus.FAILED, e);
        }
        catch (NotSerializableException e)
        {
            LOGGER.error("Query parameter was not serializable: " + e, e);
            tracker.setQueryStatus(QueryStatus.FAILED, e);
        }
    }

    /**
     * Perform a cache query.
     *
     * @param cacheTracker The query tracker.
     * @param synchronous If the query should be done on the current thread.
     * @return {@code true} if the query is synchronous and the cache has
     *         results.
     */
    private boolean performCacheQuery(final MutableQueryTracker cacheTracker, boolean synchronous)
    {
        Runnable r = cacheTracker.wrapRunnable(() -> performCacheQuery(cacheTracker));
        if (synchronous)
        {
            r.run();
            return cacheTracker.getIds().length > 0;
        }
        myExecutor.execute(r);
        return false;
    }

    /**
     * Get the property values associated with the given ids from the cache.
     *
     * @param tracker The query tracker.
     * @param startIndex The index of the first property value.
     * @param ids The ids for the models.
     * @param receivers The objects that define what properties are to be
     *            retrieved and also receive the retrieved properties.
     * @return The number of ids matched.
     * @throws CacheException If the properties cannot be retrieved.
     */
    private int retrievePropertyValues(MutableQueryTracker tracker, int startIndex, long[] ids,
            Collection<? extends PropertyValueReceiver<?>> receivers)
        throws CacheException
    {
        if (tracker.isCancelled())
        {
            return 0;
        }
        PropertyValueMap resultMap = new PropertyValueMap();
        for (PropertyValueReceiver<?> receiver : receivers)
        {
            resultMap.addResultList(receiver.getPropertyDescriptor(), ids.length);
        }

        TIntArrayList failedIndices = new TIntArrayList();
        try
        {
            myCache.getValues(ids, resultMap, failedIndices);
        }
        finally // deliver any retrieved values, even if there was an exception
        {
            for (PropertyValueReceiver<?> receiver : receivers)
            {
                deliverValuesToReceiver(ids, startIndex, resultMap, failedIndices, receiver);
            }
        }

        return resultMap.values().iterator().next().size();
    }

    /**
     * Run queries against the cache and data providers.
     *
     * @param cacheTrackers The cache query trackers.
     * @param dataProviders The optional data providers.
     * @param synchronous Flag indicating if the queries must be done on the
     *            current thread.
     * @param intervalQuery Flag indicating if this is an interval-based query.
     */
    private void runQueries(Collection<? extends MutableQueryTracker> cacheTrackers,
            Collection<Pair<CachingDataRegistryDataProvider, MutableQueryTracker>> dataProviders, boolean synchronous,
            boolean intervalQuery)
    {
        for (MutableQueryTracker cacheTracker : cacheTrackers)
        {
            performCacheQuery(cacheTracker, synchronous);
        }
        if (dataProviders != null)
        {
            boolean shortCircuit = false;
            for (Pair<CachingDataRegistryDataProvider, MutableQueryTracker> pair : dataProviders)
            {
                if (shortCircuit)
                {
                    pair.getSecondObject().setQueryStatus(QueryStatus.SUCCESS, (Throwable)null);
                }
                else
                {
                    pair.getFirstObject().query(pair.getSecondObject(), intervalQuery, myListenerManager);
                    if (synchronous && !intervalQuery && pair.getSecondObject().awaitCompletion().length > 0)
                    {
                        shortCircuit = true;
                    }
                }
            }
        }
    }

    /**
     * Helper method that initiates a query.
     *
     * @param query The query.
     * @param cacheOnly If the query is for the local cache only.
     * @param synchronous If the query is to be done on the current thread.
     * @return The query tracker.
     */
    private QueryTracker startQuery(Query query, final boolean cacheOnly, final boolean synchronous)
    {
        final MultiQueryTracker tracker;
        if (cacheOnly)
        {
            tracker = new MultiQueryTracker(query, null);
        }
        else
        {
            // Submit the query to the query manager. This will check for other
            // concurrent queries that overlay this query. If any are found,
            // slave trackers will be added to the tracker that comes
            // back from the query manager. The slave trackers listen for the
            // other concurrent queries to finish, and trigger this query to be
            // resubmitted when they do.
            tracker = myQueryManager.submitQuery(query);

            if (!tracker.hasUnsatisfied())
            {
                return tracker;
            }
        }

        Runnable runner = tracker.wrapRunnable(() -> performQuery(tracker, cacheOnly, synchronous));

        if (synchronous)
        {
            runner.run();
        }
        else
        {
            myExecutor.execute(runner);
        }

        return tracker;
    }

    /**
     * Check a query to see if it can be supported.
     *
     * @param query The query.
     */
    private void validateQuery(Query query)
    {
        if (query.getStartIndex() != 0)
        {
            throw new UnsupportedOperationException("Queries are not currently supported with a start index > 0.");
        }
        if (query.getLimit() != Integer.MAX_VALUE)
        {
            throw new UnsupportedOperationException("Queries are not currently supported with a limit < 2147483647.");
        }
    }

    /**
     * Wait for a latch, or report an error if interrupted.
     *
     * @param latch The latch.
     */
    private void waitForLatch(CountDownLatch latch)
    {
        try
        {
            if (!latch.await(1, TimeUnit.SECONDS))
            {
                LOGGER.warn("Time expired waiting for latch.");
            }
        }
        catch (InterruptedException e)
        {
            LOGGER.warn("Interrupted while waiting for latch: " + e, e);
        }
    }

    /**
     * Implementation of {@link io.opensphere.core.data.util.Satisfaction} that
     * tracks the ids that satisfy the query.
     */
    protected static class IdSatisfaction extends SingleSatisfaction
    {
        /** The ids. */
        private final long[] myIds;

        /**
         * Constructor.
         *
         * @param ids The ids.
         */
        public IdSatisfaction(long[] ids)
        {
            super(null);
            myIds = ids.clone();
        }

        /**
         * The ids.
         *
         * @return The ids.
         */
        public long[] getIds()
        {
            return myIds.clone();
        }
    }
}
