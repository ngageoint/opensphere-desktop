package io.opensphere.core.animationhelper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;

/** Manages which time spans have been queried, so you don't have to. */
@ThreadSafe
public abstract class TimeSpanGovernor
{
    /** The callbacks to execute when after pending requests are handled. */
    @GuardedBy("this")
    private final List<Runnable> myCallbacks = New.list();

    /** The executor. */
    private final Executor myExecutor = new ProcrastinatingExecutor("TimeSpanQueryTracker", 500, 2000);

    /** The pending times to request. */
    @GuardedBy("this")
    private final List<TimeSpan> myPendingTimes = New.list();

    /** The times that have not been requested. */
    private final List<TimeSpan> myUnrequestedTimes;

    /** The original available span. */
    private final TimeSpan myAvailableSpan;

    /**
     * Calculates the intersections of the two collections of spans.
     *
     * @param spans1 the first spans
     * @param spans2 the second spans
     * @return the intersections
     */
    private static List<TimeSpan> getIntersections(Collection<? extends TimeSpan> spans1, Collection<? extends TimeSpan> spans2)
    {
        List<TimeSpan> intersections = New.list();
        for (TimeSpan span1 : spans1)
        {
            for (TimeSpan span2 : spans2)
            {
                TimeSpan intersection = span1.getIntersection(span2);
                if (intersection != null)
                {
                    intersections.add(intersection);
                }
            }
        }
        return intersections;
    }

    /**
     * Subtracts the span from the spans.
     *
     * @param spans the spans
     * @param span the span to subtract
     * @return the differences
     */
    private static List<TimeSpan> subtract(Collection<? extends TimeSpan> spans, TimeSpan span)
    {
        List<TimeSpan> differences = New.list();
        for (TimeSpan span1 : spans)
        {
            if (span1.overlaps(span))
            {
                differences.addAll(span1.subtract(span));
            }
            else
            {
                differences.add(span1);
            }
        }
        return differences;
    }

    /**
     * Constructor.
     *
     * @param availableSpan the span of available data
     */
    public TimeSpanGovernor(TimeSpan availableSpan)
    {
        myUnrequestedTimes = Collections.synchronizedList(New.list(availableSpan));
        myAvailableSpan = availableSpan;
    }

    /**
     * Requests that data be requested for the span.
     *
     * @param span the span
     * @param callbacks callbacks to run after the request is performed
     */
    public void requestData(TimeSpan span, Runnable... callbacks)
    {
        addCallbacks(span, callbacks);
        myExecutor.execute(this::handleRequests);
    }

    /**
     * Requests that data be requested for the span.
     *
     * @param span the span
     * @param callbacks callbacks to run after the request is performed
     */
    public void requestDataNow(TimeSpan span, Runnable... callbacks)
    {
        addCallbacks(span, callbacks);
        handleRequests();
    }

    /**
     * Clears the data. Intended to be overridden.
     */
    public void clearData()
    {
        myExecutor.execute(() ->
        {
            synchronized (myUnrequestedTimes)
            {
                myUnrequestedTimes.clear();
                myUnrequestedTimes.add(myAvailableSpan);
            }
        });
    }

    /**
     * Adds back the specified times so they can be queryable again.
     *
     * @param spans The spans to add back as queryable.
     */
    public void clearData(Collection<? extends TimeSpan> spans)
    {
        synchronized (myUnrequestedTimes)
        {
            myUnrequestedTimes.addAll(spans);
        }
    }

    /**
     * Called when a request needs to be performed.
     *
     * @param spans the time spans to request
     * @return whether the request was completed
     */
    protected abstract boolean performRequest(List<? extends TimeSpan> spans);

    /**
     * Adds the callbacks for the time span.
     *
     * @param span The time span.
     * @param callbacks The callbacks to add.
     */
    private void addCallbacks(TimeSpan span, Runnable... callbacks)
    {
        synchronized (this)
        {
            myPendingTimes.add(span);
            for (Runnable callback : callbacks)
            {
                myCallbacks.add(callback);
            }
        }
    }

    /**
     * Handles the pending requests.
     */
    private void handleRequests()
    {
        List<TimeSpan> timesToRequest;
        synchronized (this)
        {
            TimeSpanList.mergeOverlaps(myPendingTimes, true);
            synchronized (myUnrequestedTimes)
            {
                timesToRequest = getIntersections(myPendingTimes, myUnrequestedTimes);
            }
            myPendingTimes.clear();
        }

        if (!timesToRequest.isEmpty())
        {
            boolean requestComplete = performRequest(timesToRequest);
            if (requestComplete)
            {
                for (TimeSpan span : timesToRequest)
                {
                    synchronized (myUnrequestedTimes)
                    {
                        List<TimeSpan> differences = subtract(myUnrequestedTimes, span);
                        myUnrequestedTimes.clear();
                        myUnrequestedTimes.addAll(differences);
                    }
                }
            }
        }

        List<Runnable> callbacks;
        synchronized (this)
        {
            callbacks = New.list(myCallbacks);
            myCallbacks.clear();
        }
        for (Runnable callback : callbacks)
        {
            callback.run();
        }
    }
}
