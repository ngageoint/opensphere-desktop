package io.opensphere.core.server;

import java.io.InputStream;
import java.util.UUID;

/**
 * Interface to an object interested in receiving new data from a streaming
 * server.
 */
@FunctionalInterface
public interface StreamHandler
{
    /**
     * Called when a new data is available, implementations of this call must be
     * synchronous.
     *
     * @param streamId The id of the stream this new data belongs to.
     * @param stream The stream of new data.
     */
    void newData(UUID streamId, InputStream stream);
}
