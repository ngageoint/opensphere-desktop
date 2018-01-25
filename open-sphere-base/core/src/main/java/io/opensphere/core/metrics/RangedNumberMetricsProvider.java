package io.opensphere.core.metrics;

/**
 * A Number Metrics Provider who's number values will always be within a bounded
 * range.
 */
public interface RangedNumberMetricsProvider extends NumberMetricsProvider
{
    /**
     * Gets the max value for range.
     *
     * @return the max value
     */
    Number getMaxValue();

    /**
     * Gets the min value for range.
     *
     * @return the min value.
     */
    Number getMinValue();
}
