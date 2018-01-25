package io.opensphere.core.model;

import java.util.Collection;

/**
 * A generic accumulator of intervals.
 *
 * @param <T> The type of the intervals.
 */
public interface Accumulator<T>
{
    /**
     * Add an interval.
     *
     * @param interval The interval.
     */
    void add(T interval);

    /**
     * Add some intervals.
     *
     * @param intervals The intervals.
     */
    void addAll(Collection<? extends T> intervals);

    /**
     * Get the extent of the intervals that have been added to the accumulator.
     *
     * @return The extent.
     */
    T getExtent();
}
