package io.opensphere.wms;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.log4j.Logger;

import io.opensphere.core.AnimationChangeAdapter;
import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.TimeManager.ActiveTimeSpans;
import io.opensphere.core.TimeManager.DataLoadDurationChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.CatchingRunnable;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * WMSActiveTimeMonitor monitors the core processes that control time and
 * notifies a listener when things change. The listener can elect to take the
 * time differences from the event or retrieve the full sequence of active
 * timespans.
 */
@ThreadSafe
@SuppressWarnings("PMD.GodClass")
public class WMSActiveTimeMonitor
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WMSActiveTimeMonitor.class);

    /**
     * Listen for Primary and held TimeSpan changes from the TimeManager in
     * core.
     */
    private final ActiveTimeSpanChangeListener myActiveTimespanChangeListener = new ActiveTimeSpanChangeListener()
    {
        @Override
        public void activeTimeSpansChanged(ActiveTimeSpans active)
        {
            updateTimes();
            fireActiveTimeChanged(active.getPrimary().get(0));
        }
    };

    /** My animation listener. */
    private final AnimationChangeAdapter myAnimationListener = new AnimationChangeAdapter()
    {
        @Override
        public void animationPlanCancelled()
        {
            updateTimes();
        }

        @Override
        public void animationPlanEstablished(AnimationPlan plan)
        {
            updateTimes();
        }
    };

    /** The core animation manager. */
    private final AnimationManager myAnimationManager;

    /** The Change support. */
    private final WeakChangeSupport<TimeChangeListener> myChangeSupport = new WeakChangeSupport<>();

    /** My sequence of currently-active times. */
    private volatile Set<? extends TimeSpan> myCurrentSequence;

    /** Listener for changes to the data load duration. */
    private final DataLoadDurationChangeListener myDataLoadDurationChangeListener = new DataLoadDurationChangeListener()
    {
        @Override
        public void dataLoadDurationChanged(Duration dataLoadDuration)
        {
            updateTimes();
        }
    };

    /** Executor used to notify listeners of changes. */
    private final ExecutorService myExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("WMSActiveTimeMonitor"));

    /**
     * This can be used to cancel the current processing if another time change
     * comes in.
     */
    private Future<?> myFuture;

    /** Flag used to track whether this instance has been activated. */
    private volatile boolean myIsActive;

    /** The core time manager. */
    private final TimeManager myTimeManager;

    /**
     * Instantiates a class that monitors the Active TimeSpans.
     *
     * @param toolbox the toolbox
     */
    public WMSActiveTimeMonitor(Toolbox toolbox)
    {
        myAnimationManager = toolbox.getAnimationManager();
        myTimeManager = toolbox.getTimeManager();
    }

    /**
     * Activate this time monitor.
     */
    public void activate()
    {
        myTimeManager.addActiveTimeSpanChangeListener(myActiveTimespanChangeListener);
        myTimeManager.addDataLoadDurationChangeListener(myDataLoadDurationChangeListener);
        myAnimationManager.addAnimationChangeListener(myAnimationListener);
        myIsActive = true;
    }

    /**
     * Adds a {@link TimeChangeListener}.
     *
     * <p>
     * NOTE: The listener is held as a weak reference.
     *
     * @param listener the TimeChangeListener to add
     */
    public void addListener(TimeChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Deactivate this time monitor.
     */
    public void deactivate()
    {
        myAnimationManager.removeAnimationChangeListener(myAnimationListener);
        myTimeManager.removeDataLoadDurationChangeListener(myDataLoadDurationChangeListener);
        myTimeManager.removeActiveTimeSpanChangeListener(myActiveTimespanChangeListener);
        myIsActive = false;
    }

    /**
     * Gets of copy of the current sequence.
     *
     * @return copy of the current sequence
     */
    public Set<? extends TimeSpan> getCurrentSequence()
    {
        return New.set(myCurrentSequence);
    }

    /**
     * Removes a {@link TimeChangeListener}.
     *
     * @param listener the TimeChangeListener to remove
     */
    public void removeListener(TimeChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Notify listeners that timespans have changed.
     *
     * @param addedSpans the TimeSpans that were added
     * @param removedSpans the TimeSpans that were removed
     */
    protected void fireTimeSpansChanged(Collection<? extends TimeSpan> addedSpans, Collection<? extends TimeSpan> removedSpans)
    {
        if (myIsActive)
        {
            myChangeSupport.notifyListeners(listener -> listener.timespansChanged(addedSpans, removedSpans), myExecutor);
        }
    }

    /**
     * Notify listeners that the active time has changed.
     *
     * @param active The new active time
     */
    protected void fireActiveTimeChanged(TimeSpan active)
    {
        if (myIsActive)
        {
            myChangeSupport.notifyListeners(listener -> listener.activeTimeChanged(active), myExecutor);
        }
    }

    /**
     * Combine the TimeSpans from a collection with those from a Map like the
     * ones used by the TimeManager.
     *
     * @param spans the base collection of spans
     * @param map the map whose values will be added to the base
     * @return the combined set of TimeSpans
     */
    private Set<TimeSpan> combineSpans(Collection<? extends TimeSpan> spans, Map<Object, Collection<? extends TimeSpan>> map)
    {
        Set<TimeSpan> spanSet = New.set();
        if (spans != null && !spans.isEmpty())
        {
            spanSet.addAll(spans);
        }
        if (map != null && !map.isEmpty())
        {
            for (Collection<? extends TimeSpan> set : map.values())
            {
                if (set != null && !set.isEmpty())
                {
                    spanSet.addAll(set);
                }
            }
        }
        return spanSet;
    }

    /**
     * Update the current time sequence. Event changes out to the listeners.
     *
     * @throws InterruptedException If the update is interrupted.
     */
    private void doUpdateTimes() throws InterruptedException
    {
        Set<TimeSpan> tilesToLoad = getTilesToLoad();

        Set<TimeSpan> added = New.set(tilesToLoad);
        if (myCurrentSequence != null)
        {
            added.removeAll(myCurrentSequence);
        }

        Set<TimeSpan> removed = myCurrentSequence == null ? New.<TimeSpan>set() : New.set(myCurrentSequence);
        removed.removeAll(tilesToLoad);

        // Make sure something actually changed before proceeding
        if (!added.isEmpty() || !removed.isEmpty())
        {
            if (myCurrentSequence != null)
            {
                // If any timespan that was added or removed is part of
                // a larger span in the original sequence, that larger
                // span needs to be updated (i.e. removed and then
                // re-added)
                Set<TimeSpan> changed = New.set(added);
                changed.addAll(removed);
                for (TimeSpan span : changed)
                {
                    TimeSpan largerSpan = findEnclosingSpan(span, myCurrentSequence);
                    if (largerSpan != null && tilesToLoad.contains(largerSpan))
                    {
                        added.add(largerSpan);
                        removed.add(largerSpan);
                    }
                }
            }

            ThreadControl.check();
            myCurrentSequence = tilesToLoad;
            fireTimeSpansChanged(added, removed);
        }
    }

    /**
     * Find the shortest time span in the given sequence that completely
     * encloses the given time span.
     *
     * @param span the span
     * @param sequence the sequence
     * @return the time span
     */
    private TimeSpan findEnclosingSpan(TimeSpan span, Set<? extends TimeSpan> sequence)
    {
        Duration duration = span.getDuration();
        TimeSpan smallestEnclosingSpan = null;

        // Find the smallest timespan that completely encloses the specified
        // span
        for (TimeSpan candidate : sequence)
        {
            if (candidate.contains(span) && candidate.getDuration().compareTo(duration) > 0 && (smallestEnclosingSpan == null
                    || smallestEnclosingSpan.getDuration().compareTo(candidate.getDuration()) > 0))
            {
                smallestEnclosingSpan = candidate;
            }
        }
        return smallestEnclosingSpan;
    }

    /**
     * Get the tiles to load.
     *
     * @return The tiles to load.
     * @throws InterruptedException If interrupted.
     */
    private Set<TimeSpan> getTilesToLoad() throws InterruptedException
    {
        Duration dataLoadDuration = myTimeManager.getDataLoadDuration();
        if (dataLoadDuration == null)
        {
            return Collections.emptySet();
        }

        AnimationPlan plan = myAnimationManager.getCurrentPlan();
        Set<TimeSpan> spans;
        if (plan == null)
        {
            TimeSpanList timeList = myTimeManager.getPrimaryActiveTimeSpans();
            TimeSpan extent = timeList.getExtent();
            spans = TimeSpan.ZERO.equals(extent) ? New.<TimeSpan>set() : New.set(myTimeManager.getPrimaryActiveTimeSpans());
        }
        else
        {
            Map<Object, Collection<? extends TimeSpan>> secondaryMap = myTimeManager.getSecondaryActiveTimeSpans();
            spans = combineSpans(plan.getAnimationSequence(), secondaryMap);
        }

        Set<TimeSpan> tilesToLoad = New.set();
        for (TimeSpan span : spans)
        {
            Calendar cal = TimelineUtilities.roundDown(span.getStartDate(), dataLoadDuration);
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(span.getEnd());
            do
            {
                ThreadControl.check();
                TimeSpan tileSpan = TimeSpan.get(cal.getTimeInMillis(), dataLoadDuration);
                tilesToLoad.add(tileSpan);
                cal.setTimeInMillis(tileSpan.getEnd());
            }
            while (cal.before(end));
        }
        return tilesToLoad;
    }

    /**
     * Update the current time sequence. If a timeline is established, the
     * active set of spans will be set to the Primary and Secondary spans in the
     * animation plan. If not, the active set will be set to the Primary active
     * span in the TimeManager. <br>
     * <p>
     * All changes are evented out to the listeners.
     */
    private synchronized void updateTimes()
    {
        if (myFuture != null)
        {
            myFuture.cancel(true);
        }
        myFuture = myExecutor.submit(new CatchingRunnable(new Runnable()
        {
            @Override
            public void run()
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Starting WMS time update");
                }
                try
                {
                    doUpdateTimes();

                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Completed WMS time update");
                    }
                }
                catch (InterruptedException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("WMS time update was interrupted: " + e);
                    }
                }
            }
        }));
    }

    /**
     * Listener interface for receiving changes to the current active time
     * sequence.
     */
    public interface TimeChangeListener
    {
        /**
         * Notification to listeners when Timespans change.
         *
         * @param added the TimeSpans that were added
         * @param removed the TimeSpans that were removed
         */
        void timespansChanged(Collection<? extends TimeSpan> added, Collection<? extends TimeSpan> removed);

        /**
         * Notification to listeners when the active time changes.
         *
         * @param active the new active time
         */
        void activeTimeChanged(TimeSpan active);
    }
}
