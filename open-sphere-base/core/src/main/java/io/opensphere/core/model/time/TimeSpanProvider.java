package io.opensphere.core.model.time;

/**
 * The Interface TimeSpanProvider.
 */
@FunctionalInterface
public interface TimeSpanProvider
{
    /**
     * Gets the time span. This cannot return null, it must return a non-null
     * TimeSpan, or {@link TimeSpan}.TIMELESS
     *
     * @return the time span
     */
    TimeSpan getTimeSpan();
}
