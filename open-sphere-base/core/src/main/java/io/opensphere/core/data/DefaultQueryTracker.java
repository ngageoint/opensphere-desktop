package io.opensphere.core.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcherUtilities;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.util.lang.TaskCanceller;
import io.opensphere.core.util.lang.ThreadControl;

/**
 * Default implementation of {@link QueryTracker}.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultQueryTracker extends TaskCanceller implements MutableQueryTracker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultQueryTracker.class);

    /**
     * The change support. This is created lazily, but once it is set it does
     * not go back to {@code null}.
     */
    private volatile StrongChangeSupport<QueryTrackerListener> myChangeSupport;

    /** Condition used to signal completion. */
    private final Condition myCompletionCondition;

    /** If the query produced an exception, it is stored here. */
    private volatile Throwable myException;

    /** The fraction complete, ranging from 0 to 1. */
    private volatile float myFractionComplete;

    /** The result ids. */
    private long[] myIds = new long[0];

    /** Lock for {@link #myIds}. */
    private final Lock myIdsLock = new ReentrantLock();

    /** Flag indicating if this is a local query. */
    private final boolean myLocal;

    /**
     * The parameters which are more restricted versions of the ones in the
     * query so as to only match the regions in the satisfactions.
     */
    private final List<? extends PropertyMatcher<?>> myParameters;

    /** The query. */
    private final Query myQuery;

    /** The query status. */
    private volatile QueryStatus myQueryStatus = QueryStatus.RUNNING;

    /** Lock on query status changes. */
    private final Lock myQueryStatusLock = new ReentrantLock();

    /**
     * The satisfactions that this query is supporting, which may be
     * {@code null}.
     */
    private final Collection<? extends Satisfaction> mySatisfactions;

    /**
     * Constructor with no satisfactions.
     *
     * @param query The query being tracked.
     * @param local Indicates if this is a local query.
     */
    public DefaultQueryTracker(Query query, boolean local)
    {
        this(query, local, Nulls.<Satisfaction>collection());
    }

    /**
     * Constructor that takes a query and some optional satisfactions to be
     * fulfilled by the query.
     *
     * @param query The query being tracked.
     * @param local Indicates if this is a local query.
     * @param satisfactions The satisfactions that this query is supporting,
     *            which may be {@code null}.
     */
    public DefaultQueryTracker(Query query, boolean local, Collection<? extends Satisfaction> satisfactions)
    {
        this(query, local, satisfactions, Nulls.<IntervalPropertyMatcher<?>>collection());
    }

    /**
     * Constructor that takes a query and some optional satisfactions to be
     * fulfilled by the query.
     *
     * @param query The query being tracked.
     * @param local Indicates if this is a local query.
     * @param satisfactions The satisfactions that this query is supporting,
     *            which may be {@code null}.
     * @param intervalParams The interval parameters for this portion of the
     *            query, overriding what's in the satisfactions.
     */
    public DefaultQueryTracker(Query query, boolean local, Collection<? extends Satisfaction> satisfactions,
            Collection<? extends IntervalPropertyMatcher<?>> intervalParams)
    {
        myQuery = query;
        myLocal = local;
        myCompletionCondition = myQueryStatusLock.newCondition();
        mySatisfactions = New.unmodifiableCollection(satisfactions);

        List<PropertyMatcher<?>> parameters = New.list(query.getParameters().size());
        PropertyMatcherUtilities.splitIntervalMatchers(query.getParameters(), Nulls.<PropertyMatcher<?>>list(), parameters);
        if (intervalParams != null)
        {
            parameters.addAll(intervalParams);
        }
        myParameters = parameters.isEmpty() ? Collections.<IntervalPropertyMatcher<?>>emptyList()
                : Collections.unmodifiableList(parameters);
    }

    @Override
    public void addIds(long[] ids)
    {
        myIdsLock.lock();
        try
        {
            myIds = Utilities.concatenate(myIds, ids);
        }
        finally
        {
            myIdsLock.unlock();
        }
    }

    @Override
    public void addListener(QueryTrackerListener listener)
    {
        if (isDone())
        {
            /* If the query is already done, just notify the listener and don't
             * add it to the change support. This is to avoid the race between
             * the listener being added to the change support and the change
             * support being notified of the change in setQueryStatus. */
            listener.statusChanged(this, getQueryStatus());
        }
        else
        {
            if (myChangeSupport == null)
            {
                AtomicReferenceFieldUpdater.newUpdater(DefaultQueryTracker.class, StrongChangeSupport.class, "myChangeSupport")
                        .compareAndSet(this, null, new StrongChangeSupport<QueryTrackerListener>());
            }
            myChangeSupport.addListener(listener);
        }
    }

    @Override
    public long[] awaitCompletion()
    {
        myQueryStatusLock.lock();
        try
        {
            super.wrap(() ->
            {
                while (getQueryStatus() == QueryStatus.RUNNING)
                {
                    try
                    {
                        myCompletionCondition.await();
                    }
                    catch (InterruptedException e)
                    {
                        cancel(true);
                    }
                }
            }).run();
        }
        finally
        {
            myQueryStatusLock.unlock();
        }
        return getIds();
    }

    @Override
    public void cancel()
    {
        cancel(true);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        myQueryStatusLock.lock();
        try
        {
            if (getQueryStatus() == QueryStatus.RUNNING)
            {
                setQueryStatus(QueryStatus.CANCELLED, null);
                cancelThreads();
            }
        }
        finally
        {
            myQueryStatusLock.unlock();
        }
        return getQueryStatus() == QueryStatus.CANCELLED;
    }

    @Override
    public long[] get() throws ExecutionException, InterruptedException
    {
        try
        {
            myQueryStatusLock.lockInterruptibly();
            try
            {
                while (getQueryStatus() == QueryStatus.RUNNING)
                {
                    myCompletionCondition.await();
                }
            }
            finally
            {
                myQueryStatusLock.unlock();
            }
            if (myException != null)
            {
                throw new ExecutionException(getException());
            }
            return getIds();
        }
        catch (InterruptedException e)
        {
            cancel(true);
            throw e;
        }
    }

    @Override
    public long[] get(long timeout, TimeUnit unit) throws ExecutionException, TimeoutException
    {
        long nanos = unit.toNanos(timeout);
        try
        {
            myQueryStatusLock.lockInterruptibly();
            try
            {
                while (getQueryStatus() == QueryStatus.RUNNING && nanos > 0)
                {
                    nanos = myCompletionCondition.awaitNanos(nanos);
                }
            }
            finally
            {
                myQueryStatusLock.unlock();
            }
        }
        catch (InterruptedException e)
        {
            cancel(true);
        }
        if (getQueryStatus() == QueryStatus.RUNNING)
        {
            throw new TimeoutException();
        }
        else if (getQueryStatus() == QueryStatus.CANCELLED)
        {
            throw new CancellationException();
        }
        else if (getQueryStatus() == QueryStatus.FAILED)
        {
            throw new ExecutionException("Query execution failed.", getException());
        }
        return getIds();
    }

    @Override
    public Throwable getException()
    {
        return myException;
    }

    @Override
    public float getFractionComplete()
    {
        return myFractionComplete;
    }

    @Override
    public long[] getIds()
    {
        myIdsLock.lock();
        try
        {
            return myIds;
        }
        finally
        {
            myIdsLock.unlock();
        }
    }

    @Override
    public List<? extends PropertyMatcher<?>> getParameters()
    {
        return myParameters;
    }

    @Override
    public Query getQuery()
    {
        return myQuery;
    }

    @Override
    public QueryStatus getQueryStatus()
    {
        return myQueryStatus;
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfactions()
    {
        return mySatisfactions;
    }

    @Override
    public boolean isCancelled()
    {
        if (ThreadControl.isThreadCancelled())
        {
            cancel(true);
            return true;
        }

        return getQueryStatus() == QueryStatus.CANCELLED;
    }

    @Override
    public boolean isDone()
    {
        return getQueryStatus() != QueryStatus.RUNNING;
    }

    @Override
    public boolean isLocal()
    {
        return myLocal;
    }

    @Override
    public void logException()
    {
        Throwable t = myException;
        if (t != null)
        {
            LOGGER.error("Query failed: " + t, t);
        }
    }

    @Override
    public void logException(Logger logger)
    {
        Throwable t = myException;
        if (t != null)
        {
            logger.log(DefaultQueryTracker.class.getName(), Level.ERROR, "Query failed: " + t, t);
        }
    }

    @Override
    public void removeListener(QueryTrackerListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void setFractionComplete(final float fractionComplete)
    {
        if (fractionComplete < 0f || fractionComplete > 1f)
        {
            throw new IllegalArgumentException("Fraction complete must be between 0 and 1 inclusive.");
        }
        myFractionComplete = fractionComplete;
        if (myChangeSupport != null)
        {
            myChangeSupport.notifyListeners(new ChangeSupport.Callback<QueryTracker.QueryTrackerListener>()
            {
                @Override
                public void notify(QueryTrackerListener listener)
                {
                    listener.fractionCompleteChanged(DefaultQueryTracker.this, fractionComplete);
                }
            }, null);
        }
    }

    @Override
    public void setQueryStatus(final QueryStatus queryStatus, Throwable e)
    {
        myQueryStatusLock.lock();
        try
        {
            if (queryStatus == QueryStatus.RUNNING)
            {
                if (myQueryStatus == QueryStatus.RUNNING)
                {
                    return;
                }
                throw new IllegalArgumentException("Cannot set query status to " + queryStatus);
            }
            // Only set the query status if it is currently RUNNING, or if the
            // new status is FAILED. Possible transitions are:
            // RUNNING -> SUCCESS
            // RUNNING -> FAILED
            // RUNNING -> CANCELLED
            // SUCCESS -> FAILED
            // CANCELLED -> FAILED
            if (myQueryStatus == QueryStatus.RUNNING || queryStatus == QueryStatus.FAILED)
            {
                myQueryStatus = queryStatus;
                if (e != null)
                {
                    myException = e;
                }
            }
            myCompletionCondition.signalAll();
        }
        finally
        {
            myQueryStatusLock.unlock();
        }
        setFractionComplete(1f);
        if (myChangeSupport != null)
        {
            myChangeSupport.notifyListeners(new ChangeSupport.Callback<QueryTracker.QueryTrackerListener>()
            {
                @Override
                public void notify(QueryTrackerListener listener)
                {
                    listener.statusChanged(DefaultQueryTracker.this, queryStatus);
                }
            }, null);
        }
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(getClass().getSimpleName()).append("[query[").append(getQuery()).append("] status[")
                .append(getQueryStatus()).append("] threads[").append(getThreads()).append("]]").toString();
    }

    @Override
    public Runnable wrapRunnable(final Runnable r)
    {
        return wrap(r);
    }

    @Override
    public Runnable wrap(Runnable r)
    {
        return super.wrap(new RunnableWrapper(r));
    }

    /**
     * A wrapper for a {@link Runnable}, that logs message when the runnable
     * starts and finishes.
     */
    private final class RunnableWrapper implements Runnable
    {
        /** The wrapped runnable. */
        private final Runnable myRunnable;

        /**
         * Constructor.
         *
         * @param r The wrapped runnable.
         */
        public RunnableWrapper(Runnable r)
        {
            myRunnable = r;
        }

        @Override
        public void run()
        {
            try
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Beginning query [" + getQuery() + "] satisfactions [" + getSatisfactions() + "]");
                }
                myRunnable.run();
            }
            finally
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Query complete [" + getQuery() + "] satisfactions [" + getSatisfactions() + "] status: "
                            + getQueryStatus());
                }
            }
        }
    }
}
