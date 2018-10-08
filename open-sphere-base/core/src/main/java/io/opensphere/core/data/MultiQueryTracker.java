package io.opensphere.core.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.TopologyException;

import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcherUtilities;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import net.jcip.annotations.GuardedBy;

/**
 * Keeps track of the parts of a multi-segment query.
 */
@SuppressWarnings("PMD.GodClass")
public class MultiQueryTracker extends DefaultQueryTracker
{
    /**
     * A dummy interval property value set for use with non-interval queries.
     */
    private static final IntervalPropertyValueSet DUMMY_IPVS = new IntervalPropertyValueSet();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MultiQueryTracker.class);

    /** An interval set that encompasses all of my unsatisfied intervals. */
    private final IntervalPropertyValueSet myBoundingIntervalSet;

    /** Cache of interval matchers in the query. */
    @SuppressWarnings("PMD.SingularField")
    private final transient List<IntervalPropertyMatcher<?>> myIntervalMatchers;

    /** Indicates if the tracker is an interval query. */
    private boolean myIntervalQuery;

    /** Listener attached to the subordinate trackers. */
    private final QueryTrackerListener myListener = new QueryTrackerListener()
    {
        @Override
        public void fractionCompleteChanged(QueryTracker tracker, float fractionComplete)
        {
            updateFractionComplete();
        }

        @Override
        public void statusChanged(QueryTracker tracker, QueryStatus status)
        {
            if (tracker instanceof SlaveQueryTracker)
            {
                if (tracker.isDone() && removeSlave((SlaveQueryTracker)tracker) && !isCancelled())
                {
                    myResubmitListener.slaveDone(MultiQueryTracker.this);
                }
                updateFractionComplete();
            }
            updateStatus();
        }
    };

    /** The listener to be notified when a slave is done. */
    private final ResubmitListener myResubmitListener;

    /** Cache of non-interval matchers in the query. */
    private final transient List<PropertyMatcher<?>> myScalarMatchers;

    /** Flag indicating if a slave has ever been added to this tracker. */
    /** The subordinate trackers. */
    private final List<QueryTracker> myTrackers = new CopyOnWriteArrayList<>();

    /**
     * What portion of the query has not been satisfied by subordinate trackers.
     */
    @GuardedBy("myUnsatisfiedLock")
    private final List<IntervalPropertyValueSet> myUnsatisfied;

    /** Lock for {@link #myUnsatisfied}. */
    private final ReadWriteLock myUnsatisfiedLock = new ReentrantReadWriteLock();

    /**
     * Constructor that takes the top-level query.
     *
     * @param query The query.
     * @param slaveDoneListener The listener to be notified when a slave tracker
     *            completes. When a slave tracker completes, this query needs to
     *            be resubmitted to the data registry.
     */
    public MultiQueryTracker(Query query, ResubmitListener slaveDoneListener)
    {
        super(query, false);
        myResubmitListener = slaveDoneListener;

        List<IntervalPropertyMatcher<?>> intervalMatchers = New.list(getQuery().getParameters().size());
        List<PropertyMatcher<?>> scalarMatchers = New.list(getQuery().getParameters().size());
        PropertyMatcherUtilities.splitIntervalMatchers(getQuery().getParameters(), intervalMatchers, scalarMatchers);
        myIntervalMatchers = Collections.unmodifiableList(intervalMatchers);
        myScalarMatchers = Collections.unmodifiableList(scalarMatchers);

        myUnsatisfied = New.linkedList();
        if (myIntervalMatchers.isEmpty())
        {
            myUnsatisfied.add(DUMMY_IPVS);
            myBoundingIntervalSet = null;
            myIntervalQuery = false;
        }
        else
        {
            myUnsatisfied.add(PropertyMatcherUtilities.buildIntervalPropertyValueSet(myIntervalMatchers));
            myBoundingIntervalSet = PropertyMatcherUtilities.buildBoundingIntervalSet(myIntervalMatchers);
            myIntervalQuery = true;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        boolean result = super.cancel(mayInterruptIfRunning);
        for (QueryTracker tracker : myTrackers)
        {
            result = tracker.cancel(mayInterruptIfRunning) && result;
        }
        return result;
    }

    /**
     * Determine if another query tracker overlaps any of the unsatisfied
     * portion of my query.
     *
     * @param tracker The other query tracker.
     */
    public void checkOverlap(MultiQueryTracker tracker)
    {
        if (Utilities.sameInstance(tracker, this))
        {
            throw new IllegalArgumentException("Cannot check tracker overlap with itself.");
        }

        // If any of the scalar matchers don't match, this tracker
        // doesn't match.
        if (tracker.isIntervalQuery() == isIntervalQuery()
                && tracker.getQuery().getDataModelCategory().matches(getQuery().getDataModelCategory())
                && CollectionUtilities.containsAll(myScalarMatchers, tracker.myScalarMatchers)
                && (!isIntervalQuery() || myBoundingIntervalSet.intersects(tracker.myBoundingIntervalSet)))
        {
            // Prefer to add slave trackers that link directly to the other
            // tracker's sub-trackers to minimize notifications, but if the
            // other tracker's sub-trackers have not been populated, just link a
            // slave to the other tracker itself.
            if (tracker.getTrackers().isEmpty())
            {
                addSlave(tracker);
            }
            else
            {
                // Determine if the other tracker has unsatisfied before getting
                // its trackers. This is to avoid the situation where a
                // sub-tracker is added to the other tracker after we get its
                // sub-trackers, changing its unsatisfied.
                boolean otherHasUnsatisfied = tracker.hasUnsatisfied();

                for (QueryTracker subTracker : tracker.getTrackers())
                {
                    if (!(subTracker instanceof SlaveQueryTracker) && !subTracker.isLocal())
                    {
                        addSlave(subTracker);
                    }
                    else if (LOGGER.isTraceEnabled())
                    {
                        if (subTracker.isLocal())
                        {
                            if (LOGGER.isTraceEnabled())
                            {
                                LOGGER.trace("No slave added because sub tracker " + subTracker + " is local");
                            }
                        }
                        else
                        {
                            if (LOGGER.isTraceEnabled())
                            {
                                LOGGER.trace("No slave added because sub tracker " + subTracker + " is a slave");
                            }
                        }
                    }
                }

                // If I didn't find overlaps for all my unsatisfied and the
                // MultiQueryTracker still has unsatisfied, add a slave to the
                // MultiQueryTracker. This catches the case where the other
                // tracker is between adding slaves and running its queries.
                if (hasUnsatisfied() && otherHasUnsatisfied)
                {
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Adding slave to MultiTracker.");
                    }
                    addSlave(tracker);
                }
            }
        }
        else if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(new StringBuilder().append("No overlap between ").append(this).append(" and ").append(tracker));
        }
    }

    /**
     * Create a new query tracker that is subordinate to this one. If the
     * satisfactions do not overlap my unsatisfied, {@code null} will be
     * returned.
     *
     * @param local Flag indicating if this portion of the query is local.
     * @param satisfactions The satisfactions that this tracker will be
     *            responsible for.
     * @return The new tracker, or {@code null}.
     */
    public MutableQueryTracker createSubTracker(boolean local, Collection<? extends Satisfaction> satisfactions)
    {
        return createSubTracker(local, satisfactions, Nulls.<IntervalPropertyMatcher<?>>collection());
    }

    /**
     * Create a new query tracker that is subordinate to this one. If the
     * satisfactions do not overlap my unsatisfied, {@code null} will be
     * returned.
     *
     * @param local Flag indicating if this portion of the query is local.
     * @param satisfactions The satisfactions that this tracker will be
     *            responsible for.
     * @param params Interval parameters to override what's in the
     *            satisfactions.
     * @return The new tracker, or {@code null}.
     */
    public MutableQueryTracker createSubTracker(boolean local, Collection<? extends Satisfaction> satisfactions,
            Collection<? extends IntervalPropertyMatcher<?>> params)
    {
        // Ensure that the unsatisfied collection and the trackers are modified
        // atomically.
        myUnsatisfiedLock.writeLock().lock();
        try
        {
            if (subtractSatisfactions(satisfactions))
            {
                MutableQueryTracker tracker = new DefaultQueryTracker(getQuery(), local, satisfactions, params);
                myTrackers.add(tracker);
                tracker.addListener(myListener);
                tracker.setQueryStatus(getQueryStatus(), (Throwable)null);
                return tracker;
            }
            return null;
        }
        finally
        {
            myUnsatisfiedLock.writeLock().unlock();
        }
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfactions()
    {
        List<IntervalPropertyMatcher<?>> intervalParams = PropertyMatcherUtilities.getGroupMatchers(getQuery().getParameters());

        IntervalPropertyValueSet queryRegion = PropertyMatcherUtilities.buildIntervalPropertyValueSet(intervalParams);
        if (queryRegion == null)
        {
            queryRegion = DUMMY_IPVS;
        }

        return Collections.singleton(new SingleSatisfaction(queryRegion));
    }

    /**
     * Get the portion of the query that is not being handled by subordinate
     * trackers.
     *
     * @return The unsatisfied portion of the query.
     */
    public List<IntervalPropertyValueSet> getUnsatisfied()
    {
        myUnsatisfiedLock.readLock().lock();
        try
        {
            return New.list(myUnsatisfied);
        }
        finally
        {
            myUnsatisfiedLock.readLock().unlock();
        }
    }

    /**
     * Determine if this tracker has any children.
     *
     * @return {@code true} if this tracker has children.
     */
    public boolean hasChildren()
    {
        return !myTrackers.isEmpty();
    }

    /**
     * Determine if there are any unsatisfied portions of the query.
     *
     * @return {@code true} if there are unsatisfied portions.
     */
    public boolean hasUnsatisfied()
    {
        myUnsatisfiedLock.readLock().lock();
        try
        {
            return !myUnsatisfied.isEmpty();
        }
        finally
        {
            myUnsatisfiedLock.readLock().unlock();
        }
    }

    /**
     * Get if this is an interval query.
     *
     * @return {@code true} if this is an interval query.
     */
    public boolean isIntervalQuery()
    {
        return myIntervalQuery;
    }

    @Override
    public void setQueryStatus(QueryStatus queryStatus, Throwable e)
    {
        int count = 0;
        List<long[]> idBlocks = New.list();
        for (QueryTracker tracker : myTrackers)
        {
            long[] ids = tracker.getIds();
            idBlocks.add(ids);
            count += ids.length;
        }

        if (count > 0)
        {
            int index = 0;
            long[] ids = new long[count];
            for (long[] idBlock : idBlocks)
            {
                System.arraycopy(idBlock, 0, ids, index, idBlock.length);
                index += idBlock.length;
            }

            addIds(ids);
        }

        // Call super after adding the ids so that the listeners can access the
        // ids.
        super.setQueryStatus(queryStatus, e);
    }

    @Override
    public String toString()
    {
        if (myUnsatisfiedLock.readLock().tryLock())
        {
            try
            {
                return new StringBuilder(128).append(getClass().getSimpleName()).append("[query[").append(getQuery())
                        .append("] status[").append(getQueryStatus()).append("] threads[").append(getThreads())
                        .append("] unsatisfied[").append(myUnsatisfied).append("]]").toString();
            }
            finally
            {
                myUnsatisfiedLock.readLock().unlock();
            }
        }
        return super.toString();
    }

    /**
     * Add a query tracker that is a slave to another query that is already in
     * progress. When the other query finishes, this query will be notified. If
     * I am an interval query, remove the other query's satisfaction intervals
     * from my unsatisfied intervals.
     *
     * @param tracker The other query's tracker.
     */
    protected void addSlave(QueryTracker tracker)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(new StringBuilder().append("Adding slave to ").append(this).append(" waiting for ").append(tracker));
        }
        if (isIntervalQuery())
        {
            myUnsatisfiedLock.writeLock().lock();
            try
            {
                Collection<? extends Satisfaction> satisfactions = tracker.getSatisfactions();
                Collection<IntervalPropertyValueSet> sets = New.collection(satisfactions.size());
                for (Satisfaction sat : satisfactions)
                {
                    sets.add(sat.getIntervalPropertyValueSet());
                }

                // See what's left if the interval property value
                // sets of this sub-tracker are subtracted from my
                // unsatisfied.
                List<IntervalPropertyValueSet> remains = New.list(myUnsatisfied);

                if (IntervalPropertyValueSet.subtract(remains, sets) && !tracker.isDone())
                {
                    // Get the intersection of my unsatisfied and
                    // the sub-tracker's interval property value
                    // sets.
                    List<IntervalPropertyValueSet> intersection = New.list(myUnsatisfied);
                    IntervalPropertyValueSet.subtract(intersection, remains);

                    addSlave(tracker, SingleSatisfaction.generateSatisfactions(intersection), remains);
                }
            }
            catch (TopologyException e)
            {
                LOGGER.error("Failed to add query slave: " + e, e);
            }
            finally
            {
                myUnsatisfiedLock.writeLock().unlock();
            }
        }
        else
        {
            myUnsatisfiedLock.writeLock().lock();
            try
            {
                if (!myUnsatisfied.isEmpty())
                {
                    addSlave(tracker, tracker.getSatisfactions(), Collections.<IntervalPropertyValueSet>emptyList());
                }
            }
            finally
            {
                myUnsatisfiedLock.writeLock().unlock();
            }
        }
    }

    /**
     * Add a query tracker that is a slave to another query that is already in
     * progress. When the other query finishes, this query will be notified.
     * This checks to ensure that a master-slave-master-slave-master loop isn't
     * created.
     *
     * @param tracker The tracker to follow.
     * @param satisfaction The satisfaction that this tracker covers.
     * @param remains The remains of my unsatisfied interval after this slave is
     *            added.
     */
    protected void addSlave(QueryTracker tracker, Collection<? extends Satisfaction> satisfaction,
            Collection<? extends IntervalPropertyValueSet> remains)
    {
        if (tracker instanceof MultiQueryTracker)
        {
            // Make sure a loop isn't created.
            synchronized (MultiQueryTracker.class)
            {
                boolean isDescendant = isDescendant((MultiQueryTracker)tracker);

                if (!isDescendant)
                {
                    doAddSlave(tracker, satisfaction, remains);
                }
            }
        }
        else
        {
            doAddSlave(tracker, satisfaction, remains);
        }
    }

    /**
     * Remove a slave tracker and add its satisfaction back into my unsatisfied
     * collection.
     *
     * @param tracker The slave tracker.
     * @return If the tracker was removed.
     */
    protected boolean removeSlave(SlaveQueryTracker tracker)
    {
        myUnsatisfiedLock.writeLock().lock();
        try
        {
            if (myTrackers.remove(tracker))
            {
                QueryTracker masterTracker = tracker.getMasterTracker();
                if (masterTracker instanceof DefaultQueryTracker)
                {
                    ((DefaultQueryTracker)masterTracker).removeListener(tracker.getListener());
                }

                Collection<? extends Satisfaction> satisfactions = tracker.getSatisfactions();
                for (Satisfaction satisfaction : satisfactions)
                {
                    myUnsatisfied.add(satisfaction.getIntervalPropertyValueSet());
                }
                return true;
            }
            return false;
        }
        finally
        {
            myUnsatisfiedLock.writeLock().unlock();
        }
    }

    /**
     * Update my fraction complete based on my subordinate trackers fraction
     * complete.
     */
    protected void updateFractionComplete()
    {
        float fractionComplete = 0f;
        Object[] arr = myTrackers.toArray();
        if (arr.length > 0)
        {
            for (int index = 0; index < arr.length;)
            {
                fractionComplete += ((QueryTracker)arr[index++]).getFractionComplete();
            }
            fractionComplete /= arr.length;
            setFractionComplete(fractionComplete);
        }
    }

    /**
     * Check the status of my subordinate trackers and set my status
     * accordingly.
     */
    protected void updateStatus()
    {
        // Synchronize here to make sure that we aren't between removing a
        // tracker and adding unsatisfied.
        myUnsatisfiedLock.readLock().lock();
        try
        {
            boolean allDone = true;
            if (getQueryStatus() == QueryStatus.RUNNING)
            {
                for (QueryTracker tracker : myTrackers)
                {
                    if (tracker.getQueryStatus() == QueryStatus.FAILED)
                    {
                        setQueryStatus(QueryStatus.FAILED, tracker.getException());
                        allDone = false;
                        break;
                    }
                    else if (tracker.getQueryStatus() == QueryStatus.CANCELLED)
                    {
                        cancel(true);
                        allDone = false;
                        break;
                    }
                    else if (tracker.getQueryStatus() == QueryStatus.RUNNING)
                    {
                        allDone = false;
                    }
                }
            }
            if (allDone && !hasUnsatisfied())
            {
                setQueryStatus(QueryStatus.SUCCESS, (Throwable)null);
            }
        }
        finally
        {
            myUnsatisfiedLock.readLock().unlock();
        }
    }

    /**
     * Actually add the slave query tracker and adjust my unsatisfied. This
     * assumes that all checks are done.
     *
     * @param tracker The tracker to follow.
     * @param satisfaction The satisfaction that this tracker covers.
     * @param remains The remains of my unsatisfied interval after this slave is
     *            added.
     */
    private void doAddSlave(QueryTracker tracker, Collection<? extends Satisfaction> satisfaction,
            Collection<? extends IntervalPropertyValueSet> remains)
    {
        // This will add the listener to be notified when tracker is done.
        // If the tracker is already done, the check below will discard the
        // slave and keep the unsatisfied the same.
        // If the tracker finishes after the check below, the listener will wait
        // until proceed() is called, and resubmit the slave's parent tracker.
        SlaveQueryTracker slave = new SlaveQueryTracker(tracker, myListener, satisfaction);

        if (!tracker.isDone())
        {
            myTrackers.add(slave);
            myUnsatisfied.clear();
            if (!remains.isEmpty())
            {
                myUnsatisfied.addAll(remains);
            }

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(new StringBuilder().append("Slaved ").append(this).append(" to ").append(tracker)
                        .append(" unsatisfied is now ").append(myUnsatisfied));
            }
        }
    }

    /**
     * Access the subordinate trackers.
     *
     * @return The subordinate trackers.
     */
    private List<QueryTracker> getTrackers()
    {
        return myTrackers;
    }

    /**
     * Checks to see if this tracker is a descendant in the slave-master tracker
     * structure. Prevents any loops from forming.
     *
     * @param tracker The tracker to check.
     * @return True if this tracker is a descendant false otherwise.
     */
    private boolean isDescendant(MultiQueryTracker tracker)
    {
        boolean isCloseFamily = false;
        for (QueryTracker child : tracker.myTrackers)
        {
            if (child instanceof SlaveQueryTracker)
            {
                QueryTracker masterTracker = ((SlaveQueryTracker)child).getMasterTracker();
                if (Utilities.sameInstance(masterTracker, this))
                {
                    isCloseFamily = true;
                    break;
                }
                else if (masterTracker instanceof MultiQueryTracker)
                {
                    isCloseFamily = isDescendant((MultiQueryTracker)masterTracker);
                    if (isCloseFamily)
                    {
                        break;
                    }
                }
            }
        }

        return isCloseFamily;
    }

    /**
     * Subtract some satisfactions from my unsatisfied collection.
     *
     * @param satisfactions The satisfactions.
     * @return {@code true} if any changes were made to my unsatisfied
     *         collection.
     */
    private boolean subtractSatisfactions(Collection<? extends Satisfaction> satisfactions)
    {
        boolean result = false;
        for (Satisfaction satisfaction : satisfactions)
        {
            if (isIntervalQuery())
            {
                result |= IntervalPropertyValueSet.subtract(myUnsatisfied, satisfaction.getIntervalPropertyValueSet());
            }
            else if (!myUnsatisfied.isEmpty())
            {
                myUnsatisfied.clear();
                result = true;
            }

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(new StringBuilder().append("Subtracted satisfaction from ").append(this).append(':')
                        .append(satisfaction).append(", unsatisfied is now ").append(myUnsatisfied));
            }
        }
        return result;
    }

    /** Interface for a listener to be notified when a slave tracker is done. */
    @FunctionalInterface
    interface ResubmitListener
    {
        /**
         * Method called when a slave is done.
         *
         * @param multiTracker The multi tracker.
         */
        void slaveDone(MultiQueryTracker multiTracker);
    }
}
