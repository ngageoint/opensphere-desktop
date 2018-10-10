package io.opensphere.core.data;

import java.io.InputStream;
import java.io.NotSerializableException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.cache.Cache;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.PropertyValueMap;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.DataRegistryListenerManager.ChangeType;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A data provider that automatically caches retrieved data. The cache is
 * <b>not</b> queried for data.
 */
public class CachingDataRegistryDataProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CachingDataRegistryDataProvider.class);

    /** The cache used to cache retrieved data. */
    private final Cache myCache;

    /** The nested data provider. */
    private final DataRegistryDataProvider myDataProvider;

    /** The executor to use for queries. */
    private final ThreadPoolExecutor myExecutor;

    /**
     * Constructor.
     *
     * @param dataProvider The nested data provider.
     * @param executor The executor to use for queries.
     * @param cache The cache to use for retrieved data.
     */
    public CachingDataRegistryDataProvider(DataRegistryDataProvider dataProvider, ThreadPoolExecutor executor, Cache cache)
    {
        Utilities.checkNull(dataProvider, "dataProvider");
        Utilities.checkNull(executor, "executor");
        Utilities.checkNull(cache, "cache");
        myDataProvider = dataProvider;
        myExecutor = executor;
        myCache = cache;
        myExecutor.getActiveCount();
    }

    /**
     * Get the nested data provider.
     *
     * @return The data provider.
     */
    public DataRegistryDataProvider getDataProvider()
    {
        return myDataProvider;
    }

    /**
     * Get ratio of the number of running threads to the maximum for this
     * executor.
     *
     * @return The executor saturation ratio.
     */
    public double getExecutorSaturation()
    {
        return (double)myExecutor.getActiveCount() / myExecutor.getMaximumPoolSize();
    }

    /**
     * Get the satisfaction that can be had from my nested data provider.
     *
     * @param dataModelCategory The data model category.
     * @param intervalSets The interval sets of interest.
     * @return The satisfaction.
     */
    public Collection<? extends Satisfaction> getSatisfaction(DataModelCategory dataModelCategory,
            Collection<? extends IntervalPropertyValueSet> intervalSets)
    {
        try
        {
            return myDataProvider.getSatisfaction(dataModelCategory, intervalSets);
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Exception encountered getting satisfactions from data provider: " + e, e);
            return Collections.emptySet();
        }
    }

    /**
     * Determine if this provider can provide data for a certain category.
     *
     * @param category The category, which may contain {@code null}s for
     *            wildcards.
     * @return {@code true} if the category can be handled.
     */
    public boolean providesDataFor(DataModelCategory category)
    {
        try
        {
            return myDataProvider.providesDataFor(category);
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Exception encountered in providesDataFor: " + e, e);
            return false;
        }
    }

    /**
     * Query the nested data provider and cache the results.
     *
     * @param tracker The query tracker.
     * @param intervalQuery Flag indicating if this is an interval-based query.
     * @param listenerManager Listener for cache modifications.
     */
    public void query(final MutableQueryTracker tracker, final boolean intervalQuery,
            final DataRegistryListenerManager listenerManager)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(new StringBuilder().append("Sending query to ").append(myDataProvider).append(" for ")
                    .append(tracker.getSatisfactions()));
        }
        Runnable runner = tracker.wrapRunnable(() ->
        {
            if (tracker.isCancelled())
            {
                return;
            }
            Query query = tracker.getQuery();

            Collection<PropertyDescriptor<?>> propertyDescriptors = getPropertyDescriptors(query);
            CacheDepositReceiver cacheDepositReceiver = new CachingCacheDepositReceiver(tracker, listenerManager);

            try
            {
                Collection<? extends Satisfaction> satisfactions = intervalQuery ? tracker.getSatisfactions() : null;
                myDataProvider.query(query.getDataModelCategory(), satisfactions, tracker.getParameters(),
                        query.getOrderSpecifiers(), query.getLimit(), propertyDescriptors, cacheDepositReceiver);
                tracker.setQueryStatus(QueryStatus.SUCCESS, (Throwable)null);
            }
            catch (InterruptedException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Data provider query interrupted.");
                }

                // The interrupt is likely because the tracker was
                // cancelled already, but let us be sure.
                tracker.cancel(true);
            }
            catch (RuntimeException e)
            {
                tracker.setQueryStatus(QueryStatus.FAILED, e);
                LOGGER.error("Query failed: " + e, e);
                throw e;
            }
            catch (QueryException e)
            {
                tracker.setQueryStatus(QueryStatus.FAILED, e);
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Query failed: " + e, e);
                }
            }
        });
        myExecutor.execute(runner);
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64).append(getClass().getSimpleName()).append('[').append(myDataProvider).append(']').toString();
    }

    /**
     * Extract the property descriptors from a query.
     *
     * @param query The query.
     * @return The property descriptors.
     */
    private Collection<PropertyDescriptor<?>> getPropertyDescriptors(Query query)
    {
        Collection<PropertyDescriptor<?>> propertyDescriptors = New.collection(query.getPropertyValueReceivers().size());
        for (PropertyValueReceiver<?> propertyValueReceiver : query.getPropertyValueReceivers())
        {
            propertyDescriptors.add(propertyValueReceiver.getPropertyDescriptor());
        }
        return propertyDescriptors;
    }

    /**
     * Send any input streams associated with some model ids to some property
     * value receivers.
     *
     * @param ids The model ids.
     * @param receivers The property value receivers.
     * @throws CacheException If there's an error retrieving the values from the
     *             cache.
     */
    private void sendStreamsToPropertyReceivers(long[] ids, Collection<? extends PropertyValueReceiver<?>> receivers)
            throws CacheException
    {
        PropertyValueMap resultMap = new PropertyValueMap();
        for (PropertyValueReceiver<?> propertyValueReceiver : receivers)
        {
            if (InputStream.class.isAssignableFrom(propertyValueReceiver.getPropertyDescriptor().getType()))
            {
                resultMap.addResultList(propertyValueReceiver.getPropertyDescriptor(), ids.length);
            }
        }
        if (!resultMap.isEmpty())
        {
            TIntList failedIndices = new TIntArrayList();
            myCache.getValues(ids, resultMap, failedIndices);

            for (PropertyValueReceiver<?> receiver : receivers)
            {
                sendToPropertyReceiver(resultMap, receiver);
            }
        }
    }

    /**
     * Send the values of a property to a property value receiver.
     *
     * @param <S> The type of the property values.
     * @param <T> The type of the input objects.
     * @param input The input objects.
     * @param accessor The object that will access the property values in the
     *            input objects.
     * @param receiver The property value receiver.
     */
    private <S, T> void sendToPropertyReceiver(Iterable<? extends T> input, PropertyAccessor<? super T, S> accessor,
            PropertyValueReceiver<?> receiver)
    {
        if (accessor.getPropertyDescriptor().equals(receiver.getPropertyDescriptor()))
        {
            @SuppressWarnings("unchecked")
            PropertyValueReceiver<S> typedReceiver = (PropertyValueReceiver<S>)receiver;

            List<S> values = New.list(input instanceof Collection ? ((Collection<?>)input).size() : 10);
            for (T obj : input)
            {
                values.add(accessor.access(obj));
            }
            typedReceiver.receive(values);
        }
        else
        {
            LOGGER.error("Accessor descriptor: " + accessor.getPropertyDescriptor());
            LOGGER.error("Receiver descriptor: " + receiver.getPropertyDescriptor());
            throw new IllegalArgumentException("Accessor has a different property descriptor from receiver.");
        }
    }

    /**
     * Send the values from a {@link PropertyValueMap} to a
     * {@link PropertyValueReceiver}.
     *
     * @param <T> The type of the values.
     * @param resultMap The result map.
     * @param receiver The receiver.
     */
    private <T> void sendToPropertyReceiver(PropertyValueMap resultMap, PropertyValueReceiver<T> receiver)
    {
        receiver.receive(resultMap.getResultList(receiver.getPropertyDescriptor()));
    }

    /**
     * Send the values from a {@link CacheDeposit} to the
     * {@link PropertyValueReceiver}s in a {@link Query}.
     *
     * @param <T> The type of the objects in the deposit.
     * @param deposit The cache deposit.
     * @param tracker The query tracker.
     */
    private <T> void sendToPropertyReceivers(final CacheDeposit<T> deposit, MutableQueryTracker tracker)
    {
        Iterator<? extends PropertyAccessor<? super T, ?>> accessorIter = deposit.getAccessors().iterator();
        PropertyAccessor<? super T, ?> accessor = null;
        for (PropertyValueReceiver<?> propertyValueReceiver : (Collection<? extends PropertyValueReceiver<?>>)tracker.getQuery()
                .getPropertyValueReceivers())
        {
            // Do not send input streams now, to avoid concurrent access.
            if (!InputStream.class.isAssignableFrom(propertyValueReceiver.getPropertyDescriptor().getType()))
            {
                // This logic is an optimization to take advantage of the
                // deposit accessors and property value receivers likely being
                // in the same order.
                boolean iteratorCreatedForThisProperty = false;
                while (accessor == null
                        || !accessor.getPropertyDescriptor().equals(propertyValueReceiver.getPropertyDescriptor()))
                {
                    if (accessorIter == null || !accessorIter.hasNext())
                    {
                        if (iteratorCreatedForThisProperty)
                        {
                            // Avoid an infinite loop if no accessor exists for
                            // this property.
                            break;
                        }
                        accessorIter = deposit.getAccessors().iterator();
                        iteratorCreatedForThisProperty = true;
                    }
                    accessor = accessorIter.next();
                }
                if (accessor != null)
                {
                    // TODO: is there a way to do this multi-threaded?
                    sendToPropertyReceiver(deposit.getInput(), accessor, propertyValueReceiver);
                }
            }
        }
    }

    /**
     * This receives {@link CacheDeposit}s and sends their property values to
     * the property receivers in {@link #myTracker} and also to
     * {@link CachingDataRegistryDataProvider#myCache}.
     */
    private final class CachingCacheDepositReceiver implements CacheDepositReceiver
    {
        /** Manager for data registry listeners. */
        private final DataRegistryListenerManager myDataRegistryListenerManager;

        /** The query tracker. */
        private final MutableQueryTracker myTracker;

        /**
         * Constructor.
         *
         * @param tracker The query tracker.
         * @param listenerManager Manager for data registry listeners.
         */
        public CachingCacheDepositReceiver(MutableQueryTracker tracker, DataRegistryListenerManager listenerManager)
        {
            myTracker = tracker;
            myDataRegistryListenerManager = listenerManager;
        }

        @Override
        public <T> long[] receive(final CacheDeposit<T> deposit) throws CacheException
        {
            try
            {
                sendToPropertyReceivers(deposit, myTracker);
                long[] ids = myCache.put(deposit,
                        cacheModificationReport -> myDataRegistryListenerManager.notifyAddsOrUpdates(cacheModificationReport,
                                cacheModificationReport.getIds(), deposit.getInput(),
                                cacheModificationReport.filterAccessors(deposit.getAccessors()), ChangeType.ADD,
                                getDataProvider()));
                myTracker.addIds(ids);

                sendStreamsToPropertyReceivers(ids, myTracker.getQuery().getPropertyValueReceivers());

                return ids;
            }
            catch (NotSerializableException e)
            {
                throw new CacheException("Failed to cache data: " + e, e);
            }
            catch (CacheException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Failed to cache data: " + e, e);
                }
                throw e;
            }
        }
    }
}
