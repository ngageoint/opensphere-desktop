package io.opensphere.core.api;

import java.util.concurrent.ExecutorService;

/**
 * Component that retrieves data and creates models to represent them in the
 * system. The models are published to the data registry. The models are of
 * arbitrary type by design to allow for serialized objects from external
 * sources to be used.
 */
public interface Envoy
{
    /**
     * Cease data retrieval.
     */
    void close();

    /**
     * Begin retrieving data.
     *
     * @param executor An executor that can be used for additional envoy tasks.
     */
    void open(ExecutorService executor);

    /**
     * Set a filter to be applied to the retrieved data. The type for the filter
     * is plug-in specific.
     *
     * @param filter The filter.
     */
    void setFilter(Object filter);
}
