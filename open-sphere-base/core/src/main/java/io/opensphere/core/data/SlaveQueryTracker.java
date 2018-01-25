package io.opensphere.core.data;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.Satisfaction;

/**
 * A query tracker that reports its status based on the progress of another
 * query tracker (the master). If this tracker is cancelled, the master tracker
 * is <b>not</b> cancelled.
 */
public class SlaveQueryTracker implements QueryTracker
{
    /** Flag indicating if this query has been cancelled. */
    private volatile boolean myCancelled;

    /** The listener for changes to this tracker's status. */
    private final QueryTrackerListener myListener;

    /** The other tracker that this one is following. */
    private final QueryTracker myMaster;

    /** The listener that is registered on the master tracker. */
    private final QueryTrackerListener myMasterTrackerListener = new QueryTrackerListener()
    {
        @Override
        public void fractionCompleteChanged(QueryTracker tracker, float fractionComplete)
        {
            myListener.fractionCompleteChanged(SlaveQueryTracker.this, fractionComplete);
        }

        @Override
        public void statusChanged(QueryTracker tracker, QueryStatus status)
        {
            myListener.statusChanged(SlaveQueryTracker.this, status);
        }
    };

    /**
     * The satisfactions that this tracker is responsible for.
     */
    private final Collection<? extends Satisfaction> mySatisfactions;

    /**
     * Constructor.
     *
     * @param master The master tracker.
     * @param listener The listener to be notified of changes of the master
     *            tracker's status. The tracker provided in the calls to the
     *            listener will be the slave, not the master.
     * @param satisfactions The satisfactions that this query is supporting,
     *            which may be {@code null}.
     */
    public SlaveQueryTracker(QueryTracker master, QueryTrackerListener listener, Collection<? extends Satisfaction> satisfactions)
    {
        myMaster = master;
        myListener = listener;
        myMaster.addListener(myMasterTrackerListener);
        mySatisfactions = satisfactions;
    }

    @Override
    public void addListener(QueryTrackerListener listener)
    {
        throw new UnsupportedOperationException("Only one listener is supported.");
    }

    @Override
    public long[] awaitCompletion()
    {
        return myMaster.awaitCompletion();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        boolean done = myMaster.isDone();
        myCancelled = true;
        return !done;
    }

    @Override
    public long[] get() throws InterruptedException, ExecutionException
    {
        return myMaster.get();
    }

    @Override
    public long[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return myMaster.get(timeout, unit);
    }

    @Override
    public Throwable getException()
    {
        return myMaster.getException();
    }

    @Override
    public float getFractionComplete()
    {
        return myMaster.getFractionComplete();
    }

    @Override
    public long[] getIds()
    {
        return myMaster.getIds();
    }

    /**
     * The listener that listens for status changes from the master tracker.
     *
     * @return The listener.
     */
    public QueryTrackerListener getListener()
    {
        return myMasterTrackerListener;
    }

    /**
     * Access the master tracker.
     *
     * @return The master tracker.
     */
    public QueryTracker getMasterTracker()
    {
        return myMaster;
    }

    @Override
    public List<? extends PropertyMatcher<?>> getParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query getQuery()
    {
        return myMaster.getQuery();
    }

    @Override
    public QueryStatus getQueryStatus()
    {
        return myMaster.getQueryStatus();
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfactions()
    {
        return mySatisfactions;
    }

    @Override
    public boolean isCancelled()
    {
        return myCancelled;
    }

    @Override
    public boolean isDone()
    {
        return myCancelled || myMaster.isDone();
    }

    @Override
    public boolean isLocal()
    {
        return myMaster.isLocal();
    }

    @Override
    public void logException()
    {
        myMaster.logException();
    }

    @Override
    public void logException(Logger logger)
    {
        myMaster.logException(logger);
    }

    @Override
    public void removeListener(QueryTrackerListener listener)
    {
        throw new UnsupportedOperationException("Only one listener is supported.");
    }
}
