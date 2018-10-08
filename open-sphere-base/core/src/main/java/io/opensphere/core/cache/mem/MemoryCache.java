package io.opensphere.core.cache.mem;

import java.io.InputStream;
import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import io.opensphere.core.cache.Cache;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheIdUtilities;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.CacheModificationReport;
import io.opensphere.core.cache.CacheRemovalListener;
import io.opensphere.core.cache.ClassProvider;
import io.opensphere.core.cache.PropertyValueMap;
import io.opensphere.core.cache.accessor.PersistentPropertyAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyArrayDescriptor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.LazyMap.Factory;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ImpossibleException;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.SoftReference;
import io.opensphere.core.util.ref.StrongReference;

/**
 * Implementation of the {@link Cache} interface that uses the VM memory.
 */
@SuppressWarnings("PMD.GodClass")
public class MemoryCache implements Cache
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MemoryCache.class);

    /** The maximum size for objects cached using soft references. */
    private static final long MAX_SOFT_CACHE_VALUE_BYTES = Long
            .getLong("opensphere.db.memoryCache.maxValueSizeBytes", Constants.BYTES_PER_KILOBYTE).longValue();

    /** A map of property descriptors to primary keys to property values. */
    // TODO: Change this to be a map of group ids to descriptors to arrays of
    // values
    private final Map<PropertyDescriptor<?>, TLongObjectHashMap<Reference<Object>>> myCacheMap = New.map();

    /** Flag indicating if caching for persistent deposits is enabled. */
    private volatile boolean myCachingForPersistentDepositsEnabled = true;

    /** Map of group ids to data model categories. */
    private final TIntObjectHashMap<DataModelCategory> myGroupIdToDataModelCategoryMap = new TIntObjectHashMap<>();

    /**
     * A nested, possibly more persistent cache to be used if my cache map is
     * missing a needed value.
     */
    private final Cache myNestedCache;

    /**
     * Constructor with no nested cache.
     */
    public MemoryCache()
    {
        myNestedCache = null;
    }

    /**
     * Constructor that takes a nested cache.
     *
     * @param nestedCache The cache to be used if a value is missing from the
     *            memory cache.
     */
    public MemoryCache(Cache nestedCache)
    {
        Utilities.checkNull(nestedCache, "nestedCache");
        myNestedCache = nestedCache;
    }

    @Override
    public boolean acceptsPropertyDescriptor(PropertyDescriptor<?> desc)
    {
        return true;
    }

    @Override
    public void clear()
    {
        synchronized (myCacheMap)
        {
            myCacheMap.clear();
        }
        if (myNestedCache != null)
        {
            myNestedCache.clear();
        }
    }

    @Override
    public void clear(DataModelCategory category, boolean returnIds, CacheRemovalListener listener) throws CacheException
    {
        if (myNestedCache == null)
        {
            throw new UnsupportedOperationException("Cannot clear by category with no nested cache installed.");
        }

        final int[] groupIds = getGroupIds(category);
        if (groupIds.length > 0)
        {
            try
            {
                final List<DataModelCategory> affectedDataModelCategories = getDataModelCategoriesByGroupId(groupIds, true, true, true,
                        true);

                for (final DataModelCategory dmc : affectedDataModelCategories)
                {
                    // TODO: If the transient cache is changed so that it
                    // is partitioned by dmc, change this so it checks the
                    // listeners to see if they need the model ids. If the
                    // transient cache doesn't need the ids and the
                    // listeners don't need the ids, we can skip retrieving
                    // them.
                    final long[] idsForGroup = getIds(dmc, null, null, 0, Integer.MAX_VALUE);
                    if (listener != null)
                    {
                        listener.valuesRemoved(dmc, idsForGroup);
                    }
                    final Map<PropertyDescriptor<?>, Iterable<?>> removedValues = removeValues(dmc, idsForGroup);

                    for (final Entry<PropertyDescriptor<?>, Iterable<?>> entry : removedValues.entrySet())
                    {
                        @SuppressWarnings("unchecked")
                        final
                        PropertyDescriptor<Object> desc = (PropertyDescriptor<Object>)entry.getKey();
                        @SuppressWarnings("unchecked")
                        final
                        Iterable<Object> values = (Iterable<Object>)entry.getValue();
                        if (listener != null)
                        {
                            listener.valuesRemoved(dmc, idsForGroup, desc, values);
                        }
                    }
                }

                myNestedCache.clearGroups(groupIds);
            }
            catch (final NotSerializableException e)
            {
                throw new ImpossibleException(e);
            }
        }
    }

    @Override
    public void clear(long[] ids) throws CacheException
    {
        clear(ids, (CacheRemovalListener)null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void clear(long[] ids, CacheRemovalListener listener) throws CacheException
    {
        if (myNestedCache != null)
        {
            myNestedCache.clear(ids, listener);
        }

        Map<PropertyDescriptor<?>, TLongObjectHashMap<Reference<Object>>> cacheCopy;
        synchronized (myCacheMap)
        {
            cacheCopy = new HashMap<>(myCacheMap);
        }

        Map<PropertyDescriptor, Pair<TLongSet, List<Object>>> removed;
        if (listener == null)
        {
            removed = null;
        }
        else
        {
            Factory<PropertyDescriptor, Pair<TLongSet, List<Object>>> factory;
            factory = new LazyMap.Factory<>()
            {
                @Override
                public Pair<TLongSet, List<Object>> create(PropertyDescriptor key)
                {
                    return Pair.create((TLongSet)new TLongHashSet(), New.<Object>list());
                }
            };
            removed = LazyMap.<PropertyDescriptor, Pair<TLongSet, List<Object>>>create(
                    New.<PropertyDescriptor, Pair<TLongSet, List<Object>>>map(), PropertyDescriptor.class, factory);
        }

        for (final Entry<PropertyDescriptor<?>, TLongObjectHashMap<Reference<Object>>> entry : cacheCopy.entrySet())
        {
            final PropertyDescriptor<?> key = entry.getKey();
            final TLongObjectHashMap<Reference<Object>> map = entry.getValue();
            synchronized (map)
            {
                for (int index = 0; index < ids.length; ++index)
                {
                    final Reference<Object> remove = map.remove(ids[index]);
                    final Object obj = remove == null ? null : remove.get();
                    if (obj != null && removed != null)
                    {
                        final Pair<TLongSet, List<Object>> pair = removed.get(key);
                        pair.getFirstObject().add(ids[index]);
                        removed.get(key).getSecondObject().add(obj);
                    }
                }
            }
        }

        if (listener != null && removed != null)
        {
            for (final Entry<PropertyDescriptor, Pair<TLongSet, List<Object>>> entry : removed.entrySet())
            {
                listener.valuesRemoved((DataModelCategory)null, entry.getValue().getFirstObject().toArray(), entry.getKey(),
                        entry.getValue().getSecondObject());
            }
        }
    }

    @Override
    public void clearGroups(int[] groupIds) throws CacheException
    {
        Map<PropertyDescriptor<?>, TLongObjectHashMap<Reference<Object>>> cacheCopy;
        synchronized (myCacheMap)
        {
            cacheCopy = new HashMap<>(myCacheMap);
        }

        // TODO: This would be much faster if it were indexed by group.
        final Collection<TLongObjectHashMap<Reference<Object>>> values = cacheCopy.values();
        for (final TLongObjectHashMap<Reference<Object>> map : values)
        {
            synchronized (map)
            {
                for (final TLongObjectIterator<Reference<Object>> iterator = map.iterator(); iterator.hasNext();)
                {
                    iterator.advance();
                    for (int index = 0; index < groupIds.length; ++index)
                    {
                        if (CacheIdUtilities.getGroupIdFromCombinedId(iterator.key()) == groupIds[index])
                        {
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close()
    {
        if (myNestedCache != null)
        {
            myNestedCache.close();
        }
    }

    @Override
    public DataModelCategory[] getDataModelCategories(long[] ids) throws CacheException
    {
        if (ids.length == 0)
        {
            return New.emptyArray(DataModelCategory.class);
        }

        final int[] groupIds = CacheIdUtilities.getGroupIdsFromCombinedIds(ids, false);
        final List<DataModelCategory> dataModelCategories = getDataModelCategoriesByGroupId(groupIds, true, true, true, false);

        return New.array(dataModelCategories, DataModelCategory.class);
    }

    @Override
    public List<DataModelCategory> getDataModelCategoriesByGroupId(int[] ids, boolean source, boolean family, boolean category,
            boolean distinct) throws CacheException
    {
        List<DataModelCategory> results = New.<DataModelCategory>randomAccessList(ids.length);

        TIntObjectHashMap<TIntArrayList> neededIdMap = null;

        for (int index = 0; index < ids.length; ++index)
        {
            DataModelCategory dmc;
            synchronized (myGroupIdToDataModelCategoryMap)
            {
                dmc = myGroupIdToDataModelCategoryMap.get(ids[index]);
            }
            if (dmc == null && myNestedCache != null)
            {
                if (neededIdMap == null)
                {
                    neededIdMap = new TIntObjectHashMap<>();
                }
                CollectionUtilities.multiMapAdd(neededIdMap, ids[index], index);
            }
            results.add(dmc);
        }

        if (myNestedCache != null && neededIdMap != null)
        {
            final int[] neededIds = neededIdMap.keys();
            final List<DataModelCategory> fromNestedCache = myNestedCache.getDataModelCategoriesByGroupId(neededIds, source, family,
                    category, false);
            synchronized (myGroupIdToDataModelCategoryMap)
            {
                if (fromNestedCache.size() < neededIds.length)
                {
                    throw new IllegalStateException("Wrong number of results returned from nested cache.");
                }
                for (int index = 0; index < neededIds.length; ++index)
                {
                    final DataModelCategory dmc = fromNestedCache.get(index);
                    final TIntArrayList indices = neededIdMap.get(neededIds[index]);
                    for (final int resultIndex : indices.toArray())
                    {
                        results.set(resultIndex, dmc);
                    }
                    myGroupIdToDataModelCategoryMap.put(neededIds[index], dmc);
                }
            }
        }

        if (distinct)
        {
            results = New.list(New.set(results));
        }

        return results;
    }

    @Override
    public List<DataModelCategory> getDataModelCategoriesByModelId(long[] ids, boolean source, boolean family, boolean category)
        throws CacheException
    {
        final int[] groupIds = getGroupIds(ids, true);
        return getDataModelCategoriesByGroupId(groupIds, source, family, category, true);
    }

    @Override
    public int[] getGroupIds(DataModelCategory category) throws CacheException
    {
        verifyNestedCache();
        return myNestedCache.getGroupIds(category);
    }

    @Override
    public int[] getGroupIds(long[] ids, boolean distinct) throws CacheException
    {
        return CacheIdUtilities.getGroupIdsFromCombinedIds(ids, distinct);
    }

    @Override
    public long[] getIds(Collection<? extends Satisfaction> satisfactions, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
                throws CacheException, NotSerializableException
    {
        verifyNestedCache();
        return myNestedCache.getIds(satisfactions, parameters, orderSpecifiers, startIndex, limit);
    }

    @Override
    public long[] getIds(DataModelCategory category, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
                throws CacheException, NotSerializableException
    {
        verifyNestedCache();
        return myNestedCache.getIds(category, parameters, orderSpecifiers, startIndex, limit);
    }

    @Override
    public long[] getIds(int[] groupIds, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
                throws NotSerializableException, CacheException
    {
        verifyNestedCache();
        return myNestedCache.getIds(groupIds, parameters, orderSpecifiers, startIndex, limit);
    }

    @Override
    public Collection<? extends Satisfaction> getIntervalSatisfactions(DataModelCategory category,
            Collection<? extends IntervalPropertyMatcher<?>> parameters)
    {
        if (myNestedCache == null)
        {
            throw new UnsupportedOperationException("Cannot get satisfaction with no nested cache installed.");
        }
        return myNestedCache.getIntervalSatisfactions(category, parameters);
    }

    @Override
    public void getValues(long[] ids, PropertyValueMap resultMap, TIntList failedIndices) throws CacheException
    {
        // These are indices into the current batch, not the overall results.
        int[] neededIndices = new int[ids.length];
        long[] neededIds = new long[ids.length];
        final long t0 = System.nanoTime();
        final int missingCount = getValues(resultMap, ids, neededIndices, neededIds);
        final long t1 = System.nanoTime();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(StringUtilities.formatTimingMessage(
                    "Time to get " + (ids.length - missingCount) * resultMap.size() + " values from the memory cache: ",
                    t1 - t0));
        }

        if (missingCount > 0)
        {
            neededIndices = Arrays.copyOf(neededIndices, missingCount);
            neededIds = Arrays.copyOf(neededIds, missingCount);

            // TODO: This could be smarter and only request the missing
            // properties.
            final long t2 = System.nanoTime();
            final int countFromNestedCache = getValuesFromNestedCache(resultMap, neededIndices, neededIds, failedIndices);
            final long t3 = System.nanoTime();

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(StringUtilities
                        .formatTimingMessage("Time to get " + countFromNestedCache + " values from the nested cache: ", t3 - t2));
            }

            if (countFromNestedCache > 0)
            {
                for (final PropertyDescriptor<?> desc : resultMap.getPropertyDescriptors())
                {
                    if (desc.getEstimatedValueSizeBytes() <= MAX_SOFT_CACHE_VALUE_BYTES)
                    {
                        final boolean overwrite = true;
                        final boolean soft = true;

                        List<?> values = resultMap.getResultList(desc);
                        if (countFromNestedCache < values.size())
                        {
                            final List<Object> valuesToCache = New.list(countFromNestedCache);
                            for (final int index : neededIndices)
                            {
                                valuesToCache.add(values.get(index));
                            }
                            values = valuesToCache;
                        }
                        cacheObjects(desc, neededIds, values, overwrite, soft);
                    }
                }
            }
        }

        if (!failedIndices.isEmpty())
        {
            final long[] failedIds = new long[failedIndices.size()];
            int index = 0;
            for (final TIntIterator iter = failedIndices.iterator(); iter.hasNext();)
            {
                failedIds[index++] = ids[iter.next()];
            }
            clear(failedIds);
        }
    }

    @Override
    public long[] getValueSizes(long[] ids, PropertyDescriptor<?> property) throws CacheException
    {
        if (myNestedCache != null)
        {
            return myNestedCache.getValueSizes(ids, property);
        }
        return new long[ids.length];
    }

    @Override
    public void initialize(long millisecondsWait) throws CacheException
    {
        if (myNestedCache != null)
        {
            myNestedCache.initialize(millisecondsWait);
        }
    }

    @Override
    public <T> long[] put(CacheDeposit<T> insert, final CacheModificationListener listener)
        throws CacheException, NotSerializableException
    {
        if (myNestedCache == null)
        {
            throw new UnsupportedOperationException("Cannot insert objects without a nested cache to generate ids.");
        }
        final long[] ids = myNestedCache.put(insert, listener);

        if (ids.length > 0)
        {
            cacheObjects(ids, insert.getInput(), insert.getAccessors());
        }

        if (listener != null)
        {
            notifyListenerForNonPersistentAccessors(listener, insert.getAccessors(), ids);
        }

        return ids;
    }

    @Override
    public void setClassProvider(ClassProvider provider)
    {
        myNestedCache.setClassProvider(provider);
    }

    @Override
    public void setInMemorySizeBytes(long bytes) throws CacheException
    {
        // This cache does not track in-memory size, but if the setting is zero,
        // disable the cache for soft reference values.
        final boolean wasEnabled = myCachingForPersistentDepositsEnabled;
        myCachingForPersistentDepositsEnabled = bytes > 0;
        if (wasEnabled && bytes <= 0)
        {
            Collection<TLongObjectHashMap<Reference<Object>>> values;
            synchronized (myCacheMap)
            {
                values = New.collection(myCacheMap.values());
            }

            for (final TLongObjectHashMap<Reference<Object>> map : values)
            {
                synchronized (map)
                {
                    for (final TLongObjectIterator<Reference<Object>> iterator = map.iterator(); iterator.hasNext();)
                    {
                        iterator.advance();
                        if (iterator.value() instanceof SoftReference)
                        {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        myNestedCache.setInMemorySizeBytes(bytes);
    }

    @Override
    public void setOnDiskSizeLimitBytes(long sizeLimitBytes)
    {
    }

    @Override
    public <T> void updateValues(final long[] ids, final Collection<? extends T> input,
            final Collection<? extends PropertyAccessor<? super T, ?>> accessors, final Executor executor,
            final CacheModificationListener listener) throws CacheException, NotSerializableException
    {
        final long t0 = System.nanoTime();
        cacheObjects(ids, input, accessors);
        final long t1 = System.nanoTime();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(
                    StringUtilities.formatTimingMessage("Time to update " + ids.length + " values in memory cache: ", t1 - t0));
        }

        if (listener != null)
        {
            notifyListenerForNonPersistentAccessors(listener, accessors, ids);
        }

        if (myNestedCache != null)
        {
            final Runnable runner = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final long t2 = System.nanoTime();

                        myNestedCache.updateValues(ids, input, accessors, executor, listener);
                        if (LOGGER.isDebugEnabled())
                        {
                            final long t3 = System.nanoTime();
                            LOGGER.debug(StringUtilities
                                    .formatTimingMessage("Time to update " + ids.length + " values in nested cache: ", t3 - t2));
                        }
                    }
                    catch (final CacheException e)
                    {
                        LOGGER.error("Failed to cache data: " + e, e);
                    }
                    catch (final NotSerializableException e)
                    {
                        LOGGER.error("Data was not serializable for input object [" + input + "]: " + e, e);
                    }
                }
            };

            if (executor == null)
            {
                try
                {
                    runner.run();
                }
                catch (final RuntimeException e)
                {
                    LOGGER.error("Failed to cache data: " + e, e);
                }
            }
            else
            {
                executor.execute(runner);
            }
        }
    }

    /**
     * Get property values from the memory cache for a single property.
     *
     * @param propertyDescriptor The property to be retrieved.
     * @param resultMap Result map of property descriptors to lists of property
     *            values.
     * @param ids The ids of the models.
     * @param missingIndices An optional return array indicating what indices
     *            are not in the memory cache.
     * @param missingIds An optional return array indicating what ids are not in
     *            the memory cache.
     * @param missingCount The number of missing values, which is also the index
     *            of the next value in the missingFlags array.
     * @param missingFlags An array of flags that indicate if a value is
     *            missing.
     * @param <T> The property type.
     * @return The number of missing values.
     */
    protected <T> int getValues(PropertyDescriptor<T> propertyDescriptor, PropertyValueMap resultMap, long[] ids,
            int[] missingIndices, long[] missingIds, int missingCount, boolean[] missingFlags)
    {
        final List<? super T> result = resultMap.getResultList(propertyDescriptor);

        int missingIndex = missingCount;

        TLongObjectHashMap<Reference<Object>> cache;
        synchronized (myCacheMap)
        {
            cache = myCacheMap.get(propertyDescriptor);
        }
        if (cache == null)
        {
            for (int index = 0; index < ids.length; ++index)
            {
                if (missingIndices != null)
                {
                    missingIndices[index] = index;
                }
                if (missingIds != null)
                {
                    missingIds[index] = ids[index];
                }
            }
            result.addAll(New.<T>listOfNulls(ids.length));
            missingIndex = ids.length;
        }
        else
        {
            synchronized (cache)
            {
                for (int index = 0; index < ids.length; ++index)
                {
                    final Reference<Object> ref = cache.get(ids[index]);
                    T obj;
                    if (ref == null)
                    {
                        obj = null;
                    }
                    else
                    {
                        @SuppressWarnings("unchecked")
                        final
                        T cast = (T)ref.get();
                        obj = cast;
                        if (obj == null)
                        {
                            cache.remove(ids[index]);
                        }
                    }
                    if (obj == null)
                    {
                        if (missingFlags != null && !missingFlags[index] && missingIndex < ids.length)
                        {
                            missingFlags[index] = true;
                            if (missingIndices != null)
                            {
                                missingIndices[missingIndex] = index;
                            }
                            if (missingIds != null)
                            {
                                missingIds[missingIndex] = ids[index];
                            }
                            ++missingIndex;
                        }
                    }
                    else if (propertyDescriptor instanceof PropertyArrayDescriptor)
                    {
                        // Only put in the requested values, to be consistent
                        // with the persistent cache behavior.
                        final Object[] arr = (Object[])obj;
                        final PropertyArrayDescriptor pad = (PropertyArrayDescriptor)propertyDescriptor;
                        final int[] activeColumns = pad.getActiveColumns();
                        if (activeColumns.length < arr.length)
                        {
                            final Object[] filtered = new Object[activeColumns.length];
                            for (int ix = 0; ix < activeColumns.length; ++ix)
                            {
                                filtered[ix] = arr[activeColumns[ix]];
                            }
                            @SuppressWarnings("unchecked")
                            final
                            T cast = (T)filtered;
                            obj = cast;
                        }
                    }
                    result.add(obj);
                }
            }
        }
        return missingIndex;
    }

    /**
     * Pull the property values from the cache.
     *
     * @param resultMap Result map of property descriptors to lists of property
     *            values.
     * @param indices Which indices from the input id list need to be retrieved.
     * @param neededIds The needed ids.
     * @param failedIndices Indices into the id array of elements that could not
     *            be retrieved.
     * @return The number of ids matched.
     * @throws CacheException If the objects cannot be retrieved.
     */
    protected int getValuesFromNestedCache(PropertyValueMap resultMap, int[] indices, long[] neededIds, TIntList failedIndices)
        throws CacheException
    {
        boolean allFail = false;
        final PropertyValueMap cacheResultMap = new PropertyValueMap();
        for (final PropertyDescriptor<?> descriptor : resultMap.getPropertyDescriptors())
        {
            if (myNestedCache.acceptsPropertyDescriptor(descriptor))
            {
                cacheResultMap.addResultList(descriptor, neededIds.length);
            }
            else
            {
                allFail = true;
            }
        }
        myNestedCache.getValues(neededIds, cacheResultMap, failedIndices);
        int count = 0;
        for (final PropertyDescriptor<?> propertyDescriptor : cacheResultMap.getPropertyDescriptors())
        {
            count = copyResults(propertyDescriptor, indices, cacheResultMap, resultMap);
        }
        if (allFail)
        {
            for (final int index : indices)
            {
                failedIndices.add(index);
            }
        }
        return count - failedIndices.size();
    }

    /**
     * Cache objects in my in-memory map.
     *
     * @param <T> The type of the input objects.
     * @param ids The primary keys of the models as generated by the cache.
     * @param objects The object references.
     * @param accessors Objects that provide access to the models' properties.
     */
    private <T> void cacheObjects(long[] ids, Iterable<? extends T> objects,
            Collection<? extends PropertyAccessor<? super T, ?>> accessors)
    {
        for (final PropertyAccessor<? super T, ?> accessor : accessors)
        {
            // If this is a persistent property accessor and the value size is
            // over the threshold, don't put it in the memory cache.
            if (!accessor.getPropertyDescriptor().isCacheable()
                    || accessor instanceof PersistentPropertyAccessor && (!myCachingForPersistentDepositsEnabled
                            || accessor.getPropertyDescriptor().getEstimatedValueSizeBytes() > MAX_SOFT_CACHE_VALUE_BYTES))
            {
                continue;
            }

            final List<Object> values = new ArrayList<>(ids.length);
            final Iterator<? extends T> iterator = objects.iterator();
            T obj = iterator.next();
            if (iterator.hasNext())
            {
                for (int index = 0;;)
                {
                    if (ids[index++] != -1)
                    {
                        values.add(accessor.access(obj));
                    }
                    if (index < ids.length)
                    {
                        if (iterator.hasNext())
                        {
                            obj = iterator.next();
                        }
                        else
                        {
                            throw new IllegalArgumentException("Number of ids does not match number of input objects.");
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
            else
            {
                final Object value = accessor.access(objects.iterator().next());
                for (final long id : ids)
                {
                    if (id != -1)
                    {
                        values.add(value);
                    }
                }
            }

            final boolean overwrite = true;
            final boolean soft = accessor instanceof PersistentPropertyAccessor;
            final PropertyDescriptor<?> propertyDescriptor = accessor.getPropertyDescriptor();
            cacheObjects(propertyDescriptor, ids, values, overwrite, soft);
        }
    }

    /**
     * Cache property values in my in-memory map.
     *
     * @param propertyDescriptor The descriptor for the property being cached.
     * @param ids The primary keys of the models as generated by the cache.
     * @param values The property values.
     * @param overwrite Flag indicating if existing cached objects should be
     *            overwritten.
     * @param soft Flag indicating if the references should be soft references.
     */
    private void cacheObjects(PropertyDescriptor<?> propertyDescriptor, long[] ids, List<? extends Object> values,
            boolean overwrite, boolean soft)
    {
        // Only cache input streams in memory if they are hard references. If
        // they are soft references, it implies that they are stored
        // persistently, in which case they should always be streamed from the
        // persistent store, so that each consumer gets their own stream. If
        // they are hard references, it implies that they are not stored
        // persistently, and the original streams are the only ones available.
        // Therefore, we want to cache the original streams, and leave it up to
        // the consumers to organize themselves.
        if (soft && InputStream.class.isAssignableFrom(propertyDescriptor.getType()))
        {
            return;
        }

        // Get the value map for this property name.
        TLongObjectHashMap<Reference<Object>> map;
        synchronized (myCacheMap)
        {
            map = myCacheMap.get(propertyDescriptor);
            if (map == null)
            {
                map = new TLongObjectHashMap<>();
                myCacheMap.put(propertyDescriptor, map);
            }
        }

        synchronized (map)
        {
            for (int index = 0; index < ids.length; ++index)
            {
                final long id = ids[index];
                if (id != -1 && (overwrite || !map.containsKey(id)))
                {
                    final Object value = values.get(index);
                    final Reference<Object> ref = soft ? new SoftReference<>(value) : new StrongReference<>(value);
                    map.put(id, ref);
                }
            }
        }
    }

    /**
     * Copy results from one result map to another. Each element in the indices
     * array corresponds to an element in the from list, and indicates what
     * position the element should be inserted at in the to list.
     *
     * @param <T> The type of the values.
     * @param propertyDescriptor The descriptor for the property being copied.
     * @param indices The destination indices.
     * @param from The map to be copied from.
     * @param to The map to be copied to.
     * @return The number of values copied.
     */
    private <T> int copyResults(PropertyDescriptor<T> propertyDescriptor, int[] indices, PropertyValueMap from,
            PropertyValueMap to)
    {
        final List<T> fromList = from.getResultList(propertyDescriptor);
        final List<T> toList = to.getResultList(propertyDescriptor);
        for (int j = 0; j < indices.length && j < fromList.size(); ++j)
        {
            toList.set(indices[j], fromList.get(j));
        }
        return fromList.size();
    }

    /**
     * Search for some ids in the cache. Add the found objects to the result map
     * and the indices and ids of the missing models to the missing collections.
     *
     * @param resultMap Result map of property descriptors to lists of property
     *            values.
     * @param ids The ids of the models.
     * @param missingIndices An optional return array indicating what indices
     *            are not in the memory cache.
     * @param missingIds An optional return array indicating what ids are not in
     *            the memory cache.
     * @return The number of missing values.
     */
    private int getValues(PropertyValueMap resultMap, long[] ids, int[] missingIndices, long[] missingIds)
    {
        int missingCount = 0;
        final boolean[] missingFlags = missingIndices == null && missingIds == null ? null : new boolean[ids.length];
        for (final PropertyDescriptor<?> propertyDescriptor : resultMap.getPropertyDescriptors())
        {
            missingCount = getValues(propertyDescriptor, resultMap, ids, missingIndices, missingIds, missingCount, missingFlags);
        }
        return missingCount;
    }

    /**
     * Notify a cache modification listener of cache modifications related to
     * non-persistent accessors.
     *
     * @param listener The listener.
     * @param accessors The full set of accessors, including persistent ones.
     * @param ids The ids for the inserted records.
     *
     * @throws CacheException If there is a cache error.
     */
    private void notifyListenerForNonPersistentAccessors(final CacheModificationListener listener,
            Collection<? extends PropertyAccessor<?, ?>> accessors, long[] ids) throws CacheException
    {
        final Collection<PropertyDescriptor<?>> transientPropertyDescriptors = New.collection();
        for (final PropertyAccessor<?, ?> propertyAccessor : accessors)
        {
            if (!(propertyAccessor instanceof PersistentPropertyAccessor))
            {
                transientPropertyDescriptors.add(propertyAccessor.getPropertyDescriptor());
            }
        }
        if (transientPropertyDescriptors.isEmpty())
        {
            return;
        }
        final int[] distinctGroupIds = getGroupIds(ids, true);
        final List<DataModelCategory> dataModelCategories = getDataModelCategoriesByGroupId(distinctGroupIds, true, true, true, false);
        final TIntObjectHashMap<CacheModificationReport> groupIdToReportMap = new TIntObjectHashMap<>();
        for (int index = 0; index < distinctGroupIds.length; ++index)
        {
            groupIdToReportMap.put(distinctGroupIds[index],
                    new CacheModificationReport(dataModelCategories.get(index), new long[0], transientPropertyDescriptors));
        }

        CacheIdUtilities.forEachGroup(ids, (long[] combinedIds0, int groupId, int[] dataIds) ->
        {
            final CacheModificationReport report = groupIdToReportMap.get(groupId);

            groupIdToReportMap.put(groupId, new CacheModificationReport(report.getDataModelCategory(),
                    Utilities.concatenate(report.getIds(), combinedIds0), report.getPropertyDescriptors()));
        });

        groupIdToReportMap.forEachValue(report ->
        {
            if (report.getIds().length > 0)
            {
                listener.cacheModified(report);
            }
            return true;
        });
    }

    /**
     * Remove objects from the object cache.
     *
     * @param dataModelCategory The data model category for the removed objects.
     * @param ids The ids of the objects.
     * @return A map of property descriptors to removed property values.
     */
    private Map<PropertyDescriptor<?>, Iterable<?>> removeValues(DataModelCategory dataModelCategory, long[] ids)
    {
        Map<PropertyDescriptor<?>, TLongObjectHashMap<Reference<Object>>> transientCache;
        synchronized (myCacheMap)
        {
            transientCache = new HashMap<>(myCacheMap);
        }

        final Map<PropertyDescriptor<?>, Iterable<?>> removedValues = new HashMap<>();
        for (final Entry<PropertyDescriptor<?>, TLongObjectHashMap<Reference<Object>>> entry : transientCache.entrySet())
        {
            @SuppressWarnings("unchecked")
            final
            PropertyDescriptor<Object> propertyDescriptor = (PropertyDescriptor<Object>)entry.getKey();
            final TLongObjectHashMap<Reference<Object>> map = entry.getValue();
            final List<Object> values = new ArrayList<>(ids.length);
            synchronized (map)
            {
                for (int index = 0; index < ids.length; ++index)
                {
                    final long id = ids[index];
                    final Reference<Object> obj = map.remove(id);
                    final Object value = obj == null ? null : obj.get();
                    if (value != null)
                    {
                        values.add(value);
                    }
                }
            }

            if (!values.isEmpty())
            {
                removedValues.put(propertyDescriptor, values);
            }
        }

        return removedValues;
    }

    /**
     * Throw an {@link UnsupportedOperationException} if there is no nested
     * cache.
     */
    private void verifyNestedCache()
    {
        if (myNestedCache == null)
        {
            throw new UnsupportedOperationException("Cannot query for ids with no nested cache installed.");
        }
    }
}
