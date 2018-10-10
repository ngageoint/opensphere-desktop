package io.opensphere.core.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.ref.WeakReference;

/**
 * Default implementation of {@link GenericPublisher}. This manages a collection
 * of subscribers and notifies them of changes.
 *
 * @param <E> The type of object to be published
 */
public class DefaultGenericPublisher<E> implements GenericFilteringPublisher<E>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultGenericPublisher.class);

    /** WeakHashMap of subscribers to accept filters. */
    private final Map<GenericSubscriber<E>, GenericSubscriberAcceptFilter> myFilters = New.weakMap();

    /** Reference to the next worker. */
    private volatile PublishWorker<E> myPending;

    /** A runnable that clears {@link #myPending}. */
    private final Runnable myPendingClearer = () -> myPending = null;

    /** Weak list of subscribers. */
    private final Collection<WeakReference<GenericSubscriber<E>>> mySubscribers = new ArrayList<>();

    @Override
    public void addSubscriber(GenericSubscriber<E> subscriber)
    {
        synchronized (mySubscribers)
        {
            mySubscribers.add(new WeakReference<>(subscriber));
        }
    }

    @Override
    public boolean addSubscriberAcceptFilter(GenericSubscriber<E> subscriber, GenericSubscriberAcceptFilter filter)
    {
        boolean added = false;
        if (filter != null)
        {
            synchronized (mySubscribers)
            {
                boolean isAlreadyInSubscriberList = false;
                Iterator<WeakReference<GenericSubscriber<E>>> itr = mySubscribers.iterator();
                WeakReference<GenericSubscriber<E>> wr = null;
                while (itr.hasNext())
                {
                    wr = itr.next();
                    GenericSubscriber<E> sub = wr.get();
                    if (sub == null)
                    {
                        itr.remove();
                    }
                    else
                    {
                        if (Utilities.sameInstance(sub, subscriber))
                        {
                            isAlreadyInSubscriberList = true;
                        }
                    }
                }

                if (!isAlreadyInSubscriberList)
                {
                    mySubscribers.add(new WeakReference<>(subscriber));
                }

                myFilters.put(subscriber, filter);
                added = true;
            }
        }
        return added;
    }

    @Override
    public void removeSubscriber(GenericSubscriber<E> subscriber)
    {
        synchronized (mySubscribers)
        {
            for (Iterator<WeakReference<GenericSubscriber<E>>> iter = mySubscribers.iterator(); iter.hasNext();)
            {
                WeakReference<GenericSubscriber<E>> ref = iter.next();
                if (ref.get() == subscriber)
                {
                    iter.remove();
                    myFilters.remove(subscriber);
                    break;
                }
            }
        }
    }

    @Override
    public void removeSubscriberAcceptFilter(GenericSubscriber<E> subscriber)
    {
        if (subscriber != null)
        {
            synchronized (mySubscribers)
            {
                myFilters.remove(subscriber);
            }
        }
    }

    /**
     * Send objects to the subscribers.
     *
     * @param source The source of the objects.
     * @param adds The added objects.
     * @param removes The removed objects.
     */
    public void sendObjects(Object source, Collection<? extends E> adds, Collection<? extends E> removes)
    {
        sendObjects(source, adds, removes, null);
    }

    /**
     * Send objects to the subscribers.
     *
     * @param source The source of the objects.
     * @param adds The added objects.
     * @param removes The removed objects.
     * @param executor The executor to use when calling to the subscribers. If
     *            this is <code>null</code>, the subscribers will be called on
     *            the current thread.
     */
    public void sendObjects(final Object source, final Collection<? extends E> adds, final Collection<? extends E> removes,
            Executor executor)
    {
        if (adds.isEmpty() && removes.isEmpty())
        {
            return;
        }
        PublishWorker<E> pending = myPending;
        if (pending == null || !pending.replaceAdds(removes, adds))
        {
            PublishWorker<E> worker = new PublishWorker<>(source, adds, removes, getSubscribers(), getSubscriberAcceptFilters(),
                    myPendingClearer);
            if (executor != null)
            {
                myPending = worker;
                executor.execute(worker);
            }
            else
            {
                worker.run();
            }
        }
    }

    /**
     * Get the filters map.
     *
     * @return the filters map
     */
    protected Map<GenericSubscriber<E>, GenericSubscriberAcceptFilter> getSubscriberAcceptFilters()
    {
        synchronized (mySubscribers)
        {
            if (mySubscribers.isEmpty())
            {
                return Collections.emptyMap();
            }
            return New.map(myFilters);
        }
    }

    /**
     * Get the subscribers.
     *
     * @return The subscribers.
     */
    protected Collection<GenericSubscriber<E>> getSubscribers()
    {
        // Remove any that have been garbage-collected.
        Collection<GenericSubscriber<E>> subscribers = new ArrayList<>();
        synchronized (mySubscribers)
        {
            for (Iterator<WeakReference<GenericSubscriber<E>>> iter = mySubscribers.iterator(); iter.hasNext();)
            {
                WeakReference<GenericSubscriber<E>> ref = iter.next();
                GenericSubscriber<E> subscriber = ref.get();
                if (subscriber == null)
                {
                    iter.remove();
                }
                else
                {
                    subscribers.add(subscriber);
                }
            }
        }
        return subscribers;
    }

    /**
     * A runnable that does the actual publishing.
     *
     * @param <E> The type of the objects.
     */
    private static final class PublishWorker<E> implements Runnable
    {
        /** Atomic updater for {@link #myAdds}. */
        @SuppressWarnings("rawtypes")
        private static final AtomicReferenceFieldUpdater<PublishWorker, Collection> ADDS_UPDATER = AtomicReferenceFieldUpdater
        .newUpdater(PublishWorker.class, Collection.class, "myAdds");

        /** The collection of objects to be added. */
        private volatile Collection<? extends E> myAdds;

        /** The subscriber filters. */
        private final Map<GenericSubscriber<E>, GenericSubscriberAcceptFilter> myFilters;

        /** The collection of objects to be removed. */
        private final Collection<? extends E> myRemoves;

        /** The source of the objects. */
        private final Object mySource;

        /** The subscribers to deliver the objects to. */
        private final Collection<GenericSubscriber<E>> mySubscribers;

        /** Optional runnable to call when the worker is done. */
        private final Runnable myCallback;

        /**
         * Constructor.
         *
         * @param source The source of the objects.
         * @param adds The collection of objects to be added.
         * @param removes The collection of objects to be removed.
         * @param subscribers The subscribers to deliver the objects to.
         * @param filters The subscriber filters.
         * @param callback A runnable to call when the worker is done.
         */
        public PublishWorker(Object source, Collection<? extends E> adds, Collection<? extends E> removes,
                Collection<GenericSubscriber<E>> subscribers, Map<GenericSubscriber<E>, GenericSubscriberAcceptFilter> filters,
                Runnable callback)
        {
            myFilters = filters;
            myAdds = CollectionUtilities.unmodifiableCollection(adds);
            myRemoves = CollectionUtilities.unmodifiableCollection(removes);
            mySubscribers = subscribers;
            mySource = source;
            myCallback = callback;
        }

        /**
         * If the adds collection in this worker match {@code expected}, replace
         * them with {@code replacement} and return {@code true}. Otherwise,
         * take no action and return {@code false}.
         *
         * @param expected The expected objects.
         * @param replacement The replacement objects.
         * @return {@code true} if successful.
         */
        public synchronized boolean replaceAdds(Collection<? extends E> expected, Collection<? extends E> replacement)
        {
            Collection<? extends E> adds = myAdds;
            if (adds == null)
            {
                return false;
            }
            return expected.size() == adds.size() && expected.containsAll(adds)
                    && ADDS_UPDATER.compareAndSet(this, adds, CollectionUtilities.unmodifiableCollection(replacement));
        }

        @Override
        public void run()
        {
            myCallback.run();
            @SuppressWarnings("unchecked")
            Collection<? extends E> adds = ADDS_UPDATER.getAndSet(this, null);
            for (GenericSubscriber<E> subscriber : mySubscribers)
            {
                boolean send = true;

                // Do any necessary filtering if we have a filter for this
                // subscriber
                GenericSubscriberAcceptFilter filter = myFilters.isEmpty() ? null : myFilters.get(subscriber);
                if (filter != null)
                {
                    if (filter.filtersBySource() && !filter.acceptsSource(mySource))
                    {
                        send = false;
                    }
                    if (send && !filter.filtersBySource() && filter.filtersBySourceClasses())
                    {
                        // Don't send if the source is null
                        send = mySource != null && filter.acceptsSourceClass(mySource.getClass());
                    }
                }
                if (send)
                {
                    try
                    {
                        subscriber.receiveObjects(mySource, adds, myRemoves);
                    }
                    catch (RuntimeException e)
                    {
                        LOGGER.error("Exception while sending objects to subscriber: " + e, e);
                    }
                }
            }
        }
    }
}
