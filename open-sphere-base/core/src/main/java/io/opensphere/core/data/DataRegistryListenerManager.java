package io.opensphere.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.opensphere.core.cache.CacheModificationReport;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.util.ref.SoftReference;

/**
 * Manager for data registry listeners. This keeps track of the listeners and
 * handles notifying them of data registry changes.
 */
@SuppressWarnings("PMD.GodClass")
public class DataRegistryListenerManager
{
    /**
     * Category to listener map. Listeners only in appear in one of the maps.
     */
    private final Map<String, List<ListenerData<?>>> myCategoryToListenerMap = new ConcurrentHashMap<>();

    /** Executor to use when notifying listeners. */
    private final ExecutorService myExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("DataRegistry-listener"));

    /** Family to listener map. Listeners only in appear in one of the maps. */
    private final Map<String, List<ListenerData<?>>> myFamilyToListenerMap = new ConcurrentHashMap<>();

    /**
     * List of listeners that want updates for all data model categories. These
     * listeners do not appear in the other maps.
     */
    private final List<ListenerData<?>> myListenersForAll = Collections.synchronizedList(new ArrayList<ListenerData<?>>());

    /** Source to listener map. Listeners only in appear in one of the maps. */
    private final Map<String, List<ListenerData<?>>> mySourceToListenerMap = new ConcurrentHashMap<>();

    /** Count of listeners that want removed objects. */
    private final AtomicInteger myWantRemovedObjectsListenerCount = new AtomicInteger();

    /**
     * Add a listener to be notified of data registry modifications.
     *
     * @param <T> The type of the property of interest to the listener.
     * @param listener The listener.
     * @param dataModelCategory The data model category of interest. The data
     *            model category may contain <code>null</code>s to indicate
     *            wildcards.
     * @param propertyDescriptor Descriptor for the property the listener is
     *            interested in.
     */
    public synchronized <T> void addChangeListener(DataRegistryListener<T> listener, DataModelCategory dataModelCategory,
            PropertyDescriptor<T> propertyDescriptor)
    {
        if (listener.isWantingRemovedObjects())
        {
            myWantRemovedObjectsListenerCount.incrementAndGet();
        }
        ListenerData<T> listenerData = new ListenerData<>(listener, dataModelCategory, propertyDescriptor);
        if (dataModelCategory.getFamily() != null)
        {
            CollectionUtilities.multiMapAdd(myFamilyToListenerMap, dataModelCategory.getFamily(), listenerData, true);
        }
        else if (dataModelCategory.getCategory() != null)
        {
            CollectionUtilities.multiMapAdd(myCategoryToListenerMap, dataModelCategory.getCategory(), listenerData, true);
        }
        else if (dataModelCategory.getSource() != null)
        {
            CollectionUtilities.multiMapAdd(mySourceToListenerMap, dataModelCategory.getSource(), listenerData, true);
        }
        else
        {
            myListenersForAll.add(listenerData);
        }
    }

    /**
     * Determine if there is a listener for a certain category.
     *
     * @param dataModelCategory The data model category.
     * @return If a listener exists, {@code true}.
     */
    public boolean hasListenersForCategory(DataModelCategory dataModelCategory)
    {
        return !myListenersForAll.isEmpty() || !getListeners(dataModelCategory).isEmpty();
    }

    /**
     * Get if there are any listeners that want removed objects.
     *
     * @return If there are any listeners that want removed objects.
     */
    public boolean isWantingRemovedObjects()
    {
        return myWantRemovedObjectsListenerCount.get() > 0;
    }

    /**
     * Notify interested listeners of changes to the data registry.
     *
     * @param <T> The type of the input objects.
     * @param cacheModificationReport The cache modification report.
     * @param ids The ids for the changed models, in the same order as the input
     *            objects.
     * @param input The input objects that are the sources of the property
     *            values.
     * @param accessors The accessors for the property values.
     * @param type The change type.
     * @param source the originator of the change
     */
    public <T> void notifyAddsOrUpdates(CacheModificationReport cacheModificationReport, long[] ids, Iterable<? extends T> input,
            Collection<? extends PropertyAccessor<? super T, ?>> accessors, final ChangeType type, final Object source)
    {
        final DataModelCategory dataModelCategory = cacheModificationReport.getDataModelCategory();
        Collection<? extends ListenerData<?>> listeners = getListeners(dataModelCategory);
        for (ListenerData<?> listenerData : listeners)
        {
            notifyAddsOrUpdates(ids, input, accessors, type, cacheModificationReport, dataModelCategory, listenerData, source);
        }
    }

    /**
     * Notify all the listeners that all values have been removed.
     *
     * @param source the originator of the change
     */
    public void notifyAllRemoved(final Object source)
    {
        Collection<? extends ListenerData<?>> listeners = getAllListeners();
        for (ListenerData<?> listenerData : listeners)
        {
            final DataRegistryListener<?> listener = listenerData.getListener();
            if (listener != null)
            {
                myExecutor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.allValuesRemoved(source);
                    }
                });
            }
        }
    }

    /**
     * Notify interested listeners of models removed from the data registry.
     *
     * @param dataModelCategory The data model category for the removed models.
     * @param ids The ids for the removed models.
     * @param source The originator of the change.
     * @return A latch that can be used to know when the listeners have been
     *         notified.
     */
    public CountDownLatch notifyRemoves(final DataModelCategory dataModelCategory, final long[] ids, final Object source)
    {
        Collection<? extends ListenerData<?>> listeners = getListeners(dataModelCategory);
        final CountDownLatch latch = new CountDownLatch(listeners.size());
        for (ListenerData<?> listenerData : listeners)
        {
            final DataRegistryListener<?> listener = listenerData.getListener();
            if (listener == null)
            {
                latch.countDown();
            }
            else
            {
                myExecutor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            listener.valuesRemoved(dataModelCategory, ids, source);
                        }
                        finally
                        {
                            latch.countDown();
                        }
                    }
                });
            }
        }

        return latch;
    }

    /**
     * Notify interested listeners of models removed from the data registry.
     *
     * @param <T> The type of the property values.
     *
     * @param dataModelCategory The data model category for the removed models.
     * @param ids The ids for the removed models.
     * @param propertyDescriptor The property descriptor.
     * @param values The removed values (if available).
     * @param source The originator of the change.
     * @return A latch that can be used to know when the listeners have been
     *         notified.
     */
    public <T> CountDownLatch notifyRemoves(final DataModelCategory dataModelCategory, long[] ids,
            PropertyDescriptor<T> propertyDescriptor, Iterable<? extends T> values, Object source)
    {
        Collection<? extends ListenerData<?>> listeners = getListeners(dataModelCategory);
        CountDownLatch latch = new CountDownLatch(listeners.size());
        for (ListenerData<?> listenerData : listeners)
        {
            notifyRemoves(dataModelCategory, ids, propertyDescriptor, values, listenerData, source, latch);
        }

        return latch;
    }

    /**
     * Remove a listener.
     *
     * @param listener The listener.
     */
    public void removeChangeListener(DataRegistryListener<?> listener)
    {
        if (listener.isWantingRemovedObjects())
        {
            myWantRemovedObjectsListenerCount.decrementAndGet();
        }
        removeListener(listener, mySourceToListenerMap.values());
        removeListener(listener, myFamilyToListenerMap.values());
        removeListener(listener, myCategoryToListenerMap.values());
        removeListener(listener, myListenersForAll);
    }

    /**
     * Get all the listeners.
     *
     * @return The listeners.
     */
    protected Collection<? extends ListenerData<?>> getAllListeners()
    {
        Collection<ListenerData<?>> listeners = new ArrayList<>();
        getAllListeners(myCategoryToListenerMap, listeners);
        getAllListeners(myFamilyToListenerMap, listeners);
        getAllListeners(mySourceToListenerMap, listeners);
        getListeners((DataModelCategory)null, myListenersForAll, listeners);
        return listeners;
    }

    /**
     * Get listeners interested in changes to some properties.
     *
     * @param dataModelCategory The data model category for the changes.
     * @return The listeners.
     */
    protected Collection<? extends ListenerData<?>> getListeners(DataModelCategory dataModelCategory)
    {
        Collection<ListenerData<?>> listeners = new ArrayList<>();
        getListeners(dataModelCategory, myCategoryToListenerMap, dataModelCategory.getCategory(), listeners);
        getListeners(dataModelCategory, myFamilyToListenerMap, dataModelCategory.getFamily(), listeners);
        getListeners(dataModelCategory, mySourceToListenerMap, dataModelCategory.getSource(), listeners);
        getListeners((DataModelCategory)null, myListenersForAll, listeners);
        return listeners;
    }

    /**
     * Given some input objects, their corresponding ids, and a subset of the
     * ids, generate an iterable that iterates over values returned by a
     * property accessor operating on the input objects with ids matching the
     * subset of ids.
     *
     * @param <S> The types of the input objects.
     * @param <T> The type of the property values.
     * @param ids The ids of the objects to be iterated.
     * @param inputIds The ids corresponding to the input objects.
     * @param input The input objects.
     * @param propertyAccessor The property accessor.
     * @return The selected ids, in the order that objects will be returned from
     *         the iterable, and an iterable that returns the property values.
     */
    private <S, T> Pair<long[], Iterable<T>> generateIterable(long[] ids, final long[] inputIds,
            final Iterable<? extends S> input, final PropertyAccessor<S, ?> propertyAccessor)
    {
        long[] idsToReport;
        Iterable<T> newValues;

        Iterator<? extends S> iterator = input.iterator();
        if (iterator.hasNext())
        {
            S obj = iterator.next();
            if (iterator.hasNext())
            {
                // If the id arrays are the same length, then only one data
                // model category was affected, and the array contents should be
                // identical. This means that no special handling is required to
                // make sure that the values passed to the listeners match the
                // "ids" array.
                if (ids.length == inputIds.length)
                {
                    newValues = new AccessingIterable<>(input, propertyAccessor);
                    idsToReport = ids;
                }
                else
                {
                    // This is the case there the "ids" array does not contain
                    // the same ids as the "inputIds" array. The "ids" array
                    // indicates what needs to be sent to the listener, but the
                    // "inputIds" array indicates what ids are associated with
                    // what input objects.

                    final long[] sortedIds = ids.clone();
                    Arrays.sort(sortedIds);

                    idsToReport = new long[ids.length];
                    int index = 0;
                    for (int inputIdIndex = 0; inputIdIndex < inputIds.length; ++inputIdIndex)
                    {
                        if (Arrays.binarySearch(sortedIds, inputIds[inputIdIndex]) >= 0)
                        {
                            idsToReport[index++] = inputIds[inputIdIndex];
                        }
                    }
                    newValues = new SkippingAccessingIterable<>(inputIds, input, propertyAccessor, sortedIds);
                }
            }
            else
            {
                @SuppressWarnings("unchecked")
                final T value = (T)propertyAccessor.access(obj);
                newValues = new RepeatingIterable<>(value, ids.length);
                idsToReport = ids;
            }
        }
        else
        {
            newValues = new AccessingIterable<>(input, propertyAccessor);
            idsToReport = ids;
        }

        final Pair<long[], Iterable<T>> pair = Pair.create(idsToReport, newValues);
        return pair;
    }

    /**
     * Get all the listeners from a map.
     *
     * @param map The map.
     * @param listeners The return collection of listeners.
     */
    private void getAllListeners(Map<String, List<ListenerData<?>>> map, Collection<? super ListenerData<?>> listeners)
    {
        for (List<ListenerData<?>> list : map.values())
        {
            getListeners((DataModelCategory)null, list, listeners);
        }
    }

    /**
     * Copy the listener data from one collection to another, removing invalid
     * listeners from the <i>from</i> list. This synchronizes on the <i>from</i>
     * list.
     *
     * @param dmc The data model category.
     * @param from The input collection.
     * @param to The return collection.
     */
    private void getListeners(DataModelCategory dmc, Collection<? extends ListenerData<?>> from,
            Collection<? super ListenerData<?>> to)
    {
        synchronized (from)
        {
            for (Iterator<? extends ListenerData<?>> iter = from.iterator(); iter.hasNext();)
            {
                ListenerData<?> listenerData = iter.next();
                if (listenerData.getListener() == null)
                {
                    iter.remove();
                }
                else
                {
                    if (dmc == null)
                    {
                        to.add(listenerData);
                    }
                    else
                    {
                        DataModelCategory listenerDmc = listenerData.getDataModelCategory();
                        if (listenerDmc.getSource() != null && !listenerDmc.getSource().equals(dmc.getSource()))
                        {
                            continue;
                        }
                        else if (listenerDmc.getFamily() != null && !listenerDmc.getFamily().equals(dmc.getFamily()))
                        {
                            continue;
                        }
                        else if (listenerDmc.getCategory() != null && !listenerDmc.getCategory().equals(dmc.getCategory()))
                        {
                            continue;
                        }
                        else
                        {
                            to.add(listenerData);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the listeners from a map that match a particular key and data model
     * category.
     *
     * @param dmc The data model category the listeners are interested in.
     * @param map The map.
     * @param key The key.
     * @param listeners The return collection.
     */
    private void getListeners(DataModelCategory dmc, Map<String, List<ListenerData<?>>> map, String key,
            Collection<? super ListenerData<?>> listeners)
    {
        if (key != null)
        {
            List<ListenerData<?>> list = map.get(key);
            if (list != null)
            {
                getListeners(dmc, list, listeners);
            }
        }
    }

    /**
     * Notify a listener of changes to the data registry.
     *
     * @param <S> Property type expected by the listener.
     * @param <T> The type of the input objects.
     * @param ids The ids for the changed models, in the same order as the input
     *            objects.
     * @param input The input objects that are the sources of the property
     *            values.
     * @param accessors The accessors for the property values.
     * @param type The change type.
     * @param report The cache modification report to be delivered.
     * @param dataModelCategory The affected data model category.
     * @param listenerData The listener data.
     * @param source the source of the change
     */
    private <S, T> void notifyAddsOrUpdates(long[] ids, Iterable<? extends T> input,
            Collection<? extends PropertyAccessor<? super T, ?>> accessors, final ChangeType type, CacheModificationReport report,
            final DataModelCategory dataModelCategory, ListenerData<S> listenerData, final Object source)
    {
        final DataRegistryListener<S> listener = listenerData.getListener();
        if (listener != null)
        {
            final PropertyDescriptor<S> desc = listenerData.getPropertyDescriptor();
            for (PropertyAccessor<? super T, ?> acc : accessors)
            {
                PropertyDescriptor<?> accessorDesc = acc.getPropertyDescriptor();
                if (desc.getType().isAssignableFrom(accessorDesc.getType())
                        && accessorDesc.getPropertyName().equals(desc.getPropertyName()))
                {
                    // Because of the check above, this Iterable can be declared
                    // as <S>.
                    final Pair<long[], Iterable<S>> pair = generateIterable(report.getIds(), ids, input, acc);

                    myExecutor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (type == ChangeType.ADD)
                            {
                                listener.valuesAdded(dataModelCategory, pair.getFirstObject(), pair.getSecondObject(), source);
                            }
                            else if (type == ChangeType.UPDATE)
                            {
                                listener.valuesUpdated(dataModelCategory, pair.getFirstObject(), pair.getSecondObject(), source);
                            }
                            else
                            {
                                throw new UnexpectedEnumException(type);
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Notify a listener of some removes.
     *
     * @param <S> The type expected by the listener.
     * @param <T> The type of the removed values.
     * @param dataModelCategory The affected data model category.
     * @param ids The ids for the removed models.
     * @param propertyDescriptor The property descriptor for the removed models.
     * @param values the removed values (if available).
     * @param listenerData The listener.
     * @param source The originator of the change.
     * @param latch A latch to be counted down when listeners complete.
     */
    private <S, T> void notifyRemoves(final DataModelCategory dataModelCategory, final long[] ids,
            final PropertyDescriptor<T> propertyDescriptor, final Iterable<? extends T> values, ListenerData<S> listenerData,
            final Object source, final CountDownLatch latch)
    {
        final DataRegistryListener<S> listener = listenerData.getListener();
        PropertyDescriptor<S> listenerDesc = listenerData.getPropertyDescriptor();
        if (listener != null && listenerDesc.getType().isAssignableFrom(propertyDescriptor.getType()))
        {
            myExecutor.execute(new Runnable()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void run()
                {
                    try
                    {
                        ((DataRegistryListener<T>)listener).valuesRemoved(dataModelCategory, ids, values, source);
                    }
                    finally
                    {
                        latch.countDown();
                    }
                }
            });
        }
        else
        {
            latch.countDown();
        }
    }

    /**
     * Remove a listener from some lists.
     *
     * @param listener The listener.
     * @param lists The lists.
     */
    private void removeListener(DataRegistryListener<?> listener, Collection<List<ListenerData<?>>> lists)
    {
        for (List<ListenerData<?>> list : lists)
        {
            removeListener(listener, list);
        }
    }

    /**
     * Remove a listener from a list of listener data.
     *
     * @param listener The listener.
     * @param list The list.
     */
    private void removeListener(DataRegistryListener<?> listener, List<ListenerData<?>> list)
    {
        synchronized (list)
        {
            for (Iterator<ListenerData<?>> iter = list.iterator(); iter.hasNext();)
            {
                ListenerData<?> data = iter.next();
                DataRegistryListener<?> listener2 = data.getListener();
                if (listener2 == null || listener2.equals(listener))
                {
                    iter.remove();
                }
            }
        }
    }

    /** Enumeration of change types. */
    protected enum ChangeType
    {
        /** Type for adds. */
        ADD,

        /** Type for updates. */
        UPDATE,
    }

    /**
     * Class used to associate listeners with their property descriptors.
     *
     * @param <T> The type of the property values of interest to the listener.
     */
    protected static class ListenerData<T>
    {
        /** The data model category. */
        private final DataModelCategory myDataModelCategory;

        /** The listener. */
        private final SoftReference<DataRegistryListener<T>> myListener;

        /** The property descriptor associated with the listener. */
        private final PropertyDescriptor<T> myPropertyDescriptor;

        /**
         * Constructor.
         *
         * @param listener The listener.
         * @param dataModelCategory The category of interest.
         * @param propertyDescriptor The property of interest.
         */
        public ListenerData(DataRegistryListener<T> listener, DataModelCategory dataModelCategory,
                PropertyDescriptor<T> propertyDescriptor)
        {
            myListener = new SoftReference<>(listener);
            myDataModelCategory = dataModelCategory;
            myPropertyDescriptor = propertyDescriptor;
        }

        /**
         * Accessor for the dataModelCategory.
         *
         * @return The dataModelCategory.
         */
        public DataModelCategory getDataModelCategory()
        {
            return myDataModelCategory;
        }

        /**
         * Accessor for the listener.
         *
         * @return The listener.
         */
        public DataRegistryListener<T> getListener()
        {
            return myListener.get();
        }

        /**
         * Accessor for the propertyDescriptor.
         *
         * @return The propertyDescriptor.
         */
        public PropertyDescriptor<T> getPropertyDescriptor()
        {
            return myPropertyDescriptor;
        }
    }

    /**
     * An iterable that iterates over the results from a property accessor
     * called with the elements of a nested collection.
     *
     * @param <S> The type of the source objects.
     * @param <T> The type of the return objects.
     */
    private static class AccessingIterable<S, T> implements Iterable<T>
    {
        /** The input objects. */
        private final Iterable<? extends S> myInput;

        /** The accessor. */
        private final PropertyAccessor<S, ?> myPropertyAccessor;

        /**
         * Construct the iterable.
         *
         * @param input The input objects.
         * @param propertyAccessor The accessor for the objects.
         */
        public AccessingIterable(Iterable<? extends S> input, PropertyAccessor<S, ?> propertyAccessor)
        {
            myPropertyAccessor = propertyAccessor;
            myInput = input;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new Iterator<T>()
            {
                /** Iterator over input objects. */
                private final Iterator<? extends S> myIter = myInput.iterator();

                @Override
                public boolean hasNext()
                {
                    return myIter.hasNext();
                }

                @SuppressWarnings("unchecked")
                @Override
                public T next()
                {
                    return (T)myPropertyAccessor.access(myIter.next());
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException("Remove is not supported.");
                }
            };
        }
    }

    /**
     * An iterable that returns the same object some set number of times.
     *
     * @param <T> The type of the object.
     */
    private static class RepeatingIterable<T> implements Iterable<T>
    {
        /** How many times to return the object. */
        private final int myLimit;

        /** The value to return. */
        private final T myValue;

        /**
         * Constructor.
         *
         * @param value The value to return.
         * @param times The number of times to return the value.
         */
        public RepeatingIterable(T value, int times)
        {
            myValue = value;
            myLimit = times;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new Iterator<T>()
            {
                /** Counter. */
                private int myCount;

                @Override
                public boolean hasNext()
                {
                    return myCount < myLimit;
                }

                @Override
                public T next()
                {
                    if (myCount >= myLimit)
                    {
                        throw new NoSuchElementException();
                    }
                    myCount++;
                    return myValue;
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException("Remove is not supported.");
                }
            };
        }
    }

    /**
     * A specialized iterable that is similar to {@link AccessingIterable}, but
     * has two arrays of ids that it uses to determine which input objects to
     * use when returning values.
     *
     * @param <S> The type of the source objects.
     * @param <T> The type of the return objects.
     */
    private static class SkippingAccessingIterable<S, T> implements Iterable<T>
    {
        /** The input objects. */
        private final Iterable<? extends S> myInput;

        /** The ids that correspond one-to-one with the input objects. */
        private final long[] myInputIds;

        /** Iterator over input objects. */
        private final Iterator<? extends S> myIter;

        /** The property accessor. */
        private final PropertyAccessor<S, ?> myPropertyAccessor;

        /** The ids used to filter. */
        private final long[] mySortedIds;

        /**
         * Constructor.
         *
         * @param inputIds The ids that correspond one-to-one with the input
         *            objects.
         * @param input The input objects.
         * @param propertyAccessor The accessor for the objects.
         * @param sortedIds A sorted array of ids indicating which of the ids in
         *            inputIds should be used to return values.
         */
        public SkippingAccessingIterable(long[] inputIds, Iterable<? extends S> input, PropertyAccessor<S, ?> propertyAccessor,
                long[] sortedIds)
        {
            myInputIds = inputIds;
            myInput = input;
            myPropertyAccessor = propertyAccessor;
            mySortedIds = sortedIds;
            myIter = myInput.iterator();
        }

        @Override
        public Iterator<T> iterator()
        {
            return new Iterator<T>()
            {
                /** The index into the inputIds array. */
                private int myIndex;

                /**
                 * The next input object that corresponds to an id in the
                 * sortedIds array.
                 */
                private S myNext;

                {
                    findNext();
                }

                @Override
                public boolean hasNext()
                {
                    return myNext != null;
                }

                @SuppressWarnings("unchecked")
                @Override
                public T next()
                {
                    if (myNext == null)
                    {
                        throw new NoSuchElementException();
                    }
                    T result = (T)myPropertyAccessor.access(myNext);
                    findNext();
                    return result;
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException("Remove is not supported.");
                }

                /**
                 * Find the next input object with an id in the sortedIds array.
                 */
                private void findNext()
                {
                    while (myIter.hasNext())
                    {
                        myNext = myIter.next();
                        if (Arrays.binarySearch(mySortedIds, myInputIds[myIndex++]) >= 0)
                        {
                            break;
                        }
                    }
                }
            };
        }
    }
}
