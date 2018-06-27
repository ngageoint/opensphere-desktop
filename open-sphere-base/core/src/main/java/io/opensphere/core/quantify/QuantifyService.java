package io.opensphere.core.quantify;

/**
 * An interface defining a service that collects and periodically sends metrics
 * to a remote location.
 */
public interface QuantifyService
{
    /**
     * Collects an instance of the metric identified by the key.
     *
     * @param key the metric key for which to collect the metric.
     */
    void collectMetric(String key);

    /**
     * Flushes, sends all collected metrics through the sender to the remote
     * collector. Also resets the internal metric store to empty so that metrics
     * will not be counted twice.
     */
    void flush();

    /** Terminates the service. */
    void close();
}
