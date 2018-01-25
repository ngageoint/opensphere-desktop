package io.opensphere.core.cache.matcher;

import java.io.Serializable;

import io.opensphere.core.model.Accumulator;

/**
 * An object that knows how to match an interval property value.
 *
 * @param <T> The type of the property to be matched.
 */
public interface IntervalPropertyMatcher<T extends Serializable> extends PropertyMatcher<T>
{
    /**
     * Get an accumulator for values that could be matched.
     *
     * @return The accumulator.
     */
    Accumulator<T> getAccumulator();

    /**
     * Get a matcher equivalent to this one except that it matches interval
     * overlaps. If this matcher already matches interval overlaps, {@code this}
     * may be returned.
     *
     * @return The overlap matcher.
     */
    IntervalPropertyMatcher<T> getGroupMatcher();

    /**
     * Get the smallest interval that will overlap all property values that can
     * satisfy this matcher. If the interval is infinite, return {@code null}.
     *
     * @return The overlap interval.
     */
    T getMinimumOverlapInterval();

    /**
     * Get a simple interval property value that is at least large enough to
     * overlap all property values that can satisfy this matcher, but may be
     * larger. This may be used to do quick intersection elimination.
     *
     * @return The simplified bounds.
     */
    T getSimplifiedBounds();

    /**
     * Get if a value is indefinite.
     *
     * @param object The value.
     * @return {@code true} if the input value is indefinite
     */
    boolean isIndefinite(Object object);

    /**
     * Determine if another matcher's result set can overlap mine.
     *
     * @param other The other matcher.
     * @return {@code true} if the result sets may overlap.
     */
    boolean overlaps(IntervalPropertyMatcher<?> other);
}
