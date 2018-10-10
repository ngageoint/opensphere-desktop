package io.opensphere.core.data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import io.opensphere.core.data.MultiQueryTracker.ResubmitListener;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.data.util.QueryTracker.QueryTrackerListener;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Manager for active and completed data registry queries.
 */
public class QueryManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(QueryManager.class);

    /** An external listener to be notified when a slave tracker is done. */
    private final ResubmitListener myExternalResubmitListener;

    /** A map of families to query trackers. */
    private final Map<String, List<MultiQueryTracker>> myFamilyToTrackerMap = New.map();

    /** The lock used to synchronize changes to the queries being managed. */
    private final ReadWriteLock myLock = new ReentrantReadWriteLock();

    /** A map of queries to query trackers. */
    private final Map<Query, List<MultiQueryTracker>> myQueryToTrackerMap = New.map();

    /** Listener to be notified when a slave tracker is done. */
    private final ResubmitListener myResubmitListener = multiTracker -> handleSlaveDone(multiTracker);

    /** Listener for when query trackers are done. */
    private final QueryTrackerListener myTrackerDoneListener = new QueryTrackerListener()
    {
        @Override
        public void fractionCompleteChanged(QueryTracker tracker, float fractionComplete)
        {
            /* intentionally blank */
        }

        @Override
        public void statusChanged(QueryTracker tracker, QueryStatus status)
        {
            if (tracker.isDone())
            {
                removeTracker(tracker);
            }
        }
    };

    /**
     * Constructor.
     *
     * @param resubmitListener A listener to be notified when a query needs to
     *            be resubmitted.
     */
    public QueryManager(ResubmitListener resubmitListener)
    {
        myExternalResubmitListener = resubmitListener;
    }

    /**
     * Submit a new query to the manager.
     *
     * @param query The query.
     * @return The query tracker.
     */
    public MultiQueryTracker submitQuery(Query query)
    {
        MultiQueryTracker newTracker = new MultiQueryTracker(query, myResubmitListener);

        Lock lock = myLock.writeLock();
        lock.lock();
        try
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Query submitted: " + query);
            }
            long t0 = System.nanoTime();
            checkTrackerOverlaps(newTracker);
            if (LOGGER.isDebugEnabled())
            {
                long t1 = System.nanoTime();
                LOGGER.debug(StringUtilities.formatTimingMessage("Time to determine query overlaps: ", t1 - t0));
            }

            CollectionUtilities.multiMapAdd(myQueryToTrackerMap, query, newTracker, false);
            if (query.getDataModelCategory().getFamily() != Nulls.STRING)
            {
                CollectionUtilities.multiMapAdd(myFamilyToTrackerMap, query.getDataModelCategory().getFamily(), newTracker,
                        false);
            }
            CollectionUtilities.multiMapAdd(myFamilyToTrackerMap, Nulls.STRING, newTracker, false);
            newTracker.addListener(myTrackerDoneListener);
        }
        finally
        {
            lock.unlock();
        }

        return newTracker;
    }

    /**
     * Determine if the given query overlaps any other queries that are in
     * progress. If so, create slave trackers for this tracker to track the
     * progress of the other trackers.
     *
     * @param tracker The input tracker.
     * @return {@code true} if the query completely overlaps existing queries.
     */
    protected boolean checkTrackerOverlaps(MultiQueryTracker tracker)
    {
        Lock lock = myLock.readLock();
        lock.lock();
        try
        {
            List<MultiQueryTracker> list = myFamilyToTrackerMap.get(tracker.getQuery().getDataModelCategory().getFamily());
            if (CollectionUtilities.hasContent(list))
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Checking tracker overlaps for " + tracker + " for " + list.size() + " other trackers");
                }
                for (int index = 0; index < list.size(); ++index)
                {
                    MultiQueryTracker other = list.get(index);
                    if (!other.isDone() && !tracker.equals(other))
                    {
                        tracker.checkOverlap(other);

                        if (!tracker.hasUnsatisfied())
                        {
                            if (LOGGER.isTraceEnabled())
                            {
                                LOGGER.trace("Slaves created for all satisfactions");
                            }
                            return true;
                        }
                    }
                    else if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Other tracker is done or equal: " + other);
                    }
                }
            }
            else
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("No other trackers to check overlaps with");
                }
            }
        }
        finally
        {
            lock.unlock();
        }

        return false;
    }

    /**
     * Handle the situation when a tracker's slave is done.
     *
     * @param multiTracker The multi-tracker.
     */
    protected void handleSlaveDone(MultiQueryTracker multiTracker)
    {
        if (!multiTracker.isCancelled() && !checkTrackerOverlaps(multiTracker))
        {
            myExternalResubmitListener.slaveDone(multiTracker);
        }
    }

    /**
     * Attempt to remove a tracker from my maps. To avoid dead-locks, this will
     * do nothing if the lock cannot be immediately obtained.
     *
     * @param tracker The tracker to remove.
     */
    protected void removeTracker(QueryTracker tracker)
    {
        Lock lock = myLock.writeLock();
        lock.lock();
        try
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Removing tracker " + tracker);
            }
            if (tracker instanceof MultiQueryTracker)
            {
                CollectionUtilities.multiMapRemove(myQueryToTrackerMap, tracker.getQuery(), (MultiQueryTracker)tracker);
            }
            if (tracker.getQuery().getDataModelCategory().getFamily() != Nulls.STRING)
            {
                CollectionUtilities.multiMapRemove(myFamilyToTrackerMap, tracker.getQuery().getDataModelCategory().getFamily(),
                        (MultiQueryTracker)tracker);
            }
            CollectionUtilities.multiMapRemove(myFamilyToTrackerMap, Nulls.STRING, (MultiQueryTracker)tracker);
        }
        finally
        {
            lock.unlock();
        }
    }
}
