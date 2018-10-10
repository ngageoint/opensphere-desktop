package io.opensphere.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.Service;

/**
 * Interface for the time management facility. This facility keeps track of what
 * time spans are currently active, and allows for multiple time spans to be
 * active at any given time.
 *
 * At any given time there is always at least one <i>primary</i> active time
 * span. If there are more than one, they are guaranteed to not overlap. There
 * may also be zero or more <i>secondary</i> active time spans, which are
 * allowed to overlap each other and the primary active time span.
 */
public interface TimeManager
{
    /**
     * Special constraint key that indicates a secondary active time span that
     * applies to all constraint keys.
     */
    Object WILDCARD_CONSTRAINT_KEY = new Object();

    /**
     * Add a time span change listener. Implementations may use weak references
     * for listeners, so callers must maintain a strong reference to ensure that
     * the listener is not garbage-collected.
     *
     * @param listener The listener.
     */
    void addActiveTimeSpanChangeListener(ActiveTimeSpanChangeListener listener);

    /**
     * Add a data load duration change listener. Implementations may use weak
     * references for listeners, so callers must maintain a strong reference to
     * ensure that the listener is not garbage-collected.
     *
     * @param listener The listener.
     */
    void addDataLoadDurationChangeListener(DataLoadDurationChangeListener listener);

    /**
     * Add a primary time span change listener.
     * <p>
     * <b>WARNING:</b> The listener will be called synchronously with the thread
     * making the change the the primary time spans, so care must be taken to
     * avoid dead-locks and to limit execution time so as to not hold up the
     * calling thread.
     * <p>
     * Implementations may use weak references for listeners, so callers must
     * maintain a strong reference to ensure that the listener is not
     * garbage-collected.
     *
     * @param listener The listener.
     */
    void addPrimaryTimeSpanChangeListener(PrimaryTimeSpanChangeListener listener);

    /**
     * Add a requested data duration change listener. Implementations may use
     * weak references for listeners, so callers must maintain a strong
     * reference to ensure that the listener is not garbage-collected.
     *
     * @param listener The listener.
     */
    void addRequestedDataDurationsChangeListener(RequestedDataDurationsChangeListener listener);

    /**
     * Clear active time spans.
     */
    void clearActiveTimeSpans();

    /**
     * Get the current active time spans (primary and secondary).
     *
     * @return The current active time spans.
     */
    ActiveTimeSpans getActiveTimeSpans();

    /**
     * Get the current data load duration, which is guaranteed to be one of the
     * requested ones.
     *
     * @return The current data load duration, or {@code null} if none have been
     *         requested.
     */
    Duration getDataLoadDuration();

    /**
     * Get the fade specification, which may be {@code null}.
     *
     * @return The fade.
     */
    Fade getFade();

    /**
     * Gets the current load time spans. Subscribe to this list if wanting
     * notifications of changes.
     *
     * @return The current load times.
     */
    ObservableList<TimeSpan> getLoadTimeSpans();

    /**
     * Get the primary active time spans.
     *
     * @return The primary active time spans.
     */
    TimeSpanList getPrimaryActiveTimeSpans();

    /**
     * Plug-ins may request that data be loaded at particular resolutions. An
     * empty collection is returned if no such requests have been made. If there
     * have been multiple requests, the intersection of the requests is
     * returned.
     *
     * @return The requested data durations.
     */
    Set<? extends Duration> getRequestedDataDurations();

    /**
     * Get the map of constraint keys to secondary active time spans.
     *
     * @return The map.
     */
    Map<Object, Collection<? extends TimeSpan>> getSecondaryActiveTimeSpans();

    /**
     * Get the secondary active time spans specific to a constraint key.
     *
     * @param constraintKey The constraint key.
     *
     * @return The secondary active time spans for the specified constraint key.
     */
    Collection<? extends TimeSpan> getSecondaryActiveTimeSpans(Object constraintKey);

    /**
     * Release a request for particular data load resolutions.
     *
     * @param source The source of the request.
     */
    void releaseDataDurationRequest(Object source);

    /**
     * Remove an active time span change listener.
     *
     * @param listener The listener.
     */
    void removeActiveTimeSpanChangeListener(ActiveTimeSpanChangeListener listener);

    /**
     * Remove a data load duration change listener.
     *
     * @param listener The listener.
     */
    void removeDataLoadDurationChangeListener(DataLoadDurationChangeListener listener);

    /**
     * Removes the primary time span change listener.
     *
     * @param listener The listener
     */
    void removePrimaryTimeSpanChangeListener(PrimaryTimeSpanChangeListener listener);

    /**
     * Remove a requested data duration change listener.
     *
     * @param listener The listener.
     */
    void removeRequestedDataDurationsChangeListener(RequestedDataDurationsChangeListener listener);

    /**
     * Removes the span from all constraints that contain that span in their
     * set.
     *
     * @param span The {@link TimeSpan} to remove
     */
    void removeSecondaryActiveTimeSpan(TimeSpan span);

    /**
     * Removes all the secondary time spans for a specific constraint.
     *
     * @param constraintKey The constraint key.
     */
    void removeSecondaryActiveTimeSpansByConstraint(Object constraintKey);

    /**
     * Request that data be loaded at some particular resolutions.
     *
     * @param source The source of the request, to which a strong reference must
     *            be held elsewhere.
     * @param durations The requested durations.
     * @throws IllegalArgumentException If the new requested durations do not
     *             intersect the current requested durations.
     */
    void requestDataDurations(Object source, Collection<? extends Duration> durations) throws IllegalArgumentException;

    /**
     * Set the current fade specification.
     *
     * @param fade The fade specification.
     */
    void setFade(Fade fade);

    /**
     * Set the primary active time span. This will notify listeners of the
     * change.
     *
     * @param span The new primary active time span.
     */
    void setPrimaryActiveTimeSpan(TimeSpan span);

    /**
     * Set the primary active time spans. This will notify listeners of the
     * change.
     *
     * @param span The new primary active time spans.
     */
    void setPrimaryActiveTimeSpans(TimeSpanList span);

    /**
     * Set the secondary active time spans specific to a constraint key. This
     * will notify listeners of the change.
     *
     * @param constraintKey The constraint key.
     * @param spans The new secondary active time spans.
     */
    void setSecondaryActiveTimeSpans(Object constraintKey, Collection<? extends TimeSpan> spans);

    /**
     * Creates a service that can be used to add/remove the given primary time
     * span change listener.
     *
     * @param listener the listener
     * @return the service
     */
    default Service getPrimaryTimeSpanListenerService(final PrimaryTimeSpanChangeListener listener)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                addPrimaryTimeSpanChangeListener(listener);
            }

            @Override
            public void close()
            {
                removePrimaryTimeSpanChangeListener(listener);
            }
        };
    }

    /** Listener interface for active time span changes. */
    @FunctionalInterface
    public interface ActiveTimeSpanChangeListener
    {
        /**
         * Method called when the active time spans (primary or secondary)
         * change, or a fade duration changes.
         *
         * @param active The active time spans.
         */
        void activeTimeSpansChanged(ActiveTimeSpans active);
    }

    /**
     * Interface used to relay information about the current active time spans
     * to {@link ActiveTimeSpanChangeListener}s.
     */
    public interface ActiveTimeSpans
    {
        /**
         * Get the direction of the change to the primary active time span.
         *
         * @return -1 if the change was backward in time, 0 if there was no
         *         change, or 1 if the change was forward in time.
         */
        int getDirection();

        /**
         * Get the fade specification.
         *
         * @return The fade specification, or {@code null} if fade is disabled.
         */
        @Nullable
        Fade getFade();

        /**
         * Get the primary active time spans. The primary active time spans are
         * intended to be dynamic and may change rapidly during an animation.
         * <p>
         * This is guaranteed to contain at least one time span. It will likely
         * only contain one time span, but may contain more than one if the
         * active time span contains a gap. Note that since this returns a
         * {@link TimeSpanList}, there can be no overlap in the time spans.
         *
         * @return The primary active time spans.
         */
        @NonNull
        TimeSpanList getPrimary();

        /**
         * Get the secondary active time spans. These are more static than the
         * primary active time spans.
         *
         * @return The secondary active time spans.
         */
        @NonNull
        Map<Object, Collection<? extends TimeSpan>> getSecondary();
    }

    /** Listener interface for changes to the current data load duration. */
    @FunctionalInterface
    public interface DataLoadDurationChangeListener
    {
        /**
         * Method called when the selected data load duration changes.
         *
         * @param dataLoadDuration The new duration.
         */
        void dataLoadDurationChanged(Duration dataLoadDuration);
    }

    /**
     * A specification of how to do fading.
     */
    public interface Fade
    {
        /**
         * Get the duration prior (opposite the direction of the previous active
         * primary time span) to the primary active time spans that geometries
         * become visible for the fade effect.
         *
         * @return The duration.
         */
        Duration getFadeIn();

        /**
         * Get the duration after (in the direction of the previous active
         * primary time span) the primary active time spans that geometries
         * remain visible for the fade effect.
         *
         * @return The duration.
         */
        Duration getFadeOut();

        /**
         * Get a fade specification that has the fade-in and fade-out switched
         * from this one.
         *
         * @return The reversed fade.
         */
        Fade reverse();
    }

    /**
     * The listener interface for primary time span changes only.
     */
    public interface PrimaryTimeSpanChangeListener
    {
        /**
         * Method called when only the primary time spans have changed.
         *
         * @param spans The collection of spans for the primary
         */
        void primaryTimeSpansChanged(TimeSpanList spans);

        /**
         * Method called when all primary active time spans are cleared.
         */
        void primaryTimeSpansCleared();

        /**
         * Creates a PrimaryTimeSpanChangeListener that notifies the consumer
         * only for changes.
         *
         * @param consumer the consumer
         * @return the listener
         */
        static PrimaryTimeSpanChangeListener newChangedListener(Consumer<? super TimeSpanList> consumer)
        {
            return new PrimaryTimeSpanChangeListener()
            {
                @Override
                public void primaryTimeSpansChanged(TimeSpanList spans)
                {
                    consumer.accept(spans);
                }

                @Override
                public void primaryTimeSpansCleared()
                {
                }
            };
        }
    }

    /** Listener interface for changes to the requested data durations. */
    @FunctionalInterface
    public interface RequestedDataDurationsChangeListener
    {
        /**
         * Method called when the requested data durations change.
         *
         * @param durations The new durations.
         */
        void requestedDataDurationsChanged(Set<? extends Duration> durations);
    }
}
