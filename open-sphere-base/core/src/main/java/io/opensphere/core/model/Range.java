package io.opensphere.core.model;

import java.io.Serializable;

/**
 * Range interface.
 *
 * @param <T> The range type.
 */
public interface Range<T extends Comparable<?>> extends Serializable
{
    /**
     * Gets the maximum value.
     *
     * @return The maximum value
     */
    T getMax();

    /**
     * Gets the minimum value.
     *
     * @return The minimum value
     */
    T getMin();
}
