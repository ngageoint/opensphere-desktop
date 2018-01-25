package io.opensphere.core.util.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventConsolidator;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.ref.WeakReference;

/**
 * This class subscribes to a source event type E from the EventManager and
 * performs a coalesce/consolidation function where an {@link EventConsolidator}
 * is used to consolidate events by source into a summary event which is then
 * dispatched after no new source events occur within a pre-determined time.
 *
 * How the summary event is constructed and what it contains is up to the
 * {@link EventConsolidator}.
 *
 * After construction, call start() to begin listening for events to
 * consolidate. Use stop() to stop listening for new events. Note that after
 * stop() is called, any remaining events that were coalescing will fire on
 * their regular schedule. If stopped, it can be restarted by calling start().
 *
 * If the source event type is a {@link SourceableEvent} it will be coalesced
 * along with only others from its source. If no source is provided because
 * either the event is not a {@link SourceableEvent} or the
 * {@link SourceableEvent} provides no source, it will be consolidated together
 * with all other non-sourced events.
 *
 * @param <E> the element type
 */
public class EventCoalescer<E extends Event> implements EventListener<E>
{
    /** Logger reference. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(EventCoalescer.class);

    /** The Constant DEFAULT_MAX_DISPATCH_WAIT_MS. */
    private static final long DEFAULT_MAX_DISPATCH_WAIT_MS = 200L;

    /**
     * Fixed thread pools to assist with incoming events and firing consolidated
     * events.
     */

    /** The event firing executor. */
    private final ThreadPoolExecutor myEventFiringExecutor;

    /** The incoming event executor. */
    private final ThreadPoolExecutor myIncomingEventExecutor;

    /**
     * A map between data point event type and a worker that tracks the event
     * and fires it off when no changes have been detected in the time alloted.
     */
    private final Map<Object, EventCoalescentWorker> mySourceToWorkerMap;

    /** The source proxy to source wr map. */
    private final Map<Long, WeakReference<Object>> mySourceProxyToSourceWRMap;

    /** The source instance counter. */
    private final AtomicLong mySourceInstanceCounter = new AtomicLong(1);

    /** The Constant NULL_SOURCE_PROXY. */
    private static final Long NULL_SOURCE_PROXY = Long.valueOf(0);

    /** The subscribe event class. */
    private final Class<E> mySubscribeEventClass;

    /** The consolidator class. */
    private final EventConsolidator<E> myConsolidator;

    /** The event manager. */
    private final EventManager myEventManager;

    /**
     * The max wait time to dispatch a Consolidated event that is coalescing.
     */
    private long myMaxWaitToDispatchTimeMs = DEFAULT_MAX_DISPATCH_WAIT_MS;

    /**
     * Flag that indicates that this coalescer is subscribed for its designated
     * events.
     */
    private final AtomicBoolean mySubscribed;

    /**
     * Instantiates a new event coalescer.
     *
     * @param em the EventManager to dispatch consolidated events
     * @param eventTypeClass the type of event for which the EventCoalescer
     *            should subscribe and consolidate
     * @param consolidator the consolidator to use to consolidate the events
     */
    public EventCoalescer(EventManager em, Class<E> eventTypeClass, EventConsolidator<E> consolidator)
    {
        if (em == null || consolidator == null)
        {
            throw new IllegalArgumentException("Input parameters can not be null!");
        }

        myEventFiringExecutor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory("EventCoalescer<" + eventTypeClass.getClass().getName() + ">:Dispatch"));

        myIncomingEventExecutor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory("EventCoalescer<" + eventTypeClass.getClass().getName() + ">:Ingest"));

        myIncomingEventExecutor.allowCoreThreadTimeOut(true);
        myEventFiringExecutor.allowCoreThreadTimeOut(true);

        mySourceProxyToSourceWRMap = new HashMap<>();
        mySourceToWorkerMap = new HashMap<>();
        myConsolidator = consolidator;
        mySubscribeEventClass = eventTypeClass;
        myEventManager = em;
        mySubscribed = new AtomicBoolean(false);
    }

    /**
     * Instantiates a new event coalescer.
     *
     * @param em the EventManager to dispatch consolidated events
     * @param eventTypeClass the type of event for which the EventCoalescer
     *            should subscribe and consolidate
     * @param consolidator the consolidator to use to consolidate the events
     * @param maxWaitToDispatchMs - the maximum time to wait to dispatch a
     *            coalescing event in miliseconds unless another event is added.
     */
    public EventCoalescer(EventManager em, Class<E> eventTypeClass, EventConsolidator<E> consolidator, long maxWaitToDispatchMs)
    {
        this(em, eventTypeClass, consolidator);
        myMaxWaitToDispatchTimeMs = maxWaitToDispatchMs;
    }

    @Override
    public void notify(E event)
    {
        if (event != null)
        {
            myIncomingEventExecutor.execute(new NewEventWorker(event));
        }
    }

    /**
     * Sets this coalescer to start listening for its designated events and
     * producing the coalesced events.
     */
    public void start()
    {
        if (!mySubscribed.get())
        {
            mySubscribed.set(true);
            myEventManager.subscribe(mySubscribeEventClass, this);
        }
    }

    /**
     * Sets this coalescer to no longer listen for its designated events.
     * Consolidated events already in process will still fire on schedule.
     */
    public void stop()
    {
        if (mySubscribed.get())
        {
            mySubscribed.set(false);
            myEventManager.unsubscribe(mySubscribeEventClass, this);
        }
    }

    /**
     * Fires events.
     *
     * @param worker , the Worker that is firing the event
     */
    protected void fireEvent(EventCoalescentWorker worker)
    {
        if (worker != null)
        {
            myEventFiringExecutor.execute(new NotifyWorker(worker.getEvent(), myEventManager));

            // Now that we have fired, clean up some to ensure we don't hold any
            // old references
            // to sources in memory.
            synchronized (mySourceToWorkerMap)
            {
                Iterator<Map.Entry<Object, EventCoalescentWorker>> entryItr = mySourceToWorkerMap.entrySet().iterator();
                boolean found = false;
                while (entryItr.hasNext() && !found)
                {
                    Map.Entry<Object, EventCoalescentWorker> entry = entryItr.next();

                    if (Utilities.sameInstance(entry.getValue(), worker))
                    {
                        entryItr.remove();
                        found = true;
                    }
                }
            }
        }
    }

    /**
     * Gets an event source proxy object for the given event source.
     *
     * @param eventSource the event source
     * @return Long ( the proxy ) or NULL_SOURCE_PROXY if eventSource is null
     */
    private Long getEventSourceProxy(Object eventSource)
    {
        if (eventSource == null)
        {
            return NULL_SOURCE_PROXY;
        }
        else
        {
            // Search through the proxy to source weak reference map
            // and prune out any GC'd sources. If we don't find it in the map
            // then create a new proxy, if we do see it then return the found
            // proxy.
            Long foundProxy = null;
            synchronized (mySourceProxyToSourceWRMap)
            {
                for (Iterator<Entry<Long, WeakReference<Object>>> itr = mySourceProxyToSourceWRMap.entrySet().iterator(); itr
                        .hasNext();)
                {
                    Entry<Long, WeakReference<Object>> entry = itr.next();
                    WeakReference<Object> wr = entry.getValue();
                    if (wr.get() == null)
                    {
                        itr.remove();
                    }
                    else if (wr.get() == eventSource)
                    {
                        foundProxy = entry.getKey();
                    }
                }

                if (foundProxy == null)
                {
                    foundProxy = Long.valueOf(mySourceInstanceCounter.getAndIncrement());
                    mySourceProxyToSourceWRMap.put(foundProxy, new WeakReference<Object>(eventSource));
                }
            }
            return foundProxy;
        }
    }

    /**
     * A worker class that consolidates/coalesces the event of the same type and
     * then fires off the consolidated event after a pre-determined time of
     * no-additional changes to the event.
     *
     */
    protected class EventCoalescentWorker implements Runnable
    {
        /** The my no change fire time ms. */
        private final long myNoChangeFireTimeMs;

        /** The my last added to time. */
        private long myLastAddedToTime = -1;

        /** The my event. */
        private final EventConsolidator<E> myWorkerConsolidator;

        /** The my thread. */
        private final Thread myThread;

        /** The my fired. */
        private boolean myFired;

        /** The lock. */
        private final ReentrantLock myLock = new ReentrantLock();

        /**
         * Instantiates a new event coalescent worker.
         *
         * @param eventConsolidator the consolidator
         * @param waitTime the max time to wait if no further event has been
         *            added to fire.
         */
        public EventCoalescentWorker(EventConsolidator<E> eventConsolidator, long waitTime)
        {
            myNoChangeFireTimeMs = waitTime;
            myWorkerConsolidator = eventConsolidator;
            myLastAddedToTime = System.currentTimeMillis();
            myThread = new Thread(this, "EventCoalescentWorker");
            myWorkerConsolidator.reset();
        }

        /**
         * Adds all the event from the provided event to this workers event
         * provided they are of the same.
         *
         * @param evt , the event to add ( null is acceptable but ignored )
         * @return true if added to event, false if not
         */
        public boolean addToEvent(E evt)
        {
            if (evt == null)
            {
                return false;
            }

            boolean added = false;
            try
            {
                // Only block 10 ms to avoid deadlock.
                if (myLock.tryLock(10, TimeUnit.MILLISECONDS))
                {
                    try
                    {
                        if (!myFired)
                        {
//                            long ttaStart = System.currentTimeMillis();
                            myWorkerConsolidator.addEvent(evt);
                            added = true;
                            myLastAddedToTime = System.currentTimeMillis();
//                            LOGGER.info("EventCoalescentWorker Adding: LAT: " + myLastAddedToTime + " TTA: " + (myLastAddedToTime-ttaStart));
                        }
                    }
                    finally
                    {
                        myLock.unlock();
                    }
                }
            }
            catch (InterruptedException e)
            {
            }
            return added;
        }

        /**
         * Fired.
         *
         * @return true, if successful
         */
        public boolean fired()
        {
            return myFired;
        }

        /**
         * Gets the event.
         *
         * @return the event
         */
        public Event getEvent()
        {
            return myWorkerConsolidator.createConsolidatedEvent();
        }

        @Override
        public void run()
        {
            while (!myFired)
            {
                if (myLock.tryLock())
                {
                    try
                    {
                        long diffTime = System.currentTimeMillis() - myLastAddedToTime;
                        if (myLastAddedToTime != -1 && diffTime > myNoChangeFireTimeMs)
                        {
                            myFired = true;
//                            LOGGER.info("EventCoalescentWorker Firing: " + System.currentTimeMillis());
                            fireEvent(this);
                        }
//                        else
//                        {
//                            LOGGER.info("EventCoalescentWorker NOT Firing: Diff: " + diffTime + " LAT: " + myLastAddedToTime
//                                    + " NCFT: " + myNoChangeFireTimeMs);
//                        }
                    }
                    finally
                    {
                        myLock.unlock();
                    }
                }

                if (!myFired)
                {
                    // Wait 20 milliseconds to allow more events to come in, and
                    // so that we don't burn
                    // too much CPU time checking our last fire time.
                    ThreadUtilities.sleep(20);
                }
            }
        }

        /**
         * Start.
         */
        public void start()
        {
//            LOGGER.info("EventCoalescentWorker Started: " + System.currentTimeMillis());
            myThread.start();
        }
    }

    /**
     * A worker class that takes a new event and figures out if there is an
     * active {@link EventCoalescentWorker} to handle it. If there is the new
     * event is given to the existing worker, if not a new worker is created.
     */
    protected class NewEventWorker implements Runnable
    {
        /** The event. */
        private final E myEvent;

        /**
         * Instantiates a new new event worker.
         *
         * @param evt the evt
         */
        public NewEventWorker(E evt)
        {
            myEvent = evt;
        }

        @Override
        public void run()
        {
            if (myEvent == null)
            {
                return;
            }

            synchronized (mySourceToWorkerMap)
            {
                Long sourceProxy = NULL_SOURCE_PROXY;
                if (myEvent instanceof SourceableEvent)
                {
                    sourceProxy = getEventSourceProxy(((SourceableEvent)myEvent).getSource());
                }

                EventCoalescentWorker worker = mySourceToWorkerMap.get(sourceProxy);
                if (worker == null || worker.fired())
                {
                    worker = new EventCoalescentWorker(myConsolidator.newInstance(), myMaxWaitToDispatchTimeMs);
                    worker.addToEvent(myEvent);
                    mySourceToWorkerMap.put(sourceProxy, worker);
                    worker.start();
                }
                else
                {
                    boolean added = worker.addToEvent(myEvent);

                    // If it wasn't added then create a new worker as the
                    // older worker is probably in the act of finalizing and
                    // firing.
                    if (!added)
                    {
                        worker = new EventCoalescentWorker(myConsolidator.newInstance(), myMaxWaitToDispatchTimeMs);
                        worker.addToEvent(myEvent);
                        mySourceToWorkerMap.put(sourceProxy, worker);
                        worker.start();
                    }
                }
            }
        }
    }

    /**
     * A worker class used to publish the consolidated events.
     */
    protected static class NotifyWorker implements Runnable
    {
        /** The listener. */
        private final EventManager myEventManager;

        /** The event. */
        private final Event myConsolidatedEvent;

        /**
         * Instantiates a new notify worker.
         *
         * @param consolidatedEvent the event
         * @param em the EventManager
         */
        public NotifyWorker(Event consolidatedEvent, EventManager em)
        {
            this.myEventManager = em;
            this.myConsolidatedEvent = consolidatedEvent;
        }

        @Override
        public void run()
        {
            if (this.myEventManager != null && this.myConsolidatedEvent != null)
            {
                this.myEventManager.publishEvent(myConsolidatedEvent);
            }
        }
    }
}
