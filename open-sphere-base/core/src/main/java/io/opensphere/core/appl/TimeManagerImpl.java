package io.opensphere.core.appl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import io.opensphere.core.TimeManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.WeakHashSet;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.StringUtilities;
import net.jcip.annotations.GuardedBy;

/**
 * Implementation of the {@link TimeManager} interface.
 */
@SuppressWarnings("PMD.GodClass")
final class TimeManagerImpl implements TimeManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TimeManagerImpl.class);

    /**
     * List of active time span change listeners. Using weak references to the
     * listeners requires the registering class to maintain a reference to the
     * listener.
     */
    private final Collection<TimeManager.ActiveTimeSpanChangeListener> myActiveTimeSpanChangeListeners = Collections
            .synchronizedCollection(new WeakHashSet<TimeManager.ActiveTimeSpanChangeListener>());

    /** Flag indicating if a time change is currently underway. */
    private boolean myChanging;

    /** The requested data duration that is currently selected. */
    @GuardedBy("myRequestedDataDurations")
    private Duration myDataLoadDuration;

    /** Change support for data load duration. */
    private final ChangeSupport<DataLoadDurationChangeListener> myDataLoadDurationChangeSupport = WeakChangeSupport.create();

    /**
     * The different time spans to load data for.
     */
    private final ObservableList<TimeSpan> myLoadTimes = new ObservableList<>();

    /** The current fade specification. */
    private volatile Fade myFade;

    /**
     * Result of {@link Comparable#compareTo(Object)} of the current primary
     * time span with the previous one.
     */
    private int myLastChangeComparison;

    /** The primary active time spans. */
    private volatile TimeSpanList myPrimaryActiveTimeSpans = TimeSpanList.singleton(TimeSpan.ZERO);

    /** The executor used for launching primary time updates. */
    private final ChangeSupport<PrimaryTimeSpanChangeListener> myPrimaryTimeChangeSupport = new WeakChangeSupport<>();

    /** The requested data durations. */
    @GuardedBy("myRequestedDataDurations")
    private final Map<Object, Set<Duration>> myRequestedDataDurations = New.weakMap();

    /** Change support for RequestedDataDurationsChangeListeners. */
    private final ChangeSupport<RequestedDataDurationsChangeListener> myRequestedDataDurationsChangeSupport = WeakChangeSupport
            .create();

    /**
     * Map of constraint keys to timespans that are active for that constraint
     * key.
     */
    private final Map<Object, Collection<? extends TimeSpan>> mySecondaryActiveTimeSpans = new ConcurrentHashMap<>();

    /** The executor used for launching updates. */
    private final Executor myUpdateExecutor;

    /** Constructor. */
    public TimeManagerImpl()
    {
        final String timePropertyValue = System.getProperty("opensphere.time");
        if (timePropertyValue != null && timePropertyValue.length() > 0)
        {
            try
            {
                final DateFormat format = new SimpleDateFormat("yyyy-MMM-dd");
                final Date start = DateTimeUtilities.parse(format, timePropertyValue);
                final Date end = new Date(start.getTime() + Long.parseLong(System.getProperty("opensphere.time.interval")));
                setPrimaryActiveTimeSpan(TimeSpan.get(start, end));
            }
            catch (final ParseException e)
            {
                LOGGER.warn("Failed to parse opensphere.time property: " + e, e);
            }
        }

        final ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory("Time-update"), SuppressableRejectedExecutionHandler.getInstance());

        myUpdateExecutor = new ProcrastinatingExecutor(exec);
    }

    @Override
    public void addActiveTimeSpanChangeListener(ActiveTimeSpanChangeListener listener)
    {
        myActiveTimeSpanChangeListeners.add(listener);
    }

    @Override
    public void addDataLoadDurationChangeListener(DataLoadDurationChangeListener listener)
    {
        myDataLoadDurationChangeSupport.addListener(listener);
    }

    @Override
    public void addPrimaryTimeSpanChangeListener(PrimaryTimeSpanChangeListener listener)
    {
        myPrimaryTimeChangeSupport.addListener(listener);
    }

    @Override
    public void addRequestedDataDurationsChangeListener(RequestedDataDurationsChangeListener listener)
    {
        myRequestedDataDurationsChangeSupport.addListener(listener);
    }

    @Override
    public void clearActiveTimeSpans()
    {
        final boolean primaryEmpty = myPrimaryActiveTimeSpans.isEmpty();
        final boolean secondaryEmpty = mySecondaryActiveTimeSpans.isEmpty();
        myPrimaryActiveTimeSpans = TimeSpanList.singleton(TimeSpan.ZERO);
        mySecondaryActiveTimeSpans.clear();
        if (!primaryEmpty)
        {
            primaryTimeSpansCleared();
        }
        if (!primaryEmpty || !secondaryEmpty)
        {
            timeSpansChanged();
        }
        if (!primaryEmpty)
        {
            setDataLoadDuration(getRequestedDataDurations());
        }
    }

    @Override
    public synchronized ActiveTimeSpans getActiveTimeSpans()
    {
        return new DefaultActiveTimeSpans(getPrimaryActiveTimeSpans(), mySecondaryActiveTimeSpans, myLastChangeComparison,
                getFade());
    }

    @Override
    public Duration getDataLoadDuration()
    {
        synchronized (myRequestedDataDurations)
        {
            return myDataLoadDuration;
        }
    }

    @Override
    public Fade getFade()
    {
        return myFade;
    }

    @Override
    public ObservableList<TimeSpan> getLoadTimeSpans()
    {
        return myLoadTimes;
    }

    @Override
    public TimeSpanList getPrimaryActiveTimeSpans()
    {
        return myPrimaryActiveTimeSpans;
    }

    @Override
    public Set<? extends Duration> getRequestedDataDurations()
    {
        synchronized (myRequestedDataDurations)
        {
            return getRequestedDataDurations(myRequestedDataDurations.values());
        }
    }

    @Override
    public Map<Object, Collection<? extends TimeSpan>> getSecondaryActiveTimeSpans()
    {
        return Collections.unmodifiableMap(mySecondaryActiveTimeSpans);
    }

    @Override
    public Collection<? extends TimeSpan> getSecondaryActiveTimeSpans(Object constraintKey)
    {
        if (Utilities.sameInstance(constraintKey, WILDCARD_CONSTRAINT_KEY))
        {
            return mySecondaryActiveTimeSpans.get(WILDCARD_CONSTRAINT_KEY);
        }

        Utilities.checkNull(constraintKey, "constraintKey");
        final Collection<? extends TimeSpan> spans = mySecondaryActiveTimeSpans.get(constraintKey);
        final Collection<? extends TimeSpan> wildcardSpans = mySecondaryActiveTimeSpans.get(WILDCARD_CONSTRAINT_KEY);
        if (spans == null)
        {
            return wildcardSpans;
        }
        else if (wildcardSpans == null)
        {
            return spans;
        }
        else
        {
            return CollectionUtilities.concat(wildcardSpans, spans);
        }
    }

    @Override
    public void releaseDataDurationRequest(Object source)
    {
        synchronized (myRequestedDataDurations)
        {
            final Set<? extends Duration> before = getRequestedDataDurations();
            myRequestedDataDurations.remove(source);
            final Set<? extends Duration> after = getRequestedDataDurations();
            if (!before.equals(after))
            {
                notifyRequestedDataDurationsChangeListeners(after);
                setDataLoadDuration(after);
            }
        }
    }

    @Override
    public void removeActiveTimeSpanChangeListener(ActiveTimeSpanChangeListener listener)
    {
        myActiveTimeSpanChangeListeners.remove(listener);
    }

    @Override
    public void removeDataLoadDurationChangeListener(DataLoadDurationChangeListener listener)
    {
        myDataLoadDurationChangeSupport.removeListener(listener);
    }

    @Override
    public void removePrimaryTimeSpanChangeListener(PrimaryTimeSpanChangeListener listener)
    {
        myPrimaryTimeChangeSupport.removeListener(listener);
    }

    @Override
    public void removeRequestedDataDurationsChangeListener(RequestedDataDurationsChangeListener listener)
    {
        myRequestedDataDurationsChangeSupport.removeListener(listener);
    }

    @Override
    public void removeSecondaryActiveTimeSpan(TimeSpan span)
    {
        synchronized (this)
        {
            if (myChanging)
            {
                throw new IllegalStateException("Cannot change time from within a time change listener.");
            }
            myChanging = true;
            try
            {
                /* Figure out which secondary sets have the span we are going to
                 * remove, collect them up into a map with the constraints that
                 * will change and the new sets without the removed span. */
                final Map<Object, Collection<? extends TimeSpan>> changedMap = New.map();
                for (final Map.Entry<Object, Collection<? extends TimeSpan>> entry : mySecondaryActiveTimeSpans.entrySet())
                {
                    if (entry.getValue().contains(span))
                    {
                        final Collection<? extends TimeSpan> set = New.list(entry.getValue());
                        set.remove(span);
                        changedMap.put(entry.getKey(), New.unmodifiableCollection(set));
                    }
                }

                /* Used the changed map to alter our secondary active time
                 * spans, fire events only for changed types. */
                for (final Map.Entry<Object, Collection<? extends TimeSpan>> entry : changedMap.entrySet())
                {
                    if (entry.getValue().isEmpty())
                    {
                        mySecondaryActiveTimeSpans.remove(entry.getKey());
                    }
                    else
                    {
                        mySecondaryActiveTimeSpans.put(entry.getKey(), entry.getValue());
                    }
                }

                // Only if we had changes do we fire this time spans changed
                // event.
                if (!changedMap.isEmpty())
                {
                    timeSpansChanged();
                }
            }
            finally
            {
                myChanging = false;
            }
        }
    }

    @Override
    public void removeSecondaryActiveTimeSpansByConstraint(Object constraintKey)
    {
        final Collection<? extends TimeSpan> oldSpans = Collections.<TimeSpan>emptyList();
        setSecondaryActiveTimeSpans(constraintKey, oldSpans);
    }

    @Override
    public void requestDataDurations(Object source, Collection<? extends Duration> durations) throws IllegalArgumentException
    {
        synchronized (myRequestedDataDurations)
        {
            final Set<Duration> newSet = New.set(durations);

            final Collection<Set<Duration>> values = New.collection(myRequestedDataDurations.values());
            values.add(newSet);
            final Set<? extends Duration> after = getRequestedDataDurations(values);
            if (after.isEmpty())
            {
                throw new IllegalArgumentException("The input data durations do not intersect the existing data durations.");
            }

            final Set<? extends Duration> before = getRequestedDataDurations();
            myRequestedDataDurations.put(source, newSet);
            if (!before.equals(after))
            {
                notifyRequestedDataDurationsChangeListeners(after);
                setDataLoadDuration(after);
            }
        }
    }

    @Override
    public void setFade(Fade fade)
    {
        if (fade == null ? myFade == null : fade.equals(myFade))
        {
            return;
        }
        synchronized (this)
        {
            if (myChanging)
            {
                throw new IllegalStateException("Cannot change fade from within a time change listener.");
            }
            myChanging = true;
            try
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Fade set to " + fade);
                }
                myFade = fade;
                timeSpansChanged();
            }
            finally
            {
                myChanging = false;
            }
        }
    }

    @Override
    public void setPrimaryActiveTimeSpan(TimeSpan span)
    {
        Utilities.checkNull(span, "span");
        setPrimaryActiveTimeSpans(TimeSpanList.singleton(span));
    }

    @Override
    public void setPrimaryActiveTimeSpans(TimeSpanList spans)
    {
        Utilities.checkNull(spans, "spans");

        if (spans.equals(myPrimaryActiveTimeSpans))
        {
            return;
        }
        if (spans.isEmpty())
        {
            throw new IllegalArgumentException("Primary spans cannot be empty.");
        }
        synchronized (this)
        {
            if (myChanging)
            {
                throw new IllegalStateException("Cannot change time from within a time change listener.");
            }
            myChanging = true;
            try
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Primary active time span set to " + spans.toString());
                }
                final int compareTo = spans.get(0).compareTo(myPrimaryActiveTimeSpans.get(0));
                if (compareTo != 0)
                {
                    myLastChangeComparison = compareTo;
                }
                else if (myPrimaryActiveTimeSpans.equals(spans))
                {
                    return;
                }
                final Duration oldextent = myPrimaryActiveTimeSpans.getExtent().getDuration();
                myPrimaryActiveTimeSpans = spans;
                if (!myPrimaryActiveTimeSpans.getExtent().getDuration().equalsIgnoreUnits(oldextent))
                {
                    setDataLoadDuration(getRequestedDataDurations());
                }
                primaryTimeSpansChanged(spans);
                timeSpansChanged();
            }
            finally
            {
                myChanging = false;
            }
        }
    }

    @Override
    public void setSecondaryActiveTimeSpans(Object constraintKey, Collection<? extends TimeSpan> spans)
    {
        Utilities.checkNull(constraintKey, "constraintKey");
        Utilities.checkNull(spans, "spans");
        if (spans.equals(mySecondaryActiveTimeSpans.get(constraintKey)))
        {
            return;
        }
        synchronized (this)
        {
            if (myChanging)
            {
                throw new IllegalStateException("Cannot change time from within a time change listener.");
            }
            myChanging = true;
            try
            {
                final Collection<? extends TimeSpan> oldSpans = mySecondaryActiveTimeSpans.get(constraintKey);
                final Collection<? extends TimeSpan> newSpans = New.unmodifiableCollection(spans);
                if (!newSpans.equals(oldSpans)) // paranoia
                {
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Secondary active time spans for constraint key [" + constraintKey + "] set to "
                                + spans.toString());
                    }
                    if (newSpans.isEmpty())
                    {
                        mySecondaryActiveTimeSpans.remove(constraintKey);
                    }
                    else
                    {
                        mySecondaryActiveTimeSpans.put(constraintKey, newSpans);
                    }
                    timeSpansChanged();
                }
            }
            finally
            {
                myChanging = false;
            }
        }
    }

    /**
     * Get the intersection of some sets of durations.
     *
     * @param durationSets The sets of durations.
     * @return The set of durations contained by all of the input sets.
     */
    private Set<? extends Duration> getRequestedDataDurations(Collection<Set<Duration>> durationSets)
    {
        if (durationSets.isEmpty())
        {
            return Collections.emptySet();
        }
        else if (durationSets.size() == 1)
        {
            return durationSets.iterator().next();
        }
        else
        {
            final Set<Duration> durations = New.set();
            for (final Set<Duration> set : durationSets)
            {
                if (durations.isEmpty())
                {
                    durations.addAll(set);
                }
                else
                {
                    durations.retainAll(set);
                }
            }
            return durations;
        }
    }

    /**
     * Notify the active time span change listeners.
     */
    private void notifyActiveTimeSpanChangeListeners()
    {
        final List<ActiveTimeSpanChangeListener> changeListeners;
        synchronized (myActiveTimeSpanChangeListeners)
        {
            changeListeners = new ArrayList<>(myActiveTimeSpanChangeListeners);
        }
        final ActiveTimeSpans active = getActiveTimeSpans();
        myUpdateExecutor.execute(() ->
        {
            final long t0 = System.nanoTime();
            for (final ActiveTimeSpanChangeListener listener : changeListeners)
            {
                listener.activeTimeSpansChanged(active);
            }
            if (LOGGER.isTraceEnabled())
            {
                final long t1 = System.nanoTime();
                LOGGER.trace(StringUtilities.formatTimingMessage("Time to notify active time span listeners: ", t1 - t0));
            }
        });
    }

    /**
     * Notify the primary active time span change listeners.
     *
     * @param spans the spans
     */
    private void notifyPrimaryTimeSpanChangeListeners(final TimeSpanList spans)
    {
        myPrimaryTimeChangeSupport.notifyListeners(listener -> listener.primaryTimeSpansChanged(spans), null);
    }

    /**
     * Notify listeners of new requested durations.
     *
     * @param durations The requested durations.
     */
    private void notifyRequestedDataDurationsChangeListeners(final Set<? extends Duration> durations)
    {
        myRequestedDataDurationsChangeSupport.notifyListeners(listener -> listener.requestedDataDurationsChanged(durations));
    }

    /**
     * Method called when the primary time span set is changed.
     *
     * @param spans the new primary time spans.
     */
    private synchronized void primaryTimeSpansChanged(TimeSpanList spans)
    {
        notifyPrimaryTimeSpanChangeListeners(spans);
    }

    /**
     * Primary time spans cleared.
     */
    private synchronized void primaryTimeSpansCleared()
    {
        myPrimaryTimeChangeSupport.notifyListeners(listener -> listener.primaryTimeSpansCleared(), null);
    }

    /**
     * Set the selected data load duration based on the latest set of available
     * data load durations.
     *
     * @param availableDataLoadDurations The available data load durations.
     */
    private void setDataLoadDuration(Set<? extends Duration> availableDataLoadDurations)
    {
        Duration active = getPrimaryActiveTimeSpans().getExtent().getDuration();
        Duration minimumDuration = active.compareTo(new Days(27)) > 0 ? Weeks.ONE : Days.ONE;

        Duration selected;
        if (availableDataLoadDurations.isEmpty())
        {
            selected = null;
        }
        else if (availableDataLoadDurations.contains(minimumDuration))
        {
            selected = minimumDuration;
        }
        else
        {
            selected = null;
            List<Duration> sorted = CollectionUtilities.sort(availableDataLoadDurations);
            for (Duration dur : sorted)
            {
                try
                {
                    if (dur.compareTo(minimumDuration) >= 0)
                    {
                        selected = dur;
                        break;
                    }
                }
                catch (InconvertibleUnits e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Failed to compare units: " + e, e);
                    }
                }
            }

            // If there is no data load duration larger than the active
            // duration, select the largest data load duration.
            if (selected == null)
            {
                selected = sorted.get(sorted.size() - 1);
            }
        }

        if (!Objects.equals(myDataLoadDuration, selected))
        {
            myDataLoadDuration = selected;
            myDataLoadDurationChangeSupport.notifyListeners(listener -> listener.dataLoadDurationChanged(myDataLoadDuration));
        }
    }

    /**
     * Method called when either the primary or secondary time spans change.
     */
    private synchronized void timeSpansChanged()
    {
        notifyActiveTimeSpanChangeListeners();
    }

    /**
     * Default implementation of
     * {@link io.opensphere.core.TimeManager.ActiveTimeSpans}.
     */
    private static class DefaultActiveTimeSpans implements ActiveTimeSpans
    {
        /** The direction. */
        private final int myDirection;

        /** The fade. */
        private final Fade myFade;

        /** The primary time spans. */
        private final TimeSpanList myPrimary;

        /** The secondary time spans. */
        private final Map<Object, Collection<? extends TimeSpan>> mySecondary;

        /**
         * Constructor.
         *
         * @param primary The primary time spans.
         * @param secondary The secondary time spans.
         * @param direction The direction.
         * @param fade The fade.
         */
        public DefaultActiveTimeSpans(TimeSpanList primary, Map<Object, Collection<? extends TimeSpan>> secondary, int direction,
                Fade fade)
        {
            myPrimary = primary;
            mySecondary = New.unmodifiableMap(secondary);
            myDirection = direction;
            myFade = fade;
        }

        @Override
        public int getDirection()
        {
            return myDirection;
        }

        @Override
        public Fade getFade()
        {
            return myFade;
        }

        @Override
        public TimeSpanList getPrimary()
        {
            return myPrimary;
        }

        @Override
        public Map<Object, Collection<? extends TimeSpan>> getSecondary()
        {
            return mySecondary;
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append("ActiveTimeSpans primary [").append(myPrimary).append(" secondary [")
                    .append(mySecondary).append(']').toString();
        }
    }
}
