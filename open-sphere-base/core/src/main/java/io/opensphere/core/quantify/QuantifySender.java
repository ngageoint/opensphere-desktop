package io.opensphere.core.quantify;

import java.util.Collection;

import io.opensphere.core.quantify.model.Metric;

/**
 * A interface defining a metrics sender for a given receiver type.
 */
public interface QuantifySender
{
    /**
     * Sends the supplied collection of metrics to the remote endpoint.
     *
     * @param metrics the metrics to send to the remote endpoint.
     */
    void send(Collection<Metric> metrics);
}
