package io.opensphere.core.metrics;

/**
 * A Marker interface for a {@link RangedNumberMetricsProvider} that only allows
 * values in the 0.0 to 1.0 range.
 *
 */
public interface PercentageMetricsProvider extends RangedNumberMetricsProvider
{
}
