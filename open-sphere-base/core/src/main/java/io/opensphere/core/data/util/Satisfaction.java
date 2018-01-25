package io.opensphere.core.data.util;

import io.opensphere.core.cache.util.IntervalPropertyValueSet;

/**
 * The satisfaction of a query space.
 */
@FunctionalInterface
public interface Satisfaction
{
    /**
     * Get the value set that models the extent of the satisfaction for each
     * interval property value.
     *
     * @return The interval property value set.
     */
    IntervalPropertyValueSet getIntervalPropertyValueSet();
}
