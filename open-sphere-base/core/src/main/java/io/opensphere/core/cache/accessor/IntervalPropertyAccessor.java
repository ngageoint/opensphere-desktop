package io.opensphere.core.cache.accessor;

import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;

/**
 * An accessor for property values with lower and upper bounds.
 *
 * @param <S> The type of object that provides the property values.
 * @param <T> The type of the property values.
 */
public interface IntervalPropertyAccessor<S, T> extends PropertyAccessor<S, T>
{
    /**
     * Create a property matcher that will match property values provided by
     * this accessor.
     *
     * @return A property matcher.
     */
    IntervalPropertyMatcher<?> createMatcher();

    /**
     * An extent that comprises all of the property values returned by this
     * accessor.
     *
     * @return The extent.
     */
    T getExtent();
}
