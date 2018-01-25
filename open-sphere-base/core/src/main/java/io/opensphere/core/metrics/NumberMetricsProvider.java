package io.opensphere.core.metrics;

/**
 * The Interface NumberMetricsProvider.
 */
public interface NumberMetricsProvider extends MetricsProvider
{
    /**
     * Gets the value as {@link Number}.
     *
     * @return the value as {@link Number}
     */
    Number getValueAsNumber();
}
